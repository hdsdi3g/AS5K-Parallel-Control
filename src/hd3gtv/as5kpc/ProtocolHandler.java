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

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class ProtocolHandler implements Closeable {
	
	private static final Logger log = LogManager.getLogger(ProtocolHandler.class);
	
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	
	public ProtocolHandler(Socket socket) throws ParserConfigurationException, IOException {
		this.socket = socket;
		is = socket.getInputStream();
		os = socket.getOutputStream();
	}
	
	private Document createDocument() {
		try {
			DocumentBuilderFactory fq = DocumentBuilderFactory.newInstance();
			DocumentBuilder constructeur = fq.newDocumentBuilder();
			Document document = constructeur.newDocument();
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			return document;
		} catch (Exception e) {
			log.error("Can't make XML document", e);
		}
		return null;
	}
	
	/**
	 * @return maybe null
	 */
	private ServerResponse send(Document document, ServerResponse response) throws IOException {
		byte[] message = null;
		try {
			DOMSource domSource = new DOMSource(document);
			StringWriter stringwriter = new StringWriter();
			StreamResult streamresult = new StreamResult(stringwriter);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.transform(domSource, streamresult);
			message = stringwriter.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException uee) {
			throw new IOException("Encoding XML is not supported", uee);
		} catch (TransformerException tc) {
			throw new IOException("Converting error between XML and String", tc);
		}
		
		if (log.isTraceEnabled()) {
			log.trace("Raw send >> " + new String(message, "UTF-8"));
		}
		
		/**
		 * Send XML request
		 */
		os.write(message);
		os.flush();
		
		/**
		 * Wait and recevied XML response
		 */
		int bytesRead;
		byte[] bytes = new byte[4092];
		try {
			while (socket.isConnected()) {
				try {
					while ((bytesRead = is.read(bytes)) != -1) {
						try {
							if (log.isTraceEnabled()) {
								log.trace("Raw receive << " + new String(bytes, 0, bytesRead, "UTF-8"));
							}
							/**
							 * Decode XML response
							 */
							ByteArrayInputStream bais = new ByteArrayInputStream(bytes, 0, bytesRead);
							DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder xmlDocumentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
							xmlDocumentBuilder.setErrorHandler(null);
							document = xmlDocumentBuilder.parse(bais);
							Element root_element = document.getDocumentElement();
							
							/**
							 * Check if result is valid/positive.
							 */
							Element rply = (Element) root_element.getElementsByTagName("Reply").item(0);
							String status = rply.getAttribute("Status").toLowerCase();
							if (status.equals("ok") == false) {
								String _message = "(no message)";
								if (rply.hasAttribute("Msg")) {
									_message = rply.getAttribute("Msg");
								}
								
								String error_type = "";
								if (rply.hasAttribute("ErrNum")) {
									int err = Integer.parseInt(rply.getAttribute("ErrNum"));
									switch (err) {
									case 1:
										error_type = "Command Error";
										break;
									case 2:
										error_type = "System Error";
										break;
									case 3:
										error_type = "XML format error";
										break;
									case 4:
										error_type = "System BUSY";
										break;
									default:
										break;
									}
								}
								
								if (status.equals("warning")) {
									log.warn(_message + ": " + error_type);
								}
								if (status.equals("error")) {
									if (response instanceof ServerResponseClipdata && _message.toLowerCase().endsWith("does not exist")) {
										((ServerResponseClipdata) response).not_found = true;
										return response;
									} else {
										throw new IOException("Server return: \"" + _message + ": " + error_type + "\"");
									}
								}
							}
							
							if (response != null) {
								response.injectServerResponse(root_element);
							}
							
							return response;
						} catch (ParserConfigurationException pce) {
							log.error("DOM parser error", pce);
						} catch (SAXException se) {
							log.error("XML Struct error", se);
						} catch (Exception e) {
							log.error("Invalid response", e);
						}
					}
				} catch (SocketTimeoutException soe) {
					Thread.sleep(100);
				}
			}
		} catch (InterruptedException e) {
		} catch (IOException e) {
			if (e.getMessage().equalsIgnoreCase("Socket closed")) {
				log.debug("Socket is closed, quit parser");
			} else {
				throw e;
			}
		}
		return null;
	}
	
	public ServerResponseAbout initialize() throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("Configuration");
		Element init = document.createElement("Initialize");
		last_order_element.appendChild(init);
		root_ams_element.appendChild(last_order_element);
		return (ServerResponseAbout) send(document, new ServerResponseAbout());
	}
	
	public void close() throws IOException {
		IOUtils.closeQuietly(is);
		IOUtils.closeQuietly(os);
	}
	
	public ServerResponseOk disconnect() throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("Configuration");
		Element init = document.createElement("Disconnect");
		last_order_element.appendChild(init);
		root_ams_element.appendChild(last_order_element);
		ServerResponseOk result = (ServerResponseOk) send(document, new ServerResponseOk());
		close();
		return result;
	}
	
	public ServerResponseAbout getConfigInfo(ServerResponseAbout init_result) throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("Configuration");
		Element ci = document.createElement("GetConfigInfo");
		last_order_element.appendChild(ci);
		root_ams_element.appendChild(last_order_element);
		send(document, init_result);
		return init_result;
	}
	
	public ServerResponseHWstatus getHardwareStatus() throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("Configuration");
		Element hs = document.createElement("GetHardwareStatus");
		hs.setAttribute("Verbose", "true");
		last_order_element.appendChild(hs);
		root_ams_element.appendChild(last_order_element);
		return (ServerResponseHWstatus) send(document, new ServerResponseHWstatus());
	}
	
	public ServerResponseStatus getStatus() throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("TransportControl");
		Element status = document.createElement("GetStatus");
		status.setAttribute("InputStatus", "true");
		last_order_element.appendChild(status);
		root_ams_element.appendChild(last_order_element);
		return (ServerResponseStatus) send(document, new ServerResponseStatus());
	}
	
	public ServerResponseId recordCue(String id, String name) throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("TransportControl");
		Element rec = document.createElement("RecordCue");
		rec.setAttribute("ID", id);
		rec.setAttribute("Name", name);
		rec.setAttribute("EjectFirst", "true");
		last_order_element.appendChild(rec);
		root_ams_element.appendChild(last_order_element);
		return (ServerResponseId) send(document, new ServerResponseId());
	}
	
	public ServerResponseOk record() throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("TransportControl");
		Element rec = document.createElement("Record");
		last_order_element.appendChild(rec);
		root_ams_element.appendChild(last_order_element);
		return (ServerResponseOk) send(document, new ServerResponseOk());
	}
	
	public ServerResponseOk eject() throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("TransportControl");
		Element ej = document.createElement("Eject");
		last_order_element.appendChild(ej);
		root_ams_element.appendChild(last_order_element);
		return (ServerResponseOk) send(document, new ServerResponseOk());
	}
	
	public ServerResponseOk stop() throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("TransportControl");
		Element stop = document.createElement("Stop");
		last_order_element.appendChild(stop);
		root_ams_element.appendChild(last_order_element);
		return (ServerResponseOk) send(document, new ServerResponseOk());
	}
	
	public ServerResponseClipdata getClipData(String id) throws IOException {
		Document document = createDocument();
		Element root_ams_element = document.createElement("AMS");
		document.appendChild(root_ams_element);
		
		Element last_order_element = document.createElement("DatabaseControl");
		Element cd = document.createElement("GetClipData");
		cd.setAttribute("ID", id);
		last_order_element.appendChild(cd);
		root_ams_element.appendChild(last_order_element);
		return (ServerResponseClipdata) send(document, new ServerResponseClipdata());
	}
	
}
