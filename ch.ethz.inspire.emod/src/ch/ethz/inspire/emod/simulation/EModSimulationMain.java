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

import java.util.List;
import java.util.logging.Logger;

import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;

/**
 * Main simulation class
 * 
 * @author dhampl
 *
 */
public class EModSimulationMain {
	
	private static Logger logger = Logger.getLogger(EModSimulationMain.class.getName());
	
	private double sampleperiod;
	private SimulationState machineState;
	private List<IOConnection> connectionList;
	
	private List<ASimulationControl> simulators;
	
	public EModSimulationMain(String machineName, String simConfigName) {
		super(); // ?? XXX
		sampleperiod = 0.2; // seconds
		/* Read simulation states from file */
		machineState = new SimulationState(machineName, simConfigName);
	}

	/**
	 * Set IO connection list
	 * 
	 * @param list
	 */
	public void setIOConnectionList(List<IOConnection> list)
	{
		connectionList = list;
	}
	
	/**
	 * Set list with object for input parameter generation.
	 * 
	 * @param list
	 */
	public void setInputparamObjectList(List<ASimulationControl> list)
	{
		simulators = list;
	}
	
	/**
	 * Do the simulation
	 */
	public void runSimulation() {
		
		double time; 
		
		/* Create simulation output file to store the simulation
		 * data. 
		 */
		SimulationOutput simoutput = new SimulationOutput("simulation_output.dat", 
			Machine.getInstance().getMachineComponentList(), simulators);
		
		logger.info("starting simulation");
		time = 0.0;
		
		/* Init simulation control objects. */
		MachineState mstate = machineState.getState(time);
		for(ASimulationControl sc:simulators) {
			sc.setState(mstate);
			sc.update(); // TODO: write init method.
		}
		

		/* Time 0.0 s:
		 * All model and simulation outputs must be initiated. */
		// Log data at time 0.0 s
		simoutput.logData(time);
		// Simulation mail loop:
		while (time < machineState.simEndTime()) {
			
			/* Increment actual simulation time */
			time += sampleperiod;
			
			/* Set the inputs of all component models. */
			setInputs();
			
			/* Iterate all models. The outputs of all component models are updated.*/
			for(MachineComponent mc : Machine.getInstance().getMachineComponentList())
				mc.getComponent().update();
			
			/* Get next value from simulation control. */
			mstate = machineState.getState(time);
			for(ASimulationControl sc:simulators) {
				sc.setState(mstate);
				sc.update();
			}
			
			/*	Log data of the actual time sample */
			simoutput.logData(time);
		}
		/* Close simulation output */
		simoutput.close();
	}
	
	/**
	 * sets all inputs
	 */
	private void setInputs() {
		for(IOConnection ioc : connectionList) {
			ioc.getTarget().setValue(ioc.getSoure().getValue());
		}
	}
}
