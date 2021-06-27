package io.github.benoitduffez.cupsprint.ssl

import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val UNTRUSTED_CERTIFICATE = "Untrusted Certificate"

internal class AdditionalKeyStoresTrustManager(vararg additionalKeyStores: KeyStore) : X509TrustManager {
    private val x509TrustManagers = ArrayList<X509TrustManager>()
    var certChain: Array<X509Certificate>? = null
        private set

    init {
        val factories = ArrayList<TrustManagerFactory>()

        try {
            // The default Trust manager with default keystore
            val original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            original.init(null as KeyStore?)
            factories.add(original)

            for (keyStore in additionalKeyStores) {
                val additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                additionalCerts.init(keyStore)
                factories.add(additionalCerts)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        /*
         * Iterate over the returned trust managers, and hold on
         * to any that are X509TrustManagers
         */
        for (tmf in factories) {
            for (tm in tmf.trustManagers) {
                if (tm is X509TrustManager) {
                    x509TrustManagers.add(tm)
                }
            }
        }

        if (x509TrustManagers.size == 0) {
            throw RuntimeException("Couldn't find any X509TrustManagers")
        }
    }

    /*
     * Delegate to the default trust manager.
     */
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        val defaultX509TrustManager = x509TrustManagers[0]
        defaultX509TrustManager.checkClientTrusted(chain, authType)
    }

    /*
     * Loop over the trustmanagers until we find one that accepts our server
     */
    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        certChain = chain
        for (tm in x509TrustManagers) {
            try {
                tm.checkServerTrusted(chain, authType)
                return
            } catch (e: CertificateException) {
                // ignore
            }
        }
        throw CertificateException(UNTRUSTED_CERTIFICATE)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        val list = ArrayList<X509Certificate>()
        for (tm in x509TrustManagers) {
            list.addAll(Arrays.asList(*tm.acceptedIssuers))
        }
        return list.toTypedArray()
    }
}
