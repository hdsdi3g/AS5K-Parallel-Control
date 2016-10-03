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
import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class ControllerMode1 extends ControllerUpdater {
	
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
	private Label vtr1_status;
	
	@FXML
	private Label vtr1_tc;
	
	@FXML
	private Label vtr1_warn;
	
	protected ArrayList<VTRSet> getVTRSets() {
		ArrayList<VTRSet> sets = new ArrayList<>();
		sets.add(new VTRSet(vtr1_name, vtr1_id, vtr1_file, vtr1_status, vtr1_tc, vtr1_warn));
		return sets;
	}
	
	@FXML
	public void initialize() throws Exception {
		ApplicationMode1.channel1.appInitialize(this);
	}
	
	@FXML
	void onbtncueclick(ActionEvent event) {
		btncue.setDisable(true);
		btnrec.setDisable(true);
		mediaid.setDisable(true);
		showname.setDisable(true);
		
		ApplicationMode1.channel1.appCue(mediaid, takenum, showname, btncue, btnrec);
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
	
	@FXML
	void onbtnrecclick(ActionEvent event) {
		int _take = Integer.valueOf(takenum.getText());
		ApplicationMode1.channel1.appRec(mediaid, _take, showname);
		takenum.setText(String.valueOf(_take + 1));
	}
	
	@FXML
	void onbtnstopclick(ActionEvent event) {
		ApplicationMode1.channel1.appStop();
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
