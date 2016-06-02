package org.cups4j.operations.ipp;

/**
 * Copyright (C) 2009 Harald Weyhing
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

/*Notice
 * This file has been modified. It is not the original. 
 * Jon Freeman - 2013
 */

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

import org.cups4j.operations.IppOperation;

import ch.ethz.vppserver.ippclient.IppTag;

public class IppPrintJobOperation extends IppOperation {

  public IppPrintJobOperation() {
    operationID = 0x0002;
    bufferSize = 8192;
  }

  public ByteBuffer getIppHeader(URL url, Map<String, String> map) throws UnsupportedEncodingException {
    if (url == null) {
      System.err.println("IppPrintJobOperation.getIppHeader(): uri is null");
      return null;
    }

    ByteBuffer ippBuf = ByteBuffer.allocateDirect(bufferSize);
    ippBuf = IppTag.getOperation(ippBuf, operationID);
    ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(url));

    if (map == null) {
      ippBuf = IppTag.getEnd(ippBuf);
      ippBuf.flip();
      return ippBuf;
    }

    ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map.get("requesting-user-name"));

    if (map.get("job-name") != null) {
      ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "job-name", map.get("job-name"));
    }

    if (map.get("ipp-attribute-fidelity") != null) {
      boolean value = false;
      if (map.get("ipp-attribute-fidelity").equals("true")) {
        value = true;
      }
      ippBuf = IppTag.getBoolean(ippBuf, "ipp-attribute-fidelity", value);
    }

    if (map.get("document-name") != null) {
      ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "document-name", map.get("document-name"));
    }

    if (map.get("compression") != null) {
      ippBuf = IppTag.getKeyword(ippBuf, "compression", map.get("compression"));
    }

    if (map.get("document-format") != null) {
      ippBuf = IppTag.getMimeMediaType(ippBuf, "document-format", map.get("document-format"));
    }

    if (map.get("document-natural-language") != null) {
      ippBuf = IppTag.getNaturalLanguage(ippBuf, "document-natural-language", map.get("document-natural-language"));
    }

    if (map.get("job-k-octets") != null) {
      int value = Integer.parseInt(map.get("job-k-octets"));
      ippBuf = IppTag.getInteger(ippBuf, "job-k-octets", value);
    }

    if (map.get("job-impressions") != null) {
      int value = Integer.parseInt(map.get("job-impressions"));
      ippBuf = IppTag.getInteger(ippBuf, "job-impressions", value);
    }

    if (map.get("job-media-sheets") != null) {
      int value = Integer.parseInt(map.get("job-media-sheets"));
      ippBuf = IppTag.getInteger(ippBuf, "job-media-sheets", value);
    }

    if (map.get("job-attributes") != null) {
      String[] attributeBlocks = map.get("job-attributes").split("#");
      ippBuf = getJobAttributes(ippBuf, attributeBlocks);
    }

    ippBuf = IppTag.getEnd(ippBuf);
    ippBuf.flip();
    return ippBuf;
  }

  /**
   * TODO: not all possibilities implemented
   * 
   * @param ippBuf
   * @param attributeBlocks
   * @return
   * @throws UnsupportedEncodingException
   */
  private static ByteBuffer getJobAttributes(ByteBuffer ippBuf, String[] attributeBlocks)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      System.err.println("IppPrintJobOperation.getJobAttributes(): ippBuf is null");
      return null;
    }
    if (attributeBlocks == null) {
      return ippBuf;
    }

    ippBuf = IppTag.getJobAttributesTag(ippBuf);

    int l = attributeBlocks.length;
    for (int i = 0; i < l; i++) {
      String[] attr = attributeBlocks[i].split(":");
      if ((attr == null) || (attr.length != 3)) {
        return ippBuf;
      }
      String name = attr[0];
      String tagName = attr[1];
      String value = attr[2];

      if (tagName.equals("boolean")) {
        if (value.equals("true")) {
          ippBuf = IppTag.getBoolean(ippBuf, name, true);
        } else {
          ippBuf = IppTag.getBoolean(ippBuf, name, false);
        }
      } else if (tagName.equals("integer")) {
        ippBuf = IppTag.getInteger(ippBuf, name, Integer.parseInt(value));
      } else if (tagName.equals("rangeOfInteger")) {
        String[] range = value.split("-");
        int low = Integer.parseInt(range[0]);
        int high = Integer.parseInt(range[1]);
        ippBuf = IppTag.getRangeOfInteger(ippBuf, name, low, high);
      } else if (tagName.equals("setOfRangeOfInteger")) {
        String ranges[] = value.split(",");

        for (String range : ranges) {
          range = range.trim();
          String[] values = range.split("-");

          int value1 = Integer.parseInt(values[0]);
          int value2 = value1;
          // two values provided?
          if (values.length == 2) {
            value2 = Integer.parseInt(values[1]);
          }

          // first attribute value needs name, additional values need to get the "null" name
          ippBuf = IppTag.getRangeOfInteger(ippBuf, name, value1, value2);
          name = null;
        }
      } else if (tagName.equals("keyword")) {
        ippBuf = IppTag.getKeyword(ippBuf, name, value);
      } else if (tagName.equals("name")) {
        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, name, value);
      } else if (tagName.equals("enum")) {
        ippBuf = IppTag.getEnum(ippBuf, name, Integer.parseInt(value));
      } else if (tagName.equals("resolution")) {
        String[] resolution = value.split(",");
        int value1 = Integer.parseInt(resolution[0]);
        int value2 = Integer.parseInt(resolution[1]);
        byte value3 = Byte.valueOf(resolution[2]);
        ippBuf = IppTag.getResolution(ippBuf, name, value1, value2, value3);
      }
    }
    return ippBuf;
  }

}
