package org.cups4j

/**
 * Copyright (C) 2009 Harald Weyhing
 *
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 * See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program; if not, see
 * <http:></http:>//www.gnu.org/licenses/>.
 */

/*Notice
 * This file has been modified. It is not the original.
 * Jon Freeman - 2013
 */


import android.content.Context
import org.cups4j.operations.cups.CupsGetDefaultOperation
import org.cups4j.operations.cups.CupsGetPrintersOperation
import org.cups4j.operations.cups.CupsMoveJobOperation
import org.cups4j.operations.ipp.*
import timber.log.Timber
import java.net.URL
import java.security.cert.X509Certificate

/**
 * Main Client for accessing CUPS features like
 * - get printers
 * - print documents
 * - get job attributes
 * - ...
 */
class CupsClient @JvmOverloads constructor(
        val context: Context,
        private val url: URL = URL(DEFAULT_URL),
        private val userName: String = DEFAULT_USER
) {
    var serverCerts: Array<X509Certificate>? = null
        private set // Storage for server certificates if they're not trusted

    var lastResponseCode: Int = 0
        private set

    /**
     * Path to list of printers, like xxx://ip:port/printers/printer_name => will contain '/printers/'
     * seen in issue: https://github.com/BenoitDuffez/AndroidCupsPrint/issues/40
     */
    private var path = "/printers/"

    // add default printer if available
    @Throws(Exception::class)
    fun getPrinters(firstName: String? = null, limit: Int? = null): List<CupsPrinter> {
        val cupsGetPrintersOperation = CupsGetPrintersOperation(context)
        val printers: List<CupsPrinter>
        try {
            printers = cupsGetPrintersOperation.getPrinters(url, path, firstName, limit)
        } finally {
            serverCerts = cupsGetPrintersOperation.serverCerts
            lastResponseCode = cupsGetPrintersOperation.lastResponseCode
        }
        val defaultPrinter = defaultPrinter

        for (p in printers) {
            if (defaultPrinter != null && p.printerURL.toString() == defaultPrinter.printerURL.toString()) {
                p.isDefault = true
            }
        }

        return printers
    }

    private val defaultPrinter: CupsPrinter?
        @Throws(Exception::class)
        get() = CupsGetDefaultOperation(context).getDefaultPrinter(url, path)

    val host: String
        get() = url.host

    @Throws(Exception::class)
    fun getPrinter(printerURL: URL): CupsPrinter? {
        // Extract the printer name
        var name = printerURL.path
        val pos = name.indexOf('/', 1)
        if (pos > 0) {
            name = name.substring(pos + 1)
        }

        val printers = getPrinters(name, 1)

        Timber.d("getPrinter: Found ${printers.size} possible CupsPrinters")
        for (p in printers) {
            if (p.printerURL.path == printerURL.path)
                return p
        }
        return null
    }

    @Throws(Exception::class)
    fun getJobAttributes(jobID: Int): PrintJobAttributes = getJobAttributes(url, userName, jobID)

    @Throws(Exception::class)
    fun getJobAttributes(userName: String, jobID: Int): PrintJobAttributes =
            getJobAttributes(url, userName, jobID)

    @Throws(Exception::class)
    private fun getJobAttributes(url: URL, userName: String, jobID: Int): PrintJobAttributes =
            IppGetJobAttributesOperation(context).getPrintJobAttributes(url, userName, jobID)

    @Throws(Exception::class)
    fun getJobs(printer: CupsPrinter, whichJobs: WhichJobsEnum, userName: String, myJobs: Boolean): List<PrintJobAttributes> =
            IppGetJobsOperation(context).getPrintJobs(printer, whichJobs, userName, myJobs)

    @Throws(Exception::class)
    fun cancelJob(jobID: Int): Boolean = IppCancelJobOperation(context).cancelJob(url, userName, jobID)

    @Throws(Exception::class)
    fun cancelJob(url: URL, userName: String, jobID: Int): Boolean =
            IppCancelJobOperation(context).cancelJob(url, userName, jobID)

    @Throws(Exception::class)
    fun holdJob(jobID: Int): Boolean = IppHoldJobOperation(context).holdJob(url, userName, jobID)

    @Throws(Exception::class)
    fun holdJob(url: URL, userName: String, jobID: Int): Boolean =
            IppHoldJobOperation(context).holdJob(url, userName, jobID)

    @Throws(Exception::class)
    fun releaseJob(jobID: Int): Boolean = IppReleaseJobOperation(context).releaseJob(url, userName, jobID)

    @Throws(Exception::class)
    fun releaseJob(url: URL, userName: String, jobID: Int): Boolean =
            IppReleaseJobOperation(context).releaseJob(url, userName, jobID)

    @Throws(Exception::class)
    fun moveJob(jobID: Int, userName: String, currentPrinter: CupsPrinter, targetPrinter: CupsPrinter): Boolean {
        val currentHost = currentPrinter.printerURL.host

        return CupsMoveJobOperation(context).moveJob(currentHost, userName, jobID, targetPrinter.printerURL)
    }

    /**
     * Ensure path starts and ends with a slash
     *
     * @param path Path to printers on server
     * @return Self for easy chained calls
     */
    fun setPath(path: String): CupsClient {
        this.path = path
        if (!path.startsWith("/")) {
            this.path = "/$path"
        }
        if (!path.endsWith("/")) {
            this.path += "/"
        }
        return this
    }

    companion object {
        const val DEFAULT_USER = "anonymous"
        private const val DEFAULT_URL = "http://localhost:631"
    }
}
