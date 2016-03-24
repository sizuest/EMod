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

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.States;
import ch.ethz.inspire.emod.simulation.EModSimulationRun;
import ch.ethz.inspire.emod.simulation.SimulationState;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * @author manick
 *
 */

public class SimGUI extends AGUITab  {
	
	private static Logger logger = Logger.getLogger(EModGUI.class.getName());
	
	protected static TabFolder tabFolder;
	
	private InitialConditionGUI initialConditionGUI;
	private StatesGUI statesGUI;
	private ProcessGUI processGUI;
	
	private Button buttonCheckCfg, buttonRunSim;
	
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
		
		//Sub-GUIs
		initialConditionGUI = new InitialConditionGUI(tabFolder, SWT.NONE);
		statesGUI = new StatesGUI(tabFolder, SWT.NONE);
		processGUI = new ProcessGUI(tabFolder, SWT.NONE);
		
		//Tab for IC
		TabItem tabCompDBItem = new TabItem(tabFolder, SWT.NONE);
		tabCompDBItem.setText(LocalizationHandler.getItem("app.gui.sim.initialconditions.title"));
		tabCompDBItem.setControl(initialConditionGUI); 
		
		//Tab for State sequence
		TabItem tabStatesItem = new TabItem(tabFolder, SWT.NONE);
		tabStatesItem.setText(LocalizationHandler.getItem("app.gui.sim.machinestatesequence.title"));
		tabStatesItem.setControl(statesGUI);
		
		//Tab for State sequence
		TabItem tabProcessItem = new TabItem(tabFolder, SWT.NONE);
		tabProcessItem.setText(LocalizationHandler.getItem("app.gui.sim.inputs.title"));
		tabProcessItem.setControl(processGUI);
		
		//Selection Listener
		tabFolder.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkIfActual();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});		        
	}
	
	private void checkIfActual(){
		initialConditionGUI.askForSaving();
		statesGUI.askForSaving();
		processGUI.askForSaving();
	}
	
	public void reset(){
		initialConditionGUI.reset();
		statesGUI.reset();
		processGUI.reset();
	}
	
	@Override
	public void update(){
		initialConditionGUI.update();
		statesGUI.update();
		processGUI.update();
		this.redraw();
	}
	
	public void initTabGeneral(TabFolder tabFolder){
		//Tab for State sequence
		TabItem tabGenerlItem = new TabItem(tabFolder, SWT.NONE);
		tabGenerlItem.setText(LocalizationHandler.getItem("app.gui.sim.general.title"));
		
		//add composite to the tabfolder for different values to change
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		//add text for stepsize
		Text textStepSize = new Text(composite, SWT.READ_ONLY);
		textStepSize.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false, 1, 1));
		textStepSize.setText(LocalizationHandler.getItem("app.gui.sim.general.stepsize"));
		
		//add value_field for stepsize
		Text valueStepSize = new Text(composite, SWT.NONE);
		valueStepSize.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false, 1, 1));
		valueStepSize.setText("TODO: let user change step size here");
		
		//set the composite to the tab and show it
		tabGenerlItem.setControl(composite);
		tabFolder.pack();
		
		buttonCheckCfg = new Button(composite, SWT.NONE);
		buttonCheckCfg.setText(LocalizationHandler.getItem("app.gui.sim.general.checkcfg"));
		buttonCheckCfg.setEnabled(false);
		
		buttonRunSim = new Button(composite, SWT.NONE);
		buttonRunSim.setText(LocalizationHandler.getItem("app.gui.sim.general.runsim"));
		buttonRunSim.setEnabled(true);
		buttonRunSim.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				runSimulation();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	
	private void runSimulation(){
		System.out.println("Simulation start initialization: Saving Machine and IOLinking");
		// Save all
		Machine.saveMachine(PropertiesHandler.getProperty("sim.MachineName"), PropertiesHandler.getProperty("sim.MachineConfigName"));
		States.saveStates(PropertiesHandler.getProperty("sim.MachineName"), PropertiesHandler.getProperty("sim.SimulationConfigName"));
		// manick: EModSimRun contains all the necessary commands to run a simulation
		EModSimulationRun.EModSimRun();
		logger.log(LogLevel.DEBUG, "simulation run");
	}
	
}

