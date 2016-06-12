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

import java.awt.Desktop;
import java.net.URI;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import hd3gtv.as5kpc.Serverchannel.AboutServerbackgound;
import hd3gtv.as5kpc.Serverchannel.BackgroundWatcher;
import hd3gtv.as5kpc.Serverchannel.GetFreeClipIdBackgound;
import hd3gtv.as5kpc.Serverchannel.RecBackgound;
import hd3gtv.as5kpc.Serverchannel.StopEjectBackgound;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

public class FormController {
	
	private static Logger log = LogManager.getLogger("as5kpc.app");
	
	@FXML
	private Button btnrec;
	
	@FXML
	private TextField mediaid;
	
	@FXML
	private Label takenum;
	
	@FXML
	private TextField showname;
	
	@FXML
	private Button btnstop;
	
	@FXML
	private Button btncue;
	
	@FXML
	private Label vtr1_name;
	
	@FXML
	private Label vtr1_id;
	
	@FXML
	private Label vtr1_file;
	
	@FXML
	private Label vtr2_id;
	
	@FXML
	private Label vtr2_file;
	
	@FXML
	private Label vtr1_status;
	
	@FXML
	private Label vtr1_tc;
	
	@FXML
	private Label vtr2_name;
	
	@FXML
	private Label vtr2_status;
	
	@FXML
	private Label vtr2_tc;
	
	@FXML
	private Label vtr1_warn;
	
	@FXML
	private Label vtr2_warn;
	
	@FXML
	public void initialize() throws Exception {
		
		MainClass.channels.forEach(channel -> {
			AboutServerbackgound absb = channel.createAboutServerbackgound();
			
			absb.setOnSucceeded((WorkerStateEvent event_absb) -> {
				MainClass.scene.setCursor(Cursor.DEFAULT);
				
				ServerResponseAbout about = absb.getValue();
				updateVTRMedia(channel.getVtrIndex(), "PROTOCOL VERSION: " + about.version, "");
				if (about.can_record) {
					updateVTRStatus(channel.getVtrIndex(), about.osd_name + " #" + about.ch_num, about.channel_name, "--:--:--:--", "Loading...");
				} else {
					updateVTRStatus(channel.getVtrIndex(), about.osd_name + " #" + about.ch_num, about.channel_name, "--:--:--:--", "CAN'T RECORD");
					return;
				}
				BackgroundWatcher bw = channel.createBackgroundWatcher();
				bw.setDelay(Duration.seconds(1));
				
				bw.setOnSucceeded((WorkerStateEvent event_bw) -> {
					ServerResponseStatus status = bw.getValue();
					String warn = "";
					if (status.rec_mode == false) {
						warn = "Standby";
					} else if (status.has_video == false) {
						warn = "NO VIDEO!";
					}
					
					updateVTRStatus(channel.getVtrIndex(), about.osd_name, status.control, status.actual_tc, warn);
					updateVTRMedia(channel.getVtrIndex(), status.active_name, status.active_id);
				});
				
				bw.start();
			});
			
			absb.setOnFailed((WorkerStateEvent event_absb_e) -> {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Erreur");
				alert.setHeaderText("Impossible de communiquer avec le serveur");
				alert.setContentText("La machine " + channel.toString() + " n'est pas joignable. Verifiez vos parametres réseau et la configuration du client et/ou du serveur.");
				
				alert.showAndWait();
				System.exit(1);
			});
			
			absb.start();
		});
	}
	
