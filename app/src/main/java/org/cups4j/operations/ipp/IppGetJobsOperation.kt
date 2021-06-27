package org.cups4j.operations.ipp

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
 *
 *
 * Notice this file has been modified. It is not the original.
 * Job Creation Time added Jon Freeman 2013
 */

/**
 * Notice this file has been modified. It is not the original.
 * Job Creation Time added Jon Freeman 2013
 */

import android.content.Context
import ch.ethz.vppserver.ippclient.IppTag
import org.cups4j.*
import org.cups4j.operations.IppOperation
import java.io.UnsupportedEncodingException
import java.net.URL
import java.nio.ByteBuffer
import java.util.*

class IppGetJobsOperation(context: Context) : IppOperation(context) {
    init {
        operationID = 0x000a
        bufferSize = 8192
    }

    @Throws(UnsupportedEncodingException::class)
    override fun getIppHeader(url: URL, map: Map<String, String>?): ByteBuffer {
        var ippBuf = ByteBuffer.allocateDirect(bufferSize.toInt())

        //not sure why next line is here, it overwrites job attributes in map parameter - JF
        //map.put("requested-attributes", "job-name job-id job-state job-originating-user-name job-printer-uri copies");

        ippBuf = IppTag.getOperation(ippBuf, operationID)
        ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(url))

        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map!!["requesting-user-name"])

        map["limit"]?.let {
            ippBuf = IppTag.getInteger(ippBuf, "limit", it.toInt())
        }

        map["requested-attributes"]?.let { requestedAttributes ->
            val sta = requestedAttributes.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            ippBuf = IppTag.getKeyword(ippBuf, "requested-attributes", sta[0])
            val l = sta.size
            for (i in 1 until l) {
                ippBuf = IppTag.getKeyword(ippBuf, null, sta[i])
            }
        }

        if (map["which-jobs"] != null) {
            ippBuf = IppTag.getKeyword(ippBuf, "which-jobs", map["which-jobs"])
        }

        if (map["my-jobs"] != null) {
            var value = false
            if (map["my-jobs"] == "true") {
                value = true
            }
            ippBuf = IppTag.getBoolean(ippBuf, "my-jobs", value)
        }

        ippBuf = IppTag.getEnd(ippBuf)
        ippBuf.flip()
        return ippBuf
    }

    @Throws(Exception::class)
    fun getPrintJobs(printer: CupsPrinter, whichJobs: WhichJobsEnum, userName: String?,
                     myJobs: Boolean): List<PrintJobAttributes> {
        val jobs = ArrayList<PrintJobAttributes>()
        var jobAttributes: PrintJobAttributes
        val map = HashMap<String, String>()

        map["requesting-user-name"] = userName ?: CupsClient.DEFAULT_USER
        map["which-jobs"] = whichJobs.value
        if (myJobs) {
            map["my-jobs"] = "true"
        }

        //time-at-creation added JF
        map["requested-attributes"] = "page-ranges print-quality sides time-at-creation job-uri job-id job-state job-printer-uri job-name job-originating-user-name"

        val result = request(printer.printerURL, map)
        val protocol = printer.printerURL.protocol + "://"

        for (group in result!!.attributeGroupList!!) {
            if ("job-attributes-tag" == group.tagName) {
                jobAttributes = PrintJobAttributes()
                for (attr in group.attribute) {
                    if (!attr.attributeValue.isEmpty()) {
                        val attValue = getAttributeValue(attr)

                        when (attr.name) {
                            "job-uri" -> jobAttributes.jobURL = URL(attValue.replace("ipp://", protocol))
                            "job-id" -> jobAttributes.jobID = Integer.parseInt(attValue)
                            "job-state" -> jobAttributes.jobState = JobStateEnum.fromString(attValue)
                            "job-printer-uri" -> jobAttributes.printerURL = URL(attValue.replace("ipp://", protocol))
                            "job-name" -> jobAttributes.jobName = attValue
                            "job-originating-user-name" -> jobAttributes.userName = attValue
                            "time-at-creation" -> {
                                val unixTime = java.lang.Long.parseLong(attValue)
                                val dt = Date(unixTime * 1000)
                                jobAttributes.jobCreateTime = dt
                            }
                        }
                    }
                }
                jobs.add(jobAttributes)
            }
        }

        return jobs
    }
}
