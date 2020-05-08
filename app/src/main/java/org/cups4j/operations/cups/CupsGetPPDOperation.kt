package org.cups4j.operations.cups

/**
 * @author Frank Carnevale
 * / *
 *
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 *
 * See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * program; if not, see <http:></http:>//www.gnu.org/licenses/>.
 */

/*Notice. This file is not part of the original cups4j. It is an implementaion
 * of a patch to cups4j suggested by Frank Carnevale
 */

import android.content.Context
import ch.ethz.vppserver.ippclient.IppTag
import org.cups4j.operations.IppOperation
import java.io.UnsupportedEncodingException
import java.net.URL
import java.nio.ByteBuffer
import java.util.*

class CupsGetPPDOperation(context: Context) : IppOperation(context) {
    init {
        operationID = 0x400F
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

        ippBuf = IppTag.getUri(ippBuf, "printer-uri", map["printer-uri"])
        ippBuf = IppTag.getEnd(ippBuf)
        ippBuf.flip()
        return ippBuf
    }

    @Throws(Exception::class)
    fun getPPDFile(printerUrl: URL): String {
        val url = URL(printerUrl.protocol + "://" + printerUrl.host + ":" + printerUrl.port)
        val map = HashMap<String, String>()
        map["printer-uri"] = printerUrl.path
        val result = request(url, map)
        val buf = String(result!!.buf!!)
        return buf.substring(buf.indexOf("*")) // Remove request attributes when returning the string
    }
}
