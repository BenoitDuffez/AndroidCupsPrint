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
import org.cups4j.operations.IppOperation
import java.io.UnsupportedEncodingException
import java.net.URL
import java.nio.ByteBuffer

class IppPrintJobOperation(context: Context) : IppOperation(context) {
    init {
        operationID = 0x0002
        bufferSize = 8192
    }

    /**
     * TODO: not all possibilities implemented
     *
     * @param ippBuf          IPP buffer
     * @param attributeBlocks Job attributes
     * @return Modified IPP buffer
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    private fun getJobAttributes(inputIppBuf: ByteBuffer?, attributeBlocks: Array<String>?): ByteBuffer? {
        if (inputIppBuf == null) {
            System.err.println("IppPrintJobOperation.getJobAttributes(): ippBuf is null")
            return null
        }
        if (attributeBlocks == null) {
            return inputIppBuf
        }

        var ippBuf = IppTag.getJobAttributesTag(inputIppBuf)
        for (attributeBlock in attributeBlocks) {
            val attr = attributeBlock.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (attr.size != 3) {
                return ippBuf
            }
            var name: String? = attr[0]
            val tagName = attr[1]
            val value = attr[2]

            when (tagName) {
                "boolean" -> ippBuf = if (value == "true") {
                    IppTag.getBoolean(ippBuf, name, true)
                } else {
                    IppTag.getBoolean(ippBuf, name, false)
                }

                "integer" -> ippBuf = IppTag.getInteger(ippBuf, name, Integer.parseInt(value))

                "rangeOfInteger" -> {
                    val range = value.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val low = Integer.parseInt(range[0])
                    val high = Integer.parseInt(range[1])
                    ippBuf = IppTag.getRangeOfInteger(ippBuf, name, low, high)
                }

                "setOfRangeOfInteger" -> {
                    val ranges = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    for (r in ranges) {
                        val range = r.trim { it <= ' ' }
                        val values = range.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        val value1 = Integer.parseInt(values[0])
                        var value2 = value1
                        // two values provided?
                        if (values.size == 2) {
                            value2 = Integer.parseInt(values[1])
                        }

                        // first attribute value needs name, additional values need to get the "null" name
                        ippBuf = IppTag.getRangeOfInteger(ippBuf, name, value1, value2)
                        name = null
                    }
                }

                "keyword" -> ippBuf = IppTag.getKeyword(ippBuf, name, value)

                "name" -> ippBuf = IppTag.getNameWithoutLanguage(ippBuf, name, value)

                "enum" -> ippBuf = IppTag.getEnum(ippBuf, name, Integer.parseInt(value))

                "resolution" -> {
                    val resolution = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val value1 = Integer.parseInt(resolution[0])
                    val value2 = Integer.parseInt(resolution[1])
                    val value3 = java.lang.Byte.valueOf(resolution[2])
                    ippBuf = IppTag.getResolution(ippBuf, name, value1, value2, value3)
                }
            }
        }
        return ippBuf
    }

    @Throws(UnsupportedEncodingException::class)
    override fun getIppHeader(url: URL?, map: Map<String, String>?): ByteBuffer? {
        if (url == null) {
            System.err.println("IppPrintJobOperation.getIppHeader(): uri is null")
            return null
        }

        var ippBuf = ByteBuffer.allocateDirect(bufferSize.toInt())
        ippBuf = IppTag.getOperation(ippBuf, operationID)
        ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(url))

        if (map == null) {
            ippBuf = IppTag.getEnd(ippBuf)
            ippBuf.flip()
            return ippBuf
        }

        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map["requesting-user-name"])

        map["job-name"]?.let { ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "job-name", it) }
        map["ipp-attribute-fidelity"]?.let { ippBuf = IppTag.getBoolean(ippBuf, "ipp-attribute-fidelity", it == "true") }
        map["document-name"]?.let { ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "document-name", it) }
        map["compression"]?.let { ippBuf = IppTag.getKeyword(ippBuf, "compression", it) }
        map["document-format"]?.let { ippBuf = IppTag.getMimeMediaType(ippBuf, "document-format", it) }
        map["document-natural-language"]?.let { ippBuf = IppTag.getNaturalLanguage(ippBuf, "document-natural-language", it) }
        map["job-k-octets"]?.let { ippBuf = IppTag.getInteger(ippBuf, "job-k-octets", it.toInt()) }
        map["job-impressions"]?.let { ippBuf = IppTag.getInteger(ippBuf, "job-impressions", it.toInt()) }
        map["job-media-sheets"]?.let { ippBuf = IppTag.getInteger(ippBuf, "job-media-sheets", it.toInt()) }
        map["job-attributes"]?.let { jobAttributes ->
            val attributeBlocks = jobAttributes.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            ippBuf = getJobAttributes(ippBuf, attributeBlocks)
        }

        ippBuf = IppTag.getEnd(ippBuf)
        ippBuf.flip()
        return ippBuf
    }
}
