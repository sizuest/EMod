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

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.dd.gui.DuctConfigGUI;
import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * General GUI to Edit Machine Components
 * 
 * @author sizuest
 *
 */
public class EditMachineComponentGUI extends AConfigGUI{

    private static Table tableComponent;
    private ComponentConfigReader component;
    String type, parameter;

    /**
     * EditMachineComponentGUI
     * @param parent 
     * @param style 
     * @param type 
     * @param parameter 
     */
    public EditMachineComponentGUI(Composite parent, int style, String type, String parameter){
    	super(parent, style, ShowButtons.ALL);
    	
    	this.type = type;
    	this.parameter = parameter;
    	
    	try {
			component = new ComponentConfigReader(type, parameter);
		} catch (Exception e) {
			System.err.println("Failed to open Parameter set'"+type+":"+parameter+"'");
			e.printStackTrace();
		}
    	
    	this.getContent().setLayout(new GridLayout(1, true));
    	
    	tableComponent = new Table(this.getContent(), SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
    	tableComponent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    	tableComponent.setLinesVisible(true);
    	tableComponent.setHeaderVisible(true);
    	
    	String[] titles =  {LocalizationHandler.getItem("app.gui.compdb.property"),
    						LocalizationHandler.getItem("app.gui.compdb.value"),
    						LocalizationHandler.getItem("app.gui.compdb.unit"),
							LocalizationHandler.getItem("app.gui.compdb.description")};
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableComponent, SWT.NULL);
			column.setText(titles[i]);
		}
    	
