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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class LinkingGUI {

    private Shell shell;

	int intNumberOfInputs = 0;
    private String[] stringLinkFrom, stringLinkTo;
    private Table linkingTable;
    private CCombo[] comboLinkTo;
    private Button[] buttonDelete;
    private TableEditor[] editorCombo;
    private Text textFilter;
    Button buttonSave, buttonClose;
    
    ArrayList<MachineComponent> components;
	List<ASimulationControl> mdlInputs;
	List<IOConnection> linking;

    public LinkingGUI(){}

 	/**
	 * open and initialize the linking GUI
	 */ 
	public void openLinkingGUI(){
			shell = new Shell(Display.getCurrent());
	        shell.setText(LocalizationHandler.getItem("app.gui.linking.title"));
	    	shell.setLayout(new GridLayout(2, false));
	    	
			init();
			
	        //open the new shell
			shell.open();
	}
	
	public void init(){
		
		textFilter = new Text(shell, SWT.BORDER | SWT.SINGLE);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		textFilter.setMessage("Filter");
		
		textFilter.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				redraw();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// Not used
			}
		});
		
		linkingTable = new Table(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		linkingTable.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
		linkingTable.setLinesVisible(true);
		linkingTable.setHeaderVisible(true);
		
		//get titles for the 4 columns: name, input, unit, link to
	    String[] titles = {LocalizationHandler.getItem("app.gui.linking.input"),
	    				   LocalizationHandler.getItem("app.gui.linking.unit"),
	    				   LocalizationHandler.getItem("app.gui.linking.linkto"),
	    				   "        "};
	    
	    //set the four titles of the columns
	    for(int i=0; i < titles.length; i++){
	    	TableColumn column = new TableColumn(linkingTable, SWT.NULL);
			column.setText(titles[i]);
	    }
	  
		
		// Fill table
	    update();
		
		//Button to save the linking
		buttonSave = new Button(shell, SWT.NONE);
    	buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
		buttonSave.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
	    //button selection listener
		buttonSave.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){
	    		//path to the current Machine Configuration folders with the machine.xml and iolinking.txt
	    		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
	    					  PropertiesHandler.getProperty("sim.MachineName") + "/MachineConfig/" +
	    				      PropertiesHandler.getProperty("sim.MachineConfigName") + "/";
					
				//iterate over all inputs
		    	for(int i = 0; i < intNumberOfInputs; i++){
		    		//only write to the file, if a output was selected
		    		if(stringLinkTo[i] != null){
		    			//the source is the current entry of the stringLinkTo -> create IO container source
		    			String stringSource = stringLinkTo[i];
						String[] splitSource = stringSource.split("\\.", 2);
						
						IOContainer source;
						if(splitSource.length>1)
							source = Machine.getMachineComponent(splitSource[0]).getComponent().getOutput(splitSource[1]);
						else
							source = Machine.getInputObject(splitSource[0]).getOutput();
		    			
						//the target is from the matching row, in the form name.input -> create IO container target
		    			String stringTarget = stringLinkFrom[i];
						String[] splitTarget = stringTarget.split("\\.", 2);
						IOContainer target = Machine.getMachineComponent(splitTarget[0]).getComponent().getInput(splitTarget[1]);
		    				
						// remove existing links to target
						Machine.removeIOLink(target);
		    			//add IOLink to the Machine
						Machine.addIOLink(source, target);
		    		}
		    	}
		    	
		    	//TODO sizuest: already existing linkings are written a second time to the IOLinking.txt file
				Machine.saveIOLinking(path);
	    		System.out.println("Linking saved");
	    		update();
	    	}
	    	public void widgetDefaultSelected(SelectionEvent event){
	    		// Not used
	    	}
	    });
		
		//Button to save the linking
		buttonClose = new Button(shell, SWT.NONE);
		buttonClose.setText(LocalizationHandler.getItem("app.gui.close"));
		buttonClose.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false, 1, 1));
	    //button selection listener
		buttonClose.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){		
	    		shell.close();
	    	}
	    	public void widgetDefaultSelected(SelectionEvent event){
	    		// Not used
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
	}
	
	public void update(){
    	
		//get List of current Machine Components and IOLinkList
		components = Machine.getInstance().getMachineComponentList();
		mdlInputs  = Machine.getInstance().getInputObjectList();
		linking    = Machine.getInstance().getIOLinkList();
					
		//if one or zero components are added to the machine, output warning and return to main shell
		if (components.size()+mdlInputs.size()<2){
			MessageBox messageBox = new MessageBox(shell);
			messageBox.setText(LocalizationHandler.getItem("app.gui.linking.warn"));
			messageBox.setMessage(LocalizationHandler.getItem("app.gui.linking.warnmessage"));
			messageBox.open();
			return;
		}

		//get the number of connections that have to be made 
		intNumberOfInputs = 0;
		for(MachineComponent mc:components){
			intNumberOfInputs += mc.getComponent().getInputs().size();
		}
		
		// Clear existing combos
		if(null!=comboLinkTo)
			for(int i=0; i<intNumberOfInputs; i++){
				if(null!=comboLinkTo[i]){
					comboLinkTo[i].dispose();
					buttonDelete[i].dispose();
					editorCombo[i].dispose();
				}
			}

		//The values of the combo then will be written to the stringLinkTo
		stringLinkFrom = new String[intNumberOfInputs];
		stringLinkTo   = new String[intNumberOfInputs];
		comboLinkTo    = new CCombo[intNumberOfInputs];
		buttonDelete   = new Button[intNumberOfInputs];
		editorCombo    = new TableEditor[intNumberOfInputs];

		redraw();
		
	}
	
	private void redraw(){
		int widthCombo = 10;
		
		for(TableItem ti: linkingTable.getItems())
			ti.dispose();
		
		linkingTable.clearAll();
		linkingTable.setItemCount(0);
		
		//iterate over all possible connections, i.e. first iterate over machine components
		int i = 0;
		for(MachineComponent mc:components){
			
	    	//for each existing component, get the list of inputs
	    	List<IOContainer> inputs = mc.getComponent().getInputs();
	    	
	    	// Check if component matches filter string. If not, skip
	    	if(!(mc.getName().toLowerCase().contains(textFilter.getText().toLowerCase()))){
	    		i+=inputs.size();
	    		continue;
	    	}
	    	
	    	TableItem componentLine  = new TableItem(linkingTable, SWT.NONE, linkingTable.getItemCount());
	    	componentLine.setText(0, mc.getName());
	    	componentLine.setFont(new Font(componentLine.getDisplay(), componentLine.getFont().getFontData()[0].getName(), componentLine.getFont().getFontData()[0].getHeight(), SWT.BOLD));
	    	
	    	//iterate a second time, over the inputs of the current component
	    	for(IOContainer io:inputs){
	    		//get the outputs, that can be linked to the current input (from other component, same unit)
	    		ArrayList<String> outputs;
	    		if(io instanceof FluidContainer)
	    			outputs = Machine.getFluidOutputList(mc);
	    		else
	    			outputs = Machine.getOutputList(mc, io.getUnit());	
	    		
	    		TableItem inputLine  = new TableItem(linkingTable, SWT.NONE, linkingTable.getItemCount());
	    		if(io.equals(io.getReference()))
	    			inputLine.setText(0, io.getName());
	    		if(!(io instanceof FluidContainer))
	    			inputLine.setText(1, io.getUnit().toString());
	    		
	    		/* Output */
	    		editorCombo[i] = new TableEditor(linkingTable);
	    		comboLinkTo[i] = new CCombo(linkingTable, SWT.DROP_DOWN);
	    		comboLinkTo[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
	    		String[] items = outputs.toArray(new String[outputs.size()]);
	    		comboLinkTo[i].setItems(items);
	    		//prefil the combo with the value from the iolinking file
	    		for(IOConnection li:linking){
					if(li.getTarget().equals(io)){
						comboLinkTo[i].setText(Machine.getOutputFullName(li.getSource()));
						break;
					}
				}
	    		String tmp = comboLinkTo[i].getText(); 
	    		if(tmp.equals(""))
	    			comboLinkTo[i].setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
	    		else
	    			comboLinkTo[i].setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	    		
	    		//needed to get the value of i inside the selection listener
	    		final int k = i;
	    		//combo Selection Listener
	    		stringLinkFrom[k] = mc.getName()+"."+io.getName();
	    		comboLinkTo[i].addSelectionListener(new SelectionListener(){
	    			public void widgetSelected(SelectionEvent event){
	    				//when a output was selected in the combo -> write it to the stringLinkTo in the current position
	    				String string = comboLinkTo[k].getText();
	    				stringLinkTo[k] = string;
	    				comboLinkTo[k].setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	    			}
	    			public void widgetDefaultSelected(SelectionEvent event){
	    				// Not used
	    			}
	    		});
	    		comboLinkTo[i].pack();
	    		editorCombo[i].minimumWidth = comboLinkTo[i].getSize().x;
	    		editorCombo[i].horizontalAlignment = SWT.LEFT;
	    		editorCombo[i].setEditor(comboLinkTo[i], inputLine, 2);
	    		
	    		widthCombo = Math.max(widthCombo, comboLinkTo[i].getSize().x);
	    		
	    		/* Remove */
	    		TableEditor editorButton = new TableEditor(linkingTable);
	    		final IOContainer target = io;
	    		buttonDelete[i] = new Button(linkingTable, SWT.PUSH);
		        Image imageDelete = new Image(Display.getDefault(), "src/resources/Delete16.gif");
		        buttonDelete[i].setImage(imageDelete);
		        buttonDelete[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
		        buttonDelete[i].addSelectionListener(new SelectionListener(){
		        	public void widgetSelected(SelectionEvent event){
		        		stringLinkTo[k] = null;
		        		comboLinkTo[k].setText("");
		        		Machine.removeIOLink(target);
		        	}
		        	public void widgetDefaultSelected(SelectionEvent event){
		        		// Not used
		        	}
		        });
		        buttonDelete[i].pack();
				editorButton.minimumWidth = buttonDelete[i].getSize().x;
				editorButton.horizontalAlignment = SWT.LEFT;
		        editorButton.setEditor(buttonDelete[i], inputLine, 3);
	    		
		    	i++;
	    	}
		}
		
		for(int j=0; j<intNumberOfInputs; j++){
			if(null != comboLinkTo[j])
				if(!(comboLinkTo[j].isDisposed())){
					editorCombo[j].minimumWidth = widthCombo;
					comboLinkTo[j].pack();
				}
		}
			
		TableColumn[] columns = linkingTable.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
        columns[2].setWidth(widthCombo);
	}
	
 	/**
	 * close the linking GUI
	 */ 
	public void closeLinkingGUI(){
	  	shell.close();
	}
}
