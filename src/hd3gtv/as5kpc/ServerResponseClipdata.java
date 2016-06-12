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

class ServerResponseClipdata implements ServerResponse {
	
	String id;
	String name;
	String len;
	boolean not_found = false;
	
	public void injectServerResponse(Element ams_root_element) {
		NodeList nodes = ams_root_element.getChildNodes();
		Element element;
		for (int pos = 0; pos < nodes.getLength(); pos++) {
			if (nodes.item(pos).getNodeName().equalsIgnoreCase("ClipData")) {
				element = (Element) nodes.item(pos);
				id = element.getAttribute("ID");
				name = element.getAttribute("Name");
				len = element.getAttribute("Len");
				break;
			}
		}
	}
	
}
