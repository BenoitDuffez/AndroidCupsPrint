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

import android.content.Context
import org.cups4j.operations.ipp.IppGetJobAttributesOperation
import org.cups4j.operations.ipp.IppGetJobsOperation
import org.cups4j.operations.ipp.IppPrintJobOperation
import java.net.URL
import java.util.*

/**
 * Represents a printer on your IPP server
 */

/**
 * Constructor
 *
 * @param printerURL  Printer URL
 * @param name Printer name
 * @param isDefault   true if this is the default printer on this IPP server
 */
class CupsPrinter(
        /**
         * The URL for this printer
         */
        val printerURL: URL,

        /**
         * Name of this printer.
         * For a printer http://localhost:631/printers/printername 'printername' will
         * be returned.
         */
        val name: String,

        /**
         * Is this the default printer
         */
        var isDefault: Boolean,

        /**
         * Trays available in this printer
         */
        val trays: ArrayList<String> = ArrayList<String>()) {
    /**
     * Description attribute for this printer
     */
    var description: String? = null

    /**
     * Location attribute for this printer
     */
    var location: String? = null

    /**
     * Print method
     *
     * @param printJob Print job
     * @return PrintRequestResult
     * @throws Exception
     */
    @Throws(Exception::class)
    fun print(printJob: PrintJob, context: Context): PrintRequestResult {
        var ippJobID = -1
        val document = printJob.document
        var userName = printJob.userName
        val jobName = printJob.jobName ?: "Unknown"
        val copies = printJob.copies
        val pageRanges = printJob.pageRanges
        val duplex = printJob.duplex

        var attributes: MutableMap<String, String>? = printJob.attributes

        if (userName == null) {
            userName = CupsClient.DEFAULT_USER
        }
        if (attributes == null) {
            attributes = HashMap()
        }

        attributes["requesting-user-name"] = userName
        attributes["job-name"] = jobName

        val copiesString: String
        val rangesString = StringBuilder()
        if (copies > 0) {// other values are considered bad value by CUPS
            copiesString = "copies:integer:$copies"
            addAttribute(attributes, "job-attributes", copiesString)
        }
        if (pageRanges != null && "" != pageRanges) {
            val ranges = pageRanges.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            var delimiter = ""

            rangesString.append("page-ranges:setOfRangeOfInteger:")
            for (range in ranges) {
                var actualRange = range.trim { it <= ' ' }
                val values = range.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (values.size == 1) {
                    actualRange = "$range-$range"
                }

                rangesString.append(delimiter).append(actualRange)
                // following ranges need to be separated with ","
                delimiter = ","
            }
            addAttribute(attributes, "job-attributes", rangesString.toString())
        }

        addAttribute(attributes, "job-attributes", when (duplex) {
            PrintJob.DUPLEX_LONG_EDGE -> "sides:keyword:two-sided-long-edge"
            PrintJob.DUPLEX_SHORT_EDGE -> "sides:keyword:two-sided-short-edge"
            else -> "sides:keyword:one-sided"
        })

        if (printJob.tray != null)
            addAttribute(attributes, "media-source", "media-source:keyword:" + printJob.tray)

        val command = IppPrintJobOperation(context)
        val ippResult = command.request(printerURL, attributes, document)
        //    IppResultPrinter.print(ippResult);

        val result = PrintRequestResult(ippResult)


        for (group in ippResult!!.attributeGroupList!!) {
            if (group.tagName == "job-attributes-tag") {
                for (attr in group.attribute) {
                    if (attr.name == "job-id") {
                        ippJobID = attr.attributeValue[0].value?.toInt()!!
                    }
                }
            }
        }
        result.jobId = ippJobID
        return result
    }

    /**
     * @param map   Attributes map
     * @param name  Attribute key
     * @param value Attribute value
     */
    private fun addAttribute(map: MutableMap<String, String>, name: String?, value: String?) {
        if (value != null && name != null) {
            var attribute: String? = map[name]
            if (attribute == null) {
                attribute = value
            } else {
                attribute += "#$value"
            }
            map[name] = attribute
        }
    }

    /**
     * Get a list of jobs
     *
     * @param whichJobs completed, not completed or all
     * @param user      requesting user (null will be translated to anonymous)
     * @param myJobs    boolean only jobs for requesting user or all jobs for this printer?
     * @return job list
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getJobs(whichJobs: WhichJobsEnum, user: String, myJobs: Boolean, context: Context): List<PrintJobAttributes> =
            IppGetJobsOperation(context).getPrintJobs(this, whichJobs, user, myJobs)

    /**
     * Get current status for the print job with the given ID.
     *
     * @param jobID Job ID
     * @return job status
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getJobStatus(jobID: Int, context: Context): JobStateEnum? = getJobStatus(CupsClient.DEFAULT_USER, jobID, context)

    /**
     * Get current status for the print job with the given ID
     *
     * @param userName Requesting user name
     * @param jobID    Job ID
     * @return job status
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getJobStatus(userName: String, jobID: Int, context: Context): JobStateEnum? {
        val command = IppGetJobAttributesOperation(context)
        val job = command.getPrintJobAttributes(printerURL, userName, jobID)

        return job.jobState
    }

    /**
     * Get a String representation of this printer consisting of the printer URL
     * and the name
     *
     * @return String
     */
    override fun toString(): String =
            "printer uri=$printerURL default=$isDefault name=$name"
}
