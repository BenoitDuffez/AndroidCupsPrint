package ch.ethz.vppserver.ippclient;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

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
 * <http://www.gnu.org/licenses/>.
 */
public class IppTag {
  private final static byte MAJOR_VERSION = 0x01;
  private final static byte MINOR_VERSION = 0x01;

  private final static String ATTRIBUTES_CHARSET = "attributes-charset";
  private final static String ATTRIBUTES_NATURAL_LANGUAGE = "attributes-natural-language";

  private final static String ATTRIBUTES_CHARSET_VALUE = "utf-8";
  private final static String ATTRIBUTES_NATURAL_LANGUAGE_VALUE = "en-us";
  private final static short ATTRIBUTES_INTEGER_VALUE_LENGTH = 0x0004;
  private final static short ATTRIBUTES_RANGE_OF_INT_VALUE_LENGTH = 0x0008;
  private final static short ATTRIBUTES_BOOLEAN_VALUE_LENGTH = 0x0001;
  private final static short ATTRIBUTES_RESOLUTION_VALUE_LENGTH = 0x0009;
  private final static byte ATTRIBUTES_BOOLEAN_FALSE_VALUE = 0x00;
  private final static byte ATTRIBUTES_BOOLEAN_TRUE_VALUE = 0x01;

  private final static byte OPERATION_ATTRIBUTES_TAG = 0x01;
  private final static byte JOB_ATTRIBUTES_TAG = 0x02;
  private final static byte END_OF_ATTRIBUTES_TAG = 0x03;
  private final static byte PRINTER_ATTRIBUTES_TAG = 0x04;
  private final static byte UNSUPPORTED_ATTRIBUTES_TAG = 0x05;
  private final static byte SUBSCRIPTION_ATTRIBUTES_TAG = 0x06;
  private final static byte EVENT_NOTIFICATION_ATTRIBUTES_TAG = 0x07;
  private final static byte INTEGER_TAG = 0x21;
  private final static byte BOOLEAN_TAG = 0x22;
  private final static byte ENUM_TAG = 0x23;
  private final static byte RESOLUTION_TAG = 0x32;
  private final static byte RANGE_OF_INTEGER_TAG = 0x33;
  private final static byte TEXT_WITHOUT_LANGUAGE_TAG = 0x41;
  private final static byte NAME_WITHOUT_LANGUAGE_TAG = 0x42;
  private final static byte KEYWORD_TAG = 0x44;
  private final static byte URI_TAG = 0x45;
  private final static byte URI_SCHEME_TAG = 0x46;
  private final static byte CHARSET_TAG = 0x47;
  private final static byte NATURAL_LANGUAGE_TAG = 0x48;
  private final static byte MIME_MEDIA_TYPE_TAG = 0x49;

  private final static short NULL_LENGTH = 0;

  private static int requestID = 0; // required attribute within operations (will increase with
                                    // every request)

