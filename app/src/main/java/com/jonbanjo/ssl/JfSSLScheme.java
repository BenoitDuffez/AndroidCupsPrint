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

package com.jonbanjo.ssl;

import java.io.FileInputStream;
import java.security.KeyStore;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;

import io.github.benoitduffez.cupsprint.CupsPrintApp;

public class JfSSLScheme {
	
	public static final String trustfile = "cupsprint-trustfile";
	public static final String password = "i6:[(mW*xh~=Ni;S|?8lz8eZ;!SU(S";
    public static Scheme getScheme(){
        
    	FileInputStream fis = null;
    	Scheme scheme;
    	
        try {	
       		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
 
           	try {
           		fis = CupsPrintApp.getContext().openFileInput(trustfile);
           		trustStore.load(fis, password.toCharArray());
           	}
            catch (Exception e){
            	trustStore.load(null, null);
            }
           
            SSLSocketFactory sf = new AdditionalKeyStoresSSLSocketFactory(trustStore);
        	sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
           	scheme = new Scheme("https", sf, 443);
        }
        catch (Exception e){
        	scheme = getDefaultScheme();
        }
        finally {
            if (fis != null) {
            	try {
            		fis.close();
            	}catch (Exception e1){}
            }
        }
        return scheme;
    }
    
    private static Scheme getDefaultScheme(){
    	SSLSocketFactory sf = SSLSocketFactory.getSocketFactory();
    	sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    	return new Scheme("https", sf, 443);
    }
    
}