    	update();
    }

 	/**
	 * Component Edit GUI for creating a new Component
 	 * @param parent 
	 */
    public static void newMachineComponentGUI(Shell parent){
    	final Shell shell = new Shell(parent, SWT.TITLE|SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX);
        shell.setText(LocalizationHandler.getItem("app.gui.compdb.newcomp"));
    	shell.setLayout(new GridLayout(2, false));

		//Text "Type" of the Component
		Text textComponentType = new Text(shell, SWT.READ_ONLY);
		textComponentType.setText(LocalizationHandler.getItem("app.gui.model.type") + ":");
		textComponentType.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
		
		//Combo to select Value of "Type" of the Component
		final Combo comboComponentTypeValue = new Combo(shell, SWT.NONE);
		
		//according to the given component, get the path for the parameter sets
		final String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		File subdir = new File(path);
    	
    	//check if the directory exists, then show possible parameter sets to select
    	if(subdir.exists()){
    		String[] subitems = subdir.list();
    		Arrays.sort(subitems);
    		comboComponentTypeValue.setItems(subitems);
    	}
		
		comboComponentTypeValue.setText(LocalizationHandler.getItem("app.gui.compdb.selecttype"));
		comboComponentTypeValue.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
		
		//Text "Model Type" of the Component
		Text textComponentModelType = new Text(shell, SWT.READ_ONLY);
		textComponentModelType.setText(LocalizationHandler.getItem("app.gui.compdb.compname"));
		textComponentModelType.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
		
		//Combo to let the user select the desired model type of the Component
		final Text textComponentModelTypeValue = new Text(shell, SWT.NONE);
		textComponentModelTypeValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

    	//button to continue
		Button buttonContinue = new Button(shell, SWT.NONE);
		buttonContinue.setText(LocalizationHandler.getItem("app.gui.continue"));
		//selection Listener for the button, actions when button is pressed
		buttonContinue.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){
	    		String stringCompTypeValue = comboComponentTypeValue.getText();
	    		String stringCompParamValue = textComponentModelTypeValue.getText();
	    		
	    		//copy the example type of the selected component and create a copy
	    		//SOURCE for the file copy: http://www.javapractices.com/topic/TopicAction.do?Id=246 
	    		Path from = Paths.get(path + stringCompTypeValue + "/" + stringCompTypeValue + "_Example.xml");
	    	    Path to = Paths.get(path + stringCompTypeValue + "/" + stringCompTypeValue + "_" + stringCompParamValue + ".xml");
	    	    //overwrite existing file, if exists
	    	    CopyOption[] options = new CopyOption[]{
	    	      StandardCopyOption.REPLACE_EXISTING,
	    	      StandardCopyOption.COPY_ATTRIBUTES
	    	    }; 
	    	    try {
					Files.copy(from, to, options);
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		
	    		shell.close();
	    		
	    		//open the edit ComponentEditGUI with the newly created component file
	    		EditMachineComponentGUI.editMachineComponentGUI(shell, stringCompTypeValue, stringCompParamValue);
	    	}
	    	public void widgetDefaultSelected(SelectionEvent event){
	    		// Not used
	    	}
	    });
		buttonContinue.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 2, 1));
		
		shell.pack();

		//width and height of the shell
		Rectangle rect = shell.getBounds();
		int[] size = {0, 0};
		size[0] = rect.width;
		size[1] = rect.height;
		
		//position the shell into the middle of the last window
        //int[] position;
        //position = EModGUI.shellPosition();
        //shell.setLocation(position[0]-size[0]/2, position[1]-size[1]/2);
		
        //open the new shell
		shell.open();
    }
   
    
 	/**
	 * Component Edit GUI for editing a existing Component of the Component DB
 	 * @param parent 
 	 * @param type 
 	 * @param parameter 
	 */
    public static void editMachineComponentGUI(final Shell parent, final String type, final String parameter){
    	final Shell shell = new Shell(parent, SWT.TITLE|SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX);
        shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
        shell.setLayout(new GridLayout(1, true));
    	
    	EditMachineComponentGUI gui = new EditMachineComponentGUI(shell, SWT.NONE, type, parameter);
		
    	shell.pack();
		
		shell.layout();
		shell.redraw();
		shell.open();
		
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				parent.setEnabled(true);
			}
		});
		
		gui.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				shell.dispose();
			}
		});
    }
    
    public void openModelSelectGUI(String type, TableItem item){
    	SelectMachineComponentGUI compGUI= new SelectMachineComponentGUI(this.getShell());		        		
		String selection = compGUI.open(type);
		if(selection != "" & selection !=null)
			item.setText(1, selection);
    }
    
    public void openMaterialSelectGUI(TableItem item){
    	SelectMaterialGUI matGUI = new SelectMaterialGUI(this.getShell());
    	String selection = matGUI.open();
    	if(selection != "" & selection !=null)
			item.setText(1, selection);
    }
    
    public void setModelType(String type, TableItem item){
    	if(item.getText().matches(""))
			item.setText(1, type);
		else
			item.setText(1, item.getText(1)+", "+type);
    }
    
    public void setMaterialType(String type, TableItem item){
    	item.setText(1, type);
    }
    
    public void update(){
    	if(null==component)
    		return;
    	
    	for(Control c: tableComponent.getChildren())
    		c.dispose();
    	
    	try {
			TableUtils.addCellEditor(tableComponent, this, new int[]{1});
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	tableComponent.setItemCount(0);	
    	
    	ArrayList<String> keys = component.getKeys();
    	Collections.sort(keys);
    	
		for(String key: keys){
			
			try{
				String value = component.getString(key);
				
				int i                    = tableComponent.getItemCount();
				final TableItem itemProp = new TableItem(tableComponent, SWT.NONE, i);
				TableEditor editorButton = new TableEditor(tableComponent);
				
				itemProp.setText(0, key);
				itemProp.setText(1, value);
				
				/* SPECIAL CASE: Material */
				if(key.matches("[a-zA-Z]*Material")){
					final Button selectMaterialButton = new Button(tableComponent, SWT.PUSH);
					selectMaterialButton.setText("...");
					selectMaterialButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
					selectMaterialButton.addSelectionListener(new SelectionListener(){
			        	public void widgetSelected(SelectionEvent event){
			        		openMaterialSelectGUI(itemProp);
			        		wasEdited();
			        	}
			        	public void widgetDefaultSelected(SelectionEvent event){
			        		// Not used
			        	}
			        });
					selectMaterialButton.pack();
					editorButton.minimumWidth = selectMaterialButton.getSize().x;
					editorButton.horizontalAlignment = SWT.LEFT;
			        editorButton.setEditor(selectMaterialButton, itemProp, 2);
				}
				/* SPECIAL CASE: Model */
				else if(key.matches("[a-zA-Z]+Type")){
					final String modelClass = key.split("Type")[0];
					final Button selectMaterialButton = new Button(tableComponent, SWT.PUSH);
					selectMaterialButton.setText("...");
					selectMaterialButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
					selectMaterialButton.addSelectionListener(new SelectionListener(){
			        	public void widgetSelected(SelectionEvent event){
			        		openModelSelectGUI(modelClass, itemProp);
			        		wasEdited();
			        	}
			        	public void widgetDefaultSelected(SelectionEvent event){
			        		// Not used
			        	}
			        });
					selectMaterialButton.pack();
					editorButton.minimumWidth = selectMaterialButton.getSize().x;
					editorButton.horizontalAlignment = SWT.LEFT;
			        editorButton.setEditor(selectMaterialButton, itemProp, 2);
				}
				
				/* SPECIAL CASE: Duct */
				else if(key.matches("[a-zA-Z]+Duct")){
					final String name = value;
					final Button editDuctButton = new Button(tableComponent, SWT.PUSH);
					editDuctButton.setText("...");
					editDuctButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
					editDuctButton.addSelectionListener(new SelectionListener(){
			        	public void widgetSelected(SelectionEvent event){
			        		DuctConfigGUI.editDuctGUI(type, parameter,  name);	
			        		wasEdited();
			        	}
			        	public void widgetDefaultSelected(SelectionEvent event){
			        		// Not used
			        	}
			        });
					editDuctButton.pack();
					editorButton.minimumWidth = editDuctButton.getSize().x;
					editorButton.horizontalAlignment = SWT.LEFT;
			        editorButton.setEditor(editDuctButton, itemProp, 2);
				}
			}
			catch(Exception e){
				System.err.println("Failed to load Property '"+key+"' from '"+type+":"+parameter+"'");
				e.printStackTrace();
			}
		}
		
		TableColumn[] columns = tableComponent.getColumns();
        for (int j = 0; j < columns.length; j++) {
          columns[j].pack();
        }
    }

	@Override
	public void save() {
		for(TableItem ti: tableComponent.getItems()){
    		component.setValue(ti.getText(0), ti.getText(1));
    	}
		
		try {
			component.saveValues();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		update();
	}

	@Override
	public void reset() {
		try {
			component = new ComponentConfigReader(type, parameter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		update();
	}
    
}
