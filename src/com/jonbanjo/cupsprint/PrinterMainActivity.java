package com.jonbanjo.cupsprint;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

public class PrinterMainActivity extends Activity {

	ListView printersListView;
	ArrayList<String> printersArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printer_main);
		printersListView=(ListView) findViewById(R.id.printersListView);
		registerForContextMenu(printersListView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.addprintermenu, menu);
		getMenuInflater().inflate(R.menu.certificatemenu, menu);
		getMenuInflater().inflate(R.menu.aboutmenu, menu);
		return true;
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.addprinter:
	    		addPrinter();
	    		break;
	    	case R.id.about:
	    		Intent intent = new Intent(this, AboutActivity.class);
	    		intent.putExtra("printer", "");
	    		startActivity(intent);
	    		break;
	    	case R.id.certificates:
	    		intent = new Intent(this, CertificateActivity.class);
	    		startActivity(intent);
	    		break;
	    }
	    return super.onContextItemSelected(item);
	 }
	
	@Override
	public void onStart(){
		super.onStart();
		IniHandler ini = new IniHandler(getBaseContext());
		printersArray = ini.getPrinters();
		if (printersArray.size() == 0){
			new AlertDialog.Builder(this)
			.setTitle("")
			.setMessage("No printers are configured. Add new printer?")
			.setIcon(android.R.drawable.ic_input_add)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	addPrinter();
			    }})
			 .setNegativeButton(android.R.string.no, null).show();	
			
		}
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, 
			android.R.layout.simple_list_item_1, printersArray);
		printersListView.setAdapter(aa);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
		    ContextMenuInfo menuInfo){
		  
		if (v== printersListView) {
			    menu.add("Edit");
			    menu.add("Delete");
			    menu.add("Mime types");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int index = (int) info.id;
		String op = item.getTitle().toString();
		final String printer = printersArray.get(index);
		if (op.equals("Delete")){
			
			new AlertDialog.Builder(this)
			.setTitle("Confim")
			.setMessage("Delete " + printer + "?")
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	doDelete(printer);
			    }})
			 .setNegativeButton(android.R.string.no, null).show();	
		}
		else if (op.equals("Edit")){
			Intent intent = new Intent(this, PrinterAddEditActivity.class);
			intent.putExtra("printer", printer);
			startActivity(intent);
		}
		else if (op.equals("Mime types")){
			Intent intent = new Intent(this, MimeTypesActivity.class);
			intent.putExtra("printer", printer);
			startActivity(intent);
			
		}
		return true;
	}

	private void doDelete(String printer){
		System.out.println("delete called");
		IniHandler ini = new IniHandler(getBaseContext());
		ini.removePrinter(printer);
		printersArray = ini.getPrinters();
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, printersArray);
		printersListView.setAdapter(aa);
	}
	
	public void addPrinter(){
		Intent intent = new Intent(this, PrinterAddEditActivity.class);
		intent.putExtra("printer", "");
		startActivity(intent);
	}
	
}