  /**
   * 
   * @param ippBuf
   * @param operation
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getOperation(ByteBuffer ippBuf, short operation) throws UnsupportedEncodingException {
    return getOperation(ippBuf, operation, null, null);
  }

  /**
   * 
   * @param ippBuf
   * @param operation
   * @param charset
   * @param naturalLanguage
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getOperation(ByteBuffer ippBuf, short operation, String charset, String naturalLanguage)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getOperation(): ippBuf is null");
      return null;
    }
    if (charset == null) {
      charset = ATTRIBUTES_CHARSET_VALUE;
    }
    if (naturalLanguage == null) {
      naturalLanguage = ATTRIBUTES_NATURAL_LANGUAGE_VALUE;
    }
    ippBuf.put(MAJOR_VERSION);
    ippBuf.put(MINOR_VERSION);
    ippBuf.putShort(operation);
    ippBuf.putInt(++requestID);
    ippBuf.put(OPERATION_ATTRIBUTES_TAG);

    ippBuf = getCharset(ippBuf, ATTRIBUTES_CHARSET, charset);
    ippBuf = getNaturalLanguage(ippBuf, ATTRIBUTES_NATURAL_LANGUAGE, naturalLanguage);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   */
  public static ByteBuffer getOperationAttributesTag(ByteBuffer ippBuf) {
    if (ippBuf == null) {
      System.err.println("IppTag.getOperationAttributesTag(): ippBuf is null");
      return null;
    }
    ippBuf.put(OPERATION_ATTRIBUTES_TAG);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   */
  public static ByteBuffer getJobAttributesTag(ByteBuffer ippBuf) {
    if (ippBuf == null) {
      System.err.println("IppTag.getJobAttributesTag(): ippBuf is null");
      return null;
    }
    ippBuf.put(JOB_ATTRIBUTES_TAG);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   */
  public static ByteBuffer getSubscriptionAttributesTag(ByteBuffer ippBuf) {
    if (ippBuf == null) {
      System.err.println("IppTag.getSubscriptionAttributesTag(): ippBuf is null");
      return null;
    }
    ippBuf.put(SUBSCRIPTION_ATTRIBUTES_TAG);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   */
  public static ByteBuffer getEventNotificationAttributesTag(ByteBuffer ippBuf) {
    if (ippBuf == null) {
      System.err.println("IppTag.getEventNotificationAttributesTag(): ippBuf is null");
      return null;
    }
    ippBuf.put(EVENT_NOTIFICATION_ATTRIBUTES_TAG);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   */
  public static ByteBuffer getUnsupportedAttributesTag(ByteBuffer ippBuf) {
    if (ippBuf == null) {
      System.err.println("IppTag.getUnsupportedAttributesTag(): ippBuf is null");
      return null;
    }
    ippBuf.put(UNSUPPORTED_ATTRIBUTES_TAG);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   */
  public static ByteBuffer getPrinterAttributesTag(ByteBuffer ippBuf) {
    if (ippBuf == null) {
      System.err.println("IppTag.getPrinterAttributesTag(): ippBuf is null");
      return null;
    }
    ippBuf.put(PRINTER_ATTRIBUTES_TAG);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getCharset(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getCharset(ippBuf, null, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getCharset(ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
    return getCharset(ippBuf, attributeName, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getCharset(ByteBuffer ippBuf, String attributeName, String value)
      throws UnsupportedEncodingException {
    return getUsAscii(ippBuf, CHARSET_TAG, attributeName, value);
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getNaturalLanguage(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getNaturalLanguage(ippBuf, null, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getNaturalLanguage(ByteBuffer ippBuf, String attributeName)
      throws UnsupportedEncodingException {
    return getNaturalLanguage(ippBuf, attributeName, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getNaturalLanguage(ByteBuffer ippBuf, String attributeName, String value)
      throws UnsupportedEncodingException {
    return getUsAscii(ippBuf, NATURAL_LANGUAGE_TAG, attributeName, value);
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getUri(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getUri(ippBuf, null, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getUri(ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
    return getUri(ippBuf, attributeName, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getUri(ByteBuffer ippBuf, String attributeName, String value)
      throws UnsupportedEncodingException {
    return getUsAscii(ippBuf, URI_TAG, attributeName, value);
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getUriScheme(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getUriScheme(ippBuf, null, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getUriScheme(ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
    return getUriScheme(ippBuf, attributeName, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getUriScheme(ByteBuffer ippBuf, String attributeName, String value)
      throws UnsupportedEncodingException {
    return getUsAscii(ippBuf, URI_SCHEME_TAG, attributeName, value);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getNameWithoutLanguage(ByteBuffer ippBuf, String attributeName, String value)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getNameWithoutLanguage(): ippBuf is null");
      return null;
    }

    ippBuf.put(NAME_WITHOUT_LANGUAGE_TAG);

    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }

    if (value != null) {
      ippBuf.putShort((short) value.length());
      ippBuf.put(IppUtil.toBytes(value));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }
    return ippBuf;
  }
  
  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getTextWithoutLanguage(ByteBuffer ippBuf, String attributeName, String value)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getTextWithoutLanguage(): ippBuf is null");
      return null;
    }

    ippBuf.put(TEXT_WITHOUT_LANGUAGE_TAG);

    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }

    if (value != null) {
      ippBuf.putShort((short) value.length());
      ippBuf.put(IppUtil.toBytes(value));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }
    return ippBuf;
  }

  /**
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getInteger(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getInteger(ippBuf, null);
  }

  /**
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getInteger(ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getInteger(): ippBuf is null");
      return null;
    }

    ippBuf.put(INTEGER_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }

    ippBuf.putShort(NULL_LENGTH);

    return ippBuf;
  }

  /**
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getInteger(ByteBuffer ippBuf, String attributeName, int value)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getInteger(): ippBuf is null");
      return null;
    }

    ippBuf.put(INTEGER_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }

    ippBuf.putShort(ATTRIBUTES_INTEGER_VALUE_LENGTH);
    ippBuf.putInt(value);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getBoolean(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getBoolean(ippBuf, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getBoolean(ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getBoolean(): ippBuf is null");
      return null;
    }

    ippBuf.put(BOOLEAN_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }

    ippBuf.putShort(NULL_LENGTH);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getBoolean(ByteBuffer ippBuf, String attributeName, boolean value)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getBoolean(): ippBuf is null");
      return null;
    }

    ippBuf.put(BOOLEAN_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }

    ippBuf.putShort(ATTRIBUTES_BOOLEAN_VALUE_LENGTH);
    if (value == true) {
      ippBuf.put(ATTRIBUTES_BOOLEAN_TRUE_VALUE);
    } else {
      ippBuf.put(ATTRIBUTES_BOOLEAN_FALSE_VALUE);
    }
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getEnum(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getEnum(ippBuf, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getEnum(ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getEnum(): ippBuf is null");
      return null;
    }

    ippBuf.put(ENUM_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }
    ippBuf.putShort(NULL_LENGTH);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getEnum(ByteBuffer ippBuf, String attributeName, int value)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getEnum(): ippBuf is null");
      return null;
    }

    ippBuf.put(ENUM_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }
    ippBuf.putShort(ATTRIBUTES_INTEGER_VALUE_LENGTH);
    ippBuf.putInt(value);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getResolution(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getResolution(ippBuf, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getResolution(ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getResolution(): ippBuf is null");
      return null;
    }

    ippBuf.put(RESOLUTION_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }
    ippBuf.putShort(NULL_LENGTH);
    return ippBuf;
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
  public static ByteBuffer getResolution(ByteBuffer ippBuf, String attributeName, int value1, int value2, byte value3)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getResolution(): ippBuf is null");
      return null;
    }

    ippBuf.put(RESOLUTION_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }
    ippBuf.putShort(ATTRIBUTES_RESOLUTION_VALUE_LENGTH);
    ippBuf.putInt(value1);
    ippBuf.putInt(value2);
    ippBuf.put(value3);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getRangeOfInteger(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getRangeOfInteger(ippBuf, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getRangeOfInteger(ByteBuffer ippBuf, String attributeName)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getRangeOfInteger(): ippBuf is null");
      return null;
    }

    ippBuf.put(RANGE_OF_INTEGER_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }
    ippBuf.putShort(NULL_LENGTH);
    return ippBuf;
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
  public static ByteBuffer getRangeOfInteger(ByteBuffer ippBuf, String attributeName, int value1, int value2)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getRangeOfInteger(): ippBuf is null");
      return null;
    }

    ippBuf.put(RANGE_OF_INTEGER_TAG);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }
    ippBuf.putShort(ATTRIBUTES_RANGE_OF_INT_VALUE_LENGTH);
    ippBuf.putInt(value1);
    ippBuf.putInt(value2);
    return ippBuf;
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getMimeMediaType(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getMimeMediaType(ippBuf, null, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getMimeMediaType(ByteBuffer ippBuf, String attributeName)
      throws UnsupportedEncodingException {
    return getMimeMediaType(ippBuf, attributeName, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getMimeMediaType(ByteBuffer ippBuf, String attributeName, String value)
      throws UnsupportedEncodingException {
    return getUsAscii(ippBuf, MIME_MEDIA_TYPE_TAG, attributeName, value);
  }

  /**
   * 
   * @param ippBuf
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getKeyword(ByteBuffer ippBuf) throws UnsupportedEncodingException {
    return getKeyword(ippBuf, null, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getKeyword(ByteBuffer ippBuf, String attributeName) throws UnsupportedEncodingException {
    return getKeyword(ippBuf, attributeName, null);
  }

  /**
   * 
   * @param ippBuf
   * @param attributeName
   * @param value
   * @return
   * @throws UnsupportedEncodingException
   */
  public static ByteBuffer getKeyword(ByteBuffer ippBuf, String attributeName, String value)
      throws UnsupportedEncodingException {
    return getUsAscii(ippBuf, KEYWORD_TAG, attributeName, value);
  }

  /**
   * 
   * @param ippBuf
   * @return
   */
  public static ByteBuffer getEnd(ByteBuffer ippBuf) {
    if (ippBuf == null) {
      System.err.println("IppTag.getEnd(): ippBuf is null");
      return null;
    }
    ippBuf.put(END_OF_ATTRIBUTES_TAG);
    return ippBuf;
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
  private static ByteBuffer getUsAscii(ByteBuffer ippBuf, byte tag, String attributeName, String value)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppTag.getUsAscii(): ippBuf is null");
      return null;
    }

    ippBuf.put(tag);
    if (attributeName != null) {
      ippBuf.putShort((short) attributeName.length());
      ippBuf.put(IppUtil.toBytes(attributeName));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }

    if (value != null) {
      ippBuf.putShort((short) value.length());
      ippBuf.put(IppUtil.toBytes(value));
    } else {
      ippBuf.putShort(NULL_LENGTH);
    }
    return ippBuf;
  }
}