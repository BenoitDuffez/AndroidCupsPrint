package org.cups4j;

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
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cups4j.operations.ipp.IppGetJobAttributesOperation;
import org.cups4j.operations.ipp.IppGetJobsOperation;
import org.cups4j.operations.ipp.IppPrintJobOperation;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;

/**
 * Represents a printer on your IPP server
 */

public class CupsPrinter {
  private URL printerURL = null;
  private String name = null;
  private String description = null;
  private String location = null;
  private boolean isDefault = false;

  /**
   * Constructor
   * 
   * @param printerURL
   * @param printerName
   * @param isDefault
   *          true if this is the default printer on this IPP server
   */
  public CupsPrinter(URL printerURL, String printerName, boolean isDefault) {
    this.printerURL = printerURL;
    this.name = printerName;
    this.isDefault = isDefault;
  }

  /**
   * Print method
   * 
   * @param printJob
   * @return PrintRequestResult
   * @throws Exception
   */
  public PrintRequestResult print(PrintJob printJob) throws Exception {
    int ippJobID = -1;
    InputStream document = printJob.getDocument();
    String userName = printJob.getUserName();
    String jobName = printJob.getJobName();
    int copies = printJob.getCopies();
    String pageRanges = printJob.getPageRanges();

    Map<String, String> attributes = printJob.getAttributes();

    if (userName == null) {
      userName = CupsClient.DEFAULT_USER;
    }
    if (attributes == null) {
      attributes = new HashMap<String, String>();
    }

    attributes.put("requesting-user-name", userName);
    attributes.put("job-name", jobName);

    String copiesString = null;
    StringBuffer rangesString = new StringBuffer();
    if (copies > 0) {// other values are considered bad value by CUPS
      copiesString = "copies:integer:" + copies;
      addAttribute(attributes, "job-attributes", copiesString);
    }
    if (pageRanges != null && !"".equals(pageRanges)) {
      String[] ranges = pageRanges.split(",");

      String delimeter = "";

      rangesString.append("page-ranges:setOfRangeOfInteger:");
      for (String range : ranges) {
        range = range.trim();
        String[] values = range.split("-");
        if (values.length == 1) {
          range = range + "-" + range;
        }

        rangesString.append(delimeter).append(range);
        // following ranges need to be separated with ","
        delimeter = ",";
      }
      addAttribute(attributes, "job-attributes", rangesString.toString());
    }

    if (printJob.isDuplex()) {
      addAttribute(attributes, "job-attributes", "sides:keyword:two-sided-long-edge");
    }
    IppPrintJobOperation command = new IppPrintJobOperation();
    IppResult ippResult = command.request(printerURL, attributes, document);
//    IppResultPrinter.print(ippResult);
    
    PrintRequestResult result = new PrintRequestResult(ippResult);
     
    

    for (AttributeGroup group : ippResult.getAttributeGroupList()) {
      if (group.getTagName().equals("job-attributes-tag")) {
        for (Attribute attr : group.getAttribute()) {
          if (attr.getName().equals("job-id")) {
            ippJobID = Integer.parseInt(attr.getAttributeValue().get(0).getValue());
          }
        }
      }
    }
    result.setJobId(ippJobID);
    return result;
  }

  /**
   * 
   * @param map
   * @param name
   * @param value
   */
  private void addAttribute(Map<String, String> map, String name, String value) {
    if (value != null && name != null) {
      String attribute = map.get(name);
      if (attribute == null) {
        attribute = value;
      } else {
        attribute += "#" + value;
      }
      map.put(name, attribute);
    }
  }

  /**
   * Get a list of jobs
   * 
   * @param whichJobs
   *          completed, not completed or all
   * @param user
   *          requesting user (null will be translated to anonymous)
   * @param myJobs
   *          boolean only jobs for requesting user or all jobs for this printer?
   * @return job list
   * @throws Exception
   */

  public List<PrintJobAttributes> getJobs(WhichJobsEnum whichJobs, String user, boolean myJobs) throws Exception {
    IppGetJobsOperation command = new IppGetJobsOperation();

    return command.getPrintJobs(this, whichJobs, user, myJobs);
  }

  /**
   * Get current status for the print job with the given ID.
   * 
   * @param jobID
   * @return job status
   * @throws Exception
   */
  public JobStateEnum getJobStatus(int jobID) throws Exception {
    return getJobStatus(CupsClient.DEFAULT_USER, jobID);
  }

  /**
   * Get current status for the print job with the given ID
   * 
   * @param userName
   * @param jobID
   * @return job status
   * @throws Exception
   */
  public JobStateEnum getJobStatus(String userName, int jobID) throws Exception {
    IppGetJobAttributesOperation command = new IppGetJobAttributesOperation();
    PrintJobAttributes job = command.getPrintJobAttributes(printerURL, userName, jobID);

    return job.getJobState();
  }

  /**
   * Get the URL for this printer
   * 
   * @return printer URL
   */
  public URL getPrinterURL() {
    return printerURL;
  }

  /**
   * Is this the default printer
   * 
   * @return true if this is the default printer false otherwise
   */
  public boolean isDefault() {
    return isDefault;
  }

  protected void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  /**
   * Get a String representation of this printer consisting of the printer URL
   * and the name
   * 
   * @return String
   */
  public String toString() {
    return "printer uri=" + printerURL.toString() + " default=" + isDefault + " name=" + name;
  }

  /**
   * Get name of this printer.
   * <p>
   * For a printer http://localhost:631/printers/printername 'printername' will
   * be returned.
   * </p>
   * 
   * @return printer name
   */
  public String getName() {
    return name;
  }

  /**
   * Get location attribute for this printer
   * 
   * @return location
   */
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Get description attribute for this printer
   * 
   * @return description
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
