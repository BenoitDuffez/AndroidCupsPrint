package org.cups4j.operations.ipp

/**
 * Copyright (C) 2009 Harald Weyhing
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

/*Notice
 * This file has been modified. It is not the original.
 * Jon Freeman - 2013
 */

import android.content.Context
import ch.ethz.vppserver.ippclient.IppTag
import org.cups4j.operations.IppOperation
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer

class IppGetPrinterAttributesOperation(context: Context) : IppOperation(context) {
    init {
        operationID = 0x000b
        bufferSize = 8192
    }

    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getIppHeader(url: String, map: Map<String, String>? = null): ByteBuffer? {
        var ippBuf = ByteBuffer.allocateDirect(bufferSize.toInt())

        ippBuf = IppTag.getOperation(ippBuf, operationID)
        ippBuf = IppTag.getUri(ippBuf, "printer-uri", url)

        if (map == null) {
            ippBuf = IppTag.getKeyword(ippBuf, "requested-attributes", "all")
            ippBuf = IppTag.getEnd(ippBuf)
            ippBuf.flip()
            return ippBuf
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

        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "document-format", map["document-format"])

        ippBuf = IppTag.getEnd(ippBuf)
        ippBuf?.flip()
        return ippBuf
    }
}
