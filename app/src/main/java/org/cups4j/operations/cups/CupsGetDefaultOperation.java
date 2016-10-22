package org.cups4j.operations.cups;

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

import java.net.URL;
import java.util.HashMap;

import org.cups4j.CupsPrinter;
import org.cups4j.operations.IppOperation;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;

public class CupsGetDefaultOperation extends IppOperation {
  public CupsGetDefaultOperation() {
    operationID = 0x4001;
    bufferSize = 8192;
  }

  public CupsPrinter getDefaultPrinter(URL url) throws Exception {
    CupsPrinter defaultPrinter = null;
    CupsGetDefaultOperation command = new CupsGetDefaultOperation();

    HashMap<String, String> map = new HashMap<String, String>();
    map.put("requested-attributes", "printer-name printer-uri-supported printer-location");

    IppResult result = command.request(new URL(url.toString() + "/printers"), map);
    for (AttributeGroup group : result.getAttributeGroupList()) {
      if (group.getTagName().equals("printer-attributes-tag")) {
        String printerURL = null;
        String printerName = null;
        String location = null;
        for (Attribute attr : group.getAttribute()) {
          if (attr.getName().equals("printer-uri-supported")) {
            printerURL = attr.getAttributeValue().get(0).getValue().replace("ipp://", "http://");
          } else if (attr.getName().equals("printer-name")) {
            printerName = attr.getAttributeValue().get(0).getValue();
          } else if (attr.getName().equals("printer-location")) {
            if (attr.getAttributeValue() != null && attr.getAttributeValue().size() > 0) {
              location = attr.getAttributeValue().get(0).getValue();
            }
          }
        }
        defaultPrinter = new CupsPrinter(new URL(printerURL), printerName, true);
        defaultPrinter.setLocation(location);
      }
    }

    return defaultPrinter;
  }
}
