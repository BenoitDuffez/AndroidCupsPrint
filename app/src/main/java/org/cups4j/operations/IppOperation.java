package org.cups4j.operations;

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

import android.support.annotation.NonNull;
import android.util.Log;

import com.jonbanjo.ssl.AdditionalKeyStoresSSLSocketFactory;
import com.jonbanjo.ssl.AndroidCupsHttpConnectionManagement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import ch.ethz.vppserver.ippclient.IppResponse;
import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.ippclient.IppTag;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import io.github.benoitduffez.cupsprint.CupsPrintApp;

public abstract class IppOperation {
    private final static String IPP_MIME_TYPE = "application/ipp";

    protected short operationID = -1; // IPP operation ID

    protected short bufferSize = 8192; // BufferSize for this operation

    private X509Certificate[] mServerCerts; // store the certificates sent by the server if it's not trusted

    /**
     * Used to copy input data (IPP, document, etc) to HTTP connection
     *
     * @param from Data to be read
     * @param to   Destination
     * @return Number of copied bytes
     * @throws IOException
     */
    public static long copy(@NonNull InputStream from, @NonNull OutputStream to) throws IOException {
        final int BUF_SIZE = 0x1000; // 4K
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    /**
     * Gets the IPP header
     *
     * @param url Printer URL
     * @return IPP header
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer getIppHeader(URL url) throws UnsupportedEncodingException {
        return getIppHeader(url, null);
    }

    public IppResult request(URL url, Map<String, String> map) throws Exception {
        return sendRequest(url, getIppHeader(url, map));
    }

    public IppResult request(URL url, Map<String, String> map, InputStream document) throws Exception {
        return sendRequest(url, getIppHeader(url, map), document);
    }

    /**
     * Gets the IPP header
     *
     * @param url Printer URL
     * @param map Print attributes
     * @return IPP header
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer getIppHeader(URL url, Map<String, String> map) throws UnsupportedEncodingException {
        if (url == null) {
            System.err.println("IppOperation.getIppHeader(): uri is null");
            return null;
        }

        ByteBuffer ippBuf = ByteBuffer.allocateDirect(bufferSize);
        ippBuf = IppTag.getOperation(ippBuf, operationID);
        ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(url));

        if (map == null) {
            ippBuf = IppTag.getEnd(ippBuf);
            if (ippBuf != null) {
                ippBuf.flip();
            }
            return ippBuf;
        }

        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map.get("requesting-user-name"));

        if (map.get("limit") != null) {
            int value = Integer.parseInt(map.get("limit"));
            ippBuf = IppTag.getInteger(ippBuf, "limit", value);
        }

        if (map.get("requested-attributes") != null) {
            String[] sta = map.get("requested-attributes").split(" ");
            ippBuf = IppTag.getKeyword(ippBuf, "requested-attributes", sta[0]);
            int l = sta.length;
            for (int i = 1; i < l; i++) {
                ippBuf = IppTag.getKeyword(ippBuf, null, sta[i]);
            }
        }

        ippBuf = IppTag.getEnd(ippBuf);
        if (ippBuf != null) {
            ippBuf.flip();
        }
        return ippBuf;
    }

    /**
     * Sends a request to the provided URL
     *
     * @param url    Printer URL
     * @param ippBuf IPP buffer
     * @return result
     * @throws IOException
     * @throws Exception
     */
    private IppResult sendRequest(URL url, ByteBuffer ippBuf) throws Exception {
        return sendRequest(url, ippBuf, null);
    }

    /**
     * Sends a request to the provided url
     *
     * @param url            Printer URL
     * @param ippBuf         IPP buffer
     * @param documentStream Printed document input stream
     * @return result
     * @throws Exception
     */
    private IppResult sendRequest(URL url, ByteBuffer ippBuf, InputStream documentStream) throws Exception {
        IppResult ippResult;
        if (ippBuf == null) {
            return null;
        }

        if (url == null) {
            return null;
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);
            connection.setRequestProperty("Content-Type", IPP_MIME_TYPE);

            if (url.getProtocol().equals("https")) {
                AndroidCupsHttpConnectionManagement.handleHttpsUrlConnection((HttpsURLConnection) connection);
            }

            AndroidCupsHttpConnectionManagement.handleBasicAuth(url, connection);

            byte[] bytes = new byte[ippBuf.limit()];
            ippBuf.get(bytes);

            ByteArrayInputStream headerStream = new ByteArrayInputStream(bytes);
            // If we need to send a document, concatenate InputStreams
            InputStream inputStream = headerStream;
            if (documentStream != null) {
                inputStream = new SequenceInputStream(headerStream, documentStream);
            }

            connection.connect();

            // Send the data
            copy(inputStream, connection.getOutputStream());

            // Read response
            byte[] result = readInputStream(connection.getInputStream());

            // Prepare IPP result
            IppResponse ippResponse = new IppResponse();
            ippResult = ippResponse.getResponse(ByteBuffer.wrap(result));
            ippResult.setHttpStatusResponse(connection.getResponseMessage());
        } catch (Exception e) {
            Log.e(CupsPrintApp.LOG_TAG, "Caught exception while connecting to printer " + url + ": " + e.getLocalizedMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (connection instanceof HttpsURLConnection) {
                if (((HttpsURLConnection) connection).getSSLSocketFactory() instanceof AdditionalKeyStoresSSLSocketFactory) {
                    final AdditionalKeyStoresSSLSocketFactory socketFactory = (AdditionalKeyStoresSSLSocketFactory) ((HttpsURLConnection) connection).getSSLSocketFactory();
                    mServerCerts = socketFactory.getServerCert();
                }
            }
            connection.disconnect();
        }

        return ippResult;
    }

    /**
     * Store the contents of an input stream to a byte array
     *
     * @param is Input data to be read
     * @return Input data read and stored into a byte array
     * @throws IOException
     */
    private byte[] readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    protected String stripPortNumber(URL url) {
        return url.getProtocol() + "://" + url.getHost() + url.getPath();
    }

    protected String getAttributeValue(Attribute attr) {
        return attr.getAttributeValue().get(0).getValue();
    }

    public X509Certificate[] getServerCerts() {
        return mServerCerts;
    }
}
