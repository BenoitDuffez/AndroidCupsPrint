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
import android.os.ParcelFileDescriptor;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

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
import io.github.benoitduffez.cupsprint.R;

/**
 * CUPS print service
 */
public class CupsService extends PrintService {
    /**
     * When a print job is active, the app will poll the printer to retrieve the job status. This is the polling interval.
     */
    public static final int JOB_CHECK_POLLING_INTERVAL = 5000;

    Map<PrintJobId, Integer> mJobs = new HashMap<>();

    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        return new CupsPrinterDiscoverySession(this);
    }

    @Override
    protected void onRequestCancelPrintJob(final PrintJob printJob) {
        final PrintJobInfo jobInfo = printJob.getInfo();
        final PrinterId printerId = jobInfo.getPrinterId();
        if (printerId == null) {
            Crashlytics.log("Tried to cancel a job, but the printer ID is null");
            return;
        }

        String url = printerId.getLocalId();
        String clientUrl = url.substring(0, url.substring(0, url.lastIndexOf('/')).lastIndexOf('/'));
        final PrintJobId id = printJob.getId();
        if (id == null) {
            Crashlytics.log("Tried to cancel a job, but the print job ID is null");
            return;
        }
        final int jobId = mJobs.get(id);

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
            Crashlytics.log("Couldn't cancel job: " + jobId);
            Crashlytics.logException(e);
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
            Crashlytics.log("Couldn't cancel job: " + jobId);
            Crashlytics.logException(e);
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
        final PrinterId printerId = jobInfo.getPrinterId();
        if (printerId == null) {
            Crashlytics.log("Tried to queue a job, but the printer ID is null");
            return;
        }

        String url = printerId.getLocalId();
        String clientUrl = url.substring(0, url.substring(0, url.lastIndexOf('/')).lastIndexOf('/'));

        try {
            // Prepare job
            final URL printerURL = new URL(url);
            final URL clientURL = new URL(clientUrl);
            final ParcelFileDescriptor data = printJob.getDocument().getData();
            if (data == null) {
                Crashlytics.log("Tried to queue a job, but the document data (file descriptor) is null");
                Toast.makeText(this, R.string.err_document_fd_null, Toast.LENGTH_LONG).show();
                return;
            }
            final FileDescriptor fd = data.getFileDescriptor();
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
            Crashlytics.log("Couldn't queue job: " + printJob);
            Crashlytics.logException(e);
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
            Crashlytics.log("Tried to request a job status, but the job couldn't be found in the jobs list");
            return false;
        }

        final PrinterId printerId = printJob.getInfo().getPrinterId();
        if (printerId == null) {
            Crashlytics.log("Tried to request a job status, but the printer ID is null");
            return false;
        }
        String url = printerId.getLocalId();
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
            Crashlytics.log("Couldn't get job: " + printJob + " state");
            Crashlytics.logException(e);
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
            Crashlytics.log("Couldn't get job: " + jobId + " state");
            Crashlytics.logException(e);
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
            Crashlytics.log("Couldn't send filed descriptor " + fd + " to printer");
            Crashlytics.logException(e);
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

