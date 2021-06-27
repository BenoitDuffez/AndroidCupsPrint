package org.cups4j.operations.cups

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
import org.cups4j.CupsPrinter
import org.cups4j.operations.IppOperation
import java.net.URL
import java.util.*

const val DEFAULT_PRINTER_NAME = "Unknown printer"

class CupsGetDefaultOperation(context: Context) : IppOperation(context) {
    init {
        operationID = 0x4001
        bufferSize = 8192
    }

    @Throws(Exception::class)
    fun getDefaultPrinter(url: URL, path: String): CupsPrinter? {
        var defaultPrinter: CupsPrinter? = null
        val command = CupsGetDefaultOperation(context)

        val map = HashMap<String, String>()
        map["requested-attributes"] = "printer-name printer-uri-supported printer-location"

        val result = command.request(URL(url.toString() + path), map)
        for (group in result!!.attributeGroupList!!) {
            if (group.tagName == "printer-attributes-tag") {
                var printerURL: String? = null
                var printerName: String? = null
                var location: String? = null
                for (attr in group.attribute) {
                    when (attr.name) {
                        "printer-uri-supported" -> printerURL = attr.attributeValue[0].value!!.replace("ipps?://".toRegex(), url.protocol + "://")
                        "printer-name" -> printerName = attr.attributeValue[0].value
                        "printer-location" -> if (attr.attributeValue.size > 0) {
                            location = attr.attributeValue[0].value
                        }
                    }
                }
                defaultPrinter = CupsPrinter(URL(printerURL), printerName ?: DEFAULT_PRINTER_NAME, true)
                defaultPrinter.location = location
            }
        }

        return defaultPrinter
    }
}
