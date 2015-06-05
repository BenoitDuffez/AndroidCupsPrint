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

package com.jonbanjo.detect;

public class PrinterRec implements Comparable<PrinterRec>{
    
	private String nickname;
	private String protocol;
	private String host;
	private int    port;
	private String queue;
    
    public PrinterRec(String nickname, String protocol, String host,
            int port, String queue){
        this.nickname = nickname;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.queue = queue;
        }
    
    public String getNickname(){
        return nickname;
     }

    public String getProtocol(){
    	return protocol;
    }
    public String getHost(){
    	return host;
    }

    public int getPort(){
    	return port;
    }
    
    public String getQueue(){
    	return queue;
    }

    @Override
    public String toString(){
        return nickname + " (" + protocol + ")";
    }

	@Override
	public int compareTo(PrinterRec another) {
		return this.toString().compareTo(another.toString());
	}


}
