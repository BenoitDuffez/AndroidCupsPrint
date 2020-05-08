package org.cups4j.operations.cups

/**
 * Copyright (C) 2011 Harald Weyhing
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
import org.cups4j.CupsClient
import org.cups4j.PrintRequestResult
import org.cups4j.operations.IppOperation
import java.io.UnsupportedEncodingException
import java.net.URL
import java.nio.ByteBuffer
import java.util.*

class CupsMoveJobOperation(context: Context) : IppOperation(context) {
    init {
        operationID = 0x400D
        bufferSize = 8192
    }

    @Throws(UnsupportedEncodingException::class)
    override fun getIppHeader(url: URL, map: Map<String, String>?): ByteBuffer {
        var ippBuf = ByteBuffer.allocateDirect(bufferSize.toInt())
        ippBuf = IppTag.getOperation(ippBuf, operationID)
        // ippBuf = IppTag.getUri(ippBuf, "job-uri", stripPortNumber(url));

        if (map == null) {
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
        ippBuf = IppTag.getUri(ippBuf, "job-printer-uri", map["target-printer-uri"])
        ippBuf = IppTag.getEnd(ippBuf)
        ippBuf.flip()
        return ippBuf
    }

    @Throws(Exception::class)
    fun moveJob(hostname: String, userName: String?, jobID: Int, targetPrinterURL: URL): Boolean {
        val url = URL("http://" + hostname + "/jobs/" + Integer.toString(jobID))
        val map = HashMap<String, String>()
        map["requesting-user-name"] = userName ?: CupsClient.DEFAULT_USER
        map["job-uri"] = url.toString()
        map["target-printer-uri"] = stripPortNumber(targetPrinterURL)

        val result = request(url, map)
        //    IppResultPrinter.print(result);
        return PrintRequestResult(result).isSuccessfulResult
    }
}
