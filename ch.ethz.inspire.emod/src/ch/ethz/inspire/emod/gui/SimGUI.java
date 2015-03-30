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

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.States;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.simulation.MachineState;
import ch.ethz.inspire.emod.simulation.SimulationState;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * @author manick
 *
 */

public class SimGUI extends AGUITab  {
	
	protected static TabFolder tabFolder;
	
	private Table tableSimParam;
	private Table tableProcessParam;
	private Table tableStateSequence;
	
	// fields used in the Table for the State Sequences
	private String[] stateList;
	
	SimulationState machineState;
	
	/**
	 * @param parent
	 */
	public SimGUI(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(3, false));
		init();
	}

	
	public void init() {
		
		machineState = new SimulationState(PropertiesHandler.getProperty("sim.MachineName"), PropertiesHandler.getProperty("sim.SimulationConfigName"));
		
		//Tab folder for elements
		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		initTabGeneral(tabFolder);
		initTabInitialConditions(tabFolder);
		initTabStates(tabFolder);
		initTabProcess(tabFolder);
		        
	}
	
	@Override
	public void update(){
		updateInitialStates();
		updateStateSequence();
		updateProcess();
		this.redraw();
	}
	

	private void updateInitialStates(){
		
		Machine.loadInitialConditions();
		
		tableSimParam.clearAll();
		tableSimParam.setItemCount(0);
		
		try{
			for (DynamicState s:Machine.getInstance().getDynamicStatesList()){
				TableItem item = new TableItem(tableSimParam, SWT.NONE);
				item.setText(0, s.getParent());
				item.setText(1, s.getName());
				item.setText(2, s.getInitialValue()+"");
				item.setText(3, s.getUnit().toString());
			}
		}
		catch(Exception e){}
        /*for (int i = 0; i < 10; i++) {
            TableItem item = new TableItem(tableSimParam, SWT.NONE);
            item.setText(0, "Parameter " + i);
            item.setText(1, "Initial Value");
        }*/
		
        //Tabelle packen
        TableColumn[] columns = tableSimParam.getColumns();
        for (int i = 0; i < columns.length; i++) {
        	columns[i].pack();
        }
		
	}
	
	private void updateStateSequence(){
		//delete the content of the table
		tableStateSequence.setRedraw(false);
		tableStateSequence.clearAll();
		tableStateSequence.setItemCount(0);
		
		//fill the table with the values form States
		for(int i=0; i<States.getStateCount(); i++){
			addStateSequenceItem(i);
		}
		
		//show new Table
		tableStateSequence.setRedraw(true);
	}
	
	private void updateProcess(){
		tableProcessParam.clearAll();
		tableProcessParam.setItemCount(0);

		for( TableColumn tc: tableProcessParam.getColumns() )
			tc.dispose();
		
		TableColumn column = new TableColumn(tableProcessParam, SWT.NULL);
		column.setText(LocalizationHandler.getItem("app.gui.sim.inputs.time"));
		
		
		
		// Title
		List<ASimulationControl> scList = Machine.getInstance().getVariableInputObjectList();
		if(scList!=null) {
			for(int i=0; i < scList.size(); i++){
				column = new TableColumn(tableProcessParam, SWT.NULL);
				column.setText(scList.get(i).getName()+ " [" +scList.get(i).getUnit().toString() + "]");
			}
		}
		
			
        for (int i = 0; i < 10; i++) {
        	TableItem item = new TableItem(tableProcessParam, SWT.NONE);
            item.setText(0, "00:00");
            item.setText(1, "ABC");
            item.setText(2, "DEF");
            item.setText(3, "GHI");
            item.setText(4, "XYZ");
          }
		
        //Tabelle packen
        TableColumn[] columns = tableProcessParam.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
	}
	
	public void initTabGeneral(TabFolder tabFolder){
		//Tab for State sequence
		TabItem tabGenerlItem = new TabItem(tabFolder, SWT.NONE);
		tabGenerlItem.setText(LocalizationHandler.getItem("app.gui.sim.general.title"));
	}
	
	public void initTabInitialConditions(TabFolder tabFolder){		
		
		//Tabelle fuer Maschinenmodell initieren
		tableSimParam = new Table(tabFolder, SWT.BORDER);
		tableSimParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, true, 1, 1));
		tableSimParam.setLinesVisible(true);
		tableSimParam.setHeaderVisible(true);
		
		
		//Titel der Spalten setzen
		String[] aTitles =  {
				LocalizationHandler.getItem("app.gui.sim.initialconditions.component"),
				LocalizationHandler.getItem("app.gui.sim.initialconditions.state"),
				LocalizationHandler.getItem("app.gui.sim.initialconditions.value"),
				LocalizationHandler.getItem("app.gui.sim.initialconditions.unit")};
		
		for(int i=0; i < aTitles.length; i++){
			TableColumn column = new TableColumn(tableSimParam, SWT.NULL);
			column.setText(aTitles[i]);
		}
	    
	    //versuch manick: inhalt tabelle editieren:
	    //SOURCE http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
	    //create a TableCursor to navigate around the table
	    final TableCursor cursor = new TableCursor(tableSimParam, SWT.NONE);
	    // create an editor to edit the cell when the user hits "ENTER"
	    // while over a cell in the table
	    final ControlEditor editor = new ControlEditor(cursor);
	    editor.grabHorizontal = true;
	    editor.grabVertical = true;
	   
	    cursor.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
	            switch(e.keyCode) {
		            case SWT.ARROW_UP:
		            case SWT.ARROW_RIGHT:
		            case SWT.ARROW_DOWN:
		            case SWT.ARROW_LEFT:
		            //an dieser stelle fehlen auch noch alle anderen tasten die
		            //ignoriert werden sollen...wie F1-12, esc,bsp,....
		                //System.out.println("Taste ignorieren...");
		                break;
		               
		            default:
		                //System.out.println("hier jetzt text editieren");
		                final Text text = new Text(cursor, SWT.NONE);
		                //TableItem row = cursor.getRow();
		                //int column = cursor.getColumn();
		                text.append(String.valueOf(e.character));
		                text.addKeyListener(new KeyAdapter() {
		                    public void keyPressed(KeyEvent e) {
		                        // close the text editor and copy the data over
		                        // when the user hits "ENTER"
		                        if (e.character == SWT.CR) {
		                        	if( Pattern.matches("([0-9]*)\\.([0-9]*)", text.getText()) | 
		                        		Pattern.matches("([0-9]*)", text.getText())) {
		                        		TableItem row = cursor.getRow();
			                            int column = cursor.getColumn();
			                            
			                            if(2==column){
				                            row.setText(column, text.getText());
				                            // Write new value to states
				                            setInitialConditions();
			                            }   
		                        	}
			                        text.dispose();
		                        }
		                        // close the text editor when the user hits "ESC"
		                        if (e.character == SWT.ESC) {
		                            text.dispose();
		                        }
		                    }
		                });
		                editor.setEditor(text);
		                text.setFocus();
		                    break;
	            }  
	        }
	    });
	    
	    //Tab for IC
		TabItem tabCompDBItem = new TabItem(tabFolder, SWT.NONE);
		tabCompDBItem.setText(LocalizationHandler.getItem("app.gui.sim.initialconditions.title"));
		tabCompDBItem.setControl(tableSimParam);  
	}
	
	public void initTabStates(TabFolder tabFolder){
		//Tabelle fuer State Sequence	
		tableStateSequence = new Table(tabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tableStateSequence.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableStateSequence.setLinesVisible(true);
		tableStateSequence.setHeaderVisible(true);
		
		String[] bTitles =  {
				"",
				LocalizationHandler.getItem("app.gui.sim.machinestatesequence.time"),
				LocalizationHandler.getItem("app.gui.sim.machinestatesequence.duration"),
				LocalizationHandler.getItem("app.gui.sim.machinestatesequence.state"),
				"", ""};
		for(int i=0; i < bTitles.length; i++){
			TableColumn column = new TableColumn(tableStateSequence, SWT.NULL);
			column.setText(bTitles[i]);
		}
		
		
		//SOURCE http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
	    //create a TableCursor to navigate around the table
	    final TableCursor cursor = new TableCursor(tableStateSequence, SWT.NONE);
	    // create an editor to edit the cell when the user hits "ENTER"
	    // while over a cell in the table
	    final ControlEditor editor = new ControlEditor(cursor);
	    editor.grabHorizontal = true;
	    editor.grabVertical = true;
	   
	    cursor.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
	            switch(e.keyCode) {
		            case SWT.ARROW_UP:
		            case SWT.ARROW_RIGHT:
		            case SWT.ARROW_DOWN:
		            case SWT.ARROW_LEFT:
		            //an dieser stelle fehlen auch noch alle anderen tasten die
		            //ignoriert werden sollen...wie F1-12, esc,bsp,....
		                //System.out.println("Taste ignorieren...");
		                break;
		               
		            default:
		                //System.out.println("hier jetzt text editieren");
		                final Text text = new Text(cursor, SWT.NONE);
		                //TableItem row = cursor.getRow();
		                //int column = cursor.getColumn();
		                text.append(String.valueOf(e.character));
		                text.addKeyListener(new KeyAdapter() {
		                    public void keyPressed(KeyEvent e) {
		                        // close the text editor and copy the data over
		                        // when the user hits "ENTER"
		                        if (e.character == SWT.CR) {
		                        	TableItem row = cursor.getRow();
		                            int column = cursor.getColumn();
		                        	switch(column){
		                        	case 2:
			                        	if( Pattern.matches("([0-9]*)\\.([0-9]*)", text.getText()) | 
			                        		Pattern.matches("([0-9]*)", text.getText())) {
				                            	row.setText(column, text.getText());
				                            	// Write new value to states
				                            	setStateSequence();
				                            	updateStateSequence();
			                            }  
			                        	break;
		                        	case 3:
		                        		// moegliche Werte: ON, OFF, STANDBY, READY, PROCESS;
		                        		for(int j=0; j<MachineState.values().length; j++)
		                        			if(text.getText().equals(MachineState.values()[j].toString())) {
		                        				row.setText(column, text.getText());
		                        				setStateSequence();
		                        				//updateStateSequence(); //not necessary here, the drop-down-combo takes care of this
		                        			}
		                        		break;
		                        	}
			                        text.dispose();
		                        }
		                        // close the text editor when the user hits "ESC"
		                        if (e.character == SWT.ESC) {
		                            text.dispose();
		                        }
		                    }
		                });
		                editor.setEditor(text);
		                text.setFocus();
		                    break;
	            }  
	        }
	    });
		
	    // workaround for the combo to select the state
        stateList = new String[States.getStateCount()]; 
        for(int i=0; i < States.getStateCount(); i++){
            stateList[i] = States.getState(i).toString();
        }
        
		for(int i=0; i<States.getStateCount(); i++){
			addStateSequenceItem(i);
		}
        
		
		//Tab for State sequence
		TabItem tabStatesItem = new TabItem(tabFolder, SWT.NONE);
		tabStatesItem.setText(LocalizationHandler.getItem("app.gui.sim.machinestatesequence.title"));
		tabStatesItem.setControl(tableStateSequence);
	}
	
	public void initTabProcess(TabFolder tabFolder){
		//Tabelle fuer Prozess initieren
		tableProcessParam = new Table(tabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		//gridData.widthHint = 600;
		//gridData.heightHint = 300;
		tableProcessParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, true, 1, 1));
		tableProcessParam.setLinesVisible(true);
		tableProcessParam.setHeaderVisible(true);
		
		//Titel der Spalten setzen
		updateProcess();
		
		//Tab for State sequence
		TabItem tabProcessItem = new TabItem(tabFolder, SWT.NONE);
		tabProcessItem.setText(LocalizationHandler.getItem("app.gui.sim.inputs.title"));
		tabProcessItem.setControl(tableProcessParam);
	}

	private void setInitialConditions(){
		// Read table
		for(TableItem ti:tableSimParam.getItems()){
			Machine.getInstance().getDynamicState(ti.getText(1), ti.getText(0)).setInitialCondition(Double.parseDouble(ti.getText(2)));
		}
	}
	
	private void setStateSequence(){
		for(TableItem ti:tableStateSequence.getItems()){
			States.setState(Integer.valueOf(ti.getText(0)), Double.valueOf(ti.getText(2)), MachineState.valueOf(ti.getText(3)));
		}
		//updateStateSequence();
	}
	
	private void addStateSequenceItem(int index){
		tableStateSequence.setRedraw(false);
		
        //create new table item in the tableModelView
        final TableItem item = new TableItem(tableStateSequence, SWT.NONE, index);
        
        //create combo for drop-down selection of the state
        final CCombo comboEditState = new CCombo(tableStateSequence, SWT.PUSH);
        //create button to append a new state
        final Button buttonAddState = new Button(tableStateSequence, SWT.PUSH);
        //create button to delete the last state of the list
    	final Button buttonDeleteState = new Button(tableStateSequence, SWT.PUSH);
        
        //write id, time, duration and state to table
        //first colum: id
        item.setText(0, String.valueOf(index));
        
        //second column: time (if first item, time = 0.00)
        if(index == 0){
        	item.setText(1, "0.00");
        }
        else {
        	Double startTime = //States.getTime(index-1) + States.getDuration(index-1);
        					   Double.parseDouble(tableStateSequence.getItem(index-1).getText(1)) +
        					   Double.parseDouble(tableStateSequence.getItem(index-1).getText(2));
        	item.setText(1, String.valueOf(startTime));
        }
        
        //third column: duration
        item.setText(2, States.getDuration(index).toString());
        //fourth column: state
        item.setText(3, States.getState(index).toString());	
        
        //get the values for the drop-down combo
        String[] comboItems = new String[MachineState.values().length];
        for(MachineState ms : MachineState.values()){
        	comboItems[ms.ordinal()] = ms.name();
        }
        comboEditState.setItems(comboItems);
        
        //prefill the combo with the current state
        final int id = index;
        final Double duration = States.getDuration(index);
        comboEditState.setText(States.getState(index).toString());
        comboEditState.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		//set the selected value into the cell behind the combo (needed for the updateProcess)
        		item.setText(3, comboEditState.getText());
        		//set the state
        		States.setState(id, duration, MachineState.valueOf(comboEditState.getText()));
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
        
        //pack the combo and set it into the cell
        comboEditState.pack();
        final TableEditor editor = new TableEditor(tableStateSequence);
        editor.minimumWidth = comboEditState.getSize().x;
        int widthColumnFour = comboEditState.getSize().x;
        editor.grabHorizontal = true;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(comboEditState, item, 3);
        
        //the last entry of the list has a delete/add button
        if(index == States.getStateCount()-1){  

            //create button to add a new row
            Image imageAdd = new Image(Display.getDefault(), "src/resources/Add16.gif");
            buttonAddState.setImage(imageAdd);
            buttonAddState.addSelectionListener(new SelectionListener(){
            	public void widgetSelected(SelectionEvent event){
            		//append a state with duration 0 and state OFF
            		States.appendState(0, MachineState.OFF);
            		//get rid of the buttons, refresh table
            		buttonAddState.dispose();
               		buttonDeleteState.dispose();
               		updateStateSequence();
            	}
            	public void widgetDefaultSelected(SelectionEvent event){
            		
            	}
            });
            //pack the button and set it into the cell
            buttonAddState.pack();
            TableEditor editor2 = new TableEditor(tableStateSequence);  
            editor2.minimumWidth = buttonAddState.getSize().x;
            editor2.horizontalAlignment = SWT.LEFT;
            editor2.setEditor(buttonAddState, item, 5);
            
        	//create button to delete last row
            Image imageDelete = new Image(Display.getDefault(), "src/resources/Delete16.gif");
            buttonDeleteState.setImage(imageDelete);
            buttonDeleteState.addSelectionListener(new SelectionListener(){
            	public void widgetSelected(SelectionEvent event){
            		//delete the cell, remove the state from the statesList, refresh table
            		item.dispose();
					States.getStateMap().remove(id);
            		updateStateSequence();
            	}
            	public void widgetDefaultSelected(SelectionEvent event){
            		
            	}
            });
            //pack the button and set it into the cell
            buttonDeleteState.pack();
            TableEditor editor3 = new TableEditor(tableStateSequence);
            editor3.minimumWidth = buttonDeleteState.getSize().x;
            editor3.horizontalAlignment = SWT.LEFT;
            editor3.setEditor(buttonDeleteState, item, 4);
        }
        
		TableColumn[] columns = tableStateSequence.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
        tableStateSequence.getColumn(3).setWidth(widthColumnFour);
        
        //if a cell gets deleted, make shure that the combo and buttons get deleted too!
        item.addDisposeListener(new DisposeListener(){
			@Override
			public void widgetDisposed(DisposeEvent e) {
				comboEditState.dispose();
				buttonAddState.dispose();
				buttonDeleteState.dispose();
			}
        });
        tableStateSequence.setRedraw(true);
	}
}

