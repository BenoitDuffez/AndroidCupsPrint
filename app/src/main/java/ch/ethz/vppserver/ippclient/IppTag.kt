package ch.ethz.vppserver.ippclient

import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer

/**
 * Copyright (C) 2008 ITS of ETH Zurich, Switzerland, Sarah Windler Burri
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program; if not, see
 * <http:></http:>//www.gnu.org/licenses/>.
 */
object IppTag {
    private const val MAJOR_VERSION: Byte = 0x01
    private const val MINOR_VERSION: Byte = 0x01

    private const val ATTRIBUTES_CHARSET = "attributes-charset"
    private const val ATTRIBUTES_NATURAL_LANGUAGE = "attributes-natural-language"

    private const val ATTRIBUTES_CHARSET_VALUE = "utf-8"
    private const val ATTRIBUTES_NATURAL_LANGUAGE_VALUE = "en-us"
    private const val ATTRIBUTES_INTEGER_VALUE_LENGTH: Short = 0x0004
    private const val ATTRIBUTES_RANGE_OF_INT_VALUE_LENGTH: Short = 0x0008
    private const val ATTRIBUTES_BOOLEAN_VALUE_LENGTH: Short = 0x0001
    private const val ATTRIBUTES_RESOLUTION_VALUE_LENGTH: Short = 0x0009
    private const val ATTRIBUTES_BOOLEAN_FALSE_VALUE: Byte = 0x00
    private const val ATTRIBUTES_BOOLEAN_TRUE_VALUE: Byte = 0x01

    private const val OPERATION_ATTRIBUTES_TAG: Byte = 0x01
    private const val JOB_ATTRIBUTES_TAG: Byte = 0x02
    private const val END_OF_ATTRIBUTES_TAG: Byte = 0x03
    private const val PRINTER_ATTRIBUTES_TAG: Byte = 0x04
    private const val UNSUPPORTED_ATTRIBUTES_TAG: Byte = 0x05
    private const val SUBSCRIPTION_ATTRIBUTES_TAG: Byte = 0x06
    private const val EVENT_NOTIFICATION_ATTRIBUTES_TAG: Byte = 0x07
    private const val INTEGER_TAG: Byte = 0x21
    private const val BOOLEAN_TAG: Byte = 0x22
    private const val ENUM_TAG: Byte = 0x23
    private const val RESOLUTION_TAG: Byte = 0x32
    private const val RANGE_OF_INTEGER_TAG: Byte = 0x33
    private const val TEXT_WITHOUT_LANGUAGE_TAG: Byte = 0x41
    private const val NAME_WITHOUT_LANGUAGE_TAG: Byte = 0x42
    private const val KEYWORD_TAG: Byte = 0x44
    private const val URI_TAG: Byte = 0x45
    private const val URI_SCHEME_TAG: Byte = 0x46
    private const val CHARSET_TAG: Byte = 0x47
    private const val NATURAL_LANGUAGE_TAG: Byte = 0x48
    private const val MIME_MEDIA_TYPE_TAG: Byte = 0x49

    private const val NULL_LENGTH: Short = 0

    private var requestID = 0 // required attribute within operations (will increase with

