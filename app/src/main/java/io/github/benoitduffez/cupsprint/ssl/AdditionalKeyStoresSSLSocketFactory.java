/*Copyright (C) 2013 Jon Freeman

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either version 3 of the License, or (at your option) any
later version.
 
This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE.
 
See the GNU Lesser General Public License for more details. You should have
received a copy of the GNU Lesser General Public License along with this
program; if not, see <http://www.gnu.org/licenses/>.
*/


//This class taken from http://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https

package io.github.benoitduffez.cupsprint.ssl;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
    private SSLContext sslContext = SSLContext.getInstance("TLS");

    private AdditionalKeyStoresTrustManager trustManager;

    AdditionalKeyStoresSSLSocketFactory(KeyManager keyManager, KeyStore keyStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        trustManager = new AdditionalKeyStoresTrustManager(keyStore);
        sslContext.init(new KeyManager[]{keyManager}, new TrustManager[]{trustManager}, null);
    }

    public X509Certificate[] getServerCert() {
        return trustManager.getCertChain();
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
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(s, i);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(s, i, inetAddress, i1);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return sslContext.getSocketFactory().createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        return sslContext.getSocketFactory().createSocket(inetAddress, i, inetAddress1, i1);
    }
}
