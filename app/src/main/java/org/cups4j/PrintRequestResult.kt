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

import ch.ethz.vppserver.ippclient.IppResult
import java.util.regex.Pattern

/**
 * Result of a print request
 */
class PrintRequestResult(ippResult: IppResult?) {
    var jobId: Int = 0
    private var resultCode: String? = ""
    private var resultDescription = ""
    val isSuccessfulResult: Boolean
        get() = resultCode != null && resultCode!!.startsWith("0x00")

    init {
        if (ippResult != null && !isNullOrEmpty(ippResult.httpStatusResponse)) {
            initializeFromHttpStatusResponse(ippResult)
            if (ippResult.ippStatusResponse != null) {
                initializeFromIppStatusResponse(ippResult)
            }
        }
    }

    private fun initializeFromIppStatusResponse(ippResult: IppResult) {
        val p = Pattern.compile("Status Code:(0x\\d+)(.*)")
        val m = p.matcher(ippResult.ippStatusResponse!!)
        if (m.find()) {
            resultCode = m.group(1)
            resultDescription = m.group(2)
        }
    }

    private fun initializeFromHttpStatusResponse(ippResult: IppResult) {
        val p = Pattern.compile("HTTP/1.0 (\\d+) (.*)")
        val m = p.matcher(ippResult.httpStatusResponse!!)
        if (m.find()) {
            resultCode = m.group(1)
            resultDescription = m.group(2)
        }
    }

    private fun isNullOrEmpty(string: String?): Boolean = string == null || "" == string.trim { it <= ' ' }
}
