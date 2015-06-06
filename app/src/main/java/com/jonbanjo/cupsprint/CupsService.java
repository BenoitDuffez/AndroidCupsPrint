/*
 * CupsService.java
 *
 * Copyright (c) 2015, Benoit Duffez. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.jonbanjo.cupsprint;

import android.os.AsyncTask;
import android.print.PrintJobInfo;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * CUPS print service
 */
public class CupsService extends PrintService {
	@Override
	protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
		return new CupsPrinterDiscoverySession(this);
	}

	@Override
	protected void onRequestCancelPrintJob(PrintJob printJob) {
		// TODO
	}

	@Override
	protected void onPrintJobQueued(final PrintJob printJob) {
		final PrintJobInfo jobInfo = printJob.getInfo();
		String url = jobInfo.getPrinterId().getLocalId();
		String clientUrl = url.substring(0, url.substring(0, url.lastIndexOf('/')).lastIndexOf('/'));

		try {
			// Prepare job
			final URL printerURL = new URL(url);
			final URL clientURL = new URL(clientUrl);
			final FileDescriptor fd = printJob.getDocument().getData().getFileDescriptor();

			// Send print job
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					printDocument(clientURL, printerURL, fd);
					return null;
				}

				@Override
				protected void onPostExecute(Void v) {
					onPrintJobSent(printJob);
				}
			}.execute();
		} catch (MalformedURLException e) {
			Log.e("CUPS", "Couldn't queue print job: " + printJob + " because: " + e);
		}
	}

	/**
	 * Called from a background thread, when the print job has to be sent to the printer.
	 *
	 * @param clientURL  The client URL
	 * @param printerURL The printer URL
	 * @param fd         The document to print, as a {@link FileDescriptor}
	 */
	private void printDocument(URL clientURL, URL printerURL, FileDescriptor fd) {
		try {
			CupsClient client = new CupsClient(clientURL);
			CupsPrinter printer = client.getPrinter(printerURL);

			InputStream is = new FileInputStream(fd);
			org.cups4j.PrintJob job = new org.cups4j.PrintJob.Builder(is).build();
			printer.print(job);
		} catch (Exception e) {
			Log.e("CUPS", "Couldn't send file descriptor: " + fd + " to printer because: " + e);
		}
	}

	/**
	 * Called on the main thread, when the job was sent to the printer
	 *
	 * @param printJob The print job
	 */
	private void onPrintJobSent(PrintJob printJob) {
		printJob.start();
	}
}
