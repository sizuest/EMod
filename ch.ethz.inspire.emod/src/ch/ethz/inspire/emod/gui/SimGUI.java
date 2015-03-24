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
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.States;
import ch.ethz.inspire.emod.model.units.Unit;
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
		tableStateSequence.setRedraw(false);
		tableStateSequence.clearAll();
		tableStateSequence.setItemCount(0);
		
		//TODO: combos need to be disposed too!!
		
		for( TableColumn tc: tableStateSequence.getColumns() )
			tc.dispose();

		String[] bTitles =  {
				"",
				LocalizationHandler.getItem("app.gui.sim.machinestatesequence.time"),
				LocalizationHandler.getItem("app.gui.sim.machinestatesequence.duration"),
				LocalizationHandler.getItem("app.gui.sim.machinestatesequence.state")};
		for(int i=0; i < bTitles.length; i++){
			TableColumn column = new TableColumn(tableStateSequence, SWT.NULL);
			column.setText(bTitles[i]);
		}
		
		
		
		for(int i=0; i<States.getStateCount(); i++){
			final TableItem item = new TableItem(tableStateSequence, SWT.NONE);
			item.setText(0, i+"" );
			item.setText(1, (States.getTime(i)-States.getDuration(i))+"" );
            item.setText(2, States.getDuration(i).toString());
            
            //stateList = new String[States.getStateCount()]; 
            //stateList[i] = States.getState(i).toString();
            //item.setText(3, States.getState(i).toString());
            
            final int id = i;
            //final double duration = States.getDuration(i);
            
            //create combo to edit State
            TableEditor editor = new TableEditor(tableStateSequence);
            final CCombo comboEditState = new CCombo(tableStateSequence, SWT.PUSH);
            
            String[] items = new String[MachineState.values().length];
            for(MachineState ms : MachineState.values()){
            	items[ms.ordinal()] = ms.name();
            }
            
            comboEditState.setItems(items);
            comboEditState.setText(States.getState(i).toString());
            comboEditState.addSelectionListener(new SelectionListener(){
    			public void widgetSelected(SelectionEvent event){
    				System.out.println("**** " + comboEditState.getText());
    				stateList[id] = comboEditState.getText();
    				//item.setText(3, comboEditState.getText());
        		}
        		public void widgetDefaultSelected(SelectionEvent event){
        		
        		}
        	});
            
            //TODO manick: Spaltenbreite stimmt nicht!
            comboEditState.pack();
            editor.minimumWidth = comboEditState.getSize().x;
            editor.grabHorizontal = true;
            editor.horizontalAlignment = SWT.LEFT;
            editor.setEditor(comboEditState, item, 3);
		}
		
		TableColumn[] columns = tableStateSequence.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
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
		tabCompDBItem.setToolTipText("Initial Conditions");
		tabCompDBItem.setControl(tableSimParam);  
	}
	
	public void initTabStates(TabFolder tabFolder){
		
		//Tabelle fuer State Sequence	
		tableStateSequence = new Table(tabFolder, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tableStateSequence.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableStateSequence.setLinesVisible(true);
		tableStateSequence.setHeaderVisible(true);
		
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
			                            }  
			                        	break;
		                        	case 3:
		                        		// moegliche Werte: ON, OFF, STANDBY, READY, PROCESS;
		                        		for(int j=0; j<MachineState.values().length; j++)
		                        			if(text.getText().equals(MachineState.values()[j].toString())) {
		                        				row.setText(column, text.getText());
		                        				setStateSequence();
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

        //item.setText(3, States.getState(i).toString());
	    
		//Update states
		updateStateSequence();
		
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
		int id = 0;
		for(TableItem ti:tableStateSequence.getItems()){
			System.out.println("SimGUI.setStateSequence: " + ti.getText(0) + " " + ti.getText(2) + " " + stateList[id]);
			States.setState(Integer.valueOf(ti.getText(0)), Double.valueOf(ti.getText(2)), MachineState.valueOf(stateList[id]));
			id++;
		}
		updateStateSequence();
	}
}

