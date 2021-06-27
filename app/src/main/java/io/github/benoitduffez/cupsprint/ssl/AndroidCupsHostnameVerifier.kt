package io.github.benoitduffez.cupsprint.ssl

import android.content.Context
import io.github.benoitduffez.cupsprint.app.HostNotVerifiedActivity
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * Used with [HostNotVerifiedActivity] to trust certain hosts
 */
class AndroidCupsHostnameVerifier(val context: Context) : HostnameVerifier {
    override fun verify(hostname: String, session: SSLSession): Boolean =
            HostNotVerifiedActivity.isHostnameTrusted(context, hostname)
}
