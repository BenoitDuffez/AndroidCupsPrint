package ch.ethz.vppserver.ippclient

import ch.ethz.vppserver.schema.ippclient.Attribute
import ch.ethz.vppserver.schema.ippclient.AttributeGroup
import ch.ethz.vppserver.schema.ippclient.AttributeValue
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.*

/**
 * Copyright (C) 2008 ITS of ETH Zurich, Switzerland, Sarah Windler Burri
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * program; if not, see <http:></http:>//www.gnu.org/licenses/>.
 */

/*Notice
 * This file has been modified. It is not the original.
 * Ppd op patch as suggested at
 * http://www.cups4j.org/forum/viewtopic.php?f=6&t=40
 * has been applied. Additional mods to work with
 * IppLists Jon Freeman - 2013
 */

class IppResponse {
    // Saved response of printer
    private var _attributeGroupResult: AttributeGroup? = null
    private var _attributeResult: Attribute? = null
    private var _result: MutableList<AttributeGroup>? = null

    // read IPP response in global buffer
    internal var _buf: ByteBuffer? = null

    /**
     *
     * @return
     */
    private val httpHeader: String?
        get() {
            val endOf = CRLF + CRLF
            val sb = StringBuffer()
            while (sb.indexOf(endOf) == -1) {
                val b = _buf!!.get().toInt()
                val ival = b and 0xff
                val c = ival.toChar()
                sb.append(c)
            }
            return if (sb.isNotEmpty()) {
                sb.toString()
            } else null
        }

    /**
     *
     * @return
     */
    private val ippHeader: String?
        get() {
            val sb = StringBuffer()
            sb.append("Major Version:" + IppUtil.toHexWithMarker(_buf!!.get().toInt()))
            sb.append(" Minor Version:" + IppUtil.toHexWithMarker(_buf!!.get().toInt()))

            val statusCode = IppUtil.toHexWithMarker(_buf!!.get().toInt()) + IppUtil.toHex(_buf!!.get().toInt())
            val statusMessage = IppLists.statusCodeMap[statusCode]
            sb.append(" Request Id:" + _buf!!.int + "\n")
            sb.append("Status Code:$statusCode($statusMessage)")

            return if (sb.length != 0) {
                sb.toString()
            } else null
        }

    /**
     *
     *
     * **Note:** Global variables `_attributeGroupResult`,
     * `_attributeResult`, `_result` are filled by local
     * 'tag' methods.<br></br>
     * Decision for this programming solution is based on the structure of IPP tag
     * sequences to clarify the attribute structure with its values.
     *
     *
     * @return list of attributes group
     */
    private// reserved
    // operation-attributes
    // job-attributes
    // end-attributes
    // printer-attributes
    // unsupported-attributes
    // subscription-attributes
    // event-notification-attributes
    // no-value
    // integer
    // boolean
    // enumeration
    // octetString;
    // datetime
    // resolution
    // rangeOfInteger
    // textWithLanguage
    // nameWithLanguage
    // textWithoutLanguage
    // nameWithoutLanguage
    // keyword
    // uri
    // uriScheme
    // charset
    // naturalLanguage
    // mimeMediaType
    // not defined
    val attributeGroupList: List<AttributeGroup>?
        get() {
            loop@ while (_buf!!.hasRemaining()) {

                val tag = _buf!!.get().toInt()
                when (tag) {
                    0x00 -> {
                        setAttributeGroup(tag)
                        continue@loop
                    }
                    0x01 -> {
                        setAttributeGroup(tag)
                        continue@loop
                    }
                    0x02 -> {
                        setAttributeGroup(tag)
                        continue@loop
                    }
                    0x03 -> return _result
                    0x04 -> {
                        setAttributeGroup(tag)
                        continue@loop
                    }
                    0x05 -> {
                        setAttributeGroup(tag)
                        continue@loop
                    }
                    0x06 -> {
                        setAttributeGroup(tag)
                        continue@loop
                    }
                    0x07 -> {
                        setAttributeGroup(tag)
                        continue@loop
                    }
                    0x13 -> {
                        setNoValueAttribute()
                        continue@loop
                    }
                    0x21 -> {
                        setIntegerAttribute(tag)
                        continue@loop
                    }
                    0x22 -> {
                        setBooleanAttribute(tag)
                        continue@loop
                    }
                    0x23 -> {
                        setEnumAttribute(tag)
                        continue@loop
                    }
                    0x30 -> {
                        setTextAttribute(tag)
                        continue@loop
                    }
                    0x31 -> {
                        setDateTimeAttribute(tag)
                        continue@loop
                    }
                    0x32 -> {
                        setResolutionAttribute(tag)
                        continue@loop
                    }
                    0x33 -> {
                        setRangeOfIntegerAttribute(tag)
                        continue@loop
                    }
                    0x35 -> {
                        setTextWithLanguageAttribute(tag)
                        continue@loop
                    }
                    0x36 -> {
                        setNameWithLanguageAttribute(tag)
                        continue@loop
                    }
                    0x41 -> {
                        setTextAttribute(tag)
                        continue@loop
                    }
                    0x42 -> {
                        setTextAttribute(tag)
                        continue@loop
                    }
                    0x44 -> {
                        setTextAttribute(tag)
                        continue@loop
                    }
                    0x45 -> {
                        setTextAttribute(tag)
                        continue@loop
                    }
                    0x46 -> {
                        setTextAttribute(tag)
                        continue@loop
                    }
                    0x47 -> {
                        setTextAttribute(tag)
                        continue@loop
                    }
                    0x48 -> {
                        setTextAttribute(tag)
                        continue@loop
                    }
                    0x49 -> {
                        setTextAttribute(tag)
                        continue@loop
                    }
                    else -> return _result
                }
            }
            return null
        }

