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

//This class based on http://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https

package com.jonbanjo.ssl;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class AdditionalKeyStoresTrustManager implements X509TrustManager {

    private X509Certificate[] certChain = null;
    
	protected ArrayList<X509TrustManager> x509TrustManagers = new ArrayList<X509TrustManager>();
    

    protected AdditionalKeyStoresTrustManager(KeyStore... additionalkeyStores) {
        final ArrayList<TrustManagerFactory> factories = new ArrayList<TrustManagerFactory>();

        try {
            // The default Trustmanager with default keystore
            final TrustManagerFactory original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            original.init((KeyStore) null);
            factories.add(original);

            for( KeyStore keyStore : additionalkeyStores ) {
                final TrustManagerFactory additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                additionalCerts.init(keyStore);
                factories.add(additionalCerts);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }



        /*
         * Iterate over the returned trustmanagers, and hold on
         * to any that are X509TrustManagers
         */
        for (TrustManagerFactory tmf : factories)
            for( TrustManager tm : tmf.getTrustManagers() )
                if (tm instanceof X509TrustManager)
                    x509TrustManagers.add( (X509TrustManager)tm );


        if( x509TrustManagers.size()==0 )
            throw new RuntimeException("Couldn't find any X509TrustManagers");

    }

    /*
     * Delegate to the default trust manager.
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
        defaultX509TrustManager.checkClientTrusted(chain, authType);
    }

    /*
     * Loop over the trustmanagers until we find one that accepts our server
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    	certChain = null;
    	for( X509TrustManager tm : x509TrustManagers ) {
            try {
                tm.checkServerTrusted(chain,authType);
                return;
            } catch( CertificateException e ) {
                // ignore
            }
        }
    	certChain = chain;
        throw new CertificateException("No Certificate\n");
    }

    public X509Certificate[] getAcceptedIssuers() {
        final ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
        for( X509TrustManager tm : x509TrustManagers )
            list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
        return list.toArray(new X509Certificate[list.size()]);
    }
    
    public X509Certificate[] getCertChain(){
    	return certChain;
    }
}