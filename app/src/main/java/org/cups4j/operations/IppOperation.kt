package org.cups4j.operations

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
import ch.ethz.vppserver.ippclient.IppResponse
import ch.ethz.vppserver.ippclient.IppResult
import ch.ethz.vppserver.ippclient.IppTag
import ch.ethz.vppserver.schema.ippclient.Attribute
import io.github.benoitduffez.cupsprint.HttpConnectionManagement
import io.github.benoitduffez.cupsprint.ssl.AdditionalKeyStoresSSLSocketFactory
import timber.log.Timber
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.security.cert.X509Certificate
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.HttpsURLConnection

abstract class IppOperation(val context: Context) {
    protected var operationID: Short = -1 // IPP operation ID
    protected var bufferSize: Short = 8192 // BufferSize for this operation
    var serverCerts: Array<X509Certificate>? = null
        private set // store the certificates sent by the server if it's not trusted
    var lastResponseCode: Int = 0
        private set
    private val aborted: AtomicBoolean = AtomicBoolean(false)
    @Volatile
    private var threadRef: Thread? = null

    /**
     * Gets the IPP header
     *
     * @param url Printer URL
     * @return IPP header
     * @throws UnsupportedEncodingException If the ipp data can't be generated
     */
    @Throws(UnsupportedEncodingException::class)
    fun getIppHeader(url: URL): ByteBuffer? = getIppHeader(url, null)

    @Throws(Exception::class)
    fun request(url: URL, map: Map<String, String>): IppResult? = sendRequest(url, getIppHeader(url, map))

    @Throws(Exception::class)
    fun request(url: URL, map: Map<String, String>, document: InputStream): IppResult? = sendRequest(url, getIppHeader(url, map), document)

    /**
     * Gets the IPP header
     *
     * @param url Printer URL
     * @param map Print attributes
     * @return IPP header
     * @throws UnsupportedEncodingException If the ipp data can't be generated
     */
    @Throws(UnsupportedEncodingException::class)
    open fun getIppHeader(url: URL, map: Map<String, String>?): ByteBuffer {
        var ippBuf = ByteBuffer.allocateDirect(bufferSize.toInt())
        ippBuf = IppTag.getOperation(ippBuf, operationID)
        ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(url))

        if (map == null) {
            ippBuf = IppTag.getEnd(ippBuf)
            ippBuf.flip()
            return ippBuf
        }

        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map["requesting-user-name"])


        map["limit"]?.let { ippBuf = IppTag.getInteger(ippBuf, "limit", it.toInt()) }

        map["requested-attributes"]?.let { requestedAttributes ->
            val sta = requestedAttributes.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            ippBuf = IppTag.getKeyword(ippBuf, "requested-attributes", sta[0])
            val l = sta.size
            for (i in 1 until l) {
                ippBuf = IppTag.getKeyword(ippBuf, null, sta[i])
            }
        }

        ippBuf = IppTag.getEnd(ippBuf)
        ippBuf.flip()
        return ippBuf
    }

    /**
     * Sends a request to the provided url
     *
     * @param url            Printer URL
     * @param ippBuf         IPP buffer
     * @param documentStream Printed document input stream
     * @return result
     * @throws Exception If any network error occurs
     */
    @Throws(Exception::class)
    private fun sendRequest(url: URL, ippBuf: ByteBuffer, documentStream: InputStream? = null): IppResult? {
        if (isAborted()) {
            return null
        }

        val ippResult: IppResult
        val connection = url.openConnection() as HttpURLConnection
        lastResponseCode = 0

        try {
            // Set the thread reference for Interruption in abort()
            threadRef = Thread.currentThread()

            connection.requestMethod = "POST"
            connection.readTimeout = 10000
            connection.connectTimeout = 10000
            connection.doInput = true
            connection.doOutput = true
            connection.setChunkedStreamingMode(0)
            connection.setRequestProperty("Content-Type", IPP_MIME_TYPE)

            if (url.protocol == "https") {
                HttpConnectionManagement.handleHttpsUrlConnection(context, connection as HttpsURLConnection)
            }

            HttpConnectionManagement.handleBasicAuth(context, url, connection)

            val bytes = ByteArray(ippBuf.limit())
            ippBuf.get(bytes)

            val headerStream = ByteArrayInputStream(bytes)
            // If we need to send a document, concatenate InputStreams
            var inputStream: InputStream = headerStream
            if (documentStream != null) {
                inputStream = SequenceInputStream(headerStream, documentStream)
            }

            connection.connect()

            // Send the data
            copy(inputStream, connection.outputStream)

            if (isAborted()) {
                return null
            }

            // Read response
            val result = readInputStream(connection.inputStream)
            lastResponseCode = connection.responseCode

            if (isAborted()) {
                return null
            }

            // Prepare IPP result
            val ippResponse = IppResponse()
            ippResult = ippResponse.getResponse(ByteBuffer.wrap(result))
            ippResult.httpStatusResponse = connection.responseMessage
        } catch (e: Exception) {
            if (isAborted()) {
                return null
            }
            lastResponseCode = connection.responseCode
            Timber.e("Caught exception while connecting to printer $url: HTTP ${connection.responseCode} ${connection.responseMessage}")
            Timber.e("Exception: ${e.message} - $e")
            throw e
        } finally {
            if (connection is HttpsURLConnection) {
                if (connection.sslSocketFactory is AdditionalKeyStoresSSLSocketFactory) {
                    val socketFactory = connection.sslSocketFactory as AdditionalKeyStoresSSLSocketFactory
                    serverCerts = socketFactory.serverCert
                }
            }
            connection.disconnect()

            // Clear the interrupted status and the thread reference
            Thread.interrupted()
            threadRef = null
        }

        return ippResult
    }

    /**
     * Abort the IppOperation and stops the transfer directly
     */
    fun abort() {
        if (this.aborted.compareAndSet(false, true)) {
            // Interrupt the thread reference
            threadRef?.interrupt()
        }
    }

    /**
     * Tests whether this IppOperation has been aborted.
     *
     * @return true if this IppOperation has been aborted; false otherwise.
     */
    fun isAborted(): Boolean {
        return this.aborted.get()
    }

    /**
     * Store the contents of an input stream to a byte array
     *
     * @param is Input data to be read
     * @return Input data read and stored into a byte array
     * @throws IOException if the IS couldn't be read or the buffer couldn't be written to
     */
    @Throws(IOException::class)
    private fun readInputStream(`is`: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()

        var nRead: Int
        val data = ByteArray(16384)

        while (!this.aborted.get()) {
            nRead = `is`.read(data, 0, data.size)
            if (nRead == -1) {
                break
            }
            buffer.write(data, 0, nRead)
        }

        buffer.flush()

        return buffer.toByteArray()
    }

    protected fun stripPortNumber(url: URL): String = url.protocol + "://" + url.host + url.path

    protected fun getAttributeValue(attr: Attribute): String = attr.attributeValue[0].value!!

    companion object {
        private const val IPP_MIME_TYPE = "application/ipp"

        /**
         * Used to copy input data (IPP, document, etc) to HTTP connection
         *
         * @param from Data to be read
         * @param to   Destination
         * @return Number of copied bytes
         * @throws IOException If the stream can't be read/written from/to
         */
        @Throws(IOException::class)
        fun copy(from: InputStream, to: OutputStream): Long {
            val bufSize = 0x1000 // 4K
            val buf = ByteArray(bufSize)
            var total: Long = 0
            while (!Thread.interrupted()) {
                val r = from.read(buf)
                if (r == -1) {
                    break
                }
                to.write(buf, 0, r)
                total += r.toLong()
            }
            return total
        }
    }
}
