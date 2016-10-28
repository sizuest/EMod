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

package ch.ethz.inspire.emod.dd.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.dd.model.Isolation;
import ch.ethz.inspire.emod.gui.AConfigGUI;
import ch.ethz.inspire.emod.gui.SelectMaterialGUI;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.parameters.ParameterSet;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class EditDuctIsolationGUI extends AConfigGUI{
    private Table tableProperties;
    private Isolation isolationOld, isolationNew;
    private ParameterSet parameters = new ParameterSet();
    Button buttonEditMaterial;

	public EditDuctIsolationGUI(Composite parent, int style, final Isolation iso) {
		super(parent, style);
		
		if(null!=iso){
			this.isolationOld = iso;
			this.isolationNew = iso.clone();
			parameters = this.isolationOld.getParameterSet();
		}
		else
			parameters = (new Isolation()).getParameterSet();
		
		this.getContent().setLayout(new GridLayout(1, true));
		
		tableProperties = new Table(this.getContent(), SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		tableProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableProperties.setLinesVisible(true);
		tableProperties.setHeaderVisible(true);
		
		String[] titles =  {LocalizationHandler.getItem("app.dd.elemet.gui.property"),
							LocalizationHandler.getItem("app.dd.elemet.gui.value"),
							LocalizationHandler.getItem("app.dd.elemet.gui.unit"),
							"        "};
		
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableProperties, SWT.NULL);
			column.setText(titles[i]);
			column.setWidth(32);
		}
	    
	    updatePropertyTable();
	}
	
	public static Shell editDuctIsolationGUI(Shell parent, Isolation isolation) {
		final Shell shell = new Shell(parent, SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX| SWT.RESIZE);
		shell.setLayout(new GridLayout());
		
		final EditDuctIsolationGUI gui = new EditDuctIsolationGUI(shell, SWT.NONE, isolation);
		
		shell.setText(LocalizationHandler.getItem("app.dd.elemet.gui.isolation.titel"));
		
		shell.layout();
		shell.open();
		shell.layout();
		
		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		shell.addControlListener(new ControlListener() {
			
			@Override
			public void controlResized(ControlEvent e) {
				gui.layout();
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				gui.layout();
			}
		});
		
		gui.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				shell.dispose();
			}
		});
		
		return shell;
	}
	
	private void setIsolationThickness(){
		try{
			parameters.setPhysicalValue(tableProperties.getItem(1).getText(0), Double.parseDouble(tableProperties.getItem(1).getText(1)), new SiUnit(tableProperties.getItem(1).getText(2)));
    		isolationNew.setParameterSet(parameters);
		}
		catch(Exception ex){
			// Not used
		}
	}

	private void updatePropertyTable() {
		if(null!=buttonEditMaterial)
			buttonEditMaterial.dispose();
		
		tableProperties.clearAll();
    	tableProperties.setItemCount(0);
    	
    	for(Control c: tableProperties.getChildren())
    		c.dispose();
			
		
		try {
			TableUtils.addCellEditor(tableProperties, this, new int[]{1});
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	new TableItem(tableProperties, SWT.NONE, 0); //Material
    	updateIsolationItem();
    	
        for(String key: parameters.getParameterSet().keySet()){
        	final int idx = tableProperties.getItemCount();
        	final TableItem itemParam = new TableItem(tableProperties, SWT.NONE, idx);
        	
        	itemParam.setText(0, key);
        	itemParam.setText(1, parameters.getPhysicalValue(key).getValue()+"");
        	itemParam.setText(2, parameters.getPhysicalValue(key).getUnit().toString());
        	
        }   
        
        for(int i=0; i<3; i++){
        	final int idx = tableProperties.getItemCount();
        	final TableItem itemParam = new TableItem(tableProperties, SWT.NONE, idx);
        	itemParam.setText(0, "");
        }
        
        TableColumn[] columns = tableProperties.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }         
        
        this.pack();
        this.layout();
        this.getShell().pack();
        this.getShell().layout();
	}
	
	private void updateIsolationItem(){
		TableItem itemMaterial = tableProperties.getItem(0);
		
		itemMaterial.setText(0, LocalizationHandler.getItem("app.dd.elemet.gui.isolation.material"));
    	if( null!=isolationNew & null!=isolationNew.getMaterial() )
    		itemMaterial.setText(1, isolationNew.getMaterial().getType());
    	else
    		itemMaterial.setText(1, LocalizationHandler.getItem("app.dd.elemet.gui.isolation.none"));
    	
    	TableEditor editorButton = new TableEditor(tableProperties);
    	
    	buttonEditMaterial = new Button(tableProperties, SWT.NONE);
    	buttonEditMaterial.setText("...");
    	buttonEditMaterial.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
    	buttonEditMaterial.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		openMaterialGUI();
        		wasEdited();
        	}
			public void widgetDefaultSelected(SelectionEvent event){
				// Not used
        	}
        });
    	buttonEditMaterial.pack();
		editorButton.minimumWidth = buttonEditMaterial.getSize().x;
		editorButton.horizontalAlignment = SWT.LEFT;
        editorButton.setEditor(buttonEditMaterial, itemMaterial, 3);
	}
	
	private void setMaterial(String type){
		isolationNew.setMaterial(type);
		updateIsolationItem();
	}
	
	private void openMaterialGUI(){
		SelectMaterialGUI matGUI = new SelectMaterialGUI(this.getShell());
    	String selection = matGUI.open();
    	if(selection != "" & selection !=null)
    		setMaterial(selection);
	}

	@Override
	public void save() {
		setIsolationThickness();
		isolationOld.setIsolation(isolationNew);
		updatePropertyTable();
	}

	@Override
	public void reset() {
		isolationNew = isolationOld.clone();
		if(null!=isolationNew){
			parameters = this.isolationNew.getParameterSet();
		}
		else
			parameters = (new Isolation()).getParameterSet();
		
		updatePropertyTable();
	}

}
