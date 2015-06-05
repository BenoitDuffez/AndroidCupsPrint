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

public class PpdItem {
    
    String value;
    String text;
    PpdItemList parent;
    
    public PpdItem(PpdItemList parent, String value, String text){
        this.parent = parent;
        this.value = value;
        this.text = text;
    }
    
    public String getText(){
        return text;
    }
    
    public String getValue(){
        return value;
    }
    
    @Override
    public String toString(){
    	String outText = text;
		int len = outText.length();
		if (len > 30){
			outText = outText.substring(0, 28) + "..";
		}
    	
        switch (parent.commandType){
            case KEYWORD:
                if (parent.defaultValue.equals(value))
                    //return ("*" + text + "/" + value);
                	return ("*" + outText);
               else
                    //return text + "/" + value;
            	   return outText;
            case ENUM:
                if (parent.defaultValue.equals(value))
                    return ("*" + outText);
                else
                    return outText;
                
            default:
                return outText;
        }
    }
}
