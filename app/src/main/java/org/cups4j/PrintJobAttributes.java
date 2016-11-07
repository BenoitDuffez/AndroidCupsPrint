package org.cups4j;

/**
 * Copyright (C) 2009 Harald Weyhing
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program; if not, see
 * <http://www.gnu.org/licenses/>.
 */

import java.net.URL;
import java.util.Date;

/**
 * Holds print job attributes
 */
public class PrintJobAttributes {
    private URL jobURL = null;

    private URL printerURL = null;

    private int jobID = -1;

    private JobStateEnum jobState = null;

    private String jobName = null;

    private String userName = null;

    private Date jobCreateTime;

    private Date jobCompleteTime;

    private int pagesPrinted = 0;

    // Size of the job in kb (this value is rounded up by the IPP server)
    // This value is optional and might not be reported by your IPP server
    private int size = -1;

    public URL getJobURL() {
        return jobURL;
    }

    public void setJobURL(URL jobURL) {
        this.jobURL = jobURL;
    }

    public URL getPrinterURL() {
        return printerURL;
    }

    public void setPrinterURL(URL printerURL) {
        this.printerURL = printerURL;
    }

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public JobStateEnum getJobState() {
        return jobState;
    }

    public void setJobState(JobStateEnum jobState) {
        this.jobState = jobState;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getJobCreateTime() {
        return jobCreateTime;
    }

    public void setJobCreateTime(Date jobCreateTime) {
        this.jobCreateTime = jobCreateTime;
    }

    public Date getJobCompleteTime() {
        return jobCompleteTime;
    }

    public void setJobCompleteTime(Date jobCompleteTime) {
        this.jobCompleteTime = jobCompleteTime;
    }

    public int getPagesPrinted() {
        return pagesPrinted;
    }

    public void setPagesPrinted(int pagesPrinted) {
        this.pagesPrinted = pagesPrinted;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
