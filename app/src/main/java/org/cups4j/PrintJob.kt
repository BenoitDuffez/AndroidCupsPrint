package org.cups4j

/**
 * Copyright (C) 2009 Harald Weyhing
 *
 *
 * This file is part of Cups4J. Cups4J is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *
 * Cups4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License along with Cups4J. If
 * not, see <http:></http:>//www.gnu.org/licenses/>.
 */

import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Print job class
 */
class PrintJob internal constructor(builder: Builder) {

    companion object {
        const val DUPLEX_NONE = 0
        const val DUPLEX_LONG_EDGE = 1
        const val DUPLEX_SHORT_EDGE = 2
    }

    val document: InputStream
    val copies: Int
    val pageRanges: String?
    val userName: String?
    val jobName: String?
    var duplex = DUPLEX_NONE
    var attributes: MutableMap<String, String>? = null

    init {
        this.document = builder.document
        this.jobName = builder.jobName
        this.copies = builder.copies
        this.pageRanges = builder.pageRanges
        this.userName = builder.userName
        this.duplex = builder.duplex
        this.attributes = builder.attributes
    }

    /**
     *
     *
     * Builds PrintJob objects like so:
     *
     *
     *
     * PrintJob printJob = new
     * PrintJob.Builder(document).jobName("jobXY").userName
     * ("harald").copies(2).build();
     *
     *
     *
     * documents are supplied as byte[] or as InputStream
     *
     */
    class Builder {
        var document: InputStream
        var copies = 1
        var pageRanges: String? = null
        var userName: String? = null
        var jobName: String? = null
        var duplex = DUPLEX_NONE
        var attributes: MutableMap<String, String>? = null

        /**
         * Constructor
         *
         * @param document Printed document
         */
        constructor(document: ByteArray) {
            this.document = ByteArrayInputStream(document)
        }

        /**
         * Constructor
         *
         * @param document Printed document
         */
        constructor(document: InputStream) {
            this.document = document
        }

        /**
         * @param copies Number of copies - 0 and 1 are both treated as one copy
         * @return Builder
         */
        fun copies(copies: Int): Builder {
            this.copies = copies
            return this
        }

        /**
         * @param pageRanges Page ranges 1-3, 5, 8, 10-13
         * @return Builder
         */
        fun pageRanges(pageRanges: String): Builder {
            this.pageRanges = pageRanges
            return this
        }

        /**
         * @param userName Requesting user name
         * @return Builder
         */
        fun userName(userName: String): Builder {
            this.userName = userName
            return this
        }

        /**
         * @param jobName Job name
         * @return Builder
         */
        fun jobName(jobName: String): Builder {
            this.jobName = jobName
            return this
        }

        /**
         * @param duplex Duplex mode
         * @return Builder
         */
        fun duplex(duplex: Int): Builder {
            this.duplex = duplex
            return this
        }

        /**
         * Additional attributes for the print operation and the print job
         *
         * @param attributes provide operation attributes and/or a String of job-attributes
         *
         * job attributes are seperated by "#"
         *
         * example:
         * `
         * attributes.put("compression","none");
         * attributes.put("job-attributes",
         * "print-quality:enum:3#sheet-collate:keyword:collated#sides:keyword:two-sided-long-edge"
         * );
         * `
         * -> take a look config/ippclient/list-of-attributes.xml for more information
         *
         * @return Builder
         */
        fun attributes(attributes: MutableMap<String, String>): Builder {
            this.attributes = attributes
            return this
        }

        /**
         * Builds the PrintJob object.
         *
         * @return PrintJob
         */
        fun build(): PrintJob = PrintJob(this)
    }
}
