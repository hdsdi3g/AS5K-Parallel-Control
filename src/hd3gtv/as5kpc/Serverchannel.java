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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import hd3gtv.as5kpc.protocol.ProtocolHandler;
import hd3gtv.as5kpc.protocol.ServerResponseAbout;
import hd3gtv.as5kpc.protocol.ServerResponseClipdata;
import hd3gtv.as5kpc.protocol.ServerResponseStatus;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class Serverchannel {
	
	private static final Logger log = LogManager.getLogger(Serverchannel.class);
	
	private int vtr_index;
	private String server;
	private int port;
	private String server_label;
	
	public Serverchannel(Configuration config, String server_ref, int vtr_index) throws ParserConfigurationException {
		server = config.getString("server");
		port = config.getInt("port");
		
		this.vtr_index = vtr_index;
	}
	
	public int getVtrIndex() {
		return vtr_index;
	}
	
	private Socket connect() throws IOException {
		Socket socket = new Socket();
		socket.setKeepAlive(false);
		socket.connect(new InetSocketAddress(server, port), 4000);
		return socket;
	}
	
	BackgroundWatcher createBackgroundWatcher() {
		BackgroundWatcher bw = new BackgroundWatcher();
		// bw.setDelay(Duration.seconds(1));
		bw.setRestartOnFailure(true);
		bw.setMaximumFailureCount(10);
		bw.setPeriod(Duration.seconds(1));
		bw.setBackoffStrategy(service -> Duration.seconds(bw.getCurrentFailureCount() + 1));
		return bw;
	}
	
	class BackgroundWatcher extends ScheduledService<ServerResponseStatus> {
		
		private BackgroundWatcher() {
		}
		
		@Override
		protected void failed() {
			super.failed();
			if (getRestartOnFailure() && getMaximumFailureCount() > getCurrentFailureCount()) {
				if (protocol != null) {
					IOUtils.closeQuietly(protocol);
					protocol = null;
				}
				if (socket != null) {
					IOUtils.closeQuietly(socket);
					socket = null;
				}
				
				log.warn("Loose link with the server " + server);
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Erreur");
				alert.setHeaderText("La liaison avec le serveur est rompue");
				
				StringBuilder sb = new StringBuilder();
				sb.append(server);
				sb.append(":");
				sb.append(port);
				sb.append(" (vtr: ");
				sb.append(vtr_index);
				sb.append(")");
				
				alert.setContentText("La machine " + sb.toString() + " n'est plus joignable. Verifiez vos parametres réseau et la configuration du client et/ou du serveur.");
				
				alert.showAndWait();
				System.exit(1);
			}
		}
		
		private Socket socket;
		private ProtocolHandler protocol;
		
		@Override
		protected Task<ServerResponseStatus> createTask() {
			return new Task<ServerResponseStatus>() {
				@Override
				protected ServerResponseStatus call() throws Exception {
					if (socket == null) {
						socket = connect();
						protocol = new ProtocolHandler(socket);
						protocol.initialize();
					} else if (socket.isConnected() == false) {
						socket = connect();
						protocol = new ProtocolHandler(socket);
						protocol.initialize();
					}
					
					return protocol.getStatus();
				}
			};
		}
	}
	
	AboutServerbackgound createAboutServerbackgound() {
		return new AboutServerbackgound();
	}
	
	class AboutServerbackgound extends Service<ServerResponseAbout> {
		private AboutServerbackgound() {
		}
		
		@Override
		protected Task<ServerResponseAbout> createTask() {
			return new Task<ServerResponseAbout>() {
				@Override
				protected ServerResponseAbout call() throws Exception {
					Socket socket = connect();
					
					ProtocolHandler mf = new ProtocolHandler(socket);
					
					ServerResponseAbout result = mf.initialize();
					server_label = mf.getConfigInfo(result).getOsd_name();
					mf.disconnect();
					IOUtils.closeQuietly(mf);
					IOUtils.closeQuietly(socket);
					
					return result;
				}
			};
		}
		
	}
	
	StopEjectBackgound createStopEjectBackgound() {
		return new StopEjectBackgound();
	}
	
	class StopEjectBackgound extends Service<Void> {
		
		private StopEjectBackgound() {
		}
		
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Socket socket = connect();
					
					ProtocolHandler mf = new ProtocolHandler(socket);
					mf.initialize();
					mf.stop();
					mf.eject();
					
					mf.disconnect();
					IOUtils.closeQuietly(mf);
					IOUtils.closeQuietly(socket);
					return null;
				}
			};
		}
		
	}
	
	RecBackgound createRecBackgound(String id, String name) {
		return new RecBackgound(id, name);
	}
	
	class RecBackgound extends Service<Void> {
		private String id;
		private String name;
		
		private RecBackgound(String id, String name) {
			this.id = id;
			if (id == null) {
				throw new NullPointerException("\"id\" can't to be null");
			}
			this.name = name;
			if (name == null) {
				throw new NullPointerException("\"name\" can't to be null");
			}
		}
		
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Socket socket = connect();
					
					ProtocolHandler mf = new ProtocolHandler(socket);
					mf.initialize();
					mf.stop();
					mf.eject();
					mf.recordCue(id, name);
					mf.record();
					
					mf.disconnect();
					IOUtils.closeQuietly(mf);
					IOUtils.closeQuietly(socket);
					return null;
				}
			};
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(server);
		sb.append(":");
		sb.append(port);
		sb.append(" (vtr: ");
		sb.append(vtr_index);
		sb.append(")");
		return sb.toString();
	}
	
	GetFreeClipIdBackgound getFreeClipIdBackgound(int id, int take) {
		return new GetFreeClipIdBackgound(id, take);
	}
	
	class GetFreeClipIdBackgound extends Service<HashMap<String, String>> {
		
		private int id;
		private int take;
		
		private GetFreeClipIdBackgound(int id, int take) {
			this.id = id;
			this.take = take;
		}
		
		@Override
		protected Task<HashMap<String, String>> createTask() {
			return new Task<HashMap<String, String>>() {
				@Override
				protected HashMap<String, String> call() throws Exception {
					Socket socket = connect();
					
					String first_name = null;
					
					ProtocolHandler mf = new ProtocolHandler(socket);
					mf.initialize();
					String valid_id = makeValidId(id, take);
					ServerResponseClipdata cd = mf.getClipData(valid_id);
					first_name = cd.getName();
					
					while (cd.isNot_found() == false) {
						Thread.sleep(50);
						take++;
						valid_id = makeValidId(id, take);
						cd = mf.getClipData(valid_id);
					}
					
					mf.disconnect();
					IOUtils.closeQuietly(mf);
					IOUtils.closeQuietly(socket);
					
					HashMap<String, String> result = new HashMap<>(3);
					result.put("id", valid_id);
					result.put("take", String.valueOf(take));
					result.put("first_name", first_name);
					return result;
				}
			};
		}
		
	}
	
	public String getServerLabel() {
		if (independant_channel == false) {
			return " - " + (getVtrIndex() + 1);
		}
		
		return server_label;
	}
	
	void appInitialize(ControllerUpdater updater) {
		AboutServerbackgound absb = createAboutServerbackgound();
		
		absb.setOnSucceeded((WorkerStateEvent event_absb) -> {
			
			ServerResponseAbout about = absb.getValue();
			updater.updateVTRMedia(getVtrIndex(), "PROTOCOL VERSION: " + about.getVersion(), "");
			if (about.isCan_record()) {
				updater.updateVTRStatus(getVtrIndex(), about.getOsd_name() + " #" + about.getCh_num(), about.getChannel_name(), "--:--:--:--", "Loading...");
			} else {
				updater.updateVTRStatus(getVtrIndex(), about.getOsd_name() + " #" + about.getCh_num(), about.getChannel_name(), "--:--:--:--", "CAN'T RECORD");
				return;
			}
			BackgroundWatcher bw = createBackgroundWatcher();
			bw.setDelay(Duration.seconds(1));
			
			bw.setOnSucceeded((WorkerStateEvent event_bw) -> {
				ServerResponseStatus status = bw.getValue();
				String warn = "";
				if (status.isRec_mode() == false) {
					warn = "Standby";
				} else if (status.isHas_video() == false) {
					warn = "NO VIDEO!";
				}
				
				updater.updateVTRStatus(getVtrIndex(), about.getOsd_name(), status.getControl(), status.getActual_tc(), warn);
				updater.updateVTRMedia(getVtrIndex(), status.getActive_name(), status.getActive_id());
			});
			
			bw.start();
		});
		
		absb.setOnFailed((WorkerStateEvent event_absb_e) -> {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Erreur");
			alert.setHeaderText("Impossible de communiquer avec le serveur");
			alert.setContentText("La machine " + toString() + " n'est pas joignable. Verifiez vos parametres réseau et la configuration du client et/ou du serveur.");
			
			alert.showAndWait();
			System.exit(1);
		});
		
		absb.start();
	}
	
	boolean independant_channel = true;
	
	void appCue(TextField mediaid, Label takenum, TextField showname, Button btncue, Button btnrec) {
		int id;
		int take;
		try {
			id = Integer.parseInt(mediaid.getText());
			take = Integer.valueOf(takenum.getText());
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Media ID");
			alert.setHeaderText("Erreur lors de la récupération de l'ID, seuls des chiffres sont autorisés.");
			alert.setContentText("Détail: " + e.getMessage());
			alert.showAndWait();
			return;
		}
		
		GetFreeClipIdBackgound gfip = getFreeClipIdBackgound(id + getVtrIndex(), take);
		
		gfip.setOnSucceeded((WorkerStateEvent ev) -> {
			takenum.setText(gfip.getValue().get("take"));
			String first_name = gfip.getValue().get("first_name");
			if (first_name != null) {
				final String _first_name = first_name.substring("00-00 ".length());
				showname.setText(_first_name);
				if (_first_name.endsWith(getServerLabel())) {
					showname.setText(_first_name.substring(0, _first_name.length() - getServerLabel().length()).trim());
				}
			}
			
			btncue.setDisable(false);
			mediaid.setDisable(false);
			showname.setDisable(false);
			btnrec.setDisable(false);
		});
		
		gfip.setOnFailed((WorkerStateEvent ev) -> {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Cue");
			alert.setHeaderText("Impossible de récuperer un état");
			alert.setContentText("La récupération du statut de l'Id " + id + " n'a pas fonctionné.");
			alert.showAndWait();
			
			mediaid.setDisable(false);
			showname.setDisable(false);
		});
		
		gfip.start();
		
	}
	
	public static final Pattern PATTERN_Combining_Diacritical_Marks = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	
	private String makeName(String suffix_label, TextField showname) {
		StringBuilder sb = new StringBuilder();
		Calendar c = Calendar.getInstance();
		if (c.get(Calendar.DAY_OF_MONTH) < 10) {
			sb.append("0");
		}
		sb.append(c.get(Calendar.DAY_OF_MONTH));
		sb.append("-");
		if (c.get(Calendar.MONTH) + 1 < 10) {
			sb.append("0");
		}
		sb.append(c.get(Calendar.MONTH) + 1);
		sb.append(" ");
		
		String show_name = showname.getText();
		show_name = PATTERN_Combining_Diacritical_Marks.matcher(Normalizer.normalize(showname.getText(), Normalizer.Form.NFD)).replaceAll("").trim().toUpperCase();
		showname.setText(show_name);
		
		sb.append(show_name);
		
		if (show_name.endsWith(suffix_label) == false) {
			sb.append(" ");
			sb.append(suffix_label);
		}
		return sb.toString();
	}
	
	String makeValidId(int id, int take) {
		String _id = "00000000" + String.valueOf(id);
		_id = _id.substring(_id.length() - 8) + "_" + String.valueOf(take);
		if (independant_channel == false) {
			_id = _id + "_" + (getVtrIndex() + 1);
		}
		return _id;
	}
	
	void appRec(TextField mediaid, int _take, TextField showname) {
		int _id;
		try {
			_id = Integer.parseInt(mediaid.getText());
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Media ID");
			alert.setHeaderText("Erreur lors de la récupération de l'ID, seuls des chiffres sont autorisés.");
			alert.setContentText("Détail: " + e.getMessage());
			alert.showAndWait();
			return;
		}
		
		if (independant_channel) {
			_id += getVtrIndex();
		}
		
		RecBackgound rec = createRecBackgound(makeValidId(_id, _take), makeName(getServerLabel(), showname));
		rec.start();
	}
	
	void appStop() {
		StopEjectBackgound stop = createStopEjectBackgound();
		stop.start();
	}
	
}