    init {
        _result = ArrayList()
        _buf = ByteBuffer.allocate(BYTEBUFFER_CAPACITY)
    }

    /**
     *
     * @param channel
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getResponse(channel: SocketChannel?): IppResult? {
        if (channel == null) {
            System.err.println("IppResponse.getResponse(): no channel given")
            return null
        }

        _buf!!.clear()

        _attributeGroupResult = null
        _attributeResult = null
        _result!!.clear()

        val result = IppResult()
        var httpResponse = false
        var ippHeaderResponse = false

        // be careful: HTTP and IPP could be transmitted in different set of
        // buffers.
        // see RFC2910, http://www.ietf.org/rfc/rfc2910, page 19
        var ippByteCount = 0
        var tmpBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY)
        val bufferList = ArrayList<ByteBuffer>()

        while (channel.read(tmpBuffer) != -1) {
            tmpBuffer.flip()
            // read HTTP header
            if (!httpResponse && tmpBuffer.hasRemaining()) {
                _buf = tmpBuffer
                result.httpStatusResponse = httpHeader
                httpResponse = true
            }

            // read IPP header
            if (!ippHeaderResponse && tmpBuffer.hasRemaining()) {
                _buf = tmpBuffer
                result.ippStatusResponse = ippHeader
                ippHeaderResponse = true
            }

            // read the IPP-answer - this can be large, so take to read all
            // information
            if (tmpBuffer.hasRemaining()) {
                ippByteCount += tmpBuffer.remaining()
                bufferList.add(tmpBuffer)
            }
            tmpBuffer = ByteBuffer.allocate(BYTEBUFFER_CAPACITY)
        }

        _buf = concatenateBytebuffers(bufferList)
        // read attribute group list with attributes
        attributeGroupList

        closeAttributeGroup()
        result.attributeGroupList = _result
        return result
    }

    /**
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getResponse(buffer: ByteBuffer): IppResult {

        _buf!!.clear()

        _attributeGroupResult = null
        _attributeResult = null
        _result!!.clear()

        val result = IppResult()
        result.buf = buffer.array()

        // be careful: HTTP and IPP could be transmitted in different set of
        // buffers.
        // see RFC2910, http://www.ietf.org/rfc/rfc2910, page 19
        // read IPP header
        if (buffer.hasRemaining()) {
            _buf = buffer
            result.ippStatusResponse = ippHeader
        }

        _buf = buffer
        // read attribute group list with attributes
        attributeGroupList

        closeAttributeGroup()
        result.attributeGroupList = _result
        return result
    }

    /**
     * concatenate nio-ByteBuffers
     *
     * @param buffers
     * ArrayList<ByteBuffer>
     * @return ByteBuffer
    </ByteBuffer> */
    private fun concatenateBytebuffers(buffers: ArrayList<ByteBuffer>): ByteBuffer {
        var n = 0
        for (b in buffers)
            n += b.remaining()

        val buf = if (n > 0 && buffers[0].isDirect) ByteBuffer.allocateDirect(n) else ByteBuffer.allocate(n)
        if (n > 0)
            buf.order(buffers[0].order())

        for (b in buffers)
            buf.put(b.duplicate())

        buf.flip()
        return buf
    }