    /**
     *
     * @param ippBuf
     * @param operation
     * @param charset
     * @param naturalLanguage
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getOperation(ippBuf: ByteBuffer, operation: Short, charset: String? = null, naturalLanguage: String? = null): ByteBuffer {
        ippBuf.put(MAJOR_VERSION)
        ippBuf.put(MINOR_VERSION)
        ippBuf.putShort(operation)
        ippBuf.putInt(++requestID)
        ippBuf.put(OPERATION_ATTRIBUTES_TAG)

        val outputIppBuf = getCharset(ippBuf, ATTRIBUTES_CHARSET, charset ?: ATTRIBUTES_CHARSET_VALUE)
        return getNaturalLanguage(outputIppBuf, ATTRIBUTES_NATURAL_LANGUAGE, naturalLanguage ?: ATTRIBUTES_NATURAL_LANGUAGE_VALUE)
    }

    /**
     *
     * @param ippBuf
     * @return
     */
    fun getOperationAttributesTag(ippBuf: ByteBuffer): ByteBuffer {
        ippBuf.put(OPERATION_ATTRIBUTES_TAG)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @return
     */
    fun getJobAttributesTag(ippBuf: ByteBuffer): ByteBuffer {
        ippBuf.put(JOB_ATTRIBUTES_TAG)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @return
     */
    fun getSubscriptionAttributesTag(ippBuf: ByteBuffer): ByteBuffer {
        ippBuf.put(SUBSCRIPTION_ATTRIBUTES_TAG)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @return
     */
    fun getEventNotificationAttributesTag(ippBuf: ByteBuffer): ByteBuffer {
        ippBuf.put(EVENT_NOTIFICATION_ATTRIBUTES_TAG)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @return
     */
    fun getUnsupportedAttributesTag(ippBuf: ByteBuffer): ByteBuffer {
        ippBuf.put(UNSUPPORTED_ATTRIBUTES_TAG)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @return
     */
    fun getPrinterAttributesTag(ippBuf: ByteBuffer): ByteBuffer {
        ippBuf.put(PRINTER_ATTRIBUTES_TAG)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getCharset(ippBuf: ByteBuffer, attributeName: String? = null, value: String? = null): ByteBuffer {
        return getUsAscii(ippBuf, CHARSET_TAG, attributeName, value)
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getNaturalLanguage(ippBuf: ByteBuffer, attributeName: String? = null, value: String? = null): ByteBuffer {
        return getUsAscii(ippBuf, NATURAL_LANGUAGE_TAG, attributeName, value)
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getUri(ippBuf: ByteBuffer, attributeName: String? = null, value: String? = null): ByteBuffer {
        return getUsAscii(ippBuf, URI_TAG, attributeName, value)
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getUriScheme(ippBuf: ByteBuffer, attributeName: String? = null, value: String? = null): ByteBuffer {
        return getUsAscii(ippBuf, URI_SCHEME_TAG, attributeName, value)
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getNameWithoutLanguage(ippBuf: ByteBuffer, attributeName: String?, value: String?): ByteBuffer {
        ippBuf.put(NAME_WITHOUT_LANGUAGE_TAG)

        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }

        if (value != null) {
            ippBuf.putShort(value.length.toShort())
            ippBuf.put(IppUtil.toBytes(value))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getTextWithoutLanguage(ippBuf: ByteBuffer, attributeName: String?, value: String?): ByteBuffer {
        ippBuf.put(TEXT_WITHOUT_LANGUAGE_TAG)

        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }

        if (value != null) {
            ippBuf.putShort(value.length.toShort())
            ippBuf.put(IppUtil.toBytes(value))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }
        return ippBuf
    }

    /**
     * @param ippBuf
     * @param attributeName
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getInteger(ippBuf: ByteBuffer, attributeName: String? = null): ByteBuffer {
        ippBuf.put(INTEGER_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }

        ippBuf.putShort(NULL_LENGTH)

        return ippBuf
    }

    /**
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getInteger(ippBuf: ByteBuffer, attributeName: String?, value: Int): ByteBuffer {
        ippBuf.put(INTEGER_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }

        ippBuf.putShort(ATTRIBUTES_INTEGER_VALUE_LENGTH)
        ippBuf.putInt(value)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getBoolean(ippBuf: ByteBuffer, attributeName: String? = null): ByteBuffer {
        ippBuf.put(BOOLEAN_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }

        ippBuf.putShort(NULL_LENGTH)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getBoolean(ippBuf: ByteBuffer, attributeName: String?, value: Boolean): ByteBuffer {
        ippBuf.put(BOOLEAN_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }

        ippBuf.putShort(ATTRIBUTES_BOOLEAN_VALUE_LENGTH)
        if (value) {
            ippBuf.put(ATTRIBUTES_BOOLEAN_TRUE_VALUE)
        } else {
            ippBuf.put(ATTRIBUTES_BOOLEAN_FALSE_VALUE)
        }
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getEnum(ippBuf: ByteBuffer, attributeName: String? = null): ByteBuffer {
        ippBuf.put(ENUM_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }
        ippBuf.putShort(NULL_LENGTH)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getEnum(ippBuf: ByteBuffer, attributeName: String?, value: Int): ByteBuffer {
        ippBuf.put(ENUM_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }
        ippBuf.putShort(ATTRIBUTES_INTEGER_VALUE_LENGTH)
        ippBuf.putInt(value)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getResolution(ippBuf: ByteBuffer, attributeName: String? = null): ByteBuffer{
        ippBuf.put(RESOLUTION_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }
        ippBuf.putShort(NULL_LENGTH)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value1
     * @param value2
     * @param value3
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getResolution(ippBuf: ByteBuffer, attributeName: String?, value1: Int, value2: Int, value3: Byte): ByteBuffer {
        ippBuf.put(RESOLUTION_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }
        ippBuf.putShort(ATTRIBUTES_RESOLUTION_VALUE_LENGTH)
        ippBuf.putInt(value1)
        ippBuf.putInt(value2)
        ippBuf.put(value3)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getRangeOfInteger(ippBuf: ByteBuffer, attributeName: String? = null): ByteBuffer {
        ippBuf.put(RANGE_OF_INTEGER_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }
        ippBuf.putShort(NULL_LENGTH)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value1
     * @param value2
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getRangeOfInteger(ippBuf: ByteBuffer, attributeName: String?, value1: Int, value2: Int): ByteBuffer {
        ippBuf.put(RANGE_OF_INTEGER_TAG)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }
        ippBuf.putShort(ATTRIBUTES_RANGE_OF_INT_VALUE_LENGTH)
        ippBuf.putInt(value1)
        ippBuf.putInt(value2)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getMimeMediaType(ippBuf: ByteBuffer, attributeName: String? = null, value: String? = null): ByteBuffer {
        return getUsAscii(ippBuf, MIME_MEDIA_TYPE_TAG, attributeName, value)
    }

    /**
     *
     * @param ippBuf
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmOverloads
    fun getKeyword(ippBuf: ByteBuffer, attributeName: String? = null, value: String? = null): ByteBuffer {
        return getUsAscii(ippBuf, KEYWORD_TAG, attributeName, value)
    }

    /**
     *
     * @param ippBuf
     * @return
     */
    fun getEnd(ippBuf: ByteBuffer): ByteBuffer {
        ippBuf.put(END_OF_ATTRIBUTES_TAG)
        return ippBuf
    }

    /**
     *
     * @param ippBuf
     * @param tag
     * @param attributeName
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    private fun getUsAscii(ippBuf: ByteBuffer, tag: Byte, attributeName: String?, value: String?): ByteBuffer {
        ippBuf.put(tag)
        if (attributeName != null) {
            ippBuf.putShort(attributeName.length.toShort())
            ippBuf.put(IppUtil.toBytes(attributeName))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }

        if (value != null) {
            ippBuf.putShort(value.length.toShort())
            ippBuf.put(IppUtil.toBytes(value))
        } else {
            ippBuf.putShort(NULL_LENGTH)
        }
        return ippBuf
    }
}
