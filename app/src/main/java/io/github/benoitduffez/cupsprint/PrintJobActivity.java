package io.github.benoitduffez.cupsprint;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.jonbanjo.cups.CupsPpd;
import com.jonbanjo.cups.CupsPrinterExt;
import com.jonbanjo.cups.PpdItemList;
import com.jonbanjo.cups.PpdSectionList;
import com.jonbanjo.cupscontrols.CupsTableLayout;

import org.cups4j.CupsPrinter;
import org.cups4j.PrintJob;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PrintJobActivity extends Activity {

	static CupsPpd cupsppd;

	PrintConfig printer;

	Uri data;

	Button printButton;

	Button moreButton;

	boolean isImage;

	boolean moreClicked = false;

	CupsTableLayout layout;

	boolean mimeTypeSupported = false;

	boolean acceptMimeType = false;

	String mimeMessage = "";

	TableRow buttonRow;

	boolean uiSet = false;

	public static CupsPpd getPpd() {
		return cupsppd;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print_job);
		Intent intent = getIntent();
		String type = intent.getStringExtra("type");
		String sPrinter;
		if (type.equals("static")) {
			sPrinter = intent.getStringExtra("printer");
			IniHandler ini = new IniHandler(getBaseContext());
			printer = ini.getPrinter(sPrinter);
		} else {
			sPrinter = intent.getStringExtra("name");
			printer = new PrintConfig(
					intent.getStringExtra("name"),
					intent.getStringExtra("protocol"),
					intent.getStringExtra("host"),
					intent.getStringExtra("port"),
					intent.getStringExtra("queue"));
			printer.orientation = "portrait";
			printer.extensions = "";
			printer.imageFitToPage = true;

		}
		if (printer == null) {
			new AlertDialog.Builder(this)
					.setTitle("Error")
					.setMessage("Config for " + sPrinter + " not found")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					})
					.show();
			return;
		}
		data = intent.getData();
		if (data == null) {
			showToast("File URI is null");
			finish();
			return;
		}
		String fileName = getFileName();
		//if (fileName.equals("")){
		//	showToast("Document not found");
		//	finish();
		//	return;
		//}
		String[] fileParts = fileName.split("\\.");
		String mimeType = "";
		String ext = "";
		if (fileParts.length > 1) {
			ext = fileParts[fileParts.length - 1].toLowerCase(Locale.ENGLISH);
		}
		if (!ext.equals("")) {
			mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		}
		if (mimeType == null) {
			mimeType = "";
		}
		if (mimeType.equals("")) {
			mimeType = intent.getStringExtra("mimeType");
		}
		checkMimeType(mimeType);
		if (!(mimeMessage.equals(""))) {
			new AlertDialog.Builder(this)
					.setTitle("Error")
					.setMessage("Unable to get mime types\nfor " + printer.queue + "\n" + mimeMessage)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					})
					.show();
			return;
		}
		if (!mimeTypeSupported) {
			setAcceptMimeType(mimeType, ext);
		}
		if (mimeType.contains("image"))
			isImage = true;
		else
			isImage = false;

		cupsppd = new CupsPpd();
		if (printer.noOptions) {
			if (mimeTypeSupported) {
				setStdOpts();
				setPrint(cupsppd.getCupsStdString());
				return;
			}
		}
		moreButton = getButton("More...");
		moreButton.setEnabled(false);
		moreButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doGroup();
			}
		});

		if (printer.merge) {
			GetPpdTask getPpd = new GetPpdTask();
			try {
				getPpd.execute().get(7000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				printer.merge = false;
				cupsppd = new CupsPpd();
				showToast("Ppd Merge Failed\n" + e.toString());
			}
		}

		setStdOpts();
		printButton = getButton("Print");
		printButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				printButton.setFocusableInTouchMode(true);
				printButton.requestFocus();
				if (!layout.update())
					return;
				String stdAttrs = cupsppd.getCupsStdString();
				setPrint(stdAttrs);
			}
		});

		buttonRow = new TableRow(this);
		buttonRow.addView(moreButton);
		buttonRow.addView(printButton);
		setControls();
		if (!printer.merge) {
			GetPpdTask getPpd = new GetPpdTask();
			getPpd.execute();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (uiSet) {
			setControls();
		}
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

	private void setStdOpts() {
		for (PpdSectionList group : cupsppd.getStdList()) {
			for (PpdItemList section : group) {
				if (section.getName().equals("fit-to-page")) {
					if (isImage && printer.imageFitToPage)
						section.setSavedValue("true");
				} else if (section.getName().equals("orientation-requested")) {
					if (!(printer.orientation.equals(""))) {
						section.setSavedValue(printer.orientation);
						section.setDefaultValue("-1");
					}
				}
			}
		}

	}

	private void doGroup() {
		Intent intent = new Intent(this, PpdGroupsActivity.class);
		startActivity(intent);
		moreClicked = true;
	}

	private Button getButton(String defaultVal) {
		Button btn = new Button(this);
		btn.setText(defaultVal);
		return btn;
	}

	private void setControls() {
		layout = (CupsTableLayout) findViewById(R.id.printjobLayout);
		layout.reset();
		layout.setShowName(false);
		for (PpdSectionList group : cupsppd.getStdList()) {
			layout.addSection(group);
		}
		layout.addView(buttonRow, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		uiSet = true;
	}

	private void setPrint(String stdAttrs) {
		String ppdAttrs = "";
		if (moreClicked) {
			if (cupsppd != null)
				ppdAttrs = cupsppd.getCupsExtraString();
		}
		if (!(stdAttrs.equals("")) && !(ppdAttrs.equals("")))
			stdAttrs = stdAttrs + "#" + ppdAttrs;
		else
			stdAttrs = stdAttrs + ppdAttrs;
		doPrint(stdAttrs);
	}

	private URL getPrinterUrl() throws MalformedURLException {
		String sUrl = printer.protocol + "://" + printer.host + ":" + printer.port +
				"/printers/" + printer.queue;
		return new URL(sUrl);
	}

	private void setAcceptMimeType(String mimeType, String ext) {
		acceptMimeType = false;
		String[] extensions = printer.extensions.split(" ");
		for (String item : extensions) {
			if (ext.equals(item)) {
				acceptMimeType = true;
				return;
			}
		}
		String msg = printer.nickname + " \ndoes not support " + mimeType + "\n\nContinue?";
		msg = msg + "\n\nNote. If this printer does support files with the " + ext + " extension, ";
		msg = msg + "you may wish to add " + ext + " to this printers extensions";
		new AlertDialog.Builder(this)
				.setTitle("Unspported mime type")
				.setMessage(msg)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {
						acceptMimeType = true;
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {
						finish();
					}
				})
				.show();
	}

	private void checkMimeType(final String mimeType) {

		mimeTypeSupported = false;
		mimeMessage = "Timed out";
		final CountDownLatch latch = new CountDownLatch(1);
		Thread thread = new Thread() {

			@Override
			public void run() {
				try {
					CupsPrinterExt printer = new CupsPrinterExt(getPrinterUrl(), null, false);
					mimeTypeSupported = printer.mimeTypeSupported(mimeType);
					if (printer.getSupportedMimeTypes().size() > 0) {
						mimeMessage = "";
					} else {
						mimeMessage = "No mime types found";
					}
				} catch (Exception e) {
					mimeMessage = e.toString();
				}
				latch.countDown();
			}
		};
		thread.start();
		try {
			if (!latch.await(5L, TimeUnit.SECONDS)) {
				System.out.println("Timed Out");
			}
		} catch (Exception e) {
			mimeMessage = e.getMessage();
		}
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(PrintJobActivity.this, toast, Toast.LENGTH_LONG).show();
			}
		});
	}

	public String getFileName() {
		String fileName = "";
		if ("content".equals(data.getScheme())) {
			try {
				//Cursor cursor = getContentResolver().query(data, null, null, null, null);
				Cursor cursor = getContentResolver().query(data, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
				cursor.moveToFirst();
				fileName = cursor.getString(0);
				cursor.close();
			} catch (Exception e) {
				fileName = data.getPath();
			}
		} else {
			fileName = data.getPath();
		}
		if (fileName == null) {
			return "";
		}
		String[] fileParts = fileName.split("/");
		if (fileParts.length > 0)
			fileName = fileParts[fileParts.length - 1];

		return fileName;
	}

	public void doPrint(final String attrs) {


		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					System.setProperty("java.net.preferIPv4Stack", "true");
					CupsPrinter printer = new CupsPrinter(getPrinterUrl(), null, false);
					FileInputStream file;
					try {
						file = (FileInputStream) getContentResolver().openInputStream(data);
					} catch (FileNotFoundException e) {
						showToast("JfCupsPrint error\n " + e.toString());
						System.out.println(e.toString());
						return;
					}
					String fileName = getFileName();
					PrintJob job = new PrintJob.Builder(file).copies(0).jobName(fileName).build();
					if (!(attrs.equals(""))) {
						Map<String, String> attributes = new HashMap<String, String>();
						attributes.put("job-attributes", attrs);
						job.setAttributes(attributes);
					}
					String printResult = printer.print(job).getResultDescription();
					showToast("JfCupsPrint\n" + fileName + "\n" + printResult);
					System.out.println("job printed");
				} catch (Exception e) {
					showToast("JfCupsPrint error:\n" + e.toString());
					System.out.println(e.toString());
				}
			}

		};
		thread.start();
		setResult(500);
		finish();
	}

	public class GetPpdTask extends AsyncTask<Void, Void, Void> {

		private Exception exception = null;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String sUrl = printer.protocol + "://" + printer.host + ":" + printer.port +
						"/printers/" + printer.queue;
				CupsPrinter cupsPrinter = new CupsPrinter(new URL(sUrl), null, false);
				cupsppd.createExtraList(cupsPrinter, printer.merge);
			} catch (Exception e) {
				exception = e;
				showToast("Parsing ppd failed\n" + e.toString());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			if (exception == null) {
				moreButton.setEnabled(true);
			}
		}
	}

}
