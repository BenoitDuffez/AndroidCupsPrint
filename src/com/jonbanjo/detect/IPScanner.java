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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;

public class IPScanner implements Runnable{
    String ipbase;
    int port = 631;
    
    public IPScanner(String ipbase, int port){
        this.ipbase = ipbase;
        this.port = port;
    }
    
    @Override
    public void run() {
        IPTester.portTesters.incrementAndGet();
        Socket s = null;
        try {
            int ipnum;
            String ip = "";
            ipnum=IPTester.portIps.take();
            while (ipnum!= -1){
                try {
                    int hi = (ipnum & 0xFF00) >> 8;
                    int lo = ipnum & 0xFF;
                    ip = ipbase + "." + Integer.toString(hi) + "." + Integer.toString(lo);
                    s = new Socket();
                    s.connect(new InetSocketAddress(ip, port),IPTester.TIMEOUT);
                    s.close();
                    //System.out.println(ip + " open");
                    try {
                    	CupsClient cupsClient = new CupsClient(
                            new URL("http://" + ip + ":" + port));
                    	List<CupsPrinter> pList = cupsClient.getPrinters();
                    	for (CupsPrinter p: pList){
                    		PrinterRec rec = new PrinterRec(
                                p.getDescription(),
                                "http",
                                ip,
                                port,
                                p.getName()
                                );
                    		IPTester.httpResults.printerRecs.add(rec);
                    		//System.out.println(p.getName());
                    	}
                    }catch (Exception e){}
                    try {
                    	CupsClient cupsClient = new CupsClient(
                            new URL("https://" + ip + ":" + port));
                    	List<CupsPrinter> pList = cupsClient.getPrinters();
                    	for (CupsPrinter p: pList){
                    		PrinterRec rec = new PrinterRec(
                                p.getDescription(),
                                "https",
                                ip,
                                port,
                                p.getName()
                                );
                        IPTester.httpsResults.printerRecs.add(rec);
                        //System.out.println(p.getName());
                    	}
                    }catch (Exception e){
                    	System.out.println(e.toString());
                    	if (e.getMessage().contains("No Certificate")){
                    		IPTester.httpsResults.errors.add("https://" + ip + ":" + port + ": No SSL cetificate\n");
                    	}
                    }
                }
                catch (Exception e){
                    //System.out.println(e.toString());
                    //System.out.println(ip + "closed");
                }
                IPTester.tested.incrementAndGet();
                ipnum=IPTester.portIps.take();
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
         }
        finally {
            if (s != null){
                if (!s.isClosed()){
                    try{
                        s.close();
                    }
                    catch(Exception e){}
                }
            }              
        IPTester.portTesters.decrementAndGet();
        }
    }
}