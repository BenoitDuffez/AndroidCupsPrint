package org.cups4j.operations.cups;

/**
 * Copyright (C) 2011 Harald Weyhing
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
import java.util.HashMap;
import java.util.Map;

import org.cups4j.CupsClient;
import org.cups4j.PrintRequestResult;
import org.cups4j.operations.IppOperation;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.ippclient.IppTag;

public class CupsMoveJobOperation extends IppOperation {

  public CupsMoveJobOperation() {
    operationID = 0x400D;
    bufferSize = 8192;
  }

  public ByteBuffer getIppHeader(URL uri, Map<String, String> map) throws UnsupportedEncodingException {
    if (uri == null) {
      System.err.println("CupsMoveJobOperation.getIppHeader(): uri is null");
      return null;
    }

    ByteBuffer ippBuf = ByteBuffer.allocateDirect(bufferSize);
    ippBuf = IppTag.getOperation(ippBuf, operationID);
    // ippBuf = IppTag.getUri(ippBuf, "job-uri", stripPortNumber(url));

    if (map == null) {
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

    ippBuf = IppTag.getUri(ippBuf, "job-printer-uri", map.get("target-printer-uri"));

    ippBuf = IppTag.getEnd(ippBuf);
    ippBuf.flip();
    return ippBuf;
  }

  public boolean moveJob(String hostname, String userName, int jobID, URL targetPrinterURL) throws Exception {
    Map<String, String> map = new HashMap<String, String>();

    if (userName == null) {
      userName = CupsClient.DEFAULT_USER;
    }
    map.put("requesting-user-name", userName);

    URL url = new URL("http://" + hostname + "/jobs/" + Integer.toString(jobID));
    map.put("job-uri", url.toString());

    map.put("target-printer-uri", stripPortNumber(targetPrinterURL));

    IppResult result = request(url, map);
//    IppResultPrinter.print(result);
    return new PrintRequestResult(result).isSuccessfulResult();
  }

}
