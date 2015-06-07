package io.github.benoitduffez.cupsprint;

import java.util.ArrayList;
import java.util.List;

import com.jonbanjo.detect.HostScanTask;
import com.jonbanjo.detect.MdnsScanTask;
import com.jonbanjo.detect.PrinterRec;
import com.jonbanjo.detect.PrinterResult;
import com.jonbanjo.detect.PrinterUpdater;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

public class PrinterPrintToActivity extends Activity implements PrinterUpdater{

	ListView printersListView;
	ArrayList<String> printersArray;
	Uri jobUri;
	String mimeType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printer_print_to);
		printersListView=(ListView) findViewById(R.id.printersPrintToView);
		registerForContextMenu(printersListView);
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
		        jobUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
				mimeType = intent.getType();
		}
		else if (Intent.ACTION_VIEW.equals(action) && type != null) {
	        jobUri = (Uri) intent.getData();
			mimeType = intent.getType();
		}
		else if ("org.androidprinting.intent.action.PRINT".equals(action) && type != null) {
	        jobUri = (Uri) intent.getData();
	        if (jobUri == null){
	        	jobUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	        }
			mimeType = intent.getType();
		}

		if (jobUri == null){
			String toast = "No printable document found";
            Toast.makeText(PrinterPrintToActivity.this, toast, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		printersListView.setClickable(true);
		printersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if (jobUri == null)
					return;
				String printer = (String) printersListView.getItemAtPosition(position);
				doPrintJob(printer);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scanmenu, menu);
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
	    	case R.id.scanhost:
	    		new HostScanTask(this, this).execute();
	    		break;
	    	case R.id.scanmdns:
	    		new MdnsScanTask(this, this).execute();
	    		break;
	    }
	    return super.onContextItemSelected(item);
	 }

	@Override
	public void onStart(){
		super.onStart();
		IniHandler ini = new IniHandler(getBaseContext());
		printersArray = ini.getPrinters();
		//if (printersArray.size() == 0){
		//	 Intent intent = new Intent(this, PrinterMainActivity.class);
		//     startActivity(intent);
		//	 finish();
		//}
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, 
			android.R.layout.simple_list_item_1, printersArray);
		printersListView.setAdapter(aa);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == 500){
			finish();
		}
	}
	
	private void doPrintJob(String printer){
		Intent sendIntent = new Intent(this, PrintJobActivity.class);
		sendIntent.putExtra("type", "static");
		sendIntent.putExtra("printer", printer);
		sendIntent.putExtra("mimeType", mimeType);
		sendIntent.setData(jobUri);
		this.startActivityForResult(sendIntent, 500);
		}
	

	private void showResult(String title, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setTitle(title);
		AlertDialog dialog = builder.create();	
		dialog.show();
	}
	
	public void getDetectedPrinter(PrinterResult results){
		
		List<PrinterRec> printers = results.getPrinters();
		if (printers.size() < 1){
			this.showResult("", "No printers found");
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
				dynPrint(printer);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void dynPrint(PrinterRec printer){
		Intent sendIntent = new Intent(this, PrintJobActivity.class);
		sendIntent.putExtra("type", "dynamic");
		sendIntent.putExtra("name", printer.getNickname());
		sendIntent.putExtra("host", printer.getHost());
		sendIntent.putExtra("protocol", printer.getProtocol());
		sendIntent.putExtra("port", String.valueOf(printer.getPort()));
		sendIntent.putExtra("queue", printer.getQueue());
		sendIntent.putExtra("mimeType", mimeType);
		sendIntent.setData(jobUri);
		this.startActivityForResult(sendIntent, 500);
	}
	
}
