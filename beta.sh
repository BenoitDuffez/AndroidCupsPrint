#!/bin/bash


# Check repo status
BRANCH=develop

if ! grep -q ${BRANCH} <<<"$(git branch | grep ^*)"; then
    echo "This must be invoked on ${BRANCH}" >&2
    echo "  git checkout ${BRANCH} && $0 $@" >&2
    exit 1
fi

if ! grep -q "nothing to commit, working tree clean" <<<"$(git status)"; then
    echo "Directory must be clean. Please run git status and commit changes." >&2
    exit 1
fi

# Load last version
version=$(awk -F= '/version/{print $2}' gradle.properties)

# Go to beta
echo "Comparing versions with develop..."
git checkout beta 2>&1
git pull

# Build version base + next RC number
versionBeta=$(awk -F= '/version/{print $2}' gradle.properties)
if grep -qP "RC\d+$" <<<"$versionBeta"; then
    base=$(awk -F- '{print $1}' <<<"$versionBeta")
    rc=$(awk -FC '{print $NF+1}' <<<"$versionBeta")
else
    base="$versionBeta"
    rc=1
fi

# If minor/major/patch has changed, reset RC number
if [ ${base} != ${version} ]; then
    base=${version}
    rc=1
fi

# Merge and fix conflict
echo "Merging develop into beta..."
if grep -q "Already up.to.date" <<<$(git merge --no-edit develop); then
    echo "No changes on develop: do not release"
    exit 1
fi
git checkout --theirs gradle.properties

# Change version
newVersion="$base-RC$rc"
echo "Changing version $version to $newVersion"
sed -i'' "/version=/c version=$newVersion" gradle.properties

# Set changelog
lastBeta=$(git blame gradle.properties | awk '/version=/{print $1}')
mkdir -p app/src/main/play/en-US
whatsnew=app/src/main/play/en-US/whatsnew
echo $(git describe --tags || git rev-parse --short HEAD) >${whatsnew}
git log --pretty=format:'  * %s' ${lastBeta}..HEAD | grep -v 'Merge branch' >>${whatsnew}
echo "Saving changelog for release notes: ${whatsnew}"

# Commit & push
echo "Committing change & push:"
git status
git add gradle.properties ${whatsnew}
git commit -m "beta $version --> $newVersion"
git push
git tag -a "$newVersion" -m "$newVersion"
git push --tags
git checkout develop

