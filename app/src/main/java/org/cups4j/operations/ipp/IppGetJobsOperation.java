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

/**
 * Notice this file has been modified. It is not the original.
 * Job Creation Time added Jon Freeman 2013
 */

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.JobStateEnum;
import org.cups4j.PrintJobAttributes;
import org.cups4j.WhichJobsEnum;
import org.cups4j.operations.IppOperation;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.ippclient.IppTag;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;
import java.util.Date;

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
      if (sta != null) {
        ippBuf = IppTag.getKeyword(ippBuf, "requested-attributes", sta[0]);
        int l = sta.length;
        for (int i = 1; i < l; i++) {
          ippBuf = IppTag.getKeyword(ippBuf, null, sta[i]);
        }
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
    ippBuf.flip();
    return ippBuf;
  }

  public List<PrintJobAttributes> getPrintJobs(CupsPrinter printer, WhichJobsEnum whichJobs, String userName,
      boolean myJobs) throws Exception {
    List<PrintJobAttributes> jobs = new ArrayList<PrintJobAttributes>();
    PrintJobAttributes jobAttributes = null;
    Map<String, String> map = new HashMap<String, String>();

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
            
            if ("job-uri".equals(attr.getName())) {
              jobAttributes.setJobURL(new URL(attValue.replace("ipp://", protocol)));
            } else if ("job-id".equals(attr.getName())) {
              jobAttributes.setJobID(Integer.parseInt(attValue));
            } else if ("job-state".equals(attr.getName())) {
              jobAttributes.setJobState(JobStateEnum.fromString(attValue));
            } else if ("job-printer-uri".equals(attr.getName())) {
              jobAttributes.setPrinterURL(new URL(attValue.replace("ipp://", protocol)));
            } else if ("job-name".equals(attr.getName())) {
              jobAttributes.setJobName(attValue);
            } else if ("job-originating-user-name".equals(attr.getName())) {
              jobAttributes.setUserName(attValue);
            }
            else if ("time-at-creation".equals(attr.getName())){
                long unixTime = Long.parseLong(attValue);
                Date dt = new Date(unixTime * 1000);
                jobAttributes.setJobCreateTime(dt);
            }
          }
        }
        jobs.add(jobAttributes);
      }
    }

    return jobs;
  }

}
