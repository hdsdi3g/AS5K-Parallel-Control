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

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Application {
	
	private static Logger log = LogManager.getLogger("as5kpc.app");
	private Worker worker;
	private FileBasedConfiguration app_config;
	private ArrayList<Serverchannel> channels;
	
	private static FileBasedConfiguration loadConf(String file) throws ConfigurationException {
		Parameters params = new Parameters();
		PropertiesBuilderParameters pbp = params.properties().setFileName(file);
		pbp.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class);
		builder.configure(pbp);
		return builder.getConfiguration();
	}
	
	public Application() throws ConfigurationException, ParserConfigurationException {
		app_config = loadConf("app.properties");
		
		String[] conf_channels = app_config.getStringArray("channels");
		if (conf_channels != null) {
			if (conf_channels.length > 0) {
				channels = new ArrayList<Serverchannel>(conf_channels.length);
				for (int pos = 0; pos < conf_channels.length; pos++) {
					log.debug("Found channel entry in app conf: " + conf_channels[pos]);
					channels.add(new Serverchannel(loadConf(conf_channels[pos] + ".properties"), conf_channels[pos]));
				}
			}
		}
		
		if (channels == null) {
			throw new NullPointerException("Can't found a \"channels\" tag in app.properties");
		}
		if (channels.isEmpty()) {
			throw new NullPointerException("The \"channels\" tag in app.properties is empty");
		}
		
		worker = new Worker();
		worker.setDaemon(false);
		worker.setName("Application Worker");
	}
	
	public void start() {
		worker.start();
	}
	
	public synchronized void stop() throws InterruptedException {
		worker.want_to_stop = true;
		while (worker.isAlive()) {
			Thread.sleep(10);
		}
	}
	
	private class Worker extends Thread {
		
		boolean want_to_stop;
		
		public void run() {
			want_to_stop = false;
			
			channels.forEach(c -> {
				c.start();
			});
			
			while (want_to_stop == false) {
				try {
					
					Thread.sleep(10);
				} catch (Exception e) {
					log.fatal("Non managed error", e);
				}
			}
			
			channels.forEach(c -> {
				c.stopOrder();
			});
			
			channels.forEach(c -> {
				c.isAlive();
			});
		}
		
	}
	
}
