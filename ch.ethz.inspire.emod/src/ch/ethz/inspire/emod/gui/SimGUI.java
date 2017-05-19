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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.States;
import ch.ethz.inspire.emod.gui.utils.ConfigCheckResultGUI;
import ch.ethz.inspire.emod.simulation.EModSimulationMain;
import ch.ethz.inspire.emod.simulation.EModSimulationSimulationThread;
import ch.ethz.inspire.emod.simulation.SimulationState;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * @author manick
 * 
 */

public class SimGUI extends AGUITab {

	private static Logger logger = Logger.getLogger(EModGUI.class.getName());

	protected static TabFolder tabFolder;

	private InitialConditionGUI initialConditionGUI;
	private StatesGUI statesGUI;
	private ProcessGUI processGUI;
	
	private ConfigCheckResultGUI checkResults;

	private Button buttonRunSim;

	private boolean simulationWasRunning = false;

	SimulationState machineState;
	
	
	private EditSimTimeStepGUI guiTimeStep;
	private EditFluidSolverParametersGUI guiFluidSolver;
	private EditOutputGUI guiOutputCtrl;

	/**
	 * @param parent
	 */
	public SimGUI(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(3, false));
		init();
	}

	@Override
	public void init() {

		machineState = new SimulationState(
				EModSession.getMachineName(),
				EModSession.getSimulationConfig());

		// Tab folder for elements
		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setBackground(getParent().getBackground());

		initTabGeneral(tabFolder);

		// Sub-GUIs
		initialConditionGUI = new InitialConditionGUI(tabFolder, SWT.NONE);
		statesGUI = new StatesGUI(tabFolder, SWT.NONE);
		processGUI = new ProcessGUI(tabFolder, SWT.NONE);

		// Tab for IC
		TabItem tabCompDBItem = new TabItem(tabFolder, SWT.NONE);
		tabCompDBItem.setText(LocalizationHandler.getItem("app.gui.sim.initialconditions.title"));
		tabCompDBItem.setControl(initialConditionGUI);

		// Tab for State sequence
		TabItem tabStatesItem = new TabItem(tabFolder, SWT.NONE);
		tabStatesItem.setText(LocalizationHandler.getItem("app.gui.sim.machinestatesequence.title"));
		tabStatesItem.setControl(statesGUI);

		// Tab for State sequence
		TabItem tabProcessItem = new TabItem(tabFolder, SWT.NONE);
		tabProcessItem.setText(LocalizationHandler.getItem("app.gui.sim.inputs.title"));
		tabProcessItem.setControl(processGUI);

		// Selection Listener
		tabFolder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkIfActual();

				int idx = -1;

				for (int i = 0; i < tabFolder.getTabList().length; i++)
					if (e.item.equals(tabFolder.getItem(i))) {
						idx = i;
						break;
					}

				/* Update tab content */
				switch (idx) {
				case 1:
					initialConditionGUI.update();
					break;
				case 2:
					statesGUI.update();
					break;
				case 3:
					processGUI.reset();
					break;
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void checkIfActual() {
		initialConditionGUI.askForSaving();
		statesGUI.askForSaving();
		processGUI.askForSaving();
	}

	/**
	 * Resets the guis state
	 */
	public void reset() {
		initialConditionGUI.reset();
		statesGUI.reset();
		processGUI.reset();
	}

	@Override
	public void update() {
		tabFolder.setSelection(0);
		
		statesGUI.reset();
		initialConditionGUI.update();

		/* Update simulation information */
		if (EModSimulationMain.getRunningStatus()) {
			EModStatusBarGUI.getProgressBar().setText(
					EModSimulationMain.getMessage());
			EModStatusBarGUI.getProgressBar().updateProgressbar(
					EModSimulationMain.getProgress(),
					!EModSimulationMain.getForcedStop());
			EModSimulationMain.setForcedStop(EModStatusBarGUI.getProgressBar()
					.getCancelStatus());
		} else if (simulationWasRunning) {
			buttonRunSim.setEnabled(true);
			EModStatusBarGUI.getProgressBar().reset();
			simulationWasRunning = false;
		}
		
		guiFluidSolver.update();
		guiOutputCtrl.update();
		guiTimeStep.update();

		this.redraw();
	}

	private void initTabGeneral(TabFolder tabFolder) {
		// Tab for State sequence
		TabItem tabGenerlItem = new TabItem(tabFolder, SWT.NONE);
		tabGenerlItem.setText(LocalizationHandler.getItem("app.gui.sim.general.title"));

		// add composite to the tabfolder for different values to change
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(2, true));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group groupCheck = new Group(composite, SWT.NONE);
		groupCheck.setText(LocalizationHandler.getItem("app.gui.sim.general.checkcfg"));
		groupCheck.setLayout(new GridLayout(1, false));
		groupCheck.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
		
		Group groupIntegrator  = new Group(composite, SWT.NONE);
		groupIntegrator.setLayout(new GridLayout(1, false));
		groupIntegrator.setText(LocalizationHandler.getItem("app.gui.sim.integrator.titel"));
		groupIntegrator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Group groupFluid = new Group(composite, SWT.NONE);
		groupFluid.setLayout(new GridLayout(1, false));
		groupFluid.setText(LocalizationHandler.getItem("app.gui.sim.fc.titel"));
		groupFluid.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Group groupOutput = new Group(composite, SWT.NONE);
		groupOutput.setLayout(new GridLayout(1, false));
		groupOutput.setText(LocalizationHandler.getItem("app.gui.sim.output.title"));
		groupOutput.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Group groupRun = new Group(composite, SWT.NONE);
		groupRun.setText(LocalizationHandler.getItem("app.gui.sim.general.runsim"));
		groupRun.setLayout(new GridLayout(2, false));
		groupRun.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		
		/* Create sub controls */
		guiTimeStep    = new EditSimTimeStepGUI(groupIntegrator, SWT.NONE);
		guiTimeStep.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		guiFluidSolver = new EditFluidSolverParametersGUI(groupFluid, SWT.NONE);
		guiFluidSolver.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		guiOutputCtrl  = new EditOutputGUI(groupOutput, SWT.NONE);
		guiOutputCtrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		
		checkResults = new ConfigCheckResultGUI(groupCheck, SWT.NONE);
		checkResults.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		

		buttonRunSim = new Button(groupRun, SWT.NONE);
		buttonRunSim.setText("Run");
		buttonRunSim.setEnabled(true);
		buttonRunSim.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false,
				false, 2, 1));
		buttonRunSim.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				buttonRunSim.setEnabled(false);
				simulationWasRunning = true;
				startSimulation();
				try {
					Thread.sleep(1000);
					periodicUpdate();
				} catch (InterruptedException ee) {
					ee.printStackTrace();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */
			}
		});

		// set the composite to the tab and show it
		tabGenerlItem.setControl(composite);
		tabFolder.pack();

		this.layout();
		
	}

	private void startSimulation() {
		System.out
				.println("Simulation start initialization: Saving Machine and IOLinking");
		// Save all
		Machine.saveMachine(EModSession.getMachineName(), EModSession.getMachineConfig());
		States.saveStates(EModSession.getMachineName(), EModSession.getSimulationConfig());

		// EModSimulationRun.EModSimRun();
		(new EModSimulationSimulationThread()).start();

		logger.log(LogLevel.DEBUG, "simulation run");
	}

	private void periodicUpdate() {
		Thread updateThread = new Thread() {
			@Override
			public void run() {
				do {

					getDisplay().syncExec(new Runnable() {

						@Override
						public void run() {
							update();
						}
					});

				} while (simulationWasRunning);
			}
		};
		// background thread
		updateThread.setDaemon(true);
		updateThread.start();
	}

}
