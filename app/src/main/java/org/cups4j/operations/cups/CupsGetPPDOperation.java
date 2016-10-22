package org.cups4j.operations.cups;
/**
 * @author Frank Carnevale
/ *

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either version 3 of the License, or (at your option) any
later version.
 
This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE.
 
See the GNU Lesser General Public License for more details. You should have
received a copy of the GNU Lesser General Public License along with this
program; if not, see <http://www.gnu.org/licenses/>.
*/

/*Notice. This file is not part of the original cups4j. It is an implementaion
 * of a patch to cups4j suggested by Frank Carnevale
 */

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.cups4j.CupsClient;
import org.cups4j.operations.IppOperation;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.ippclient.IppTag;

public class CupsGetPPDOperation extends IppOperation {

  public CupsGetPPDOperation() {
    operationID = 0x400F;
    bufferSize = 8192;
  }

  public ByteBuffer getIppHeader(URL uri, Map<String, String> map) throws UnsupportedEncodingException {
    if (uri == null) {
      System.err.println("IppGetPPDOperation.getIppHeader(): uri is null");
      return null;
    }

    ByteBuffer ippBuf = ByteBuffer.allocateDirect(bufferSize);
    ippBuf = IppTag.getOperation(ippBuf, operationID);

    if (map == null) {
      ippBuf = IppTag.getEnd(ippBuf);
      ippBuf.flip();
      return ippBuf;
    }

    ippBuf = IppTag.getUri(ippBuf, "printer-uri", map.get("printer-uri"));
    ippBuf = IppTag.getEnd(ippBuf);
    ippBuf.flip();
    return ippBuf;
  }


  public String getPPDFile(URL printerUrl) throws Exception {
    Map<String, String> map = new HashMap<String, String>();

    map.put("printer-uri",printerUrl.getPath());
    
    URL url = new URL(printerUrl.getProtocol() + "://" + printerUrl.getHost() + ":" + printerUrl.getPort());

    IppResult result = request(url, map);
    
    String buf = new String(result.getBuf());
    buf = buf.substring(buf.indexOf("*")); // Remove request attributes when returning the string

    return buf;

    
  }

}
