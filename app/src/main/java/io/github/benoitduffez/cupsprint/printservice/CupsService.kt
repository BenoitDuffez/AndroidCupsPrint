package io.github.benoitduffez.cupsprint.printservice

import android.os.AsyncTask
import android.os.Handler
import android.print.PrintJobId
import android.printservice.PrintJob
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession
import android.widget.Toast

import com.crashlytics.android.Crashlytics

import org.cups4j.CupsClient
import org.cups4j.JobStateEnum

import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.HashMap

import io.github.benoitduffez.cupsprint.R
import timber.log.Timber

/**
 * CUPS print service
 */
class CupsService : PrintService() {
    private val jobs = HashMap<PrintJobId, Int>()

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession? =
            CupsPrinterDiscoverySession(this)

    override fun onRequestCancelPrintJob(printJob: PrintJob) {
        val jobInfo = printJob.info
        val printerId = jobInfo.printerId
        if (printerId == null) {
            Timber.d("Tried to cancel a job, but the printer ID is null")
            return
        }

        val url = printerId.localId

        val id = printJob.id
        if (id == null) {
            Timber.d("Tried to cancel a job, but the print job ID is null")
            return
        }
        val jobId = jobs[id]
        if (jobId == null) {
            Timber.d("Tried to cancel a job, but the print job ID is null")
            return
        }

        try {
            val tmpUri = URI(url)
            val schemeHostPort = tmpUri.scheme + "://" + tmpUri.host + ":" + tmpUri.port

            val clientURL = URL(schemeHostPort)
            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg params: Void): Void? {
                    cancelPrintJob(clientURL, jobId)
                    return null
                }

                override fun onPostExecute(v: Void) {
                    onPrintJobCancelled(printJob)
                }
            }.execute()
        } catch (e: MalformedURLException) {
            Timber.e(e,"Couldn't cancel print job: $printJob, jobId: $jobId")
        } catch (e: URISyntaxException) {
            Timber.e(e,"Couldn't parse URI: $url")
        }
    }

    /**
     * Called from a background thread, ask the printer to cancel a job by its printer job ID
     *
     * @param clientURL The printer client URL
     * @param jobId     The printer job ID
     */
    internal fun cancelPrintJob(clientURL: URL, jobId: Int) {
        try {
            val client = CupsClient(clientURL)
            client.cancelJob(jobId)
        } catch (e: Exception) {
            Timber.e(e,"Couldn't cancel job: $jobId")
        }
    }

    /**
     * Called on the main thread, when the print job was cancelled
     *
     * @param printJob The print job
     */
    internal fun onPrintJobCancelled(printJob: PrintJob) {
        jobs.remove(printJob.id)
        printJob.cancel()
    }

    override fun onPrintJobQueued(printJob: PrintJob) {
        startPolling(printJob)
        val jobInfo = printJob.info
        val printerId = jobInfo.printerId
        if (printerId == null) {
            Timber.d("Tried to queue a job, but the printer ID is null")
            return
        }

        val url = printerId.localId
        try {
            val tmpUri = URI(url)
            val schemeHostPort = tmpUri.scheme + "://" + tmpUri.host + ":" + tmpUri.port

            // Prepare job
            val printerURL = URL(url)
            val clientURL = URL(schemeHostPort)
            val data = printJob.document.data
            if (data == null) {
                Timber.d("Tried to queue a job, but the document data (file descriptor) is null")
                Toast.makeText(this, R.string.err_document_fd_null, Toast.LENGTH_LONG).show()
                return
            }
            val fd = data.fileDescriptor
            val jobId = printJob.id

            // Send print job
            object : AsyncTask<Void, Void, Void>() {
                var exception: Exception? = null

                override fun doInBackground(vararg params: Void): Void? {
                    try {
                        printDocument(jobId, clientURL, printerURL, fd)
                    } catch (e: Exception) {
                        exception = e
                    }

                    return null
                }

                override fun onPostExecute(result: Void) {
                    if (exception != null) {
                        handleJobException(jobId, exception!!)
                    } else {
                        onPrintJobSent(printJob)
                    }
                }
            }.execute()
        } catch (e: MalformedURLException) {
            Timber.e(e,"Couldn't queue print job: $printJob")
        } catch (e: URISyntaxException) {
            Timber.e(e,"Couldn't parse URI: $url")
        }
    }

    /**
     * Called from the UI thread.
     * Handle the exception (e.g. log or send it to crashlytics?), and inform the user of what happened
     *
     * @param jobId The print job
     * @param e     The exception that occurred
     */
    internal fun handleJobException(jobId: PrintJobId, e: Exception) {
        if (e is SocketTimeoutException) {
            Toast.makeText(this, R.string.err_job_socket_timeout, Toast.LENGTH_LONG).show()
        } else if (e is NullPrinterException) {
            Toast.makeText(this, R.string.err_printer_null_when_printing, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, getString(R.string.err_job_exception, jobId.toString(), e.localizedMessage), Toast.LENGTH_LONG).show()
            Timber.e(e,"Couldn't query job $jobId")
        }
    }

    private fun startPolling(printJob: PrintJob) {
        Handler().postDelayed(object : Runnable {
            override fun run() {
                if (updateJobStatus(printJob)) {
                    Handler().postDelayed(this, JOB_CHECK_POLLING_INTERVAL.toLong())
                }
            }
        }, JOB_CHECK_POLLING_INTERVAL.toLong())
    }

    /**
     * Called in the main thread, will ask the job status and update it in the Android framework
     *
     * @param printJob The print job
     * @return true if this method should be called again, false otherwise (in case the job is still pending or it is complete)
     */
    internal fun updateJobStatus(printJob: PrintJob): Boolean {
        // Check if the job is already gone
        if (!jobs.containsKey(printJob.id)) {
            Timber.d("Tried to request a job status, but the job couldn't be found in the jobs list")
            return false
        }

        val printerId = printJob.info.printerId
        if (printerId == null) {
            Timber.d("Tried to request a job status, but the printer ID is null")
            return false
        }
        val url = printerId.localId

        // Prepare job
        val clientURL: URL
        val jobId: Int
        try {
            val tmpUri = URI(url)
            val schemeHostPort = tmpUri.scheme + "://" + tmpUri.host + ":" + tmpUri.port

            clientURL = URL(schemeHostPort)
            jobId = jobs[printJob.id]!!
        } catch (e: MalformedURLException) {
            Timber.e(e,"Couldn't get job: $printJob state")
            return false
        } catch (e: URISyntaxException) {
            Timber.e(e,"Couldn't parse URI: $url")
            return false
        }

        // Send print job
        object : AsyncTask<Void, Void, JobStateEnum>() {
            var exception: Exception? = null

            override fun doInBackground(vararg params: Void): JobStateEnum? {
                try {
                    return getJobState(jobId, clientURL)
                } catch (e: Exception) {
                    exception = e
                }

                return null
            }

            override fun onPostExecute(state: JobStateEnum?) {
                if (exception != null) {
                    Timber.e("Couldn't get job: $jobId state because: $exception")

                    if (exception is SocketException && exception?.message?.contains("ECONNRESET")==true) {
                        Toast.makeText(this@CupsService, getString(R.string.err_job_econnreset, jobId), Toast.LENGTH_LONG).show()
                    } else if (exception is FileNotFoundException) {
                        Toast.makeText(this@CupsService, getString(R.string.err_job_not_found, jobId), Toast.LENGTH_LONG).show()
                    } else {
                        Timber.e(exception)
                    }
                } else if (state != null) {
                    onJobStateUpdate(printJob, state)
                }
            }
        }.execute()

        // We want to be called again if the job is still in this map
        // Indeed, when the job is complete, the job is removed from this map.
        return jobs.containsKey(printJob.id)
    }

    /**
     * Called in a background thread, in order to check the job status
     *
     * @param jobId     The printer job ID
     * @param clientURL The printer client URL
     * @return true if the job is complete/aborted/cancelled, false if it's still processing (printing, paused, etc)
     */
    @Throws(Exception::class)
    internal fun getJobState(jobId: Int, clientURL: URL): JobStateEnum {
        val client = CupsClient(clientURL)
        val attr = client.getJobAttributes(jobId)
        return attr.jobState!!
    }

    /**
     * Called on the main thread, when a job status has been checked
     *
     * @param printJob The print job
     * @param state    Print job state
     */
    internal fun onJobStateUpdate(printJob: PrintJob, state: JobStateEnum?) {
        // Couldn't check state -- don't do anything
        if (state == null) {
            jobs.remove(printJob.id)
            printJob.cancel()
        } else {
            if (state == JobStateEnum.CANCELED) {
                jobs.remove(printJob.id)
                printJob.cancel()
            } else if (state == JobStateEnum.COMPLETED || state == JobStateEnum.ABORTED) {
                jobs.remove(printJob.id)
                printJob.complete()
            }
        }
    }

    /**
     * Called from a background thread, when the print job has to be sent to the printer.
     *
     * @param clientURL  The client URL
     * @param printerURL The printer URL
     * @param fd         The document to print, as a [FileDescriptor]
     */
    @Throws(Exception::class)
    internal fun printDocument(jobId: PrintJobId, clientURL: URL, printerURL: URL, fd: FileDescriptor) {
        val client = CupsClient(clientURL)
        val printer = client.getPrinter(printerURL) ?: throw NullPrinterException()

        val `is` = FileInputStream(fd)
        val job = org.cups4j.PrintJob.Builder(`is`).build()
        val result = printer.print(job)
        jobs[jobId] = result.jobId
    }

    /**
     * Called on the main thread, when the job was sent to the printer
     *
     * @param printJob The print job
     */
    internal fun onPrintJobSent(printJob: PrintJob) {
        printJob.start()
    }

    private class NullPrinterException internal constructor() : Exception("Printer is null when trying to print: printer no longer available?")

    companion object {
        /**
         * When a print job is active, the app will poll the printer to retrieve the job status. This is the polling interval.
         */
        private val JOB_CHECK_POLLING_INTERVAL = 5000
    }
}
