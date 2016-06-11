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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class ServerResponseAbout implements ServerResponse {
	
	private Serverchannel channel;
	
	public ServerResponseAbout(Serverchannel channel) {
		this.channel = channel;
		if (channel == null) {
			throw new NullPointerException("\"channel\" can't to be null");
		}
	}
	
	public void injectServerResponse(Element ams_root_element) {
		NodeList nodes = ams_root_element.getChildNodes();
		Element element = null;
		for (int pos = 0; pos < nodes.getLength(); pos++) {
			if (nodes.item(pos).getNodeName().equalsIgnoreCase("Version")) {
				channel.version = nodes.item(pos).getTextContent();
			} else if (nodes.item(pos).getNodeName().equalsIgnoreCase("ChnInfo")) {
				element = (Element) nodes.item(pos);
				if (element.hasAttribute("CanRecord")) {
					channel.can_record = Boolean.valueOf(element.getAttribute("CanRecord"));
				}
				if (element.hasAttribute("ChnNum")) {
					channel.ch_num = Integer.valueOf(element.getAttribute("ChnNum"));
				}
				break;
			}
		}
		if (element != null) {
			if (element.getNodeName().equalsIgnoreCase("ChnInfo")) {
				nodes = element.getChildNodes();
				for (int pos = 0; pos < nodes.getLength(); pos++) {
					if (nodes.item(pos).getNodeName().equalsIgnoreCase("Channel")) {
						element = (Element) nodes.item(pos);
						if (element.hasAttribute("Num")) {
							if (channel.ch_num != Integer.valueOf(element.getAttribute("Num"))) {
								continue;
							}
						}
						channel.channel_name = element.getAttribute("Name");
						channel.osd_name = element.getAttribute("OSDName");
					}
				}
			}
		}
		
	}
	
}
