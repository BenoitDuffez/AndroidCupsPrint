package ch.ethz.vppserver.ippclient;

/*Copyright (C) 2013 Jon Freeman

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

import ch.ethz.vppserver.schema.ippclient.Tag;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class IppLists {

  public final static List<Tag> tagList;

  public final static LinkedHashMap<String, String> statusCodeMap;
  public final static EnumMap enumMap;
  
  static {
      
    statusCodeMap = new LinkedHashMap<String, String>();
    statusCodeMap.put("0x0000", "successful-ok");
    statusCodeMap.put("0x0001", "successful-ok-ignored-substituted-attributes");
    statusCodeMap.put("0x0002", "successful-ok-conflicting-attributes");
    statusCodeMap.put("0x0400", "client-error-bad-request");
    statusCodeMap.put("0x0401", "client-error-forbidden");
    statusCodeMap.put("0x0402", "client-error-not-authenticated");
    statusCodeMap.put("0x0403", "client-error-not-authorized");
    statusCodeMap.put("0x0404", "client-error-not-possible");
    statusCodeMap.put("0x0405", "client-error-timeout");
    statusCodeMap.put("0x0406", "client-error-not-found");
    statusCodeMap.put("0x0407", "client-error-gone");
    statusCodeMap.put("0x0408", "client-error-request-entity-too-large");
    statusCodeMap.put("0x0409", "client-error-request-status-code-too-long");
    statusCodeMap.put("0x040a", "client-error-document-format-not-supported");
    statusCodeMap.put("0x040b", "client-error-attributes-or-status-codes-not-supported");
    statusCodeMap.put("0x040c", "client-error-uri-scheme-not-supported");
    statusCodeMap.put("0x040d", "client-eror-charset-not-supported");
    statusCodeMap.put("0x040e", "client-error-conflicting-attribute");
    statusCodeMap.put("0x040f", "client-error-compession-not-supported");
    statusCodeMap.put("0x0410", "client-error-compression-error");
    statusCodeMap.put("0x0411", "client-error-document-format-error");
    statusCodeMap.put("0x0412", "client-error-document-access-error");
    statusCodeMap.put("0x0500", "server-error-internal-error");
    statusCodeMap.put("0x0501", "server-error-operation-not-supported");
    statusCodeMap.put("0x0502", "sever-error-service-unavailable");
    statusCodeMap.put("0x0503", "server-error-version-not-supported");
    statusCodeMap.put("0x0504", "server-error-device-error");
    statusCodeMap.put("0x0505", "server-error-temporary-error");
    statusCodeMap.put("0x0506", "server-error-not-accepting-jobs");
    statusCodeMap.put("0x0507", "server-error-busy");
    statusCodeMap.put("0x0508", "server-error-job-canceled");
    statusCodeMap.put("0x0509", "server-error-multiple-document-jobs-not-supported");
    statusCodeMap.put("0x050A", "server-error-printer-is-deactivated");
    statusCodeMap.put("0x00FF", "successful-ok"); 

    tagList = new ArrayList<Tag>();
    tagList.add(new Tag("0x00", "unassigned", "used for separators"));
    tagList.add(new Tag("0x01", "operation-attributes-tag", "operation group"));
    tagList.add(new Tag("0x02", "job-attributes-tag", "job group"));
    tagList.add(new Tag("0x03", "end-attributes-tag"));
    tagList.add(new Tag("0x04", "printer-attributes-tag", "printer group"));
    tagList.add(new Tag("0x05", "unsupported-attributes-tag", "unsupported attributes group"));
    tagList.add(new Tag("0x06", "subscription-attributes-tag", "subscription group"));
    tagList.add(new Tag("0x07", "event-notification-attributes-tag", "event group"));
    tagList.add(new Tag("0x08", "unassigned", "unassigned"));
    tagList.add(new Tag("0x09", "event-notification-attributes-tag", "event group"));
    tagList.add(new Tag("0x10", "unsupported", "unsupported value"));
    tagList.add(new Tag("0x11", "default", "default value"));
    tagList.add(new Tag("0x12", "unknown", "unknown value"));
    tagList.add(new Tag("0x13", "no-value", "no-value value"));
    tagList.add(new Tag("0x15", "not-settable", "not-settable value"));
    tagList.add(new Tag("0x16", "delete-attribute", "delete-attribute value"));
    tagList.add(new Tag("0x17", "admin-define", "admin-defined value"));
    tagList.add(new Tag("0x21", "integer", "integer value"));
    tagList.add(new Tag("0x22", "boolean", "boolean value"));
    tagList.add(new Tag("0x23", "enum", "enumeration value"));
    tagList.add(new Tag("0x30", "octetString", "octet string value", "1023"));
    tagList.add(new Tag("0x31", "dateTime", "date/time value", "11"));
    tagList.add(new Tag("0x32", "resolution", "resolution value"));
    tagList.add(new Tag("0x33", "rangeOfInteger", "range value"));
    tagList.add(new Tag("0x34", "begCollection", "beginning of collection value"));
    tagList.add(new Tag("0x35", "textWithLanguage", "text-with-language value", "1023"));
    tagList.add(new Tag("0x36", "nameWithLanguage", "name-with-language value", "255"));
    tagList.add(new Tag("0x37", "endCollection", "end of collection value"));
    tagList.add(new Tag("0x41", "textWithoutLanguage", "text value", "1023"));
    tagList.add(new Tag("0x42", "nameWithoutLanguage", "name value", "255"));
    tagList.add(new Tag("0x44", "keyword", "keyword value", "255"));
    tagList.add(new Tag("0x45", "uri", "URI value", "1023"));
    tagList.add(new Tag("0x46", "uriScheme", "URI scheme value", "63"));
    tagList.add(new Tag("0x47", "charset", "character set value", "63"));
    tagList.add(new Tag("0x48", "naturalLanguage", "language value", "63"));
    tagList.add(new Tag("0x49", "mimeMediaType", "MIME media type value", "255"));
    tagList.add(new Tag("0x4A", "memberAttrName", "collection member name value"));

    enumMap = new EnumMap();
    
    //Appears twice in XML, using second one which omits descriptions
    //EnumItemMap itemMap = new EnumItemMap(16,"0x23", "enum", "type1 enum");
    //enumMap.put("job-state", itemMap);
    //itemMap.put(3, new EnumItem("pending", "job is waiting to be printed"));
    //itemMap.put(4, new EnumItem("pending-held", "job is held for printing"));
    //itemMap.put(5, new EnumItem("processing", "job is currently printing"));
    //itemMap.put(6, new EnumItem("processing-stopped", "job has been stopped"));
    //itemMap.put(7, new EnumItem("canceled", "job has been canceled"));
    //itemMap.put(8, new EnumItem("aborted", "job has aborted due to error"));
    //itemMap.put(9, new EnumItem("completed", "job has completed successfully"));

    EnumItemMap itemMap = new EnumItemMap("0x23", "enum", "type2 enum");
    enumMap.put("status-code", itemMap);
    itemMap.put(0x0000, new EnumItem("successful-ok"));
    itemMap.put(0x0001, new EnumItem("successful-ok-ignored-substituted-attributes"));
    itemMap.put(0x0002, new EnumItem("successful-ok-conflicting-attributes"));
    itemMap.put(0x0400, new EnumItem("client-error-bad-request"));
    itemMap.put(0x0401, new EnumItem("client-error-forbidden"));
    itemMap.put(0x0402, new EnumItem("client-error-not-authenticated"));
    itemMap.put(0x0403, new EnumItem("client-error-not-authorized"));
    itemMap.put(0x0404, new EnumItem("client-error-not-possible"));
    itemMap.put(0x0405, new EnumItem("client-error-timeout"));
    itemMap.put(0x0406, new EnumItem("client-error-not-found"));
    itemMap.put(0x0407, new EnumItem("client-error-gone"));
    itemMap.put(0x0408, new EnumItem("client-error-request-entity-too-large"));
    itemMap.put(0x0409, new EnumItem("client-error-request-status-code-too-long"));
    itemMap.put(0x040a, new EnumItem("client-error-document-format-not-supported"));
    itemMap.put(0x040a, new EnumItem("client-error-document-format-not-supported"));
    itemMap.put(0x040a, new EnumItem("client-error-document-format-not-supported"));
    itemMap.put(0x040b, new EnumItem("client-error-attributes-or-status-codes-not-supported"));
    itemMap.put(0x040c, new EnumItem("client-error-uri-scheme-not-supported"));
    itemMap.put(0x040d, new EnumItem("client-eror-charset-not-supported"));
    itemMap.put(0x040e, new EnumItem("client-error-conflicting-attribute"));
    itemMap.put(0x040f, new EnumItem("client-error-compession-not-supported"));
    itemMap.put(0x0410, new EnumItem("client-error-compression-error"));
    itemMap.put(0x0411, new EnumItem("client-error-document-format-error"));
    itemMap.put(0x0412, new EnumItem("client-error-document-access-error"));
    itemMap.put(0x0501, new EnumItem("server-error-operation-not-supported"));
    itemMap.put(0x0502, new EnumItem("sever-error-service-unavailable"));
    itemMap.put(0x0503, new EnumItem("server-error-version-not-supported"));
    itemMap.put(0x0504, new EnumItem("server-error-device-error"));
    itemMap.put(0x0505, new EnumItem("server-error-temporary-error"));
    itemMap.put(0x0506, new EnumItem("server-error-not-accepting-jobs"));
    itemMap.put(0x0507, new EnumItem("server-error-busy"));
    itemMap.put(0x0508, new EnumItem("server-error-job-canceled"));
    itemMap.put(0x0509, new EnumItem("server-error-multiple-document-jobs-not-supported"));
    itemMap.put(0x050A, new EnumItem("server-error-printer-is-deactivated"));
    itemMap.put(0x00FF, new EnumItem("successful-ok")); 
    
    //probably don't want this one
    itemMap = new EnumItemMap("0x23", "enum", "type2 enum");
    enumMap.put("job-collation-type", itemMap);
    itemMap.put(1, new EnumItem("other"));
    itemMap.put(2, new EnumItem("unknown"));
    itemMap.put(3, new EnumItem("collated-documents"));
    itemMap.put(4, new EnumItem("collated-documents"));
    itemMap.put(5, new EnumItem("uncollated-documents"));
    
    itemMap = new EnumItemMap("0x23", "enum", "type1 enum");
    enumMap.put("job-state", itemMap);
    itemMap.put(3, new EnumItem("pending"));
    itemMap.put(4, new EnumItem("pending-held"));
    itemMap.put(5, new EnumItem("processing"));
    itemMap.put(6, new EnumItem("processing-stopped"));
    itemMap.put(7, new EnumItem("canceled"));
    itemMap.put(8, new EnumItem("aborted"));
    itemMap.put(9, new EnumItem("completed"));
    
    itemMap = new EnumItemMap("0x23", "enum", "type1 enum");
    enumMap.put("printer-state", itemMap);
    itemMap.put(3, new EnumItem("idle"));
    itemMap.put(4, new EnumItem("processing"));
    itemMap.put(5, new EnumItem("stopped"));
    
  }
}
