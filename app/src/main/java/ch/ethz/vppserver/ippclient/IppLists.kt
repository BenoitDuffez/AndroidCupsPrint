package ch.ethz.vppserver.ippclient

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

import ch.ethz.vppserver.schema.ippclient.Tag
import java.util.*

object IppLists {
    val tagList: MutableList<Tag>
    val statusCodeMap: LinkedHashMap<String, String> = LinkedHashMap()
    val enumMap: EnumMap

    init {
        statusCodeMap["0x0000"] = "successful-ok"
        statusCodeMap["0x0001"] = "successful-ok-ignored-substituted-attributes"
        statusCodeMap["0x0002"] = "successful-ok-conflicting-attributes"
        statusCodeMap["0x0400"] = "client-error-bad-request"
        statusCodeMap["0x0401"] = "client-error-forbidden"
        statusCodeMap["0x0402"] = "client-error-not-authenticated"
        statusCodeMap["0x0403"] = "client-error-not-authorized"
        statusCodeMap["0x0404"] = "client-error-not-possible"
        statusCodeMap["0x0405"] = "client-error-timeout"
        statusCodeMap["0x0406"] = "client-error-not-found"
        statusCodeMap["0x0407"] = "client-error-gone"
        statusCodeMap["0x0408"] = "client-error-request-entity-too-large"
        statusCodeMap["0x0409"] = "client-error-request-status-code-too-long"
        statusCodeMap["0x040a"] = "client-error-document-format-not-supported"
        statusCodeMap["0x040b"] = "client-error-attributes-or-status-codes-not-supported"
        statusCodeMap["0x040c"] = "client-error-uri-scheme-not-supported"
        statusCodeMap["0x040d"] = "client-eror-charset-not-supported"
        statusCodeMap["0x040e"] = "client-error-conflicting-attribute"
        statusCodeMap["0x040f"] = "client-error-compession-not-supported"
        statusCodeMap["0x0410"] = "client-error-compression-error"
        statusCodeMap["0x0411"] = "client-error-document-format-error"
        statusCodeMap["0x0412"] = "client-error-document-access-error"
        statusCodeMap["0x0500"] = "server-error-internal-error"
        statusCodeMap["0x0501"] = "server-error-operation-not-supported"
        statusCodeMap["0x0502"] = "sever-error-service-unavailable"
        statusCodeMap["0x0503"] = "server-error-version-not-supported"
        statusCodeMap["0x0504"] = "server-error-device-error"
        statusCodeMap["0x0505"] = "server-error-temporary-error"
        statusCodeMap["0x0506"] = "server-error-not-accepting-jobs"
        statusCodeMap["0x0507"] = "server-error-busy"
        statusCodeMap["0x0508"] = "server-error-job-canceled"
        statusCodeMap["0x0509"] = "server-error-multiple-document-jobs-not-supported"
        statusCodeMap["0x050A"] = "server-error-printer-is-deactivated"
        statusCodeMap["0x00FF"] = "successful-ok"

        tagList = ArrayList()
        tagList.add(Tag("0x00", "unassigned", "used for separators"))
        tagList.add(Tag("0x01", "operation-attributes-tag", "operation group"))
        tagList.add(Tag("0x02", "job-attributes-tag", "job group"))
        tagList.add(Tag("0x03", "end-attributes-tag"))
        tagList.add(Tag("0x04", "printer-attributes-tag", "printer group"))
        tagList.add(Tag("0x05", "unsupported-attributes-tag", "unsupported attributes group"))
        tagList.add(Tag("0x06", "subscription-attributes-tag", "subscription group"))
        tagList.add(Tag("0x07", "event-notification-attributes-tag", "event group"))
        tagList.add(Tag("0x08", "unassigned", "unassigned"))
        tagList.add(Tag("0x09", "event-notification-attributes-tag", "event group"))
        tagList.add(Tag("0x10", "unsupported", "unsupported value"))
        tagList.add(Tag("0x11", "default", "default value"))
        tagList.add(Tag("0x12", "unknown", "unknown value"))
        tagList.add(Tag("0x13", "no-value", "no-value value"))
        tagList.add(Tag("0x15", "not-settable", "not-settable value"))
        tagList.add(Tag("0x16", "delete-attribute", "delete-attribute value"))
        tagList.add(Tag("0x17", "admin-define", "admin-defined value"))
        tagList.add(Tag("0x21", "integer", "integer value"))
        tagList.add(Tag("0x22", "boolean", "boolean value"))
        tagList.add(Tag("0x23", "enum", "enumeration value"))
        tagList.add(Tag("0x30", "octetString", "octet string value", "1023"))
        tagList.add(Tag("0x31", "dateTime", "date/time value", "11"))
        tagList.add(Tag("0x32", "resolution", "resolution value"))
        tagList.add(Tag("0x33", "rangeOfInteger", "range value"))
        tagList.add(Tag("0x34", "begCollection", "beginning of collection value"))
        tagList.add(Tag("0x35", "textWithLanguage", "text-with-language value", "1023"))
        tagList.add(Tag("0x36", "nameWithLanguage", "name-with-language value", "255"))
        tagList.add(Tag("0x37", "endCollection", "end of collection value"))
        tagList.add(Tag("0x41", "textWithoutLanguage", "text value", "1023"))
        tagList.add(Tag("0x42", "nameWithoutLanguage", "name value", "255"))
        tagList.add(Tag("0x44", "keyword", "keyword value", "255"))
        tagList.add(Tag("0x45", "uri", "URI value", "1023"))
        tagList.add(Tag("0x46", "uriScheme", "URI scheme value", "63"))
        tagList.add(Tag("0x47", "charset", "character set value", "63"))
        tagList.add(Tag("0x48", "naturalLanguage", "language value", "63"))
        tagList.add(Tag("0x49", "mimeMediaType", "MIME media type value", "255"))
        tagList.add(Tag("0x4A", "memberAttrName", "collection member name value"))

        enumMap = EnumMap()

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

        var itemMap = EnumItemMap("0x23", "enum", "type2 enum")
        enumMap["status-code"] = itemMap
        itemMap[0x0000] = EnumItem("successful-ok")
        itemMap[0x0001] = EnumItem("successful-ok-ignored-substituted-attributes")
        itemMap[0x0002] = EnumItem("successful-ok-conflicting-attributes")
        itemMap[0x0400] = EnumItem("client-error-bad-request")
        itemMap[0x0401] = EnumItem("client-error-forbidden")
        itemMap[0x0402] = EnumItem("client-error-not-authenticated")
        itemMap[0x0403] = EnumItem("client-error-not-authorized")
        itemMap[0x0404] = EnumItem("client-error-not-possible")
        itemMap[0x0405] = EnumItem("client-error-timeout")
        itemMap[0x0406] = EnumItem("client-error-not-found")
        itemMap[0x0407] = EnumItem("client-error-gone")
        itemMap[0x0408] = EnumItem("client-error-request-entity-too-large")
        itemMap[0x0409] = EnumItem("client-error-request-status-code-too-long")
        itemMap[0x040a] = EnumItem("client-error-document-format-not-supported")
        itemMap[0x040a] = EnumItem("client-error-document-format-not-supported")
        itemMap[0x040a] = EnumItem("client-error-document-format-not-supported")
        itemMap[0x040b] = EnumItem("client-error-attributes-or-status-codes-not-supported")
        itemMap[0x040c] = EnumItem("client-error-uri-scheme-not-supported")
        itemMap[0x040d] = EnumItem("client-eror-charset-not-supported")
        itemMap[0x040e] = EnumItem("client-error-conflicting-attribute")
        itemMap[0x040f] = EnumItem("client-error-compession-not-supported")
        itemMap[0x0410] = EnumItem("client-error-compression-error")
        itemMap[0x0411] = EnumItem("client-error-document-format-error")
        itemMap[0x0412] = EnumItem("client-error-document-access-error")
        itemMap[0x0501] = EnumItem("server-error-operation-not-supported")
        itemMap[0x0502] = EnumItem("sever-error-service-unavailable")
        itemMap[0x0503] = EnumItem("server-error-version-not-supported")
        itemMap[0x0504] = EnumItem("server-error-device-error")
        itemMap[0x0505] = EnumItem("server-error-temporary-error")
        itemMap[0x0506] = EnumItem("server-error-not-accepting-jobs")
        itemMap[0x0507] = EnumItem("server-error-busy")
        itemMap[0x0508] = EnumItem("server-error-job-canceled")
        itemMap[0x0509] = EnumItem("server-error-multiple-document-jobs-not-supported")
        itemMap[0x050A] = EnumItem("server-error-printer-is-deactivated")
        itemMap[0x00FF] = EnumItem("successful-ok")

        //probably don't want this one
        itemMap = EnumItemMap("0x23", "enum", "type2 enum")
        enumMap["job-collation-type"] = itemMap
        itemMap[1] = EnumItem("other")
        itemMap[2] = EnumItem("unknown")
        itemMap[3] = EnumItem("collated-documents")
        itemMap[4] = EnumItem("collated-documents")
        itemMap[5] = EnumItem("uncollated-documents")

        itemMap = EnumItemMap("0x23", "enum", "type1 enum")
        enumMap["job-state"] = itemMap
        itemMap[3] = EnumItem("pending")
        itemMap[4] = EnumItem("pending-held")
        itemMap[5] = EnumItem("processing")
        itemMap[6] = EnumItem("processing-stopped")
        itemMap[7] = EnumItem("canceled")
        itemMap[8] = EnumItem("aborted")
        itemMap[9] = EnumItem("completed")

        itemMap = EnumItemMap("0x23", "enum", "type1 enum")
        enumMap["printer-state"] = itemMap
        itemMap[3] = EnumItem("idle")
        itemMap[4] = EnumItem("processing")
        itemMap[5] = EnumItem("stopped")
    }
}
