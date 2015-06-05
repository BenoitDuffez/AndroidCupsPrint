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

public class OptionPair {
    
    public String option;
    public String text;
    
    public OptionPair(String option, String text){
        this.option = option;
        this.text = text;
    }
    
    public String toString(){
    	return text;
    }
    
    public static OptionPair getOptionPair(String option){
        String[] parts = option.split("/");
        String opt = parts[0].trim();
        String text;
        if (parts.length <2)
            text = opt;
        else
           text = parts[1].trim(); 
        return new OptionPair(opt, text); 
    }
}
