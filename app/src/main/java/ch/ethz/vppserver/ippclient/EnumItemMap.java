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

import java.util.LinkedHashMap;

public class EnumItemMap extends LinkedHashMap<Integer, EnumItem>{
    String tag;
    String tagName;
    String description;
    
    
public EnumItemMap(String tag, String tagName, String description){
        this.tag = tag;
        this.tagName = tagName;
        this.description = description;
    }
    
}
