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
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.JobStateEnum;
import org.cups4j.PrintJobAttributes;
import org.cups4j.PrintRequestResult;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.github.benoitduffez.cupsprint.L;
import io.github.benoitduffez.cupsprint.R;

/**
 * CUPS print service
 */
public class CupsService extends PrintService {
    /**
     * When a print job is active, the app will poll the printer to retrieve the job status. This is the polling interval.
     */
    private static final int JOB_CHECK_POLLING_INTERVAL = 5000;

    @NonNull
    private final Map<PrintJobId, Integer> mJobs = new HashMap<>();

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

        final PrintJobId id = printJob.getId();
        if (id == null) {
            Crashlytics.log("Tried to cancel a job, but the print job ID is null");
            return;
        }
        final Integer jobId = mJobs.get(id);
        if (jobId == null) {
            Crashlytics.log("Tried to cancel a job, but the print job ID is null");
            return;
        }

        try {
            URI tmpUri = new URI(url);
            String schemeHostPort = tmpUri.getScheme() + "://" + tmpUri.getHost() + ":" + tmpUri.getPort();

            final URL clientURL = new URL(schemeHostPort);
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
            L.e("Couldn't cancel print job: " + printJob + ", jobId: " + jobId, e);
        } catch (URISyntaxException e) {
            L.e("Couldn't parse URI: " + url, e);
        }
    }

    /**
     * Called from a background thread, ask the printer to cancel a job by its printer job ID
     *
     * @param clientURL The printer client URL
     * @param jobId     The printer job ID
     */
    void cancelPrintJob(URL clientURL, int jobId) {
        try {
            CupsClient client = new CupsClient(clientURL);
            client.cancelJob(jobId);
        } catch (Exception e) {
            L.e("Couldn't cancel job: " + jobId, e);
        }
    }

    /**
     * Called on the main thread, when the print job was cancelled
     *
     * @param printJob The print job
     */
    void onPrintJobCancelled(PrintJob printJob) {
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
        try {
            URI tmpUri = new URI(url);
            String schemeHostPort = tmpUri.getScheme() + "://" + tmpUri.getHost() + ":" + tmpUri.getPort();

            // Prepare job
            final URL printerURL = new URL(url);
            final URL clientURL = new URL(schemeHostPort);
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
                Exception mException;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        printDocument(jobId, clientURL, printerURL, fd);
                    } catch (Exception e) {
                        mException = e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (mException != null) {
                        handleJobException(jobId, mException);
                    } else {
                        onPrintJobSent(printJob);
                    }
                }
            }.execute();
        } catch (MalformedURLException e) {
            L.e("Couldn't queue print job: " + printJob, e);
        } catch (URISyntaxException e) {
            L.e("Couldn't parse URI: " + url, e);
        }
    }

    /**
     * Called from the UI thread.
     * Handle the exception (e.g. log or send it to crashlytics?), and inform the user of what happened
     *
     * @param jobId The print job
     * @param e     The exception that occurred
     */
    void handleJobException(PrintJobId jobId, @NonNull Exception e) {
        if (e instanceof SocketTimeoutException) {
            Toast.makeText(this, R.string.err_job_socket_timeout, Toast.LENGTH_LONG).show();
        } else if (e instanceof NullPrinterException) {
            Toast.makeText(this, R.string.err_printer_null_when_printing, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.err_job_exception, jobId.toString(), e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
            L.e("Couldn't query job " + jobId, e);
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
    boolean updateJobStatus(final PrintJob printJob) {
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

        // Prepare job
        final URL clientURL;
        final int jobId;
        try {
            URI tmpUri = new URI(url);
            String schemeHostPort = tmpUri.getScheme() + "://" + tmpUri.getHost() + ":" + tmpUri.getPort();

            clientURL = new URL(schemeHostPort);
            jobId = mJobs.get(printJob.getId());
        } catch (MalformedURLException e) {
            L.e("Couldn't get job: " + printJob + " state", e);
            return false;
        } catch (URISyntaxException e) {
            L.e("Couldn't parse URI: " + url, e);
            return false;
        }

        // Send print job
        new AsyncTask<Void, Void, JobStateEnum>() {
            Exception mException;

            @Override
            protected JobStateEnum doInBackground(Void... params) {
                try {
                    return getJobState(jobId, clientURL);
                } catch (Exception e) {
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(JobStateEnum state) {
                if (mException != null) {
                    L.e("Couldn't get job: " + jobId + " state because: " + mException);

                    if (mException instanceof SocketException && mException.getMessage().contains("ECONNRESET")) {
                        Toast.makeText(CupsService.this, getString(R.string.err_job_econnreset, jobId), Toast.LENGTH_LONG).show();
                    } else if (mException instanceof FileNotFoundException) {
                        Toast.makeText(CupsService.this, getString(R.string.err_job_not_found, jobId), Toast.LENGTH_LONG).show();
                    } else {
                        Crashlytics.logException(mException);
                    }
                } else if (state != null) {
                    onJobStateUpdate(printJob, state);
                }
            }
        }.execute();

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
    JobStateEnum getJobState(int jobId, URL clientURL) throws Exception {
        CupsClient client = new CupsClient(clientURL);
        PrintJobAttributes attr = client.getJobAttributes(jobId);
        return attr.getJobState();
    }

    /**
     * Called on the main thread, when a job status has been checked
     *
     * @param printJob The print job
     * @param state    Print job state
     */
    void onJobStateUpdate(PrintJob printJob, JobStateEnum state) {
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
    void printDocument(PrintJobId jobId, URL clientURL, URL printerURL, FileDescriptor fd) throws Exception {
        CupsClient client = new CupsClient(clientURL);
        CupsPrinter printer = client.getPrinter(printerURL);
        if (printer == null) {
            throw new NullPrinterException();
        }

        InputStream is = new FileInputStream(fd);
        org.cups4j.PrintJob job = new org.cups4j.PrintJob.Builder(is).build();
        PrintRequestResult result = printer.print(job);
        mJobs.put(jobId, result.getJobId());
    }

    /**
     * Called on the main thread, when the job was sent to the printer
     *
     * @param printJob The print job
     */
    void onPrintJobSent(PrintJob printJob) {
        printJob.start();
    }

    private static class NullPrinterException extends Exception {
        NullPrinterException() {
            super("Printer is null when trying to print: printer no longer available?");
        }
    }
}

