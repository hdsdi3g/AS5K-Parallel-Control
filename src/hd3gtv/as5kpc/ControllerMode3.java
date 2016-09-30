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

public class ControllerMode3 extends ControllerUpdater {
	
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
	private Label vtr3_status;
	
	@FXML
	private Label vtr3_warn;
	
	@FXML
	private Label vtr3_tc;
	
	@FXML
	private Label vtr3_name;
	
	@FXML
	private Label vtr3_id;
	
	@FXML
	private Label vtr3_file;
	
	protected ArrayList<VTRSet> getVTRSets() {
		ArrayList<VTRSet> sets = new ArrayList<>();
		sets.add(new VTRSet(vtr1_name, vtr1_id, vtr1_file, vtr1_status, vtr1_tc, vtr1_warn));
		sets.add(new VTRSet(vtr2_name, vtr2_id, vtr2_file, vtr2_status, vtr2_tc, vtr2_warn));
		sets.add(new VTRSet(vtr3_name, vtr3_id, vtr3_file, vtr3_status, vtr3_tc, vtr3_warn));
		return sets;
	}
	
	@FXML
	public void initialize() throws Exception {
		ApplicationMode3.channel1.appInitialize(this);
		ApplicationMode3.channel2.appInitialize(this);
		ApplicationMode3.channel3.appInitialize(this);
	}
	
	@FXML
	void onbtncueclick(ActionEvent event) {
		btncue.setDisable(true);
		btnrec.setDisable(true);
		mediaid.setDisable(true);
		showname.setDisable(true);
		
		ApplicationMode3.channel1.appCue(mediaid, takenum, showname, btncue, btnrec);
		ApplicationMode3.channel2.appCue(mediaid, takenum, showname, btncue, btnrec);
		ApplicationMode3.channel3.appCue(mediaid, takenum, showname, btncue, btnrec);
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
		ApplicationMode3.channel1.appRec(mediaid, _take, showname);
		ApplicationMode3.channel2.appRec(mediaid, _take, showname);
		ApplicationMode3.channel3.appRec(mediaid, _take, showname);
		takenum.setText(String.valueOf(_take + 1));
	}
	
	@FXML
	void onbtnstopclick(ActionEvent event) {
		ApplicationMode3.channel1.appStop();
		ApplicationMode3.channel2.appStop();
		ApplicationMode3.channel3.appStop();
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
