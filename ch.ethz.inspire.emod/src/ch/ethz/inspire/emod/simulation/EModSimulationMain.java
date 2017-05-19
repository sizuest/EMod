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
package ch.ethz.inspire.emod.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.Process;
import ch.ethz.inspire.emod.femexport.FEMOutput;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitProperties;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitSolver;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Main simulation class
 * 
 * IMPORTANT: Only one simulation can take place at once!
 * 
 * @author dhampl
 * 
 */
public class EModSimulationMain {

	private static Logger logger = Logger.getLogger(EModSimulationMain.class
			.getName());

	private double sampleperiod;
	private SimulationState machineState;
	private List<IOConnection> connectionList;
	private ArrayList<MachineComponent> machineComponentList;
	private List<ASimulationControl> simulators;

	private boolean doFEMOutput = true;

	private static int progress = 0;
	private static String message = "";
	private static boolean running = false;
	private static boolean forcedStop = false;

	/**
	 * @param machineName
	 * @param simConfigName
	 */
	public EModSimulationMain(String machineName, String simConfigName) {

		/* Read simulation states from file */
		machineState = new SimulationState(machineName, simConfigName);

		machineComponentList = null;
		connectionList = null;
		simulators = null;

		readConfig();
	}

	/**
	 * Returns the current progress of the simulation
	 * @return
	 */
	public static int getProgress() {
		return Math.min(100, Math.max(0, progress));
	}

	/**
	 * Current simulation message
	 * @return
	 */
	public static String getMessage() {
		return message;
	}

	/**
	 * Current running status 
	 * @return
	 */
	public static boolean getRunningStatus() {
		return running;
	}

	/**
	 * Indicates whether the forced stop option is set
	 * @return
	 */
	public static boolean getForcedStop() {
		return forcedStop;
	}

	/**
	 * Require a forced stop
	 * If a forced stop is required, the current iteration is finished and the data is logged,
	 * befor the routine quits
	 * 
	 * @param b
	 */
	public static void setForcedStop(boolean b) {
		forcedStop = b;
	}

