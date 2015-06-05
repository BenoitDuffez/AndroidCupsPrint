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

import java.util.ArrayList;

public class PpdSectionList extends ArrayList<PpdItemList>{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
    String text;
    
    public String getName(){
        return name;
    }
    
    public String getGroupText(){
        return text;
    }
    
    @Override
    public String toString(){
        return text;
    }
}
