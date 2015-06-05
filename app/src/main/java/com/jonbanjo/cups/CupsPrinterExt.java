package com.jonbanjo.cups;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.cups4j.CupsPrinter;
import org.cups4j.operations.ipp.IppGetPrinterAttributesOperation;

import ch.ethz.vppserver.ippclient.IppResult;
import ch.ethz.vppserver.schema.ippclient.Attribute;
import ch.ethz.vppserver.schema.ippclient.AttributeGroup;
import ch.ethz.vppserver.schema.ippclient.AttributeValue;

public class CupsPrinterExt extends CupsPrinter{
	   
	  private ArrayList<String> supportedMimeTypes;
	  
	  public CupsPrinterExt(URL printerURL, String printerName, boolean isDefault) throws Exception {
	      super(printerURL, printerName, isDefault);
	      setSupportedMimeTypes();
	  }
	  
	    private void setSupportedMimeTypes() throws Exception{
	        IppGetPrinterAttributesOperation o = new IppGetPrinterAttributesOperation();
	        LinkedHashMap<String,String> map = new LinkedHashMap<String, String>();
	        ArrayList<String> supportedTypes = new ArrayList<String>();
	        map.put("requested-attributes", "document-format-supported");
	        IppResult result;
	        //try {
	            result = o.request(getPrinterURL(), map);
	        //}
	        //catch (Exception e){
	        //    System.out.println(e.toString());
	        //    return;
	        //}
	        List<AttributeGroup> gpList = result.getAttributeGroupList();
	        for (AttributeGroup gp :gpList){
	            for (Attribute at :gp.getAttribute()){
	                for (AttributeValue av :at.getAttributeValue()){
	                    if (av.getTagName().equals("mimeMediaType")){
	                        supportedTypes.add(av.getValue());
	                    }
	                }
	            }
	        }
	        supportedMimeTypes = supportedTypes;
	    }
	  
	    public ArrayList<String> getSupportedMimeTypes(){
	  
	        return supportedMimeTypes;
	    }
	  
	    public boolean mimeTypeSupported(String mimeType){
	      
	        if ((supportedMimeTypes == null) || (mimeType == null))
	            return false;
	      
	        for (String type :supportedMimeTypes){
	            if (type  != null){
	            	if (type.equals(mimeType))
	            		return true;
	            }
	        }
	        return false;
	    }
	  
	}
