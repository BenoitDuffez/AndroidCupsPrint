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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.cups4j.JobStateEnum;
import org.cups4j.PrintJobAttributes;
import org.cups4j.operations.IppOperation;
import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.ippclient.IppTag;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;

public class IppGetJobAttributesOperation extends IppOperation {

  public IppGetJobAttributesOperation() {
    operationID = 0x0009;
    bufferSize = 8192;
  }

  public ByteBuffer getIppHeader(URL uri, Map<String, String> map) throws UnsupportedEncodingException {
    ByteBuffer ippBuf = ByteBuffer.allocateDirect(bufferSize);
    ippBuf = IppTag.getOperation(ippBuf, operationID);

    if (map == null) {
      ippBuf = IppTag.getUri(ippBuf, "job-uri", stripPortNumber(uri));
      ippBuf = IppTag.getEnd(ippBuf);
      ippBuf.flip();
      return ippBuf;
    }

    if (map.get("job-id") == null) {
      ippBuf = IppTag.getUri(ippBuf, "job-uri", stripPortNumber(uri));
    } else {
      ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(uri));
      int jobId = Integer.parseInt(map.get("job-id"));
      ippBuf = IppTag.getInteger(ippBuf, "job-id", jobId);
    }

    ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map.get("requesting-user-name"));

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

  public PrintJobAttributes getPrintJobAttributes(URL url, String userName, int jobID)
      throws Exception {
    PrintJobAttributes job = null;

    Map<String, String> map = new HashMap<String, String>();
//    map.put("requested-attributes",
//        "page-ranges print-quality sides job-uri job-id job-state job-printer-uri job-name job-originating-user-name job-k-octets time-at-creation time-at-processing time-at-completed job-media-sheets-completed");
   
    map.put("requested-attributes","all");
    map.put("requesting-user-name", userName);
    IppResult result = request(new URL(url.toString() + "/jobs/" + jobID), map);

    // IppResultPrinter.print(result);
    job = new PrintJobAttributes();
    for (AttributeGroup group : result.getAttributeGroupList()) {
      if ("job-attributes-tag".equals(group.getTagName())||"unassigned".equals(group.getTagName())) {
        for (Attribute attr : group.getAttribute()) {
          if (attr.getAttributeValue() != null && !attr.getAttributeValue().isEmpty()) {
            String attValue = getAttributeValue(attr);
            
            if ("job-uri".equals(attr.getName())) {
              job.setJobURL(new URL(attValue.replace("ipp://", "http://")));
            } else if ("job-id".equals(attr.getName())) {
              job.setJobID(Integer.parseInt(attValue));
            } else if ("job-state".equals(attr.getName())) {
              System.out.println("job-state "+ attValue);
              job.setJobState(JobStateEnum.fromString(attValue));
            } else if ("job-printer-uri".equals(attr.getName())) {
              job.setPrinterURL(new URL(attValue.replace("ipp://", "http://")));
            } else if ("job-name".equals(attr.getName())) {
              job.setJobName(attValue);
            } else if ("job-originating-user-name".equals(attr.getName())) {
              job.setUserName(attValue);
            } else if ("job-k-octets".equals(attr.getName())){
              job.setSize(Integer.parseInt(attValue));
            } else if ("time-at-creation".equals(attr.getName())){
              job.setJobCreateTime(new Date(1000*Long.parseLong(attValue)));
            } else if ("time-at-completed".equals(attr.getName())){
              job.setJobCompleteTime(new Date(1000*Long.parseLong(attValue)));
            } else if ("job-media-sheets-completed".equals(attr.getName())){
              job.setPagesPrinted(Integer.parseInt(attValue));
            }
          }
        }
      }
    }
    
//    IppResultPrinter.print(result);
    return job;
  }

}
