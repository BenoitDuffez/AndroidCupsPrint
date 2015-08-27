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

package io.github.benoitduffez.cupsprint.printservice;

import android.os.AsyncTask;
import android.os.Handler;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.JobStateEnum;
import org.cups4j.PrintJobAttributes;
import org.cups4j.PrintRequestResult;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.github.benoitduffez.cupsprint.CupsPrintApp;

/**
 * CUPS print service
 */
public class CupsService extends PrintService {
	/**
	 * When a print job is active, the app will poll the printer to retrieve the job status. This is the polling interval.
	 */
	public static final int JOB_CHECK_POLLING_INTERVAL = 5000;

	/**
	 * Lock for setting/accessing the static instance
	 */
	private static final Object sLock = new Object();

	/**
	 * Static instance, valid when the service is connected
	 */
	private static CupsService sInstance;

	/**
	 * Current discovery session
	 */
	private CupsPrinterDiscoverySession mSession;

	Map<PrintJobId, Integer> mJobs = new HashMap<>();

	public static CupsService peekInstance() {
		synchronized (sLock) {
			return sInstance;
		}
	}

	@Override
	protected void onConnected() {
		synchronized (sLock) {
			sInstance = this;
		}
	}

	@Override
	protected void onDisconnected() {
		synchronized (sLock) {
			sInstance = null;
			mSession = null;
		}
	}

	public PrinterDiscoverySession getSession() {
		return mSession;
	}

