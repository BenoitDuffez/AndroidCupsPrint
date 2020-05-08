package io.github.benoitduffez.cupsprint.printservice

import android.os.Build
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintJobId
import android.print.PrintJobInfo
import android.printservice.PrintJob
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession
import android.widget.Toast
import io.github.benoitduffez.cupsprint.AppExecutors
import io.github.benoitduffez.cupsprint.R
import org.cups4j.CupsClient
import org.cups4j.CupsPrinter
import org.cups4j.JobStateEnum
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.net.*
import java.util.*
import javax.net.ssl.SSLException

/**
 * When a print job is active, the app will poll the printer to retrieve the job status. This is the polling interval.
 */
private const val JOB_CHECK_POLLING_INTERVAL = 5000

/**
 * CUPS print service
 */
class CupsService : PrintService() {
    private val executors: AppExecutors by inject()
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
            executors.networkIO.execute {
                cancelPrintJob(clientURL, jobId)
                executors.mainThread.execute { onPrintJobCancelled(printJob) }
            }
        } catch (e: MalformedURLException) {
            Timber.e(e, "Couldn't cancel print job: $printJob, jobId: $jobId")
        } catch (e: URISyntaxException) {
            Timber.e(e, "Couldn't parse URI: $url")
        }
    }

    /**
     * Called from a background thread, ask the printer to cancel a job by its printer job ID
     *
     * @param clientURL The printer client URL
     * @param jobId     The printer job ID
     */
    private fun cancelPrintJob(clientURL: URL, jobId: Int) {
        try {
            val client = CupsClient(this, clientURL)
            client.cancelJob(jobId)
        } catch (e: Exception) {
            Timber.e(e, "Couldn't cancel job: $jobId")
        }
    }

    /**
     * Called on the main thread, when the print job was cancelled
     *
     * @param printJob The print job
     */
    private fun onPrintJobCancelled(printJob: PrintJob) {
        jobs.remove(printJob.id)
        printJob.cancel()
    }

    override fun onPrintJobQueued(printJob: PrintJob) {
        printJob.start()
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
            val info = printJob.info
            val jobId = printJob.id

            // Send print job
            executors.networkIO.execute {
                try {
                    printDocument(jobId, clientURL, printerURL, data, info)
                    executors.mainThread.execute { onPrintJobSent(printJob) }
                } catch (e: Exception) {
                    executors.mainThread.execute { handleJobException(printJob, e) }
                } finally {
                    // Close the file descriptor, after printing
                    try {
                        data.close()
                    } catch (e: IOException) {
                        Timber.e("Job document data (file descriptor) couldn't close.")
                    }
                }
            }
        } catch (e: MalformedURLException) {
            printJob.fail(getString(R.string.print_job_queue_fail_malformed_url, printJob))
            Timber.e("Couldn't queue print job: $printJob")
        } catch (e: URISyntaxException) {
            printJob.fail(getString(R.string.print_job_queue_fail_uri_syntax, url))
            Timber.e("Couldn't parse URI: $url")
        }
    }

    /**
     * Called from the UI thread.
     * Handle the exception (e.g. log or send it to crashlytics?), and inform the user of what happened
     *
     * @param printJob The print job
     * @param e     The exception that occurred
     */
    private fun handleJobException(printJob: PrintJob, e: Exception) {
        when (e) {
            is SocketTimeoutException -> {
                printJob.fail(getString(R.string.err_job_socket_timeout))
                Toast.makeText(this, R.string.err_job_socket_timeout, Toast.LENGTH_LONG).show()
            }
            is NullPrinterException -> {
                printJob.fail(getString(R.string.err_printer_null_when_printing))
                Toast.makeText(this, R.string.err_printer_null_when_printing, Toast.LENGTH_LONG).show()
            }
            else -> {
                val jobId = printJob.id
                val errorMsg = getString(R.string.err_job_exception, jobId.toString(), e.localizedMessage)
                printJob.fail(errorMsg)
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                if (e is SSLException && e.message?.contains("I/O error during system call, Broken pipe") == true) {
                    // Don't send this crash report: https://github.com/BenoitDuffez/AndroidCupsPrint/issues/70
                    Timber.e("Couldn't query job $jobId")
                } else {
                    Timber.e(e, "Couldn't query job $jobId")
                }
            }
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
            Timber.e(e, "Couldn't get job: $printJob state")
            return false
        } catch (e: URISyntaxException) {
            Timber.e(e, "Couldn't parse URI: $url")
            return false
        }

        // Send print job
        executors.networkIO.execute {
            try {
                val jobState = getJobState(jobId, clientURL)
                executors.mainThread.execute { onJobStateUpdate(printJob, jobState) }
            } catch (e: Exception) {
                executors.mainThread.execute {
                    jobs.remove(printJob.id)
                    Timber.e("Couldn't get job: $jobId state because: $e")

                    when {
                        (e is SocketException || e is SocketTimeoutException)
                                && e.message?.contains("ECONNRESET") == true -> {
                            printJob.fail(getString(R.string.err_job_econnreset, jobId))
                            Toast.makeText(this@CupsService, getString(R.string.err_job_econnreset, jobId), Toast.LENGTH_LONG).show()
                        }
                        e is FileNotFoundException -> {
                            printJob.fail(getString(R.string.err_job_not_found, jobId))
                            Toast.makeText(this@CupsService, getString(R.string.err_job_not_found, jobId), Toast.LENGTH_LONG).show()
                        }
                        e is NullPointerException -> printJob.complete()
                        else -> {
                            printJob.fail(e.localizedMessage)
                            Timber.e(e)
                        }
                    }
                }
            }
        }

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
    private fun getJobState(jobId: Int, clientURL: URL): JobStateEnum {
        val client = CupsClient(this, clientURL)
        val attr = client.getJobAttributes(jobId)
        return attr.jobState!!
    }

    /**
     * Called on the main thread, when a job status has been checked
     *
     * @param printJob The print job
     * @param state    Print job state
     */
    private fun onJobStateUpdate(printJob: PrintJob, state: JobStateEnum?) {
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
     * @param fd         The document to print, as a [ParcelFileDescriptor]
     * @param info       The print job's associated info
     */
    @Throws(Exception::class)
    internal fun printDocument(jobId: PrintJobId, clientURL: URL, printerURL: URL, fd: ParcelFileDescriptor, info: PrintJobInfo) {
        val client = CupsClient(this, clientURL)
        val printer = client.getPrinter(printerURL)?.let { printer ->
            val cupsPrinter = CupsPrinter(printerURL, printer.name, true)
            cupsPrinter.location = printer.location
            cupsPrinter
        }

        val doc = ParcelFileDescriptor.AutoCloseInputStream(fd)
        val attributes = info.attributes


        val builder = org.cups4j.PrintJob.Builder(doc)
        builder.copies(info.copies)

        val pages = info.pages
        if (pages != null && pages[0] != PageRange.ALL_PAGES) {
            val ranges = ArrayList<String>()
            var start:Int
            var end:Int
            for (range in pages) {
                start = range.start
                end = range.end
                ranges.add(StringBuilder("$start-$end").toString())
            }
            builder.pageRanges(ranges.joinToString(","))
        }

        if (Build.VERSION.SDK_INT >= 23) {
            builder.duplex(when (attributes.duplexMode) {
                PrintAttributes.DUPLEX_MODE_LONG_EDGE -> org.cups4j.PrintJob.DUPLEX_LONG_EDGE
                PrintAttributes.DUPLEX_MODE_SHORT_EDGE -> org.cups4j.PrintJob.DUPLEX_SHORT_EDGE
                else -> org.cups4j.PrintJob.DUPLEX_NONE
            })
        }

        val job = builder.build()
        val result = printer?.print(job, this) ?: throw NullPrinterException()
        jobs[jobId] = result.jobId
    }

    /**
     * Called on the main thread, when the job was sent to the printer
     *
     * @param printJob The print job
     */
    private fun onPrintJobSent(printJob: PrintJob) {
        startPolling(printJob)
    }

    private class NullPrinterException internal constructor() : Exception("Printer is null when trying to print: printer no longer available?")
}
