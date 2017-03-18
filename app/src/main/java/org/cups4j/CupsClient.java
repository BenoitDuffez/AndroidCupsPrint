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

/*Notice
 * This file has been modified. It is not the original. 
 * Jon Freeman - 2013
 */


import org.cups4j.operations.cups.CupsGetDefaultOperation;
import org.cups4j.operations.cups.CupsGetPrintersOperation;
import org.cups4j.operations.cups.CupsMoveJobOperation;
import org.cups4j.operations.ipp.IppCancelJobOperation;
import org.cups4j.operations.ipp.IppGetJobAttributesOperation;
import org.cups4j.operations.ipp.IppGetJobsOperation;
import org.cups4j.operations.ipp.IppHoldJobOperation;
import org.cups4j.operations.ipp.IppReleaseJobOperation;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Main Client for accessing CUPS features like
 * <p>
 * - get printers
 * </p>
 * <p>
 * - print documents
 * </p>
 * <p>
 * - get job attributes
 * </p>
 * <p>
 * - ...
 * </p>
 */
public class CupsClient {

    public static final String DEFAULT_USER = "anonymous";

    private static final String DEFAULT_URL = "http://localhost:631";

    private URL url = null;

    private String userName = null;

    private X509Certificate[] mServerCerts; // Storage for server certificates if they're not trusted

    private int mLastResponseCode;

    /**
     * Path to list of printers, like xxx://ip:port/printers/printer_name => will contain '/printers/'
     * seen in issue: https://github.com/BenoitDuffez/AndroidCupsPrint/issues/40
     */
    private String path = "/printers/";

    public CupsClient() throws Exception {
        this(new URL(DEFAULT_URL), DEFAULT_USER);
    }

    public CupsClient(URL url) {
        this(url, DEFAULT_USER);
    }

    public CupsClient(URL url, String userName) {
        this.url = url;
        this.userName = userName;
    }

    public List<CupsPrinter> getPrinters() throws Exception {
        final CupsGetPrintersOperation cupsGetPrintersOperation = new CupsGetPrintersOperation();
        List<CupsPrinter> printers;
        try {
            printers = cupsGetPrintersOperation.getPrinters(url, path);
        } finally {
            mServerCerts = cupsGetPrintersOperation.getServerCerts();
            mLastResponseCode = cupsGetPrintersOperation.getLastResponseCode();
        }

        // add default printer if available
        CupsPrinter defaultPrinter = getDefaultPrinter();

        for (CupsPrinter p : printers) {
            if (defaultPrinter != null && p.getPrinterURL().toString().equals(defaultPrinter.getPrinterURL().toString())) {
                p.setDefault(true);
            }
        }

        return printers;
    }

    public CupsPrinter getDefaultPrinter() throws Exception {
        return new CupsGetDefaultOperation().getDefaultPrinter(url, path);
    }

    public CupsPrinter getPrinter(URL printerURL) throws Exception {
        List<CupsPrinter> printers = getPrinters();
        for (CupsPrinter p : printers) {
            if (p.getPrinterURL().getPath().equals(printerURL.getPath()))
                return p;
        }
        return null;
    }

    public PrintJobAttributes getJobAttributes(int jobID) throws Exception {
        return getJobAttributes(url, userName, jobID);
    }

    public PrintJobAttributes getJobAttributes(String userName, int jobID) throws Exception {
        return getJobAttributes(url, userName, jobID);
    }


    private PrintJobAttributes getJobAttributes(URL url, String userName, int jobID) throws Exception {
        if (userName == null || "".equals(userName)) {
            userName = DEFAULT_USER;
        }
        return new IppGetJobAttributesOperation().getPrintJobAttributes(url, userName, jobID);
    }

    public List<PrintJobAttributes> getJobs(CupsPrinter printer, WhichJobsEnum whichJobs, String userName, boolean myJobs)
            throws Exception {
        return new IppGetJobsOperation().getPrintJobs(printer, whichJobs, userName, myJobs);
    }


    public boolean cancelJob(int jobID) throws Exception {
        return new IppCancelJobOperation().cancelJob(url, userName, jobID);
    }

    public boolean cancelJob(URL url, String userName, int jobID) throws Exception {
        return new IppCancelJobOperation().cancelJob(url, userName, jobID);
    }

    public boolean holdJob(int jobID) throws Exception {
        return new IppHoldJobOperation().holdJob(url, userName, jobID);
    }

    public boolean holdJob(URL url, String userName, int jobID) throws Exception {
        return new IppHoldJobOperation().holdJob(url, userName, jobID);
    }

    public boolean releaseJob(int jobID) throws Exception {
        return new IppReleaseJobOperation().releaseJob(url, userName, jobID);
    }

    public boolean releaseJob(URL url, String userName, int jobID) throws Exception {
        return new IppReleaseJobOperation().releaseJob(url, userName, jobID);
    }

    public boolean moveJob(int jobID, String userName, CupsPrinter currentPrinter, CupsPrinter targetPrinter)
            throws Exception {
        String currentHost = currentPrinter.getPrinterURL().getHost();

        return new CupsMoveJobOperation().moveJob(currentHost, userName, jobID, targetPrinter.getPrinterURL());
    }

    public X509Certificate[] getServerCerts() {
        return mServerCerts;
    }

    public String getHost() {
        return url.getHost();
    }

    public int getLastResponseCode() {
        return mLastResponseCode;
    }

    /**
     * Ensure path starts and ends with a slash
     *
     * @param path Path to printers on server
     * @return Self for easy chained calls
     */
    public CupsClient setPath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        this.path = path;
        return this;
    }
}
