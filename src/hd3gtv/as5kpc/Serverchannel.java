/*
 * This file is part of AS5K Parallel Control.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 2016
 * 
*/
package hd3gtv.as5kpc;

import java.net.Socket;
import java.util.LinkedHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Serverchannel {
	
	private Worker worker;
	
	private final Logger log;
	private Configuration config;
	
	public Serverchannel(Configuration config, String server_ref) throws ParserConfigurationException {
		log = LogManager.getLogger("as5kpc.channel." + server_ref);
		this.config = config;
		
		worker = new Worker(this);
		worker.setDaemon(true);
		worker.setName("Server channel worker");
	}
	
	public void start() {
		worker.start();
	}
	
	public synchronized void stopOrder() {
		worker.want_to_stop = true;
	}
	
	public synchronized void isAlive() {
		try {
			while (worker.isAlive()) {
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			log.warn("Can't sleep", e);
		}
	}
	
	private class Worker extends Thread {
		
		Serverchannel ref;
		boolean want_to_stop;
		
		public Worker(Serverchannel ref) throws ParserConfigurationException {
			this.ref = ref;
		}
		
		public void run() {
			want_to_stop = false;
			
			try {
				log.info("Open connection to " + config.getString("server") + ":" + config.getInt("port") + "...");
				
				Socket socket = new Socket(config.getString("server"), config.getInt("port"));
				socket.setSoTimeout(10);
				
				try {
					ProtocolHandler mf = new ProtocolHandler(socket, log);
					mf.initialize(ref);
					System.out.println(mf.send());
					
					mf.getConfigInfo(ref);
					System.out.println(mf.send());
					
					mf.disconnect();
					System.out.println(mf.send());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				IOUtils.closeQuietly(socket);
				
				log.info("Connection to " + config.getString("server") + ":" + config.getInt("port") + " is closed.");
				
				LinkedHashMap<String, Object> dump = new LinkedHashMap<String, Object>();
				dump.put("version", version);
				dump.put("ch_num", ch_num);
				dump.put("can_record", can_record);
				dump.put("channel_name", channel_name);
				dump.put("osd_name", osd_name);
				log.info("About channel: " + dump);
				
				// while (want_to_stop == false) { //XXX
				Thread.sleep(10);
				// }
				
			} catch (InterruptedException e) {
				log.fatal("Can't sleep error", e);
			} catch (Exception e) {
				log.fatal("Non managed error", e);
			}
		}
	}
	
	String version;
	int ch_num;
	boolean can_record;
	String channel_name;
	String osd_name;
	
	public int getChNum() {
		return ch_num;
	}
	
	public String getVersion() {
		return version;
	}
	
	public boolean isCanRecord() {
		return can_record;
	}
	
	public String getChannelName() {
		return channel_name;
	}
	
	public String getOsdName() {
		return osd_name;
	}
}
