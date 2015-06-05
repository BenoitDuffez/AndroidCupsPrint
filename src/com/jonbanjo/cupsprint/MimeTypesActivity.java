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

package com.jonbanjo.cupsprint;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.jonbanjo.cups.CupsPrinterExt;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class MimeTypesActivity extends Activity {

	private CupsPrinterExt printer;
	private PrintConfig printConfig;
	private String message = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mime_types);
		Intent intent = getIntent();
		String sPrinter = intent.getStringExtra("printer");
		IniHandler ini = new IniHandler(getBaseContext());
	    printConfig = ini.getPrinter(sPrinter);
	    if (printConfig == null){
			new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage("Config for " + sPrinter + " not found")
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	finish();
			    }})
			 .show();	
	    	return;
	    }
	    
	    getPrinter();
	    
	    if (printer == null){
			new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage(message)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	finish();
			    }})
			 .show();	
	    	return;
	    }
	    
	    ArrayList<String> mimeTypes = printer.getSupportedMimeTypes();
	    if (mimeTypes.size() == 0){
			new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage("Unable to get mime types for " + sPrinter)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	finish();
			    }})
			 .show();	
	    	return;
	    }
	    
		TextView mimeList = (TextView) findViewById(R.id.mimeList);
		String S = sPrinter + "\n\n"; 
	    for(String type: mimeTypes){
	    	S = S + type + "\n";
	    }
		mimeList.setText(S);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aboutmenu, menu);
		return true;
	}

	private URL getPrinterUrl() throws MalformedURLException{
        String sUrl = printConfig.protocol + "://" + printConfig.host + ":" + printConfig.port + 
        		"/printers/" + printConfig.queue;
        	return new URL(sUrl);
 	}
	
	private void getPrinter(){
		
		final CountDownLatch latch = new CountDownLatch(1);
		Thread thread = new Thread(){
		
			@Override
			public void run(){
				try{
					printer = new CupsPrinterExt(getPrinterUrl(),null, false);
				}
				catch (Exception e){
					message = e.toString();
				}
				latch.countDown();
			}
		};
		thread.start();
		try{
			latch.await(5L, TimeUnit.SECONDS);
		}
		catch (Exception e){
			message = (e.toString());
		}
	}
}
