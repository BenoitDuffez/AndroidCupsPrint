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

package com.jonbanjo.detect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;

public class MdnsScanTask extends AsyncTask<Void, Integer, PrinterResult>
implements ProgressUpdater{

MdnsServices services;
Context context;
ProgressDialog pd;
PrinterUpdater printerUpdater;
boolean stopped = false;

public MdnsScanTask(Context context, PrinterUpdater printerUpdater){
	this.context = context;
	this.printerUpdater = printerUpdater;
}

@Override
protected PrinterResult doInBackground(Void... arg0) {
	services = new MdnsServices(this);
	PrinterResult results = services.scan();
	return results;
}

@Override
protected void onPreExecute(){
	pd = new ProgressDialog(context);
	pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	pd.setIndeterminate(false);
	pd.setCanceledOnTouchOutside(false);
	pd.setMax(100);
	pd.setTitle("Scanning mDNS");
	pd.setOnDismissListener(new OnDismissListener(){
		@Override
        public void onDismiss(DialogInterface dialog) {
			doStop();
        }			
	});
	pd.show();
}

protected void onProgressUpdate(Integer... progress) {
	pd.setProgress(progress[0]);
}

@Override
protected void onPostExecute(PrinterResult result){
	services = null;
	if (pd != null)
		pd.dismiss();
	if (!stopped)
		printerUpdater.getDetectedPrinter(result);
}

@Override
public void DoUpdate(int value) {
	this.publishProgress(value);
}

public void doStop(){
	stopped = true;
	if (!(services == null)){
		services.stop();
	}

}

}

