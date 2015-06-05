package com.jonbanjo.cupsprint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.jonbanjo.cups.OptionPair;
import com.jonbanjo.detect.HostScanTask;
import com.jonbanjo.detect.MdnsScanTask;
import com.jonbanjo.detect.PrinterRec;
import com.jonbanjo.detect.PrinterResult;
import com.jonbanjo.detect.PrinterUpdater;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.conn.HttpHostConnectException;
import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

public class PrinterAddEditActivity extends Activity implements PrinterUpdater{

	EditText nickname;
	Spinner  protocol;
	EditText host;
	EditText port;
	EditText queue;
	Spinner orientation;
	EditText extensions;
	CheckBox fitToPage;
	CheckBox fitPlot;
	CheckBox noOptions;
	CheckBox isDefault;
	CheckBox mergePpd;
	String oldPrinter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printer_add_edit);
		Intent intent = getIntent();
		
		oldPrinter = intent.getStringExtra("printer");
		nickname = (EditText) findViewById(R.id.editNickname);
		protocol = (Spinner) findViewById(R.id.editProtocol);
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, 
 				android.R.layout.simple_spinner_item, EditControls.protocols);
		protocol.setAdapter(aa);
		host = (EditText) findViewById(R.id.editHost);
		port = (EditText) findViewById(R.id.editPort);
		queue = (EditText) findViewById(R.id.editQueue);
		orientation = (Spinner) findViewById(R.id.editOrientation);
		extensions = (EditText) findViewById(R.id.editExtensions);
		ArrayAdapter<OptionPair> aa1 = new ArrayAdapter<OptionPair>(this, 
	 				android.R.layout.simple_spinner_item, EditControls.orientationOpts);
	 	orientation.setAdapter(aa1);

		fitToPage = (CheckBox) findViewById(R.id.editFitToPage);
		noOptions = (CheckBox) findViewById(R.id.editNoOptions);
		isDefault = (CheckBox) findViewById(R.id.editIsDefault);
		mergePpd = (CheckBox) findViewById(R.id.mergeppd);
		
		if (!oldPrinter.contentEquals("")){
		     IniHandler ini = new IniHandler(getBaseContext());
		     PrintConfig conf = ini.getPrinter(oldPrinter);
		     if (conf != null){
		    	 nickname.setText(conf.nickname);
		    	 int size = EditControls.protocols.size();
		    	 int pos = 0;
		 		 for (pos=0; pos<size; pos++){
		 			 String test = EditControls.protocols.get(pos);
		 			 if (test.equals(conf.protocol)){
		 				 protocol.setSelection(pos);
		 				 break;
		 			 }
		 		 }
		    	 host.setText(conf.host);
		    	 port.setText(conf.port);
		    	 queue.setText(conf.queue);
		    	 size = EditControls.orientationOpts.size();
		    	 pos = 0;
		 		 for (pos=0; pos<size; pos++){
		 			 OptionPair opt = EditControls.orientationOpts.get(pos);
		 			 if (opt.option.equals(conf.orientation)){
		 				 orientation.setSelection(pos);
		 				 break;
		 			 }
		 		 }
		 		 extensions.setText(conf.extensions);
		 		 fitToPage.setChecked(conf.imageFitToPage);
		    	 noOptions.setChecked(conf.noOptions);
		    	 isDefault.setChecked(conf.isDefault);
		    	 mergePpd.setChecked(conf.merge);
		     }
		}
		if (oldPrinter.equals("")){
			port.setText("631");
		}
			
	}

	  
   @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scanmenu, menu);
		getMenuInflater().inflate(R.menu.certificatemenu, menu);
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
	    	case R.id.certificates:
	    		intent = new Intent(this, CertificateActivity.class);
	    		intent.putExtra("host", host.getText().toString());
	    		intent.putExtra("port", port.getText().toString());
	    		startActivity(intent);
	    		break;
	    	case R.id.scanhost:
	    		new HostScanTask(this, this).execute();
	    		break;
	    	case R.id.scanmdns:
	    		new MdnsScanTask(this, this).execute();
	    		break;
	    }
	    return super.onContextItemSelected(item);
	 }

	private void alert(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setTitle("error");
		AlertDialog dialog = builder.create();	
		dialog.show();
	}
	
	
	private boolean checkEmpty(String fieldName, String value){
		if (value.equals("")){
			alert(fieldName + " missing");
			return false;
		}
		return true;
	}
	
	private boolean checkInt(String fieldName, String value){
		
		try {
			@SuppressWarnings("unused")
			int test = Integer.parseInt(value);
			return true;
		}
		catch (Exception e){
			alert(fieldName + " must be an integer");
			return false;
		}
	}
	
	private boolean checkExists(String name, IniHandler ini){
		
		if (oldPrinter.equals(name))
			return false;
		if (!ini.printerExists(name))
			return false;
		
		alert("nickname must be unique");
		return true;
				
	}
	
	private void showResult(String title, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setTitle(title);
		AlertDialog dialog = builder.create();	
		dialog.show();
	}
	
	public void testPrinter(View view){
		String testStatus = "";
		String testResult = "";
		try {
			String url = 
					(String)protocol.getSelectedItem() +
					"://" + host.getText().toString() +
					":" + port.getText().toString() +
					"/printers/" + queue.getText().toString();
			String[] schemes = {"http","https"};
			UrlValidator validator = new UrlValidator(schemes);
			if (!(validator.isValid(url))){
				showResult("Failed", "Invalid URL:" + url);
				return;
			}
			URL printerURL = new URL(url);
			
			url = 
					(String)protocol.getSelectedItem() +
					"://" + host.getText().toString() +
					":" + port.getText().toString();
			URL clientURL = new URL(url);
			testStatus = "";
			testResult = "";	Log.d("CUPS", "printer tester: "+clientURL+", "+printerURL);
			String [] results = new PrinterTester().execute(clientURL, printerURL).get(5000, TimeUnit.MILLISECONDS);
			testStatus = results[0];
			testResult = results[1];
		}
		catch (Exception e){
			testStatus = "Failed";
			testResult = e.toString();
		}
		if (testStatus.equals("")){
			testStatus = "Failed";
			testResult = "Timed out";
		}
		showResult(testStatus, testResult);
	}
	
	
	public void savePrinter(View view) {
	     IniHandler ini = new IniHandler(getBaseContext());
	     String sNickname = nickname.getText().toString().trim();
	     if (!checkEmpty("Nickname", sNickname)){
	    	 nickname.requestFocus();
	    	 return;
	     }
	     if (checkExists(sNickname, ini)){
	    	 nickname.requestFocus();
	    	 return;
	     }
	     String sHost = host.getText().toString().trim();
	     if (!checkEmpty("Host", sHost)){
	    	 host.requestFocus();
	    	 return;
	     }
	     String sPort = port.getText().toString().trim();
	     if (!checkEmpty("Port", sPort)){
	    	 port.requestFocus();
	    	 return;
	     }
	     if (!checkInt("Port", sPort)){
	    	 port.requestFocus();
	    	 return;
	     }
	     String sQueue = queue.getText().toString().trim();
	     if (!checkEmpty("Queue", sQueue)){
	    	 queue.requestFocus();
	    	 return;
	     }
	     String sProtocol = (String) protocol.getSelectedItem();
	     PrintConfig conf = new PrintConfig(sNickname, sProtocol, sHost, sPort, sQueue);
	     OptionPair opt = (OptionPair) orientation.getSelectedItem();
	     conf.orientation = opt.option;
	     conf.extensions = extensions.getText().toString().trim();
	     conf.imageFitToPage = fitToPage.isChecked();
	     conf.noOptions = noOptions.isChecked();
	     conf.isDefault = isDefault.isChecked();
	     conf.merge = mergePpd.isChecked();
	     ini.addPrinter(conf, oldPrinter);
		 Intent intent = new Intent(this, PrinterMainActivity.class);
	     startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	     }
	
		public void getDetectedPrinter(PrinterResult results){
			if (results == null){
				chooseDetectedPrinter(new ArrayList<PrinterRec>());
				return;
			}
			List<String> errors = results.getErrors();
			final List<PrinterRec> printers = results.getPrinters();
			if (errors.size() > 0){
				String errorMessage = "";
				for (String error: errors){
					errorMessage = errorMessage + error + "\n";
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Scan messages");
				builder.setMessage(errorMessage);
				builder.setOnCancelListener(new OnCancelListener() {
				    public void onCancel(final DialogInterface dialog) {
				    	chooseDetectedPrinter(printers);
				    }
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			else {
				chooseDetectedPrinter(printers);
			}
		}
			
		public void chooseDetectedPrinter(List<PrinterRec> printers){
			if (printers.size() < 1){
				showResult("", "No printers found");
				return;
			}
			final ArrayAdapter<PrinterRec> aa = new ArrayAdapter<PrinterRec>(
					this, android.R.layout.simple_list_item_1,printers); 
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select printer");
			builder.setAdapter(aa, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PrinterRec printer = aa.getItem(which);
					nickname.setText(printer.getNickname());
					int size = protocol.getCount();
					int i;
					for (i=0; i<size; i++){
						if (protocol.getItemAtPosition(i).equals(printer.getProtocol())){
							protocol.setSelection(i);
							break;
						}
					}
					protocol.setSelection(i);
					host.setText(printer.getHost());
					port.setText(String.valueOf(printer.getPort()));
					queue.setText(printer.getQueue());					
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
		private static class PrinterTester extends AsyncTask<URL,Void, String[]>{
			
			
			@Override
			protected String[] doInBackground(URL... params) {
				try{
					CupsClient client = new CupsClient(params[0]);
					CupsPrinter testPrinter = client.getPrinter(params[1]);
					if (testPrinter == null){
						return new String[] {"Failed", "Printer not found"};
					}
					else{
						String result = "Name: " + testPrinter.getName() +
								"\nDescription: " + testPrinter.getDescription() +
								"\nLocation: " + testPrinter.getLocation();
						return new String[] {"Success", result};
					}
				}
				catch (HttpHostConnectException e){
					return new String[] {"Failed", "Connection refused"};
				}
				catch (Exception e){
					return new String[] {"Failed", e.getMessage()};
				}
			}
		}
}
