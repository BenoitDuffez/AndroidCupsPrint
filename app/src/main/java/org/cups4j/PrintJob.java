package org.cups4j;

/**
 * Copyright (C) 2009 Harald Weyhing
 * <p>
 * This file is part of Cups4J. Cups4J is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * Cups4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with Cups4J. If
 * not, see <http://www.gnu.org/licenses/>.
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Print job class
 */
public class PrintJob {
    private InputStream document;

    private int copies;

    private String pageRanges;

    private String userName;

    private String jobName;

    private boolean duplex = false;

    private Map<String, String> attributes;

    PrintJob(Builder builder) {
        this.document = builder.document;
        this.jobName = builder.jobName;
        this.copies = builder.copies;
        this.pageRanges = builder.pageRanges;
        this.userName = builder.userName;
        this.duplex = builder.duplex;
        this.attributes = builder.attributes;

    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> printJobAttributes) {
        this.attributes = printJobAttributes;
    }

    public InputStream getDocument() {
        return document;
    }

    public int getCopies() {
        return copies;
    }

    public String getPageRanges() {
        return pageRanges;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isDuplex() {
        return duplex;
    }

    public String getJobName() {
        return jobName;
    }

    /**
     * <p>
     * Builds PrintJob objects like so:
     * </p>
     * <p>
     * PrintJob printJob = new
     * PrintJob.Builder(document).jobName("jobXY").userName
     * ("harald").copies(2).build();
     * </p>
     * <p>
     * documents are supplied as byte[] or as InputStream
     * </p>
     */
    public static class Builder {
        private InputStream document;

        private int copies = 1;

        private String pageRanges = null;

        private String userName = null;

        private String jobName = null;

        private boolean duplex = false;

        private Map<String, String> attributes;

        /**
         * Constructor
         *
         * @param document Printed document
         */
        public Builder(byte[] document) {
            this.document = new ByteArrayInputStream(document);
        }

        /**
         * Constructor
         *
         * @param document Printed document
         */
        public Builder(InputStream document) {
            this.document = document;
        }

        /**
         * @param copies Number of copies - 0 and 1 are both treated as one copy
         * @return Builder
         */
        public Builder copies(int copies) {
            this.copies = copies;
            return this;
        }

        /**
         * @param pageRanges Page ranges 1-3, 5, 8, 10-13
         * @return Builder
         */
        public Builder pageRanges(String pageRanges) {
            this.pageRanges = pageRanges;
            return this;
        }

        /**
         * @param userName Requesting user name
         * @return Builder
         */
        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        /**
         * @param jobName Job name
         * @return Builder
         */
        public Builder jobName(String jobName) {
            this.jobName = jobName;
            return this;
        }

        /**
         * @param duplex Duplex mode
         * @return Builder
         */
        public Builder duplex(boolean duplex) {
            this.duplex = duplex;
            return this;
        }

        /**
         * Additional attributes for the print operation and the print job
         *
         * @param attributes provide operation attributes and/or a String of job-attributes
         *                   <p>
         *                   job attributes are seperated by "#"
         *                   </p>
         *                   <p>
         *                   <p>
         *                   example:
         *                   </p>
         *                   <code>
         *                   attributes.put("compression","none");
         *                   attributes.put("job-attributes",
         *                   "print-quality:enum:3#sheet-collate:keyword:collated#sides:keyword:two-sided-long-edge"
         *                   );
         *                   </code>
         *                   <p>
         *                   -> take a look config/ippclient/list-of-attributes.xml for more information
         *                   </p>
         * @return Builder
         */
        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        /**
         * Builds the PrintJob object.
         *
         * @return PrintJob
         */
        public PrintJob build() {
            return new PrintJob(this);
        }
    }

}
