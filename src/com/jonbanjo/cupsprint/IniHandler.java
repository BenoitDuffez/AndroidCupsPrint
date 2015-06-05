package com.jonbanjo.cupsprint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.ini4j.Ini;

import android.content.Context;

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

public class IniHandler extends Ini{

	private static final long serialVersionUID = 1L;
	private static final String defaultPrinter = "jfcupsprintdefault"; 
	
	public IniHandler(Context context){
    	super();
    	try {
    		String filePath = context.getFilesDir().getPath().toString() + "/printers.conf";
    		File file = new File(filePath);
    		file.createNewFile();
    		setFile(file);
    		load();
    	}
    	catch (Exception e){
    		System.out.println(e.toString());
        return;
    	}
    }
	
	public String getDefaultPrinter(){
		return getString(IniHandler.defaultPrinter, "default");
	}
	
	public void setDefaultPrinter(String printer){
		put (IniHandler.defaultPrinter, "default", printer);
		try {
			this.store();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	public boolean printerExists(String name){
		Section section = this.get(name);
		return !(section == null);
	}
	
	public void removePrinter(String printer){
		Section section = this.get(printer);
		if (section != null){
			remove(section);
			String currDefault = getDefaultPrinter();
			if (currDefault.equals(printer))
				setDefaultPrinter("");
			try {
				store();
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
		
	}
	public void addPrinter(PrintConfig printer, String oldPrinter){
		Section section = this.get(oldPrinter);
		if (section != null){
			remove(section);
		}
		add(printer.nickname);
		put(printer.nickname, "host", printer.host);
		put(printer.nickname, "protocol", printer.protocol);
		put(printer.nickname, "port", printer.port);
		put(printer.nickname, "queue", printer.queue);
		put(printer.nickname, "orientation", printer.orientation);
		putBoolean(printer.nickname, "fittopage", printer.imageFitToPage);
		putBoolean(printer.nickname, "nooptions", printer.noOptions);
		putBoolean(printer.nickname, "merge", printer.merge);
		put(printer.nickname, "extensions", printer.extensions);
		if (printer.isDefault)
			put (IniHandler.defaultPrinter, "default", printer.nickname);
		else {
			String currDefault = getString(IniHandler.defaultPrinter, "default");
			if (currDefault != null){
				if (currDefault.equals(oldPrinter))
					put(IniHandler.defaultPrinter, "default", "");
			}
		}
		try {
			this.store();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	public ArrayList<String> getPrinters(){
		
		ArrayList<String> printerList = new ArrayList<String>();
		for (String name: keySet()){
			if (!name.equals(defaultPrinter))
				printerList.add(name);
		}
		Collections.sort(printerList);
		return printerList;
		
	}
	
	public PrintConfig getPrinter(String name){
		Section section = this.get(name);
		if (section == null)
			return null;
		String host = this.getString(name, "host");
		String protocol = this.getString(name, "protocol");
		String port = this.getString(name, "port");
		String queue = this.getString(name, "queue");
		PrintConfig pc = new PrintConfig(name, protocol, host, port, queue);
		pc.orientation = this.getString(name, "orientation");
		pc.imageFitToPage = this.getBoolean(name, "fittopage");
		pc.noOptions = this.getBoolean(name, "nooptions");
		pc.extensions = this.getString(name, "extensions");
		pc.merge = this.getBoolean(name, "merge");
		String currDefault = this.getString(defaultPrinter, "default");
		pc.isDefault = (pc.nickname.equals(currDefault));
		return pc;
	}
	
	public String getString(String section, String key){
		String val = this.get(section, key);
		if (val == null)
			return "";
		return val;
	}
	private Boolean getBoolean(String section, String key){
		String value = get(section, key);
		if (value == null)
			return false;
		return (value.equals("true"));
	}
	
	private void putBoolean(String section, String key, boolean value){
		if (value)
			put(section, key, "true");
		else
			put(section, key, "false");
			
	}
}
