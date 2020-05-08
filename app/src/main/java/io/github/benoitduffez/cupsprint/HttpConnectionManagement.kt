package io.github.benoitduffez.cupsprint

import android.content.Context
import android.util.Base64
import io.github.benoitduffez.cupsprint.app.BasicAuthActivity
import io.github.benoitduffez.cupsprint.ssl.AdditionalKeyManager
import io.github.benoitduffez.cupsprint.ssl.AdditionalKeyStoresSSLSocketFactory
import io.github.benoitduffez.cupsprint.ssl.AndroidCupsHostnameVerifier
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManager

private const val KEYSTORE_FILE = "cupsprint-trustfile"
private const val KEYSTORE_PASSWORD = "i6:[(mW*xh~=Ni;S|?8lz8eZ;!SU(S"

object HttpConnectionManagement {
    /**
     * Will handle SSL related stuff to this connection so that certs are properly managed
     *
     * @param connection The target https connection
     */
    fun handleHttpsUrlConnection(context: Context, connection: HttpsURLConnection) {
        connection.hostnameVerifier = AndroidCupsHostnameVerifier(context)

        try {
            val trustStore = loadKeyStore(context) ?: return

            var keyManager: KeyManager? = null
            try {
                keyManager = AdditionalKeyManager.fromAlias(context)
            } catch (e: CertificateException) {
                Timber.e(e, "Couldn't load system key store: ${e.localizedMessage}")
            }

            connection.sslSocketFactory = AdditionalKeyStoresSSLSocketFactory(keyManager, trustStore)
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e, "Couldn't handle SSL URL connection: ${e.localizedMessage}")
        } catch (e: UnrecoverableKeyException) {
            Timber.e(e, "Couldn't handle SSL URL connection: ${e.localizedMessage}")
        } catch (e: KeyStoreException) {
            Timber.e(e, "Couldn't handle SSL URL connection: ${e.localizedMessage}")
        } catch (e: KeyManagementException) {
            Timber.e(e, "Couldn't handle SSL URL connection: ${e.localizedMessage}")
        }
    }

    /**
     * Try to get the contents of the local key store
     *
     * @return A valid KeyStore object if nothing went wrong, null otherwise
     */
    private fun loadKeyStore(context: Context): KeyStore? {
        val trustStore: KeyStore
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        } catch (e: KeyStoreException) {
            Timber.e(e, "Couldn't open local key store")
            return null
        }

        // Load the local keystore into memory
        try {
            val fis = context.openFileInput(KEYSTORE_FILE)
            trustStore.load(fis, KEYSTORE_PASSWORD.toCharArray())
            return trustStore
        } catch (e: FileNotFoundException) {
            // This one can be ignored safely - at least not sent to crashlytics
            Timber.e("Couldn't open local key store: ${e.localizedMessage}")
        } catch (e: IOException) {
            Timber.e(e, "Couldn't open local key store")
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e, "Couldn't open local key store")
        } catch (e: CertificateException) {
            Timber.e(e, "Couldn't open local key store")
        }

        // if we couldn't load local keystore file, create an new empty one
        try {
            trustStore.load(null, null)
        } catch (e: IOException) {
            Timber.e(e, "Couldn't create new key store")
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e, "Couldn't create new key store")
        } catch (e: CertificateException) {
            Timber.e(e, "Couldn't create new key store")
        }

        return trustStore
    }

    /**
     * Add certs to the keystore (thus trusting them)
     *
     * @param chain The chain of certs to trust
     * @return true if it was saved, false otherwise
     */
    fun saveCertificates(context: Context, chain: Array<X509Certificate>): Boolean {
        // Load existing certs
        val trustStore = loadKeyStore(context) ?: return false

        // Add new certs
        try {
            for (c in chain) {
                trustStore.setCertificateEntry(c.subjectDN.toString(), c)
            }
        } catch (e: KeyStoreException) {
            Timber.e(e, "Couldn't store cert chain into key store")
            return false
        }

        // Save new keystore
        var fos: FileOutputStream? = null
        try {
            fos = context.openFileOutput(KEYSTORE_FILE, Context.MODE_PRIVATE)
            trustStore.store(fos, KEYSTORE_PASSWORD.toCharArray())
            fos!!.close()
        } catch (e: Exception) {
            Timber.e(e, "Unable to save key store")
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    Timber.e(e, "Couldn't close key store")
                }
            }
        }

        return true
    }

    /**
     * See if there are some basic auth credentials saved, and configure the connection
     *
     * @param url        URL we're about to request
     * @param connection The connection to be configured
     */
    fun handleBasicAuth(context: Context, url: URL, connection: HttpURLConnection) {
        val prefs = context.getSharedPreferences(BasicAuthActivity.CREDENTIALS_FILE, Context.MODE_PRIVATE)

        val id = BasicAuthActivity.findSavedCredentialsId(url.toString(), prefs)
        if (id < 0) {
            return
        }

        val username = prefs.getString(BasicAuthActivity.KEY_BASIC_AUTH_LOGIN + id, "")
        val password = prefs.getString(BasicAuthActivity.KEY_BASIC_AUTH_PASSWORD + id, "")
        try {
            val encoded = Base64.encodeToString(("$username:$password").toByteArray(charset("UTF-8")), Base64.NO_WRAP)
            connection.setRequestProperty("Authorization", "Basic $encoded")
        } catch (e: UnsupportedEncodingException) {
            Timber.e(e, "Couldn't base64 encode basic auth credentials")
        }
    }
}
