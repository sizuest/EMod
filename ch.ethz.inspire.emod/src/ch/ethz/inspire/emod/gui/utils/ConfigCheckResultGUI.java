/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.gui.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.simulation.ConfigCheckResult;
import ch.ethz.inspire.emod.simulation.ConfigCheckResult.MessageBundle;

/**
 * @author simon
 *
 */
public class ConfigCheckResultGUI extends Composite {
	
	private ConfigCheckResult results;
	private Table table;

	/**
	 * @param parent
	 * @param style
	 */
	public ConfigCheckResultGUI(Composite parent, int style) {
		super(parent, style);
		
		results = new ConfigCheckResult();
		
		this.setLayout(new FillLayout());
		
		table = new Table(this, SWT.NONE);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		String[] heads = {"", "Origin", "Message"};
		for(String s: heads){
			TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(s);
		}
		
		this.layout();
	}
	
	
	/**
	 * Sets the results to be displayed and updates the composite
	 * @param results
	 */
	public void setResults(ConfigCheckResult results){
		this.results = results;
		update();
	}
	
	public void update(){
		super.update();
		
		// Clear the table
		for(TableItem ti: table.getItems())
			ti.dispose();
		table.setItemCount(0);
		
		// Fill Table
		for(MessageBundle mb: results.getMessages()){
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, "  ");
			item.setText(1, mb.getOrigin()+"  ");
			item.setText(2, mb.getMessage());
			
			switch(mb.getState()){
			case OK:
				item.setBackground(0, new Color(getDisplay(), 0, 255, 0)); // Green
				break;
			case WARNING:
				item.setBackground(0, new Color(getDisplay(), 255, 255, 0)); // Yellow
				break;
			case ERROR:
				item.setBackground(0, new Color(getDisplay(), 255, 0, 0)); // Red
			}
		}
		
		// Pack
		for(TableColumn col: table.getColumns())
			col.pack();
		
		this.layout();
	}

}
