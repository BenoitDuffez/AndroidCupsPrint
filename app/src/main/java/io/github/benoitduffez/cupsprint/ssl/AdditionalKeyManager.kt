package io.github.benoitduffez.cupsprint.ssl

import android.content.Context
import androidx.preference.PreferenceManager
import android.security.KeyChain
import android.security.KeyChainException
import android.text.TextUtils
import timber.log.Timber
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509KeyManager

/**
 * Uses the system keystore
 */
class AdditionalKeyManager private constructor(val context: Context, private val clientAlias: String, private val certificateChain: Array<X509Certificate>, private val privateKey: PrivateKey) : X509KeyManager {
    override fun chooseClientAlias(keyType: Array<String>, issuers: Array<Principal>, socket: Socket): String = clientAlias
    override fun getCertificateChain(alias: String): Array<X509Certificate> = certificateChain
    override fun getPrivateKey(alias: String): PrivateKey = privateKey

    override fun getClientAliases(keyType: String, issuers: Array<Principal>): Array<String> {
        throw UnsupportedOperationException()
    }

    override fun chooseServerAlias(keyType: String, issuers: Array<Principal>, socket: Socket): String {
        throw UnsupportedOperationException()
    }

    override fun getServerAliases(keyType: String, issuers: Array<Principal>): Array<String> {
        throw UnsupportedOperationException()
    }

    companion object {
        private val KEY_CERTIFICATE_ALIAS = AdditionalKeyManager::class.java.name + ".CertificateAlias"

        /**
         * Builds an instance of a KeyChainKeyManager using the given certificate alias. If for any reason retrieval of the credentials from the system
         * KeyChain fails, a null value will be returned.
         *
         * @return System-wide KeyManager, or null if alias is empty
         * @throws CertificateException
         */
        @Throws(CertificateException::class)
        fun fromAlias(context: Context): AdditionalKeyManager? {
            val alias = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_CERTIFICATE_ALIAS, null)

            if (TextUtils.isEmpty(alias) || alias == null) {
                return null
            }

            val certificateChain = getCertificateChain(context, alias)
            val privateKey = getPrivateKey(context, alias)

            if (certificateChain == null || privateKey == null) {
                throw CertificateException("Can't access certificate from keystore")
            }

            return AdditionalKeyManager(context, alias, certificateChain, privateKey)
        }

        @Throws(CertificateException::class)
        private fun getCertificateChain(context: Context, alias: String): Array<X509Certificate>? {
            val certificateChain: Array<X509Certificate>?
            try {
                certificateChain = KeyChain.getCertificateChain(context, alias)
            } catch (e: KeyChainException) {
                logError(alias, "certificate chain", e)
                throw CertificateException(e)
            } catch (e: InterruptedException) {
                logError(alias, "certificate chain", e)
                throw CertificateException(e)
            }

            return certificateChain
        }

        @Throws(CertificateException::class)
        private fun getPrivateKey(context: Context, alias: String): PrivateKey? {
            val privateKey: PrivateKey?
            try {
                privateKey = KeyChain.getPrivateKey(context, alias)
            } catch (e: KeyChainException) {
                logError(alias, "private key", e)
                throw CertificateException(e)
            } catch (e: InterruptedException) {
                logError(alias, "private key", e)
                throw CertificateException(e)
            }

            return privateKey
        }

        private fun logError(alias: String, type: String, e: Exception) =
                Timber.e(e, "Unable to retrieve $type for [$alias]")
    }
}
