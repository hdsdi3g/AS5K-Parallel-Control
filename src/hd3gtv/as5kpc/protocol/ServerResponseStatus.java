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
package hd3gtv.as5kpc.protocol;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ServerResponseStatus implements ServerResponse {
	
	/**
	 * IDLE – No Active Clip
	 * PAUSED – Active Clip paused
	 * PLAYING – Active Clip Playing
	 * SHUTTLING – Active Clip Shuttling
	 * JOGGING – Active Clip Jogging
	 * VARIPLAY – Active Clip Variable Playing
	 * RECORDING – Active Clip recording
	 * CUED – Active Clip is cued for playback (first frame is being shown) or record.
	 * DONE – Active Clip played to end
	 * ERROR – Last cue operation failed (will stay in this state until another cue is issued)
	 */
	String control;
	
	boolean rec_mode;
	String active_name;
	String active_id;
	// String cued_id;
	String actual_tc;
	// String video_standard;
	boolean has_video;
	
	ServerResponseStatus() {
	}
	
	public String getActive_id() {
		return active_id;
	}
	
	public String getActive_name() {
		return active_name;
	}
	
	public String getActual_tc() {
		return actual_tc;
	}
	
	public boolean isHas_video() {
		return has_video;
	}
	
	public boolean isRec_mode() {
		return rec_mode;
	}
	
	public String getControl() {
		return control;
	}
	
	public void injectServerResponse(Element ams_root_element) {
		NodeList nodes = ams_root_element.getChildNodes();
		Element element;
		for (int pos = 0; pos < nodes.getLength(); pos++) {
			if (nodes.item(pos).getNodeName().equalsIgnoreCase("StatusInfo")) {
				element = (Element) nodes.item(pos);
				control = element.getAttribute("Ctrl");
				rec_mode = element.getAttribute("RecMode").equalsIgnoreCase("true");
				active_name = element.getAttribute("Name");
				active_id = element.getAttribute("Active");
				// cued_id = element.getAttribute("Cued");
				actual_tc = element.getAttribute("Pos");
				/*} else if (nodes.item(pos).getNodeName().equalsIgnoreCase("VidStandard")) {
					video_standard = nodes.item(pos).getTextContent();*/
			} else if (nodes.item(pos).getNodeName().equalsIgnoreCase("InputStatus")) {
				element = (Element) nodes.item(pos);
				has_video = element.getAttribute("Video").equalsIgnoreCase("Present");
			}
		}
	}
	
}
