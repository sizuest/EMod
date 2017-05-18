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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.ConfigurationChecker;
import ch.ethz.inspire.emod.gui.EModStatusBarGUI;
import ch.ethz.inspire.emod.simulation.ConfigCheckResult;
import ch.ethz.inspire.emod.simulation.ConfigCheckResult.MessageBundle;

/**
 * @author simon
 *
 */
public class ConfigCheckResultGUI extends Composite {
	
	private ConfigCheckResult results;
	private Table table;
	private Button buttonCheckCfg;

	/**
	 * @param parent
	 * @param style
	 */
	public ConfigCheckResultGUI(Composite parent, int style) {
		super(parent, style);
		
		results = new ConfigCheckResult();
		
		this.setLayout(new GridLayout(1, false));
		
		table = new Table(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		try {
			TableUtils.addCopyToClipboard(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String[] heads = {"", "Origin", "Message"};
		for(String s: heads){
			TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(s);
		}
		
		buttonCheckCfg = new Button(this, SWT.NONE);
		buttonCheckCfg.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
		buttonCheckCfg.setText("Run");
		buttonCheckCfg.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				buttonCheckCfg.setEnabled(false);
				
				ConfigCheckResult ccrMachine = new ConfigCheckResult();
				ConfigCheckResult ccrSimCfg = new ConfigCheckResult();
				ConfigCheckResult ccrProcess = new ConfigCheckResult();
				ConfigCheckResult ccrAll = new ConfigCheckResult();
				
				
				ccrMachine.addAll(ConfigurationChecker.checkMachineConfig());
				ccrSimCfg.addAll(ConfigurationChecker.checkSimulationConfig());
				ccrProcess.addAll(ConfigurationChecker.checkProcess());
				
				ccrAll.addAll(ccrMachine);
				ccrAll.addAll(ccrSimCfg);
				ccrAll.addAll(ccrProcess);
				
				setResults(ccrAll);
				
				EModStatusBarGUI.getConfigStatus().setMachineConfigState(ccrMachine.getStatus());
				EModStatusBarGUI.getConfigStatus().setSimulationConfigState(ccrSimCfg.getStatus());
				EModStatusBarGUI.getConfigStatus().setProcessConfigState(ccrProcess.getStatus());
				
				buttonCheckCfg.setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used*/}
		});
		
		this.layout();
		this.update();
	}
	
	
	/**
	 * Sets the results to be displayed and updates the composite
	 * @param results
	 */
	public void setResults(ConfigCheckResult results){
		this.results = results;
		update();
	}
	
	@Override
	public void update(){
		super.update();
		
		// Clear the table
		for(TableItem ti: table.getItems())
			ti.dispose();
		table.setItemCount(0);
		
		TableItem item;
		
		// Fill Table
		for(MessageBundle mb: results.getMessages()){
			item = new TableItem(table, SWT.NONE);
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
		
		if(results.getMessages().size() == 0)
			item = new TableItem(table, SWT.NONE);
		
		// Pack
		for(TableColumn col: table.getColumns())
			col.pack();
		
		this.layout();
	}

}
