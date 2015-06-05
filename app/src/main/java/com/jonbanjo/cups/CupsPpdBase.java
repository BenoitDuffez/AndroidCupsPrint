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

public abstract class CupsPpdBase {
    PpdUiList uiList;
    
    public  ArrayList <PpdSectionList> getUiList(){
        return uiList;
    }
    
    public String getCupsString(){
        String cupsString = "";
        boolean isNext = false;
        for (PpdSectionList group: uiList){
            for (PpdItemList section: group){
                if (section.defaultValue.equals(section.savedValue)){
                    if (!section.name.equals("orientation-requested"))
                    	continue;
                }
                if (isNext)
                    cupsString = cupsString + "#";
                else
                    isNext = true;
                cupsString = cupsString + section.name + ":" +
                        section.commandType.toString() + ":" +
                        section.savedValue;
            }
        }
        return cupsString;
    }
}
