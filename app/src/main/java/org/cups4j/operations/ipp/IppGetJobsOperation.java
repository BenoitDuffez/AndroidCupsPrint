package org.cups4j.operations.ipp;

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
 * <p>
 * Notice this file has been modified. It is not the original.
 * Job Creation Time added Jon Freeman 2013
 */

/**
 * Notice this file has been modified. It is not the original.
 * Job Creation Time added Jon Freeman 2013
 */

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.JobStateEnum;
import org.cups4j.PrintJobAttributes;
import org.cups4j.WhichJobsEnum;
import org.cups4j.operations.IppOperation;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.ippclient.IppTag;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;

public class IppGetJobsOperation extends IppOperation {
    public IppGetJobsOperation() {
        operationID = 0x000a;
        bufferSize = 8192;
    }

    public ByteBuffer getIppHeader(URL url, Map<String, String> map) throws UnsupportedEncodingException {
        ByteBuffer ippBuf = ByteBuffer.allocateDirect(bufferSize);

        //not sure why next line is here, it overwrites job attributes in map parameter - JF
        //map.put("requested-attributes", "job-name job-id job-state job-originating-user-name job-printer-uri copies");

        ippBuf = IppTag.getOperation(ippBuf, operationID);
        ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(url));

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

        if (map.get("which-jobs") != null) {
            ippBuf = IppTag.getKeyword(ippBuf, "which-jobs", map.get("which-jobs"));
        }

        if (map.get("my-jobs") != null) {
            boolean value = false;
            if (map.get("my-jobs").equals("true")) {
                value = true;
            }
            ippBuf = IppTag.getBoolean(ippBuf, "my-jobs", value);
        }

        ippBuf = IppTag.getEnd(ippBuf);
        if (ippBuf != null) {
            ippBuf.flip();
        }
        return ippBuf;
    }

    public List<PrintJobAttributes> getPrintJobs(CupsPrinter printer, WhichJobsEnum whichJobs, String userName,
                                                 boolean myJobs) throws Exception {
        List<PrintJobAttributes> jobs = new ArrayList<>();
        PrintJobAttributes jobAttributes;
        Map<String, String> map = new HashMap<>();

        if (userName == null)
            userName = CupsClient.DEFAULT_USER;
        map.put("requesting-user-name", userName);
        //
        map.put("which-jobs", whichJobs.getValue());
        if (myJobs) {
            map.put("my-jobs", "true");
        }

        //time-at-creation added JF
        map.put("requested-attributes",
                "page-ranges print-quality sides time-at-creation job-uri job-id job-state job-printer-uri job-name job-originating-user-name");

        IppResult result = request(printer.getPrinterURL(), map);

        String protocol = printer.getPrinterURL().getProtocol() + "://";

        for (AttributeGroup group : result.getAttributeGroupList()) {
            if ("job-attributes-tag".equals(group.getTagName())) {
                jobAttributes = new PrintJobAttributes();
                for (Attribute attr : group.getAttribute()) {
                    if (attr.getAttributeValue() != null && !attr.getAttributeValue().isEmpty()) {
                        String attValue = getAttributeValue(attr);

                        switch (attr.getName()) {
                            case "job-uri":
                                jobAttributes.setJobURL(new URL(attValue.replace("ipp://", protocol)));
                                break;
                            case "job-id":
                                jobAttributes.setJobID(Integer.parseInt(attValue));
                                break;
                            case "job-state":
                                jobAttributes.setJobState(JobStateEnum.fromString(attValue));
                                break;
                            case "job-printer-uri":
                                jobAttributes.setPrinterURL(new URL(attValue.replace("ipp://", protocol)));
                                break;
                            case "job-name":
                                jobAttributes.setJobName(attValue);
                                break;
                            case "job-originating-user-name":
                                jobAttributes.setUserName(attValue);
                                break;
                            case "time-at-creation":
                                long unixTime = Long.parseLong(attValue);
                                Date dt = new Date(unixTime * 1000);
                                jobAttributes.setJobCreateTime(dt);
                                break;
                        }
                    }
                }
                jobs.add(jobAttributes);
            }
        }

        return jobs;
    }
}