    /**
     *
     * @param tag
     */
    private fun setAttributeGroup(tag: Int) {
        _attributeGroupResult?.let {
            _attributeResult?.let { attr -> it.attribute.add(attr) }
            _result?.add(it)
        }
        _attributeResult = null
        _attributeGroupResult = AttributeGroup()
        _attributeGroupResult!!.tagName = getTagName(IppUtil.toHexWithMarker(tag))
    }

    /**
     *
     */
    private fun closeAttributeGroup() {
        _attributeGroupResult?.let {
            _attributeResult?.let { attr -> it.attribute.add(attr) }
            _result?.add(it)
        }
        _attributeResult = null
        _attributeGroupResult = null
    }

    /**
     *
     * @param tag
     */
    private fun setTextAttribute(tag: Int) {
        var length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }

        // set attribute value
        if (!_buf!!.hasRemaining()) {
            return
        }
        length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            val dst = ByteArray(length.toInt())
            _buf!!.get(dst)
            val value = IppUtil.toString(dst)
            val hex = IppUtil.toHexWithMarker(tag)
            val attrValue = AttributeValue()
            attrValue.tag = hex
            val tagName = getTagName(hex)
            attrValue.tagName = tagName
            attrValue.value = value
            _attributeResult!!.attributeValue.add(attrValue)
        }
    }

    /**
     * TODO: natural-language not considered in reporting
     *
     * @param tag
     */
    private fun setTextWithLanguageAttribute(tag: Int) {
        var length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }

        // set natural-language and attribute value
        if (!_buf!!.hasRemaining()) {
            return
        }

        // set tag, tag name, natural-language
        length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            var dst = ByteArray(length.toInt())
            _buf!!.get(dst)
            var value = IppUtil.toString(dst)
            val hex = IppUtil.toHexWithMarker(tag)
            var attrValue = AttributeValue()
            attrValue.tag = hex
            val tagName = getTagName(hex)
            attrValue.tagName = tagName
            attrValue.value = value
            _attributeResult!!.attributeValue.add(attrValue)

            // set value
            length = _buf!!.short
            if (length.toInt() != 0 && _buf!!.remaining() >= length) {
                dst = ByteArray(length.toInt())
                _buf!!.get(dst)
                value = IppUtil.toString(dst)
                attrValue = AttributeValue()
                attrValue.value = value
                _attributeResult!!.attributeValue.add(attrValue)
            }
        }
    }

    /**
     * TODO: natural-language not considered in reporting
     *
     * @param tag
     */
    private fun setNameWithLanguageAttribute(tag: Int) {
        var length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }

        // set natural-language and attribute value
        if (!_buf!!.hasRemaining()) {
            return
        }

        // set tag, tag name, natural-language
        length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            var dst = ByteArray(length.toInt())
            _buf!!.get(dst)
            var value = IppUtil.toString(dst)
            val hex = IppUtil.toHexWithMarker(tag)
            var attrValue = AttributeValue()
            attrValue.tag = hex
            val tagName = getTagName(hex)
            attrValue.tagName = tagName
            attrValue.value = value
            _attributeResult!!.attributeValue.add(attrValue)

            // set value
            length = _buf!!.short
            if (length.toInt() != 0 && _buf!!.remaining() >= length) {
                dst = ByteArray(length.toInt())
                _buf!!.get(dst)
                value = IppUtil.toString(dst)
                attrValue = AttributeValue()
                attrValue.value = value
                _attributeResult!!.attributeValue.add(attrValue)
            }
        }
    }

    /**
     *
     * @param tag
     */
    private fun setBooleanAttribute(tag: Int) {
        var length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }

        // set attribute value
        if (!_buf!!.hasRemaining()) {
            return
        }
        length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            val value = _buf!!.get()
            val hex = IppUtil.toHexWithMarker(tag)
            val attrValue = AttributeValue()
            attrValue.tag = hex
            val tagName = getTagName(hex)
            attrValue.tagName = tagName
            attrValue.value = IppUtil.toBoolean(value)
            _attributeResult!!.attributeValue.add(attrValue)
        }
    }

    /**
     *
     * @param tag
     */
    private fun setDateTimeAttribute(tag: Int) {
        var length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }

        // set attribute value
        if (!_buf!!.hasRemaining()) {
            return
        }
        length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            val dst = ByteArray(length.toInt())
            _buf!!.get(dst, 0, length.toInt())
            val value = IppUtil.toDateTime(dst)
            val hex = IppUtil.toHexWithMarker(tag)
            val attrValue = AttributeValue()
            attrValue.tag = hex
            val tagName = getTagName(hex)
            attrValue.tagName = tagName
            attrValue.value = value
            _attributeResult!!.attributeValue.add(attrValue)
        }
    }

    /**
     *
     * @param tag
     */
    private fun setIntegerAttribute(tag: Int) {
        var length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }
        // set attribute value
        if (!_buf!!.hasRemaining()) {
            return
        }
        length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            val value = _buf!!.int
            val hex = IppUtil.toHexWithMarker(tag)
            val attrValue = AttributeValue()
            attrValue.tag = hex
            val tagName = getTagName(hex)
            attrValue.tagName = tagName
            attrValue.value = Integer.toString(value)
            _attributeResult!!.attributeValue.add(attrValue)
        }
    }

    /**
     *
     */
    private fun setNoValueAttribute() {
        val length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }
    }

    /**
     *
     * @param tag
     */
    private fun setRangeOfIntegerAttribute(tag: Int) {
        var length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }
        // set attribute value
        if (!_buf!!.hasRemaining()) {
            return
        }
        length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            val value1 = _buf!!.int
            val value2 = _buf!!.int
            val hex = IppUtil.toHexWithMarker(tag)
            val attrValue = AttributeValue()
            attrValue.tag = hex
            val tagName = getTagName(hex)
            attrValue.tagName = tagName
            attrValue.value = Integer.toString(value1) + "," + Integer.toString(value2)
            _attributeResult!!.attributeValue.add(attrValue)
        }
    }

    /**
     *
     * @param tag
     */
    private fun setResolutionAttribute(tag: Int) {
        var length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }

        // set attribute value
        if (!_buf!!.hasRemaining()) {
            return
        }
        length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            val value1 = _buf!!.int
            val value2 = _buf!!.int
            val value3 = _buf!!.get()
            val hex = IppUtil.toHexWithMarker(tag)
            val attrValue = AttributeValue()
            attrValue.tag = hex
            val tagName = getTagName(hex)
            attrValue.tagName = tagName
            attrValue.value = Integer.toString(value1) + "," + Integer.toString(value2) + "," + Integer.toString(value3.toInt())
            _attributeResult!!.attributeValue.add(attrValue)
        }
    }

    /**
     *
     * @param tag
     */
    private fun setEnumAttribute(tag: Int) {
        var length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            setAttributeName(length)
        }

        // set attribute value
        if (!_buf!!.hasRemaining()) {
            return
        }

        length = _buf!!.short
        if (length.toInt() != 0 && _buf!!.remaining() >= length) {
            val hex = IppUtil.toHexWithMarker(tag)
            val attrValue = AttributeValue()
            attrValue.tag = hex
            val tagName = getTagName(hex)
            attrValue.tagName = tagName

            val value = _buf!!.int
            if (_attributeResult != null) {
                val enumName = getEnumName(value, _attributeResult!!.name)
                attrValue.value = enumName
            } else {
                _attributeResult = Attribute()
                _attributeResult!!.name = "no attribute name given:"
                attrValue.value = Integer.toString(value)
            }

            _attributeResult!!.attributeValue.add(attrValue)
        }
    }

    /**
     *
     * @param length
     */
    private fun setAttributeName(length: Short) {
        if (length.toInt() == 0 || _buf!!.remaining() < length) {
            return
        }
        val dst = ByteArray(length.toInt())
        _buf!!.get(dst)
        val name = IppUtil.toString(dst)
        _attributeResult?.let { _attributeGroupResult?.attribute?.add(it) }
        _attributeResult = Attribute()
        _attributeResult!!.name = name
    }

    /**
     *
     * @param tag
     * @return
     */
    private fun getTagName(tag: String?): String? {
        if (tag == null) {
            System.err.println("IppResponse.getTagName(): no tag given")
            return null
        }
        val l = IppLists.tagList.size
        for (i in 0 until l) {
            if (tag == IppLists.tagList[i].value) {
                return IppLists.tagList[i].name
            }
        }
        return "no name found for tag:$tag"
    }

    /**
     *
     * @param value
     * @nameOfAttribute
     * @return
     */
    private fun getEnumName(value: Int, nameOfAttribute: String?): String {
        if (nameOfAttribute == null)
            return "Null attribute requested"
        val itemMap = IppLists.enumMap[nameOfAttribute]
                ?: return "Attribute " + nameOfAttribute + "not found"
        return itemMap[value]?.name
                ?: return "Value $value for attribute $nameOfAttribute not found"
    }

    companion object {
        private const val CRLF = "\r\n"
        private const val BYTEBUFFER_CAPACITY = 8192
    }
}