	@Override
	protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
		mSession = new CupsPrinterDiscoverySession(this);
		return mSession;
	}

	@Override
	protected void onRequestCancelPrintJob(final PrintJob printJob) {
		final PrintJobInfo jobInfo = printJob.getInfo();
		String url = jobInfo.getPrinterId().getLocalId();
		String clientUrl = url.substring(0, url.substring(0, url.lastIndexOf('/')).lastIndexOf('/'));
		final int jobId = mJobs.get(printJob.getId());

		try {
			final URL clientURL = new URL(clientUrl);
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					cancelPrintJob(clientURL, jobId);
					return null;
				}

				@Override
				protected void onPostExecute(Void v) {
					onPrintJobCancelled(printJob);
				}
			}.execute();
		} catch (MalformedURLException e) {
			Log.e(CupsPrintApp.LOG_TAG, "Couldn't cancel print job: " + printJob + " because: " + e);
		}
	}

	/**
	 * Called from a background thread, ask the printer to cancel a job by its printer job ID
	 *
	 * @param clientURL The printer client URL
	 * @param jobId     The printer job ID
	 */
	private void cancelPrintJob(URL clientURL, int jobId) {
		try {
			CupsClient client = new CupsClient(clientURL);
			client.cancelJob(jobId);
		} catch (Exception e) {
			Log.e(CupsPrintApp.LOG_TAG, "Couldn't cancel job: " + jobId + " because: " + e);
		}
	}

	/**
	 * Called on the main thread, when the print job was cancelled
	 *
	 * @param printJob The print job
	 */
	private void onPrintJobCancelled(PrintJob printJob) {
		mJobs.remove(printJob.getId());
		printJob.cancel();
	}

	@Override
	protected void onPrintJobQueued(final PrintJob printJob) {
		startPolling(printJob);
		final PrintJobInfo jobInfo = printJob.getInfo();
		String url = jobInfo.getPrinterId().getLocalId();
		String clientUrl = url.substring(0, url.substring(0, url.lastIndexOf('/')).lastIndexOf('/'));

		try {
			// Prepare job
			final URL printerURL = new URL(url);
			final URL clientURL = new URL(clientUrl);
			final FileDescriptor fd = printJob.getDocument().getData().getFileDescriptor();
			final PrintJobId jobId = printJob.getId();

			// Send print job
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					printDocument(jobId, clientURL, printerURL, fd);
					return null;
				}

				@Override
				protected void onPostExecute(Void v) {
					onPrintJobSent(printJob);
				}
			}.execute();
		} catch (MalformedURLException e) {
			Log.e(CupsPrintApp.LOG_TAG, "Couldn't queue print job: " + printJob + " because: " + e);
		}
	}

	private void startPolling(final PrintJob printJob) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (updateJobStatus(printJob)) {
					new Handler().postDelayed(this, JOB_CHECK_POLLING_INTERVAL);
				}
			}
		}, JOB_CHECK_POLLING_INTERVAL);
	}

	/**
	 * Called in the main thread, will ask the job status and update it in the Android framework
	 *
	 * @param printJob The print job
	 * @return true if this method should be called again, false otherwise (in case the job is still pending or it is complete)
	 */
	private boolean updateJobStatus(final PrintJob printJob) {
		// Check if the job is already gone
		if (!mJobs.containsKey(printJob.getId())) {
			return false;
		}

		String url = printJob.getInfo().getPrinterId().getLocalId();
		String clientUrl = url.substring(0, url.substring(0, url.lastIndexOf('/')).lastIndexOf('/'));

		// Prepare job
		try {
			final URL clientURL = new URL(clientUrl);
			final int jobId = mJobs.get(printJob.getId());

			// Send print job
			new AsyncTask<Void, Void, JobStateEnum>() {
				@Override
				protected JobStateEnum doInBackground(Void... params) {
					return getJobState(jobId, clientURL);
				}

				@Override
				protected void onPostExecute(JobStateEnum state) {
					onJobStateUpdate(printJob, state);
				}
			}.execute();
		} catch (MalformedURLException e) {
			Log.e(CupsPrintApp.LOG_TAG, "Couldn't get job: " + printJob + " state because: " + e);
		}

		// We want to be called again if the job is still in this map
		// Indeed, when the job is complete, the job is removed from this map.
		return mJobs.containsKey(printJob.getId());
	}

	/**
	 * Called in a background thread, in order to check the job status
	 *
	 * @param jobId     The printer job ID
	 * @param clientURL The printer client URL
	 * @return true if the job is complete/aborted/cancelled, false if it's still processing (printing, paused, etc)
	 */
	private JobStateEnum getJobState(int jobId, URL clientURL) {
		CupsClient client = new CupsClient(clientURL);
		try {
			PrintJobAttributes attr = client.getJobAttributes(jobId);
			return attr.getJobState();
		} catch (Exception e) {
			Log.e(CupsPrintApp.LOG_TAG, "Couldn't get job: " + jobId + " state because: " + e);
		}
		return null;
	}

	/**
	 * Called on the main thread, when a job status has been checked
	 *
	 * @param printJob The print job
	 * @param state    Print job state
	 */
	private void onJobStateUpdate(PrintJob printJob, JobStateEnum state) {
		// Couldn't check state -- don't do anything
		if (state == null) {
			mJobs.remove(printJob.getId());
			printJob.cancel();
		} else {
			if (state == JobStateEnum.CANCELED) {
				mJobs.remove(printJob.getId());
				printJob.cancel();
			} else if (state == JobStateEnum.COMPLETED || state == JobStateEnum.ABORTED) {
				mJobs.remove(printJob.getId());
				printJob.complete();
			}
		}
	}

	/**
	 * Called from a background thread, when the print job has to be sent to the printer.
	 *
	 * @param clientURL  The client URL
	 * @param printerURL The printer URL
	 * @param fd         The document to print, as a {@link FileDescriptor}
	 */

	private void printDocument(PrintJobId jobId, URL clientURL, URL printerURL, FileDescriptor fd) {
		try {
			CupsClient client = new CupsClient(clientURL);
			CupsPrinter printer = client.getPrinter(printerURL);

			InputStream is = new FileInputStream(fd);
			org.cups4j.PrintJob job = new org.cups4j.PrintJob.Builder(is).build();
			PrintRequestResult result = printer.print(job);
			mJobs.put(jobId, result.getJobId());
		} catch (Exception e) {
			Log.e(CupsPrintApp.LOG_TAG, "Couldn't send file descriptor: " + fd + " to printer because: " + e);
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

