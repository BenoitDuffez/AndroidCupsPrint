package org.cups4j.operations.ipp

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

class IppCancelJobOperation(context: Context) : IppOperation(context) {
    init {
        operationID = 0x0008
        bufferSize = 8192
    }

    @Throws(UnsupportedEncodingException::class)
    override fun getIppHeader(url: URL, map: Map<String, String>?): ByteBuffer {
        var ippBuf = ByteBuffer.allocateDirect(bufferSize.toInt())
        ippBuf = IppTag.getOperation(ippBuf, operationID)

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

        if (map["message"] != null) {
            ippBuf = IppTag.getTextWithoutLanguage(ippBuf, "message", map["message"])
        }

        ippBuf = IppTag.getEnd(ippBuf)
        ippBuf.flip()
        return ippBuf
    }

    @Throws(Exception::class)
    fun cancelJob(url: URL, userName: String?, jobID: Int): Boolean {
        val requestUrl = URL(url.toString() + "/jobs/" + Integer.toString(jobID))

        val map = HashMap<String, String>()
        map["requesting-user-name"] = userName?:CupsClient.DEFAULT_USER
        map["job-uri"] = requestUrl.toString()

        return PrintRequestResult(request(requestUrl, map)).isSuccessfulResult
    }
}
