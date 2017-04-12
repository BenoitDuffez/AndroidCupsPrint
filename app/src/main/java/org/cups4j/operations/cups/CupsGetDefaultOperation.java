package org.cups4j.operations.cups;

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

import org.cups4j.CupsPrinter;
import org.cups4j.operations.IppOperation;

import java.net.URL;
import java.util.HashMap;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;

public class CupsGetDefaultOperation extends IppOperation {
    public CupsGetDefaultOperation() {
        operationID = 0x4001;
        bufferSize = 8192;
    }

    public CupsPrinter getDefaultPrinter(URL url, String path) throws Exception {
        CupsPrinter defaultPrinter = null;
        CupsGetDefaultOperation command = new CupsGetDefaultOperation();

        HashMap<String, String> map = new HashMap<>();
        map.put("requested-attributes", "printer-name printer-uri-supported printer-location");

        IppResult result = command.request(new URL(url.toString() + path), map);
        for (AttributeGroup group : result.getAttributeGroupList()) {
            if (group.getTagName().equals("printer-attributes-tag")) {
                String printerURL = null;
                String printerName = null;
                String location = null;
                for (Attribute attr : group.getAttribute()) {
                    switch (attr.getName()) {
                        case "printer-uri-supported":
                            printerURL = attr.getAttributeValue().get(0).getValue().replaceAll("ipps?://", url.getProtocol() + "://");
                            break;
                        case "printer-name":
                            printerName = attr.getAttributeValue().get(0).getValue();
                            break;
                        case "printer-location":
                            if (attr.getAttributeValue() != null && attr.getAttributeValue().size() > 0) {
                                location = attr.getAttributeValue().get(0).getValue();
                            }
                            break;
                    }
                }
                defaultPrinter = new CupsPrinter(new URL(printerURL), printerName, true);
                defaultPrinter.setLocation(location);
            }
        }

        return defaultPrinter;
    }
}
