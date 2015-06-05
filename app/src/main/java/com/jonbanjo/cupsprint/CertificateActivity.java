package com.jonbanjo.cupsprint;

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


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import com.jonbanjo.ssl.AdditionalKeyStoresSSLSocketFactory;
import com.jonbanjo.ssl.JfSSLScheme;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class CertificateActivity extends Activity {

	EditText host;
	EditText port;
	ListView certList;
	Button importButton;
	ArrayAdapter<String> certListAdaptor;
	KeyStore trustStore = null;
	X509Certificate[] certChain = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_certificate);
		host = (EditText) findViewById(R.id.cert_host_edit);
		port = (EditText) findViewById(R.id.cert_port_edit);
		certList = (ListView) findViewById(R.id.cert_list);
  		importButton = (Button) findViewById(R.id.cert_import);
  		
		Intent intent = getIntent();
		String ip = intent.getStringExtra("host");
		if (ip != null){
			host.setText(ip);
		}
		String pt = intent.getStringExtra("port");
		if (pt != null){
			port.setText(pt);
		}

		trustStore = loadTrustStore();
		if (trustStore == null){
			return;
		}
		
		ArrayList<String> certArray;
		try {
			certArray =	Collections.list(trustStore.aliases());
		}
		catch (Exception e){
			return;
		}
		
		certListAdaptor = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, certArray);
		certList.setAdapter(certListAdaptor);
		certList.setClickable(true);
		certList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				displayCert(certListAdaptor.getItem(position));
				
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aboutmenu, menu);
		return true;
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.about:
	    		Intent intent = new Intent(this, AboutActivity.class);
	    		intent.putExtra("printer", "");
	    		startActivity(intent);
	    		break;
	    }
	    return super.onContextItemSelected(item);
	 }

	
	private KeyStore loadTrustStore(){
		KeyStore ts = null;
		
		try {
			ts = KeyStore.getInstance(KeyStore.getDefaultType());
		}
		catch (Exception e){
			System.out.println(e.toString());
			return null;
		}
		
		FileInputStream fis = null;
		try {
			fis = openFileInput(JfSSLScheme.trustfile);
			ts.load(fis, JfSSLScheme.password.toCharArray());
		}
		catch (Exception e){
			try {
				ts.load(null, JfSSLScheme.password.toCharArray());
			}
			catch (Exception e1){
				System.out.println(e.toString());
				return null;
			}
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}catch (Exception e1){}
			}
		}
		return ts;
    }
       
	public void doimport(View view){
		try {
			String url = "https://" +
					host.getText().toString() + ":" +
					port.getText().toString();
			importButton.setEnabled(false);
			new importer().execute(url).get(3000, TimeUnit.MILLISECONDS);
		}
		catch (Exception e){
			
		}
		finally {
			importButton.setEnabled(true);
		}
		if (certChain == null){
			return;
		}

		for (X509Certificate cert: certChain){
			try {
				cert.checkValidity();
			} catch (Exception e) {
				showToast(e.toString());
				return;
			}
			
		}
		String certString = certChain[0].toString();
		final String alias = certChain[0].getSubjectX500Principal().getName();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add Certificate?")
		.setMessage(certString)
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
		        try {
		        	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		        	keyPairGenerator.initialize(1024);
		        	KeyPair keyPair = keyPairGenerator.generateKeyPair();
		        	PrivateKey privateKey = keyPair.getPrivate();
		        	trustStore.setKeyEntry(alias, privateKey, JfSSLScheme.password.toCharArray(), certChain);
		        	FileOutputStream outputStream = openFileOutput(JfSSLScheme.trustfile, MODE_PRIVATE);
		        	trustStore.store(outputStream, JfSSLScheme.password.toCharArray());
		        	outputStream.flush();
		        	outputStream.close();
		        	certListAdaptor.add(alias);
		        }
		        catch (Exception e){
		        	System.out.println(e.toString());
		        	return;
		        }
			}
		})
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
					}
				});		
		AlertDialog dialog = builder.create();
		dialog.show();
		
	}
	
	class importer extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... urls) {
			certChain = null;
			HttpClient client = new DefaultHttpClient();
			HttpGet request = null;
			try {
				request = new HttpGet(urls[0]);
			}
			catch (Exception e){
				showToast(e.toString());
				return null;
			}
			AdditionalKeyStoresSSLSocketFactory sf;
			try {
            	sf = new AdditionalKeyStoresSSLSocketFactory(trustStore);
            	sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            	client.getConnectionManager().getSchemeRegistry().register( 
            			new Scheme("https", sf, 443));
            }
            catch (Exception e){
            	System.out.println(e.toString());
            	return null;
            }
			try { 
				client.execute(request);
			}
			catch (SSLHandshakeException e){
				if (e.getCause() instanceof CertificateException){
					certChain = sf.getCertChain();
				}
				else {
					if (e.getCause() == null){
						showToast(e.toString());
					}
					else {
						showToast(e.getCause().toString());
					}
				}
				return null;
				
			}
			catch (Exception e){
				showToast(e.toString());
				return null;
			}
			showToast("Valid certificate exists");
			return null;
		}
	}
	
	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(CertificateActivity.this, toast, Toast.LENGTH_LONG).show();
	        }
	    });
	}
	
	private void displayCert(final String alias){
		
		X509Certificate cert;
		try {
			cert = (X509Certificate) trustStore.getCertificate(alias);
		} catch (KeyStoreException e) {
			showToast(e.toString());
			return;
		}
		String certString = cert.toString();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Certificate")
		.setMessage(certString)
		.setPositiveButton("Remove",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				removeCert(alias);
			}
		})
		.setNegativeButton("Close",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});		
		AlertDialog dialog = builder.create();
		dialog.show();
		
	}

	private void removeCert(final String alias){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Remove Certificate?")
		.setMessage(alias)
		.setPositiveButton("Remove",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				try {
					trustStore.deleteEntry(alias);
		        	FileOutputStream outputStream = openFileOutput(JfSSLScheme.trustfile, MODE_PRIVATE);
		        	trustStore.store(outputStream, JfSSLScheme.password.toCharArray());
		        	outputStream.flush();
		        	outputStream.close();
		        	certListAdaptor.remove(alias);
				}
				catch (Exception e){
					System.out.println(e.toString());
				}
			}
		})
		.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});		
		AlertDialog dialog = builder.create();
		dialog.show();
		
	}

}
