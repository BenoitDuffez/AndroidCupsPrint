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

import javax.jmdns.ServiceInfo;
import javax.jmdns.impl.DNSIncoming;
import javax.jmdns.impl.DNSRecord;

import org.cups4j.CupsClient;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MdnsServices{
	
	public MdnsServices(){
	}
    static final byte[] HEADER = {0,0,0,0,0,1,0,0,0,0,0,0};
    static final byte[] FOOTER =  {0,0,12,0,1};
    public static final String IPP_SERVICE = "_ipp._tcp.local.";
    public static final String IPPS_SERVICE = "_ipps._tcp.local.";
    static final int TIMEOUT = 1000;
    public static final int MAX_PASSES = 50;
    boolean error = false;
    
   
    private byte[] makeQuestion(String data){
        char lastChar = data.charAt(data.length()-1);
        if (lastChar == '.'){
            data = data.substring(0, data.length()-1);
        }
        ByteBuffer bytes = ByteBuffer.allocateDirect(data.length()+1);
        String[] parts = data.split("\\.");
        for (String part: parts){
            bytes.put((byte)part.length());
            bytes.put(part.getBytes());
        }
        bytes.flip();
        byte [] ret = new byte[bytes.capacity()];
        bytes.get(ret);
        return ret;
        
    }
    
    private byte[] makeMessage(String data){
        byte[] question = makeQuestion(data);
        byte[] message = new byte[HEADER.length + 
                question.length + FOOTER.length];
        int pos = 0;
        System.arraycopy(HEADER, 0, message, pos, HEADER.length);
        pos = pos + HEADER.length;
        System.arraycopy(question, 0, message, pos, question.length);
        pos = pos + question.length;
        System.arraycopy(FOOTER, 0, message, pos, FOOTER.length);
        return message;
    }
        
    private void process(Map <String, PrinterRec>list,
            DatagramPacket packet, String service){
        String protocol = "http";
        if (service.equals(IPPS_SERVICE)){
            protocol = "https";
        }
        try {
            DNSIncoming in = new DNSIncoming(packet);
            if (in.getNumberOfAnswers() < 1)
                return;
            Collection<? extends DNSRecord>answers = in.getAllAnswers();
            Iterator<? extends DNSRecord>iterator = answers.iterator();
            ServiceInfo info;
            
            Map<String, String>hosts = new HashMap<String, String>();
            while (iterator.hasNext()){
                DNSRecord record = iterator.next();
                if (record instanceof DNSRecord.Address){
                    info  = record.getServiceInfo();
                    String ip = info.getHostAddresses()[0];
                    hosts.put(info.getName() + "." + info.getDomain() + ".", ip);
                    iterator.remove();
                }
            }
            Map<String, String[]> services = new HashMap<String, String[]>();
            iterator = answers.iterator();
            while (iterator.hasNext()){
                DNSRecord record = iterator.next();
                if (record instanceof DNSRecord.Service){
                    info = record.getServiceInfo();
                    services.put(info.getKey(), new String[]{
                        hosts.get(info.getServer()),
                        String.valueOf(info.getPort())});
                    iterator.remove();
                }
            }
            
            iterator = answers.iterator();
            while (iterator.hasNext()){
                DNSRecord record = iterator.next();
                info = record.getServiceInfo();
                if (!(record instanceof DNSRecord.Text)){
                    continue;
                }
                if (!(info.getType().equals(service))){
                    continue;
                }
                String rp= info.getPropertyString("rp");
                if (rp==null){
                    continue;
                }
                String[] rps = rp.split("/");
                try{
                    rp = rps[rps.length-1];
                }catch (Exception e){
                    rp = "";
                }
                //System.out.println(info.getQualifiedName());
                String key = info.getKey();
                if (key != null){
                	PrinterRec p = getPrinterRec(
                		info.getName(),
                		protocol,
                		services.get(key)[0],
                		Integer.parseInt(services.get(key)[1]),
                		rp);

                	if (p != null){
                		list.put(key, p);
                	}
                }
            }
            
            //System.out.println();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
    
    private PrinterRec getPrinterRec(String nickname, String protocol, String host, 
    		Integer port, String queue){
    	
    	if (nickname == null)
    		nickname = "unknown";
    	if (protocol == null)
    		return null;
    	if (host == null)
    		return null;
    	if (queue == null)
    		return null;
    	return new PrinterRec(nickname, protocol, host, port, queue);
    }
    
    private Map<String, PrinterRec> getPrinters(String service, int stage){
         Map<String, PrinterRec> printers = new HashMap<String,PrinterRec>();
         try{
            MulticastSocket s;
            InetAddress group;
            group = InetAddress.getByName("224.0.0.251");
            s = new MulticastSocket(5353);
            s.setSoTimeout(TIMEOUT);
            s.joinGroup(group);
            byte[] packet = makeMessage(service);
            DatagramPacket hi = new DatagramPacket(packet, packet.length,
                         group, 5353);
            s.send(hi);
            byte[] buf = new byte[65535];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            error = false;
            int passes = 1;
            while (!error){
                try{
                    s.receive(recv);
                    process(printers, recv, service);
                    recv.setLength(buf.length);
                    passes ++;
                    if (passes > MAX_PASSES){
                        error = true;
                    }
            }
                catch (Exception e){
                    error = true;
                }
            }
            //System.out.println(passes);
            s.leaveGroup(group);
            s.close();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
        return printers;
    }
    
    public PrinterResult scan(){
        ArrayList<PrinterRec> httpRecs = new ArrayList<PrinterRec>();
        ArrayList<PrinterRec> httpsRecs = new ArrayList<PrinterRec>();
        httpRecs.addAll(getPrinters(MdnsServices.IPP_SERVICE, 0).values());
        httpsRecs.addAll(getPrinters(MdnsServices.IPPS_SERVICE, 50).values());
        
        PrinterResult result = new PrinterResult();
        String urlStr;
        Map<String, Boolean> testMap = new HashMap<String, Boolean>();
        Iterator<PrinterRec> it = httpsRecs.iterator();
        while(it.hasNext()){
        	PrinterRec rec = it.next();
     		urlStr = rec.getProtocol() + "://" + rec.getHost() + ":" + rec.getPort(); 
        	if (testMap.containsKey(urlStr)){
        		if (!testMap.get(urlStr))
        			it.remove();
        	}
        	else {
        		try {
        			URL url = new URL(urlStr);
        			CupsClient client = new CupsClient(url);
        			client.getPrinter(url);
        			testMap.put(urlStr, true);
        		}
        		catch (Exception e){
        			testMap.put(urlStr, false);
        			it.remove();
        			if (e.getMessage().contains("No Certificate")){
        				result.errors.add(urlStr + ": No SSL cetificate\n");
        			}
        		}
        	}
        }
        new Merger().merge(httpRecs, httpsRecs);
        result.printerRecs = httpsRecs;
        return result;
    }
    
    
    public void stop(){
        error = true;

	}
    
}
