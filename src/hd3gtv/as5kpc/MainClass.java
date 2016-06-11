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

import org.apache.log4j.LogManager;

public class MainClass {
	
	public static void main(String[] args) throws Exception {
		final Application app = new Application();
		app.start();
		
		Thread t = new Thread() {
			public void run() {
				try {
					app.stop();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				LogManager.shutdown();
			}
		};
		t.setName("Shutdown Hook");
		Runtime.getRuntime().addShutdownHook(t);
		
	}
	
}
