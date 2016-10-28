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
package ch.ethz.inspire.emod.gui;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.simulation.ComponentState;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;


public class EditStaticSimulationControlGUI extends AEditInputComposite {
	
	private Table tableStateStateMap, tableStateOutputMap;
	private TabFolder tabFolder;
	protected ConfigReader input;
	
	
	public EditStaticSimulationControlGUI(Composite parent, int style, ASimulationControl sc) {
		super(parent, style, sc);
		
		this.getContent().setLayout(new GridLayout(1, true));
		for(Control c: this.getContent().getChildren())
			c.dispose();
		
		tabFolder = new TabFolder(this.getContent(), SWT.FILL);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    	
		tableStateStateMap = new Table(tabFolder, SWT.FILL | SWT.SINGLE | SWT.V_SCROLL);
		tableStateStateMap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableStateStateMap.setLinesVisible(true);
		tableStateStateMap.setHeaderVisible(true);
		
		tableStateOutputMap = new Table(tabFolder, SWT.FILL | SWT.SINGLE | SWT.V_SCROLL);
		tableStateOutputMap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableStateOutputMap.setLinesVisible(true);
		tableStateOutputMap.setHeaderVisible(true);
		
		String[] titlesStates =  { "Machine State",
								   "Component State" };
		for(int i=0; i < titlesStates.length; i++){
			TableColumn column = new TableColumn(tableStateStateMap, SWT.NULL);
			column.setText(titlesStates[i]);
		}
		
		String[] titlesOutput =  { "Component State",
	                               LocalizationHandler.getItem("app.gui.compdb.value") };
		for(int i=0; i < titlesOutput.length; i++){
			TableColumn column = new TableColumn(tableStateOutputMap, SWT.NULL);
			column.setText(titlesOutput[i]);
		}
		
		TabItem tabState = new TabItem(tabFolder, SWT.FILL);
		tabState.setText("M State > C State");
		tabState.setControl(tableStateStateMap);
		
		TabItem tabOutput = new TabItem(tabFolder, SWT.FILL);
		tabOutput.setText("C State > Output");
		tabOutput.setControl(tableStateOutputMap);
		
		try {
			TableUtils.addCellEditor(tableStateOutputMap, this, new int[]{1});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
				  PropertiesHandler.getProperty("sim.MachineName") + "/" +
				  "MachineConfig/" +
				  PropertiesHandler.getProperty("sim.MachineConfigName") + "/" +
				  sc.getType() + "_" + sc.getName() + ".xml";
	
	  	try {
			input = new ConfigReader(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	  	
	  	update();
		
	}
	
	
	public void update(){
		if(null==tableStateStateMap)
			return;
		
		tableStateStateMap.setItemCount(0);		
		for(Control c: tableStateStateMap.getChildren() )
			c.dispose();
		tableStateOutputMap.setItemCount(0);	

		
		for(String key: input.getKeys()){
			if(key.contains("_state")){
				final TableItem item = new TableItem(tableStateStateMap, SWT.NONE);
				item.setText(0, key.replace("_state", ""));
				
				TableEditor editor = new TableEditor(tableStateStateMap);
		        final CCombo comboState = new CCombo(tableStateStateMap, SWT.PUSH);
		        
		        String[] items = new String[ComponentState.values().length];
		        for(int i=0; i<items.length; i++)
		        	items[i] = ComponentState.values()[i].toString();
		        comboState.setItems(items);
				
				try {
					item.setText(1, input.getString(key));
					comboState.setText(input.getString(key));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				comboState.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						item.setText(1, comboState.getText());
						wasEdited();
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
				});
				
				editor.grabHorizontal = true;
		        editor.horizontalAlignment = SWT.LEFT;
				editor.setEditor(comboState, item, 1);
			}
			else{
				TableItem item = new TableItem(tableStateOutputMap, SWT.NONE);
				item.setText(0, key);
				try {
					item.setText(1, input.getString(key));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
				
		}
		
		TableColumn[] columns = tableStateStateMap.getColumns();
        for (int j = 0; j < columns.length; j++) {
          columns[j].pack();
        }
        
        columns = tableStateOutputMap.getColumns();
        for (int j = 0; j < columns.length; j++) {
          columns[j].pack();
        }
	}



	@Override
	public void save() {
		for(TableItem ti: tableStateStateMap.getItems())
			input.setValue(ti.getText(0)+"_state", ti.getText(1));
		
		for(TableItem ti: tableStateOutputMap.getItems())
			input.setValue(ti.getText(0), ti.getText(1));
		
		try {
			input.saveValues();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		sc.readConfig();
	}

	@Override
	public void init() {
		tabFolder = new TabFolder(this.getContent(), SWT.FILL);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    	
		tableStateStateMap = new Table(tabFolder, SWT.FILL | SWT.SINGLE | SWT.V_SCROLL);
		tableStateStateMap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableStateStateMap.setLinesVisible(true);
		tableStateStateMap.setHeaderVisible(true);
		
		tableStateOutputMap = new Table(tabFolder, SWT.FILL | SWT.SINGLE | SWT.V_SCROLL);
		tableStateOutputMap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableStateOutputMap.setLinesVisible(true);
		tableStateOutputMap.setHeaderVisible(true);
		
		String[] titlesStates =  { "Machine State",
								   "Component State" };
		for(int i=0; i < titlesStates.length; i++){
			TableColumn column = new TableColumn(tableStateStateMap, SWT.NULL);
			column.setText(titlesStates[i]);
		}
		
		String[] titlesOutput =  { "Component State",
	                               LocalizationHandler.getItem("app.gui.compdb.value") };
		for(int i=0; i < titlesOutput.length; i++){
			TableColumn column = new TableColumn(tableStateOutputMap, SWT.NULL);
			column.setText(titlesOutput[i]);
		}
		
		TabItem tabState = new TabItem(tabFolder, SWT.FILL);
		tabState.setText("M State > C State");
		tabState.setControl(tableStateStateMap);
		
		TabItem tabOutput = new TabItem(tabFolder, SWT.FILL);
		tabOutput.setText("C State > Output");
		tabOutput.setControl(tableStateOutputMap);
		
		try {
			TableUtils.addCellEditor(tableStateOutputMap, this, new int[]{1});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void reset() {
		try {
			input = new ConfigReader(input.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		update();
	}

}
