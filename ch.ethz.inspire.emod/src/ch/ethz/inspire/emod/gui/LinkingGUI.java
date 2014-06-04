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
	    		    for (int j=0; j < components.size(); j++){
	    		    	
	    		    	//for each existing component, get the list of inputs
	    		    	List<IOContainer> inputs = components.get(j).getComponent().getInputs();
	    		    	
	    		    	//iterate a second time, over the inputs of the current component
	    		    	for(int k=0; k < inputs.size(); k++){
	    		    		
		    		    	//create table item for each input of the current component
		    		    	TableItem item = new TableItem(tableLinkingView, SWT.NULL);
	    		    		
		    		    	//set content of table item as following: name of component, name of input, unit of input, possible outputs
		    		    	item.setText(0, components.get(j).getName());
		    		    	item.setText(1, inputs.get(k).getName());
		    		    	item.setText(2, inputs.get(k).getUnit().toString());

		    		    	
		    		    	
		    		    	
		    		    	//for column of the possible outputs, a drop down combo is needed
		  		        	TableEditor editor = new TableEditor(tableLinkingView);
		  		        	Combo comboOutputs = new Combo(tableLinkingView, SWT.DROP_DOWN);
		    		    	
		    		    	/*/
		    		    	
		    		    	//TODO manick: NullPointerException???
		    		    	
		    		    	
		    		    	//get the outputs, that can be matched to the current input
		    				
		    		    	//ArrayList<IOContainer> outputs = Machine.getOutputList();
		    		        //ArrayList<IOContainer> outputs = Machine.getOutputList(inputs.get(k).getUnit());
		    		        ArrayList<IOContainer> outputs = Machine.getOutputList(components.get(j), inputs.get(k).getUnit());

		  		        	//iterate over the possivle outputs and add them to the combo
 		  		        	for(int l=0; l < outputs.size(); l++){
		  		        		comboOutputs.setItem(l, outputs.get(l).getName());
		  		        	}

 		  		        	//*/
	  		        	
		  		        	ArrayList<IOContainer> outputs = Machine.getOutputList(components.get(j), inputs.get(k).getUnit());
		  		        	
		  		        	String items[] = {"Output 1", "Output 2", "Output 3", "and so on"};
		  		        	comboOutputs.setItems(items);
 		  		        	
 		  		        	
 		  		        	//combo Selection Listener
 		  		        	//TODO manick: must contain -> set Linking??
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
	    		    
	    		    
	    		    /*/
	    		    for (int i=0; i < inputList.size(); i++){
	    		    	TableItem item = new TableItem(tableLinkingView, SWT.MULTI);
	    		    	item.setText(0, inputList.get(i).getName());

	  		        	TableEditor editor = new TableEditor(tableLinkingView);
	  		        	Combo comboOutputs = new Combo(tableLinkingView, SWT.DROP_DOWN);
	  		        	String items[] = {"Output 1", "Output 2", "Output 3", "and so on"};
	  		        	comboOutputs.setItems(items);
	  		        	
	  		        	comboOutputs.addSelectionListener(new SelectionListener(){
	  		        		public void widgetSelected(SelectionEvent event){
			        		
	  		        			String string = (String) event.data;
	  		        			System.out.println("Ouput selected: " + string);
	  		        		}
	  		        		public void widgetDefaultSelected(SelectionEvent event){
			        		
	  		        		}
	  		        	});
	  		        	
	  		        	comboOutputs.pack();
	  		        	editor.minimumWidth = comboOutputs.getSize().x;
	  		        	editor.horizontalAlignment = SWT.LEFT;
	  		        	editor.setEditor(comboOutputs, item, 1);
	    		    }
	    		    */
	    		    
	    		    
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
