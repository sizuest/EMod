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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class LinkingGUI {

    private Shell shell;

	int intNumberOfInputs = 0;
	private Text[] textName;
	private Text[] textInput;
	private Text[] textUnit;
    private String[] stringLinkTo;

    public LinkingGUI(){

	    }

	public void openLinkingGUI(){
			shell = new Shell(Display.getCurrent());
	        shell.setText(LocalizationHandler.getItem("app.gui.linking.title"));
	    	shell.setLayout(new GridLayout(4, false));
	    	
			//get titles for the 4 columns: name, input, unit, link to
		    String[] titles = {LocalizationHandler.getItem("app.gui.linking.name"),
		    				   LocalizationHandler.getItem("app.gui.linking.input"),
		    				   LocalizationHandler.getItem("app.gui.linking.unit"),
		    				   LocalizationHandler.getItem("app.gui.linking.linkto")};
		    
		    //set the four titles of the columns
		    for(String str:titles){
		    	Text textTitle = new Text(shell, SWT.READ_ONLY);
		    	textTitle.setText(str);
		    	textTitle.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
		    }
			
			//get List of current Machine Components and IOLinkList
			ArrayList<MachineComponent> components = Machine.getInstance().getMachineComponentList();
			List<IOConnection> linking = Machine.getInstance().getIOLinkList();
			
			//if one or zero components are added to the machine, output warning and return to main shell
			if (components.size()<2){
				MessageBox messageBox = new MessageBox(shell);
				messageBox.setText(LocalizationHandler.getItem("app.gui.linking.warn"));
				messageBox.setMessage(LocalizationHandler.getItem("app.gui.linking.warnmessage"));
				messageBox.open();
				return;
			}

			//get the number of connections that have to be made 
			for(MachineComponent mc:components){
				intNumberOfInputs += mc.getComponent().getInputs().size();
			}
			
			//for each connection a Name, Input, Unit is needed 
			textName = new Text[intNumberOfInputs];
			textInput = new Text[intNumberOfInputs];
			textUnit = new Text[intNumberOfInputs];

			//for each connection a Combo is needed, the values of the combo then will be written to the stringLinkTo
			final Combo[] comboLinkTo = new Combo[intNumberOfInputs];
			stringLinkTo = new String[intNumberOfInputs];

			//iterate over all possible connections, i.e. first iterate over machine components
			int i = 0;
			for(MachineComponent mc:components){
				
		    	//for each existing component, get the list of inputs
		    	List<IOContainer> inputs = mc.getComponent().getInputs();
		    	
		    	//iterate a second time, over the inputs of the current component
		    	for(IOContainer io:inputs){
		    		//get the outputs, that can be linked to the current input (from other component, same unit)
		    		ArrayList<String> outputs = Machine.getOutputList(mc, io.getUnit());	
		    		
		    		//create a textfield to show the name of the component
		    		textName[i] = new Text(shell, SWT.READ_ONLY | SWT.BORDER);
		    		textName[i].setText(mc.getName());
		    		textName[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
		    		
		    		//create a textfield to show the name of the input
		    		textInput[i] = new Text(shell, SWT.READ_ONLY | SWT.BORDER);
		    		textInput[i].setText(io.getName());
		    		textInput[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
		    		
		    		//create a textfield to show the unit of the input
		    		textUnit[i] = new Text(shell, SWT.READ_ONLY | SWT.BORDER);
		    		textUnit[i].setText(io.getUnit().toString());
		    		textUnit[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
				
		    		//create a combo for the selection of the desired output
		    		comboLinkTo[i] = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER);
		    		comboLinkTo[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		    		String[] items = outputs.toArray(new String[outputs.size()]);
		    		comboLinkTo[i].setItems(items);

		    		//prefil the combo with the value from the iolinking file
		    		//TODO manick: just the name, not the component of the linking are delivered??
		    		for(IOConnection li:linking){
						if(li.getTarget().equals(io)){
							comboLinkTo[i].setText(li.getSoure().getName());
						}
					}

		    		//needed to get the value of i inside the selection listener
		    		final int k = i;
		    		
		    		//combo Selection Listener
		    		comboLinkTo[i].addSelectionListener(new SelectionListener(){
		    			public void widgetSelected(SelectionEvent event){
		    				//when a output was selected in the combo -> write it to the stringLinkTo in the current position
		    				String string = comboLinkTo[k].getText();
		    				stringLinkTo[k] = string;
		    			}
		    			public void widgetDefaultSelected(SelectionEvent event){
	        				        		
		    			}
		    		});
		
		    		i++;
		    	}
			}
			
			//Button to save the linking
			Button buttonSave = new Button(shell, SWT.NONE);
	    	buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
			buttonSave.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false, 4, 1));
		    //button selection listener
			buttonSave.addSelectionListener(new SelectionListener(){
		    	public void widgetSelected(SelectionEvent event){

		    		//TODO manick: get path from app.config
		    		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
		    					  PropertiesHandler.getProperty("sim.MachineName") + "/MachineConfig/" +
		    				      PropertiesHandler.getProperty("sim.MachineConfigName") + "/";
		    		
		    		//try to write to file
		    		try {
		    			//the file is in the machinepath, named IOLinking.txt
						Writer w = new FileWriter(path + "IOLinking.txt");

						//get all units form the enum unit
						Unit[] unitlist = Unit.values();
						
						//iterate over all inputs
			    		for(int i = 0; i < intNumberOfInputs; i++){
			    			//only write to the file, if a output was selected
			    			if(stringLinkTo[i] != null){
			    				//the source is the current entry of the stringLinkTo
			    				String stringSource = stringLinkTo[i];
			    				//the target is from the matching row, in the form name.input
			    				String stringTarget = textName[i].getText() + "." + textInput[i].getText();
			    			
			    				//write to file
								w.write(stringTarget+" = "+stringSource+"\n");
			    				
								//get the unit of the current link and look up its position in the unitlist
								int k = 0;
								for(int j = 0; j < unitlist.length; j++){
									if(unitlist[j].toString().equals(textUnit[i].getText())){
										//if a match was made, save the position, exit loop
										k = j;
										continue;
									}
								}

								//create source and target iocontainer with name, unit, value, type
								//TODO manick: value, type are not considered at the moment
			    				IOContainer source = new IOContainer(stringSource, unitlist[k], 0.0, ContainerType.NONE);
			    				IOContainer target = new IOContainer(stringTarget, unitlist[k], 0.0, ContainerType.NONE);
			    				
			    				//add IOLink to the Machine
								Machine.addIOLink(source, target);
			    			}
			    		}
			    		//close file
						w.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

		    		
		    		//TODO manick: saveIOLinking doesn't write anything to file and returns errors at rerun
		    		//Machine.saveIOLinking(path);
			
		    		shell.close();
		    		System.out.println("Linking saved");
		    	}
		    	public void widgetDefaultSelected(SelectionEvent event){
		    		
		    	}
		    });
			
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
