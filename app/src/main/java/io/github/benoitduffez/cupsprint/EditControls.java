package io.github.benoitduffez.cupsprint;

import java.util.ArrayList;

import com.jonbanjo.cups.OptionPair;

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

public class EditControls {
	
	public final static ArrayList<OptionPair> orientationOpts;
	public final static ArrayList<String>protocols;
	
	static{
		orientationOpts = new ArrayList<OptionPair>();
		orientationOpts.add(new OptionPair("3", "Portrait"));
		orientationOpts.add(new OptionPair("4", "Landscape"));
		orientationOpts.add(new OptionPair("5", "Reverse Portrait"));
		orientationOpts.add(new OptionPair("6", "Reverse Landscape"));
		
		protocols = new ArrayList<String>();
		protocols.add("http");
		protocols.add("https");
	}

}
