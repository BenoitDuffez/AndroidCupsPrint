package io.github.benoitduffez.cupsprint

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManager

import io.github.benoitduffez.cupsprint.app.BasicAuthActivity
import io.github.benoitduffez.cupsprint.ssl.AdditionalKeyManager
import io.github.benoitduffez.cupsprint.ssl.AdditionalKeyStoresSSLSocketFactory
import io.github.benoitduffez.cupsprint.ssl.AndroidCupsHostnameVerifier

object HttpConnectionManagement {
    private val KEYSTORE_FILE = "cupsprint-trustfile"

    private val KEYSTORE_PASSWORD = "i6:[(mW*xh~=Ni;S|?8lz8eZ;!SU(S"

    /**
     * Will handle SSL related stuff to this connection so that certs are properly managed
     *
     * @param connection The target https connection
     */
    fun handleHttpsUrlConnection(connection: HttpsURLConnection) {
        connection.hostnameVerifier = AndroidCupsHostnameVerifier()

        try {
            val trustStore = loadKeyStore() ?: return

            var keyManager: KeyManager? = null
            try {
                keyManager = AdditionalKeyManager.fromAlias()
            } catch (e: CertificateException) {
                L.e("Couldn't load system key store: " + e.localizedMessage, e)
            }

            connection.sslSocketFactory = AdditionalKeyStoresSSLSocketFactory(keyManager, trustStore)
        } catch (e: NoSuchAlgorithmException) {
            L.e("Couldn't handle SSL URL connection: " + e.localizedMessage, e)
        } catch (e: UnrecoverableKeyException) {
            L.e("Couldn't handle SSL URL connection: " + e.localizedMessage, e)
        } catch (e: KeyStoreException) {
            L.e("Couldn't handle SSL URL connection: " + e.localizedMessage, e)
        } catch (e: KeyManagementException) {
            L.e("Couldn't handle SSL URL connection: " + e.localizedMessage, e)
        }
    }

    /**
     * Try to get the contents of the local key store
     *
     * @return A valid KeyStore object if nothing went wrong, null otherwise
     */
    private fun loadKeyStore(): KeyStore? {
        val trustStore: KeyStore
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
        } catch (e: KeyStoreException) {
            L.e("Couldn't open local key store", e)
            return null
        }

        // Load the local keystore into memory
        try {
            val fis = CupsPrintApp.context.openFileInput(KEYSTORE_FILE)
            trustStore.load(fis, KEYSTORE_PASSWORD.toCharArray())
            return trustStore
        } catch (e: FileNotFoundException) {
            // This one can be ignored safely - at least not sent to crashlytics
            L.e("Couldn't open local key store: " + e.localizedMessage)
        } catch (e: IOException) {
            L.e("Couldn't open local key store", e)
        } catch (e: NoSuchAlgorithmException) {
            L.e("Couldn't open local key store", e)
        } catch (e: CertificateException) {
            L.e("Couldn't open local key store", e)
        }

        // if we couldn't load local keystore file, create an new empty one
        try {
            trustStore.load(null, null)
        } catch (e: IOException) {
            L.e("Couldn't create new key store", e)
        } catch (e: NoSuchAlgorithmException) {
            L.e("Couldn't create new key store", e)
        } catch (e: CertificateException) {
            L.e("Couldn't create new key store", e)
        }

        return trustStore
    }

    /**
     * Add certs to the keystore (thus trusting them)
     *
     * @param chain The chain of certs to trust
     * @return true if it was saved, false otherwise
     */
    fun saveCertificates(chain: Array<X509Certificate>): Boolean {
        // Load existing certs
        val trustStore = loadKeyStore() ?: return false

        // Add new certs
        try {
            for (c in chain) {
                trustStore.setCertificateEntry(c.subjectDN.toString(), c)
            }
        } catch (e: KeyStoreException) {
            L.e("Couldn't store cert chain into key store", e)
            return false
        }

        // Save new keystore
        var fos: FileOutputStream? = null
        try {
            fos = CupsPrintApp.context.openFileOutput(KEYSTORE_FILE, Context.MODE_PRIVATE)
            trustStore.store(fos, KEYSTORE_PASSWORD.toCharArray())
            fos!!.close()
        } catch (e: Exception) {
            L.e("Unable to save key store", e)
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    L.e("Couldn't close key store", e)
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
    fun handleBasicAuth(url: URL, connection: HttpURLConnection) {
        val prefs = CupsPrintApp.context.getSharedPreferences(BasicAuthActivity.CREDENTIALS_FILE, Context.MODE_PRIVATE)

        val id = BasicAuthActivity.findSavedCredentialsId(url.toString(), prefs)
        if (id < 0) {
            return
        }

        val username = prefs.getString(BasicAuthActivity.KEY_BASIC_AUTH_LOGIN + id, "")
        val password = prefs.getString(BasicAuthActivity.KEY_BASIC_AUTH_PASSWORD + id, "")
        try {
            val encoded = Base64.encodeToString((username + ":" + password).toByteArray(charset("UTF-8")), Base64.NO_WRAP)
            connection.setRequestProperty("Authorization", "Basic $encoded")
        } catch (e: UnsupportedEncodingException) {
            L.e("Couldn't base64 encode basic auth credentials", e)
        }
    }
}
