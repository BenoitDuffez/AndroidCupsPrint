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

import java.net.URL
import java.util.*

/**
 * Holds print job attributes
 */
class PrintJobAttributes {
    var jobURL: URL? = null
    var printerURL: URL? = null
    var jobID = -1
    var jobState: JobStateEnum? = null
    var jobName: String? = null
    var userName: String? = null
    var jobCreateTime: Date? = null
    var jobCompleteTime: Date? = null
    var pagesPrinted = 0

    // Size of the job in kb (this value is rounded up by the IPP server)
    // This value is optional and might not be reported by your IPP server
    var size = -1
}
