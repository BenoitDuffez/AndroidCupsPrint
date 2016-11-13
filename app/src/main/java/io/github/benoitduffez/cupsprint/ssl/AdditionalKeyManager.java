package io.github.benoitduffez.cupsprint.ssl;

import android.content.Context;
import android.preference.PreferenceManager;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.text.TextUtils;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

import io.github.benoitduffez.cupsprint.CupsPrintApp;
import io.github.benoitduffez.cupsprint.L;

/**
 * Uses the system keystore
 */
public class AdditionalKeyManager implements X509KeyManager {
    private static final String KEY_CERTIFICATE_ALIAS = AdditionalKeyManager.class.getName() + ".CertificateAlias";

    private final String mClientAlias;

    private final X509Certificate[] mCertificateChain;

    private final PrivateKey mPrivateKey;

    private AdditionalKeyManager(final String clientAlias, final X509Certificate[] certificateChain, final PrivateKey privateKey) {
        mClientAlias = clientAlias;
        mCertificateChain = certificateChain;
        mPrivateKey = privateKey;
    }

    /**
     * Builds an instance of a KeyChainKeyManager using the given certificate alias. If for any reason retrieval of the credentials from the system
     * KeyChain fails, a null value will be returned.
     *
     * @return System-wide KeyManager, or null if alias is empty
     * @throws CertificateException
     */
    public static AdditionalKeyManager fromAlias() throws CertificateException {
        final Context context = CupsPrintApp.getContext();
        String alias = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_CERTIFICATE_ALIAS, null);

        if (TextUtils.isEmpty(alias)) {
            return null;
        }

        X509Certificate[] certificateChain = getCertificateChain(context, alias);
        PrivateKey privateKey = getPrivateKey(context, alias);

        if (certificateChain == null || privateKey == null) {
            throw new CertificateException("Can't access certificate from keystore");
        }

        return new AdditionalKeyManager(alias, certificateChain, privateKey);
    }

    private static X509Certificate[] getCertificateChain(Context context, final String alias) throws CertificateException {
        X509Certificate[] certificateChain;
        try {
            certificateChain = KeyChain.getCertificateChain(context, alias);
        } catch (final KeyChainException | InterruptedException e) {
            logError(alias, "certificate chain", e);
            throw new CertificateException(e);
        }
        return certificateChain;
    }

    private static PrivateKey getPrivateKey(Context context, String alias) throws CertificateException {
        PrivateKey privateKey;
        try {
            privateKey = KeyChain.getPrivateKey(context, alias);
        } catch (final KeyChainException | InterruptedException e) {
            logError(alias, "private key", e);
            throw new CertificateException(e);
        }

        return privateKey;
    }

    private static void logError(final String alias, final String type, final Exception ex) {
        L.e("Unable to retrieve " + type + " for [" + alias + "]", ex);
    }

    @Override
    public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
        return mClientAlias;
    }

    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        return mCertificateChain;
    }

    @Override
    public PrivateKey getPrivateKey(final String alias) {
        return mPrivateKey;
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
        throw new UnsupportedOperationException();
    }

}
