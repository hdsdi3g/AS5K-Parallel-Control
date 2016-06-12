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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainClass extends Application {
	
	private static final Logger log = LogManager.getLogger(MainClass.class);
	
	private static FileBasedConfiguration app_config;
	static ArrayList<Serverchannel> channels;
	
	private static FileBasedConfiguration loadConf(String file) throws ConfigurationException {
		org.apache.commons.configuration2.builder.fluent.Parameters params = new org.apache.commons.configuration2.builder.fluent.Parameters();
		PropertiesBuilderParameters pbp = params.properties().setFileName(file);
		pbp.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class);
		builder.configure(pbp);
		return builder.getConfiguration();
	}
	
	public static void main(String[] args) throws Exception {
		app_config = loadConf("app.properties");
		
		String[] conf_channels = app_config.getStringArray("channels");
		if (conf_channels != null) {
			if (conf_channels.length > 0) {
				channels = new ArrayList<Serverchannel>(conf_channels.length);
				for (int pos = 0; pos < conf_channels.length; pos++) {
					log.debug("Found channel entry in app conf: " + conf_channels[pos]);
					channels.add(new Serverchannel(loadConf(conf_channels[pos] + ".properties"), conf_channels[pos], pos));
				}
			}
		}
		
		if (channels == null) {
			throw new NullPointerException("Can't found a \"channels\" tag in app.properties");
		}
		if (channels.isEmpty()) {
			throw new NullPointerException("The \"channels\" tag in app.properties is empty");
		}
		
		/*channels.forEach(c -> {
			c.start();
		});*/
		
		launch(args);
	}
	
	static Scene scene;
	
	public void start(Stage stage) throws IOException {
		stage.setTitle("AirSpeed Parallel Recorder");
		stage.setResizable(false);
		stage.setAlwaysOnTop(true);
		
		stage.getIcons().add(new Image(this.getClass().getResource("icon-512.png").toString()));
		stage.getIcons().add(new Image(this.getClass().getResource("icon-48.png").toString()));
		
		Parent root = FXMLLoader.load(getClass().getResource("main-form.fxml"));
		
		scene = new Scene(root);
		
		stage.setScene(scene);
		stage.setOnShown(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				scene.setCursor(Cursor.WAIT);
			}
		});
		
		stage.show();
	}
	
}
