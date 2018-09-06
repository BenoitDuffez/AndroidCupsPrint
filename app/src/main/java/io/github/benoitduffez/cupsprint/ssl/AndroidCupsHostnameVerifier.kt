package io.github.benoitduffez.cupsprint.ssl

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

import io.github.benoitduffez.cupsprint.CupsPrintApp
import io.github.benoitduffez.cupsprint.app.HostNotVerifiedActivity

/**
 * Used with [HostNotVerifiedActivity] to trust certain hosts
 */
class AndroidCupsHostnameVerifier : HostnameVerifier {
    override fun verify(hostname: String, session: SSLSession): Boolean =
            HostNotVerifiedActivity.isHostnameTrusted(CupsPrintApp.context, hostname)
}
