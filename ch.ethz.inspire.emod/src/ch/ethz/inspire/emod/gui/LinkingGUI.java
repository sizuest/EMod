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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class LinkingGUI {

    private Shell shell;

    public LinkingGUI(){

	    }

	public void openLinkingGUI(){
			shell = new Shell(Display.getCurrent());
			
	        shell.setText(LocalizationHandler.getItem("app.gui.linking.title"));
	    	shell.setLayout(new GridLayout(1, false));
	    	
	    	//set title of the frame
	    	Text aText = new Text(shell, SWT.MULTI);
			GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 1;
			aText.setLayoutData(gridData);
			aText.setText(LocalizationHandler.getItem("app.gui.linking.title"));
	    	
			//get List of current Machinecomponents
			ArrayList<MachineComponent> components = Machine.getInstance().getMachineComponentList();
			
			if (components.size()<2){
				shell.dispose();
				//TODO manick: end function!!!
			}
			
	    	//set table for the linking
			//SOURCE http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTableSimpleDemo.htm Imported for function control
	    	Table tableLinkingView = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    	gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 1;
	    	tableLinkingView.setLayoutData(gridData);
			tableLinkingView.setHeaderVisible(true);
			
					//get titles for the 4 columns: name, input, unit, link to
	    		    String[] titles = {LocalizationHandler.getItem("app.gui.linking.name"),
	    		    				   LocalizationHandler.getItem("app.gui.linking.input"),
	    		    				   LocalizationHandler.getItem("app.gui.linking.unit"),
	    		    				   LocalizationHandler.getItem("app.gui.linking.linkto")};

	    		    //set the columns with the titles
	    		    for (int i=0; i < titles.length; i++) {
	    		      TableColumn column = new TableColumn(tableLinkingView, SWT.NULL);
	    		      column.setText(titles[i]);
	    		    }
	    		    
	    		    //iterate twice, first over all existing components
	    		    for (MachineComponent mc:components){
	    		    	//for each existing component, get the list of inputs
	    		    	List<IOContainer> inputs = mc.getComponent().getInputs();
	    		    	
	    		    	//iterate a second time, over the inputs of the current component
	    		    	for(IOContainer io:inputs){
		    		    	//create table item for each input of the current component
		    		    	TableItem item = new TableItem(tableLinkingView, SWT.NULL);
	    		    		
		    		    	//set content of table item as following: name of component, name of input, unit of input, possible outputs		    		    	
		    		    	item.setText(0, mc.getName());
		    		    	item.setText(1, io.getName());
		    		    	item.setText(2, io.getUnit().toString());

		    		    	//for column of the possible outputs, a drop down combo is needed
		  		        	TableEditor editor = new TableEditor(tableLinkingView);
		  		        	Combo comboOutputs = new Combo(tableLinkingView, SWT.DROP_DOWN);
		  		        	
							ArrayList<String> outputs = Machine.getOutputList(mc, io.getUnit());	
		  		        	String[] items = outputs.toArray(new String[outputs.size()]);	  		        	
		  		        	//String items[] = {"Output 1", "Output 2", "Output 3", "and so on"};
		  		        	comboOutputs.setItems(items);
 		  		        	
 		  		        	//combo Selection Listener
 		  		        	//TODO manick: must contain -> write Linking to file? or in save button?
		  		        	comboOutputs.addSelectionListener(new SelectionListener(){
		  		        		public void widgetSelected(SelectionEvent event){
				        		
		  		        			String string = (String) event.data;
		  		        			System.out.println("Ouput selected: " + string);
		  		        		}
		  		        		public void widgetDefaultSelected(SelectionEvent event){
				        		
		  		        		}
		  		        	});
		  		        	
		  		        	//pack combo and display in table
		  		        	comboOutputs.pack();
		  		        	editor.minimumWidth = comboOutputs.getSize().x;
		  		        	editor.horizontalAlignment = SWT.LEFT;
		  		        	editor.setEditor(comboOutputs, item, 3);
		    		    	
	    		    	}
	  
	    		    }
	    		    
			        //Tabelle schreiben
			        TableColumn[] columns = tableLinkingView.getColumns();
			        for (int i = 0; i < columns.length; i++) {
			          columns[i].pack();
			        }

			//SOURCE http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTableSimpleDemo.htm Imported for function control   	
	    	Button buttonSave = new Button(shell, SWT.NONE);
	    	buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
			gridData = new GridData(GridData.END, GridData.END, false, false);
			gridData.horizontalSpan = 1;
			buttonSave.setLayoutData(gridData);
		    buttonSave.addSelectionListener(new SelectionListener(){
		    	public void widgetSelected(SelectionEvent event){
		    		shell.close();
		    		System.out.println("Linking saved");
		    	}
		    	public void widgetDefaultSelected(SelectionEvent event){
		    		
		    	}
		    });
			
		    //shell.setSize(200,200);
			shell.pack();

			//width and height of the shell
			Rectangle rect = shell.getBounds();
			int[] size = {0, 0};
			size[0] = rect.width;
			size[1] = rect.height;
			
			//position the shell into the middle of the last window
	        int[] position;
	        position = EModGUI.shellPosition();
	        shell.setLocation(position[0]-size[0]/2, position[1]-size[1]/2);
			
	        //open the new shell
			shell.open();
	    }

	    public void closeLinkingGUI(){
	    	shell.close();
	    }
}
