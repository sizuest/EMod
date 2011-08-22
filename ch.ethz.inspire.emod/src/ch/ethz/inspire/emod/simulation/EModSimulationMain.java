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

import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.ProcessSimulationControl;

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
	private ArrayList<MachineComponent> machineComponentList;
	private List<ASimulationControl> simulators;
	
	
	public EModSimulationMain(String machineName, String simConfigName) {
		sampleperiod = 1.0; // seconds XXX
		/* Read simulation states from file */
		machineState = new SimulationState(machineName, simConfigName);
		
		machineComponentList = null;
		connectionList = null;
		simulators = null;
	}

	/**
	 * Set machine component list
	 * 
	 * @param list
	 */
	public void setMachineComponentList(ArrayList<MachineComponent> list)
	{
		machineComponentList = list;
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
	 * Set the process parameters 8time series) to the simulation control
	 * objects.
	 * 
	 * @param process Actual process
	 */
	public void setProcessParamsforSimulation(Process process) {
		for (ASimulationControl sc: simulators) {
			if (sc.getClass() == ProcessSimulationControl.class) {
				/* Set samples for process parameters */
				double[] samples = null;
				try {
					samples =	process.getDoubleArray(sc.getName());
				}
				catch (Exception ex) {
					Exception e = new Exception("Error for process parameter '" + sc.getName()+ "'\n" 
							+ ex.getMessage());
					e.printStackTrace();
					System.exit(-1);
				}
				
				((ProcessSimulationControl) sc).setProcessSamples(samples);
			}
			else if (sc.getClass() == GeometricKienzleSimulationControl.class) {
				/* Set and calculate the process moments for the Kienzle simulators */
				((GeometricKienzleSimulationControl) sc).installKienzleInputParameters(process);
			}
		}
	}
	
	/**
	 * Do the simulation
	 */
	public void runSimulation() {
		
		double time; 
		
		/* Check if all lists are defined: */
		if ( (simulators == null) || 
			 (machineComponentList == null) ||
			 (connectionList == null) ) {
				 Exception ex = new Exception("Undefined simulation lists");
				 ex.printStackTrace();
				 System.exit(-1);
		 }	
		
		/* Create simulation output file to store the simulation
		 * data. 
		 */
		SimulationOutput simoutput = new SimulationOutput("simulation_output.dat", 
				machineComponentList, simulators);
		
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
			
			/* Get next value from simulation control. */
			mstate = machineState.getState(time);
			for(ASimulationControl sc:simulators) {
				sc.setState(mstate);
				sc.update();
			}
			
			/* Set the inputs of all component models. */
			setInputs();
			
			/* Iterate all models. The outputs of all component models are updated.*/
			for(MachineComponent mc : machineComponentList)
				mc.getComponent().update();
			
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
	
	public double getSampleperiod() {
		return sampleperiod;
	}
}
