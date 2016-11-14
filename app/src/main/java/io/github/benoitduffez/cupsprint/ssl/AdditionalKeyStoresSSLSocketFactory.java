package io.github.benoitduffez.cupsprint.ssl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Allows you to trust certificates from additional KeyStores in addition to
 * the default KeyStore
 */
public class AdditionalKeyStoresSSLSocketFactory extends SSLSocketFactory {
    private final SSLContext mSslContext = SSLContext.getInstance("TLS");

    private final AdditionalKeyStoresTrustManager mTrustManager;

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
    public AdditionalKeyStoresSSLSocketFactory(@Nullable KeyManager keyManager, @NonNull KeyStore keyStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        mTrustManager = new AdditionalKeyStoresTrustManager(keyStore);

        // Ensure we don't pass an empty array. Array must contain a valid key manager, or must be null
        KeyManager[] managers;
        if (keyManager == null) {
            managers = null;
        } else {
            managers = new KeyManager[]{keyManager};
        }

        mSslContext.init(managers, new TrustManager[]{mTrustManager}, null);
    }

    public X509Certificate[] getServerCert() {
        return mTrustManager.getCertChain();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return new String[0];
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return mSslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException {
        return mSslContext.getSocketFactory().createSocket(s, i);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException {
        return mSslContext.getSocketFactory().createSocket(s, i, inetAddress, i1);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return mSslContext.getSocketFactory().createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        return mSslContext.getSocketFactory().createSocket(inetAddress, i, inetAddress1, i1);
    }
}
