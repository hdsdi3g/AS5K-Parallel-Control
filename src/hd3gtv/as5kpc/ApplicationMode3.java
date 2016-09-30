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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ApplicationMode3 extends Application {
	
	static Serverchannel channel1;
	static Serverchannel channel2;
	static Serverchannel channel3;
	static Scene scene;
	
	public void start(Stage stage) throws IOException {
		stage.setTitle("AirSpeed Parallel Recorder - 3 recorders mode");
		stage.setResizable(false);
		stage.setAlwaysOnTop(true);
		
		stage.getIcons().add(new Image(this.getClass().getResource("icon-512.png").toString()));
		stage.getIcons().add(new Image(this.getClass().getResource("icon-48.png").toString()));
		
		Parent root = FXMLLoader.load(getClass().getResource("form-mode3.fxml"));
		scene = new Scene(root);
		
		stage.setScene(scene);
		stage.show();
	}
}
