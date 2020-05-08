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
 */

/*Notice
 * This file has been modified. It is not the original.
 * Jon Freeman - 2013
 */

import android.content.Context
import ch.ethz.vppserver.ippclient.IppTag
import org.cups4j.JobStateEnum
import org.cups4j.PrintJobAttributes
import org.cups4j.operations.IppOperation
import java.io.UnsupportedEncodingException
import java.net.URL
import java.nio.ByteBuffer
import java.util.*

class IppGetJobAttributesOperation(context: Context) : IppOperation(context) {
    init {
        operationID = 0x0009
        bufferSize = 8192
    }

    @Throws(UnsupportedEncodingException::class)
    override fun getIppHeader(url: URL, map: Map<String, String>?): ByteBuffer {
        var ippBuf = ByteBuffer.allocateDirect(bufferSize.toInt())
        ippBuf = IppTag.getOperation(ippBuf, operationID)

        if (map == null) {
            ippBuf = IppTag.getUri(ippBuf, "job-uri", stripPortNumber(url))
            ippBuf = IppTag.getEnd(ippBuf)
            ippBuf.flip()
            return ippBuf
        }

        map["job-id"]?.let {
            ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(url))
            ippBuf = IppTag.getInteger(ippBuf, "job-id", it.toInt())
        } ?: run {
            ippBuf = IppTag.getUri(ippBuf, "job-uri", stripPortNumber(url))
        }

        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map["requesting-user-name"])

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
    fun getPrintJobAttributes(url: URL, userName: String, jobID: Int): PrintJobAttributes {
        val job = PrintJobAttributes()

        val map = HashMap<String, String>()
        //    map.put("requested-attributes",
        //        "page-ranges print-quality sides job-uri job-id job-state job-printer-uri job-name job-originating-user-name job-k-octets time-at-creation time-at-processing time-at-completed job-media-sheets-completed");

        map["requested-attributes"] = "all"
        map["requesting-user-name"] = userName
        val result = request(URL(url.toString() + "/jobs/" + jobID), map)

        // IppResultPrinter.print(result);
        for (group in result!!.attributeGroupList!!) {
            if ("job-attributes-tag" == group.tagName || "unassigned" == group.tagName) {
                for (attr in group.attribute) {
                    if (!attr.attributeValue.isEmpty()) {
                        val attValue = getAttributeValue(attr)

                        when {
                            "job-uri" == attr.name -> job.jobURL = URL(attValue.replace("ipp://", "http://"))
                            "job-id" == attr.name -> job.jobID = Integer.parseInt(attValue)
                            "job-state" == attr.name -> {
                                println("job-state $attValue")
                                job.jobState = JobStateEnum.fromString(attValue)
                            }
                            "job-printer-uri" == attr.name -> job.printerURL = URL(attValue.replace("ipp://", "http://"))
                            "job-name" == attr.name -> job.jobName = attValue
                            "job-originating-user-name" == attr.name -> job.userName = attValue
                            "job-k-octets" == attr.name -> job.size = Integer.parseInt(attValue)
                            "time-at-creation" == attr.name -> job.jobCreateTime = Date(1000 * java.lang.Long.parseLong(attValue))
                            "time-at-completed" == attr.name -> job.jobCompleteTime = Date(1000 * java.lang.Long.parseLong(attValue))
                            "job-media-sheets-completed" == attr.name -> job.pagesPrinted = Integer.parseInt(attValue)
                        }
                    }
                }
            }
        }

        //    IppResultPrinter.print(result);
        return job
    }
}
