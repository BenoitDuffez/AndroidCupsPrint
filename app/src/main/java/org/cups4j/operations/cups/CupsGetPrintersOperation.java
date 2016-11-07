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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;

public class CupsGetPrintersOperation extends IppOperation {
    public CupsGetPrintersOperation() {
        operationID = 0x4002;
        bufferSize = 8192;
    }

    public List<CupsPrinter> getPrinters(URL url) throws Exception {
        List<CupsPrinter> printers = new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("requested-attributes",
                "copies-supported page-ranges-supported printer-name printer-info printer-location printer-make-and-model printer-uri-supported");

        IppResult result = request(new URL(url.toString() + "/printers/"), map);

        //     IppResultPrinter.print(result);

        for (AttributeGroup group : result.getAttributeGroupList()) {
            CupsPrinter printer;
            if (group.getTagName().equals("printer-attributes-tag")) {
                String printerURI = null;
                String printerName = null;
                String printerLocation = null;
                String printerDescription = null;
                for (Attribute attr : group.getAttribute()) {
                    switch (attr.getName()) {
                        case "printer-uri-supported":
                            printerURI = attr.getAttributeValue().get(0).getValue().replace("ipp://", url.getProtocol() + "://");
                            break;
                        case "printer-name":
                            printerName = attr.getAttributeValue().get(0).getValue();
                            break;
                        case "printer-location":
                            if (attr.getAttributeValue() != null && attr.getAttributeValue().size() > 0) {
                                printerLocation = attr.getAttributeValue().get(0).getValue();
                            }
                            break;
                        case "printer-info":
                            if (attr.getAttributeValue() != null && attr.getAttributeValue().size() > 0) {
                                printerDescription = attr.getAttributeValue().get(0).getValue();
                            }
                            break;
                    }
                }
                URL printerUrl;
                try {
                    printerUrl = new URL(printerURI);
                } catch (Throwable t) {
                    t.printStackTrace();
                    System.err.println("Error encountered building URL from printer uri of printer " + printerName
                            + ", uri returned was [" + printerURI + "].  Attribute group tag/description: [" + group.getTagName()
                            + "/" + group.getDescription());
                    throw new Exception(t);
                }
                printer = new CupsPrinter(printerUrl, printerName, false);
                printer.setLocation(printerLocation);
                printer.setDescription(printerDescription);
                printers.add(printer);
            }
        }

        return printers;
    }
}
