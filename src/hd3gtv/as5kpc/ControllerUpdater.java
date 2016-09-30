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

import java.util.ArrayList;

import javafx.scene.control.Label;

abstract class ControllerUpdater {
	
	private ArrayList<VTRSet> vtr_sets;
	
	class VTRSet {
		private Label vtr_name;
		private Label vtr_id;
		private Label vtr_file;
		private Label vtr_status;
		private Label vtr_tc;
		private Label vtr_warn;
		
		VTRSet(Label vtr_name, Label vtr_id, Label vtr_file, Label vtr_status, Label vtr_tc, Label vtr_warn) {
			this.vtr_name = vtr_name;
			this.vtr_id = vtr_id;
			this.vtr_file = vtr_file;
			this.vtr_status = vtr_status;
			this.vtr_tc = vtr_tc;
			this.vtr_warn = vtr_warn;
		}
		
	}
	
	protected abstract ArrayList<VTRSet> getVTRSets();
	
	private VTRSet getVTRSet(int index) {
		if (vtr_sets == null) {
			vtr_sets = getVTRSets();
		}
		return vtr_sets.get(index);
	}
	
	final void updateVTRStatus(int index, String name, String status, String tc, String warn) {
		VTRSet vtrset = getVTRSet(index);
		
		if (vtrset.vtr_name.getText() == null) {
			vtrset.vtr_name.setText(name);
		} else if (vtrset.vtr_name.getText().equals(name) == false) {
			vtrset.vtr_name.setText(name);
		}
		if (vtrset.vtr_status.getText() == null) {
			vtrset.vtr_status.setText(status);
		} else if (vtrset.vtr_status.getText().equals(status) == false) {
			vtrset.vtr_status.setText(status);
		}
		if (vtrset.vtr_tc.getText() == null) {
			vtrset.vtr_tc.setText(tc);
		} else if (vtrset.vtr_tc.getText().equals(tc) == false) {
			vtrset.vtr_tc.setText(tc);
		}
		if (vtrset.vtr_warn.getText() == null) {
			vtrset.vtr_warn.setText(warn);
		} else if (vtrset.vtr_warn.getText().equals(warn) == false) {
			vtrset.vtr_warn.setText(warn);
		}
	}
	
	final void updateVTRMedia(int index, String file, String id) {
		VTRSet vtrset = getVTRSet(index);
		
		if (vtrset.vtr_file.getText().equals(file) == false) {
			vtrset.vtr_file.setText(file);
		}
		if (vtrset.vtr_id.getText().equals(id) == false) {
			vtrset.vtr_id.setText(id);
		}
	}
	
}
