package io.github.benoitduffez.cupsprint.ssl

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.security.*
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

/**
 * Allows you to trust certificates from additional KeyStores in addition to
 * the default KeyStore
 */
class AdditionalKeyStoresSSLSocketFactory
/**
 * Create the SSL socket factory
 *
 * @param keyManager Key manager, can be null
 * @param keyStore   Keystore, can't be null
 * @throws NoSuchAlgorithmException
 * @throws KeyManagementException
 * @throws KeyStoreException
 * @throws UnrecoverableKeyException
 */
@Throws(NoSuchAlgorithmException::class, KeyManagementException::class, KeyStoreException::class, UnrecoverableKeyException::class)
constructor(keyManager: KeyManager?, keyStore: KeyStore) : SSLSocketFactory() {
    private val sslContext = SSLContext.getInstance("TLS")
    private val trustManager: AdditionalKeyStoresTrustManager = AdditionalKeyStoresTrustManager(keyStore)

    val serverCert: Array<X509Certificate>?
        get() = trustManager.certChain

    init {
        // Ensure we don't pass an empty array. Array must contain a valid key manager, or must be null
        val managers: Array<KeyManager>? = when (keyManager) {
            null -> null
            else -> arrayOf(keyManager)
        }

        sslContext.init(managers, arrayOf<TrustManager>(trustManager), null)
    }

    override fun getDefaultCipherSuites(): Array<String?> = arrayOfNulls(0)
    override fun getSupportedCipherSuites(): Array<String?> = arrayOfNulls(0)

    @Throws(IOException::class)
    override fun createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket =
            sslContext.socketFactory.createSocket(socket, host, port, autoClose)

    @Throws(IOException::class)
    override fun createSocket(s: String, i: Int): Socket =
            sslContext.socketFactory.createSocket(s, i)

    @Throws(IOException::class)
    override fun createSocket(s: String, i: Int, inetAddress: InetAddress, i1: Int): Socket =
            sslContext.socketFactory.createSocket(s, i, inetAddress, i1)

    @Throws(IOException::class)
    override fun createSocket(inetAddress: InetAddress, i: Int): Socket =
            sslContext.socketFactory.createSocket(inetAddress, i)

    @Throws(IOException::class)
    override fun createSocket(inetAddress: InetAddress, i: Int, inetAddress1: InetAddress, i1: Int): Socket =
            sslContext.socketFactory.createSocket(inetAddress, i, inetAddress1, i1)
}