	@FXML
	void onbtncueclick(ActionEvent event) {
		btncue.setDisable(true);
		btnrec.setDisable(true);
		mediaid.setDisable(true);
		showname.setDisable(true);
		
		MainClass.channels.forEach(channel -> {
			
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
			
			GetFreeClipIdBackgound gfip = channel.getFreeClipIdBackgound(id + channel.getVtrIndex(), take);
			
			gfip.setOnSucceeded((WorkerStateEvent ev) -> {
				takenum.setText(gfip.getValue().get("take"));
				String first_name = gfip.getValue().get("first_name");
				if (first_name != null) {
					final String _first_name = first_name.substring("00-00 ".length());
					showname.setText(_first_name);
					MainClass.channels.forEach(chn -> {
						if (_first_name.endsWith(chn.getServerLabel())) {
							showname.setText(_first_name.substring(0, _first_name.length() - chn.getServerLabel().length()).trim());
						}
					});
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
		});
	}
	
	@FXML
	void onchangemediaref(KeyEvent event) {
		boolean hide_btn = true;
		
		if (mediaid.getText().length() > 0) {
			hide_btn = (this.showname.getText().length() == 0);
		}
		
		btncue.setDisable(hide_btn);
		btnrec.setDisable(true);
		if (hide_btn == false) {
			takenum.setText("1");
		}
	}
	
	private void updateVTRStatus(int index, String name, String status, String tc, String warn) {
		if (index == 0) {
			if (vtr1_name.getText() == null) {
				vtr1_name.setText(name);
			} else if (vtr1_name.getText().equals(name) == false) {
				vtr1_name.setText(name);
			}
			if (vtr1_status.getText() == null) {
				vtr1_status.setText(status);
			} else if (vtr1_status.getText().equals(status) == false) {
				vtr1_status.setText(status);
			}
			if (vtr1_tc.getText() == null) {
				vtr1_tc.setText(tc);
			} else if (vtr1_tc.getText().equals(tc) == false) {
				vtr1_tc.setText(tc);
			}
			if (vtr1_warn.getText() == null) {
				vtr1_warn.setText(warn);
			} else if (vtr1_warn.getText().equals(warn) == false) {
				vtr1_warn.setText(warn);
			}
		} else if (index == 1) {
			if (vtr2_name.getText() == null) {
				vtr2_name.setText(name);
			} else if (vtr2_name.getText().equals(name) == false) {
				vtr2_name.setText(name);
			}
			if (vtr2_status.getText() == null) {
				vtr2_status.setText(status);
			} else if (vtr2_status.getText().equals(status) == false) {
				vtr2_status.setText(status);
			}
			if (vtr2_tc.getText() == null) {
				vtr2_tc.setText(tc);
			} else if (vtr2_tc.getText().equals(tc) == false) {
				vtr2_tc.setText(tc);
			}
			if (vtr2_warn.getText() == null) {
				vtr2_warn.setText(warn);
			} else if (vtr2_warn.getText().equals(warn) == false) {
				vtr2_warn.setText(warn);
			}
		} else {
			log.error("Invalid index for update");
		}
	}
	
	private void updateVTRMedia(int index, String file, String id) {
		if (index == 0) {
			if (vtr1_file.getText().equals(file) == false) {
				vtr1_file.setText(file);
			}
			if (vtr1_id.getText().equals(id) == false) {
				vtr1_id.setText(id);
			}
		} else if (index == 1) {
			if (vtr2_file.getText().equals(file) == false) {
				vtr2_file.setText(file);
			}
			if (vtr2_id.getText().equals(id) == false) {
				vtr2_id.setText(id);
			}
		} else {
			log.error("Invalid index for update");
		}
	}
	
	@FXML
	void onbtnrecclick(ActionEvent event) {
		
		int _take = Integer.valueOf(takenum.getText());
		
		MainClass.channels.forEach(channel -> {
			int _id;
			try {
				_id = Integer.parseInt(mediaid.getText()) + channel.getVtrIndex();
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Media ID");
				alert.setHeaderText("Erreur lors de la récupération de l'ID, seuls des chiffres sont autorisés.");
				alert.setContentText("Détail: " + e.getMessage());
				alert.showAndWait();
				return;
			}
			
			RecBackgound rec = channel.createRecBackgound(Serverchannel.makeValidId(_id, _take), makeName(channel.getServerLabel()));
			rec.start();
		});
		
		takenum.setText(String.valueOf(_take + 1));
	}
	
	public static final Pattern PATTERN_Combining_Diacritical_Marks = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	
	private String makeName(String server_label) {
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
		
		if (show_name.endsWith(server_label) == false) {
			sb.append(" ");
			sb.append(server_label);
		}
		return sb.toString();
	}
	
	@FXML
	void onbtnstopclick(ActionEvent event) {
		MainClass.channels.forEach(channel -> {
			StopEjectBackgound stop = channel.createStopEjectBackgound();
			stop.start();
		});
	}
	
	@FXML
	private Hyperlink url_copyr;
	
	@FXML
	void onurlcopyrclick(ActionEvent event) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(new URI("https://github.com/hdsdi3g/AS5K-Parallel-Control"));
			} catch (Exception e) {
			}
		}
	}
	
}