	private void readConfig() {
		String file = EModSession.getSimulationConfigPath();

		ConfigReader cr = null;
		try {
			cr = new ConfigReader(file);
			cr.ConfigReaderOpen();
			sampleperiod = cr.getDoubleValue("simulationPeriod");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set machine component list
	 * 
	 * @param list
	 */
	public void setMachineComponentList(ArrayList<MachineComponent> list) {
		machineComponentList = list;
	}

	/**
	 * Set IO connection list
	 * 
	 * @param list
	 */
	public void setIOConnectionList(List<IOConnection> list) {
		connectionList = list;
	}

	/**
	 * Set list with object for input parameter generation.
	 * 
	 * @param list
	 */
	public void setInputparamObjectList(List<ASimulationControl> list) {
		simulators = list;
	}

	/**
	 * Set the process parameters 8time series) to the simulation control
	 * objects.
	 */
	public void setProcessParamsforSimulation() {
		if(null==simulators)
			return;
		
		for (ASimulationControl sc : simulators) {
			if (sc.getClass() == ProcessSimulationControl.class) {
				/* Set samples for process parameters */
				double[] samples = null;
				try {
					samples = Process.getInstance().getDoubleArray(sc.getName());
					((ProcessSimulationControl) sc).setProcessSamples(samples, Process.getTime());
				} catch (Exception ex) {
					Exception e = new Exception("Error for process parameter '" + sc.getName() + "'\n" + ex.getMessage());
					e.printStackTrace();
				}

			} else if (sc instanceof GeometricKienzleSimulationControl) {
				/*
				 * Set and calculate the process moments for the Kienzle
				 * simulators
				 */
				((GeometricKienzleSimulationControl) sc).installKienzleInputParameters(Process.getInstance());
			}
		}
	}

	/**
	 * Do the simulation
	 */
	public void runSimulation() {

		if (running) {
			System.out.println("Simulation is allready running");
			return;
		}

		/* Set flags */
		running = true;
		forcedStop = false;

		double time;
		progress = 0;
		FluidCircuitSolver fluidSolver;

		/* Check if all lists are defined: */
		if ((simulators == null) || (machineComponentList == null)
				|| (connectionList == null)) {
			Exception ex = new Exception("Undefined simulation lists");
			ex.printStackTrace();
			return;
		}

		/*
		 * Create simulation output file to store the simulation data.
		 */
		String path = EModSession.getResultFilePath();
		SimulationOutput simoutput = new SimulationOutput(path,	machineComponentList, simulators);

		String fempath = EModSession.getFEMExportFilePath();
		FEMOutput femoutput = new FEMOutput(fempath, machineComponentList);

		logger.info("initializing model");
		time = 0.0;

		/* Init simulation control objects. */
		message = "Preparing simulation controls ...";
		MachineState mstate = machineState.getState(time);
		for (ASimulationControl sc : simulators) {
			sc.setState(mstate);
			sc.update(); // TODO: write init method.
		}

		/* Init machine component objects */
		message = "Preparing machine components ...";
		for (MachineComponent mc : machineComponentList)
			mc.getComponent().preSimulation();

		/* Create solver for fluid circuits */
		message = "Initializing fluid circuit solver ...";
		ArrayList<FluidCircuitProperties> fluidPropertyList = new ArrayList<FluidCircuitProperties>();
		for (MachineComponent mc : Machine.getInstance().getFloodableMachineComponentList())
			fluidPropertyList.addAll(((Floodable) (mc.getComponent())).getFluidPropertiesList());
		fluidSolver = new FluidCircuitSolver(fluidPropertyList, Machine.getInstance().getFluidConnectionList());
		
		/* Read fluid solver and output settings */
		int fsMaxIter;
		double fsRelTol, fsMinFlowRate;
		try {
			ConfigReader simulationConfigReader = new ConfigReader(EModSession.getSimulationConfigPath());
			simulationConfigReader.ConfigReaderOpen();
			
			fsMaxIter     = simulationConfigReader.getValue("FluidSolver.MaxIter", 20);
			fsRelTol      = simulationConfigReader.getValue("FluidSolver.RelTol", 1E-4);
			fsMinFlowRate = simulationConfigReader.getValue("FluidSolver.MinFlowRate", 1E-9);
			doFEMOutput   = simulationConfigReader.getValue("Output.FEM", true);
			
		} catch (Exception e) {
			e.printStackTrace();
			fsMaxIter     = 20;
			fsRelTol      = 1E-4;
			fsMinFlowRate = 1E-9;
		}

		/* Find steady state for fluid circuits */
		try {
			fluidSolver.solve(50*fsMaxIter, fsRelTol, fsMinFlowRate);
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("starting simulation");
		message = LocalizationHandler.getItem("app.gui.analysis.progressbar");

		/*
		 * Time 0.0 s: All model and simulation outputs must be initiated.
		 */
		// Log data at time 0.0 s
		simoutput.logData(time, machineState.getState(0));
		// Simulation mail loop:
		while (time < machineState.simEndTime() & !forcedStop) {

			/* Increment actual simulation time */
			time += sampleperiod;

			/* Get next value from simulation control. */
			mstate = machineState.getState(time);
			for (ASimulationControl sc : simulators) {
				sc.setState(mstate);
				sc.update();
			}

			/* Set the inputs of all component models. */
			setInputs();

			/*
			 * Iterate all models. The outputs of all component models are
			 * updated.
			 */
			for (MachineComponent mc : machineComponentList)
				try {
					mc.getComponent().update();
					if (doFEMOutput)
						mc.getComponent().updateBoundaryConditions();
				} catch (Exception e) {
					System.out.println("EModSimulationMain.runSimulation(): update component not working for: " + mc.getComponent().toString());
				}

			/* Solve fluid circuits */
			try {
				fluidSolver.solve(fsMaxIter, fsRelTol, fsMinFlowRate);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/* Log data of the actual time sample */
			simoutput.logData(time, mstate);

			if (doFEMOutput)
				femoutput.logData(time, mstate);

			/* Update Progress */
			progress = (int) (time / machineState.simEndTime() * 100);
		}

		/* Close simulation output */
		simoutput.close();
		femoutput.close();

		/* Reset flag */
		running = false;
		message = "Simulation completet";
	}

	/**
	 * sets all inputs
	 */
	private void setInputs() {
		for (IOConnection ioc : connectionList) {
			ioc.update();
		}
	}

	/**
	 * Sets the simulation period for all components
	 */
	public void updateSimulationPeriod() {
		if(null!=simulators)
			for (ASimulationControl sc : simulators)
				sc.setSimulationPeriod(sampleperiod);

		for (MachineComponent mc : machineComponentList)
			mc.getComponent().setSimulationTimestep(sampleperiod);
	}

	/**
	 * @return the simulation period [s]
	 */
	public double getSampleperiod() {
		return sampleperiod;
	}
}
