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

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.application.Application;

public class MainClass {
	
	private static final Logger log = LogManager.getLogger(MainClass.class);
	
	private static FileBasedConfiguration app_config;
	
	static FileBasedConfiguration loadConf(String file) throws ConfigurationException {
		org.apache.commons.configuration2.builder.fluent.Parameters params = new org.apache.commons.configuration2.builder.fluent.Parameters();
		PropertiesBuilderParameters pbp = params.properties().setFileName(file);
		pbp.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class);
		builder.configure(pbp);
		return builder.getConfiguration();
	}
	
	public static void main(String[] args) throws Exception {
		app_config = loadConf("app.properties");
		String[] conf_channels;
		
		if (args.length == 0) {
			conf_channels = app_config.getStringArray("mode2");
			loadMode2(args, conf_channels);
		} else if (args[0].equals("2")) {
			conf_channels = app_config.getStringArray("mode2");
			loadMode2(args, conf_channels);
		} else if (args[0].equals("3")) {
			conf_channels = app_config.getStringArray("mode3");
			loadMode3(args, conf_channels);
		}
		
	}
	
	private static void loadMode2(String[] args, String[] conf_channels) throws Exception {
		if (conf_channels != null) {
			if (conf_channels.length == 2) {
				log.debug("Found channel entry in app conf: " + conf_channels[0]);
				log.debug("Found channel entry in app conf: " + conf_channels[1]);
				ApplicationMode2.channel1 = new Serverchannel(loadConf(conf_channels[0] + ".properties"), conf_channels[0], 0);
				ApplicationMode2.channel2 = new Serverchannel(loadConf(conf_channels[1] + ".properties"), conf_channels[1], 1);
				Application.launch(ApplicationMode2.class, args);
			} else {
				throw new NullPointerException("The \"mode2\" tag in app.properties not 2");
			}
		} else {
			throw new NullPointerException("Can't found a \"mode2\" tag in app.properties");
		}
	}
	
	private static void loadMode3(String[] args, String[] conf_channels) throws Exception {
		if (conf_channels != null) {
			if (conf_channels.length == 3) {
				log.debug("Found channel entry in app conf: " + conf_channels[0]);
				log.debug("Found channel entry in app conf: " + conf_channels[1]);
				log.debug("Found channel entry in app conf: " + conf_channels[2]);
				ApplicationMode3.channel1 = new Serverchannel(loadConf(conf_channels[0] + ".properties"), conf_channels[0], 0);
				ApplicationMode3.channel2 = new Serverchannel(loadConf(conf_channels[1] + ".properties"), conf_channels[1], 1);
				ApplicationMode3.channel3 = new Serverchannel(loadConf(conf_channels[2] + ".properties"), conf_channels[2], 2);
				
				ApplicationMode3.channel1.independant_channel = false;
				ApplicationMode3.channel2.independant_channel = false;
				ApplicationMode3.channel3.independant_channel = false;
				
				Application.launch(ApplicationMode3.class, args);
			} else {
				throw new NullPointerException("The \"mode3\" tag in app.properties not 3");
			}
		} else {
			throw new NullPointerException("Can't found a \"mode3\" tag in app.properties");
		}
	}
	
}
