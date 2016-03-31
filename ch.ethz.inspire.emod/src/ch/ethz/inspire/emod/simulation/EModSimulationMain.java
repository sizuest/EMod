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

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.Process;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.PropertiesHandler;
import ch.ethz.inspire.emod.gui.utils.ProgressbarGUI;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitSolver;
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

		/* Read simulation states from file */
		machineState = new SimulationState(machineName, simConfigName);
		
		machineComponentList = null;
		connectionList = null;
		simulators = null;
		
		readConfig();
	}
	
	private void readConfig() {
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
				PropertiesHandler.getProperty("sim.MachineName") + "/" + Defines.SIMULATIONCONFIGDIR + "/" +
				PropertiesHandler.getProperty("sim.SimulationConfigName");
		String file = path + "/" + Defines.SIMULATIONCONFIGFILE;
		
		ConfigReader cr = null;
		try {
			cr = new ConfigReader(file);
			cr.ConfigReaderOpen();
			sampleperiod = cr.getDoubleValue("simulationPeriod");
		} catch(Exception e) {
			e.printStackTrace();
		}
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
	public void setProcessParamsforSimulation() {
		for (ASimulationControl sc: simulators) {
			if (sc.getClass() == ProcessSimulationControl.class) {
				/* Set samples for process parameters */
				double[] samples = null;
				try {
					samples =	Process.getInstance().getDoubleArray(sc.getName());
					((ProcessSimulationControl) sc).setProcessSamples(samples, Process.getTime());
				}
				catch (Exception ex) {
					Exception e = new Exception("Error for process parameter '" + sc.getName()+ "'\n" 
							+ ex.getMessage());
					e.printStackTrace();
				}
				
			}
			else if (sc.getClass() == GeometricKienzleSimulationControl.class) {
				/* Set and calculate the process moments for the Kienzle simulators */
				((GeometricKienzleSimulationControl) sc).installKienzleInputParameters(Process.getInstance());
			}
		}
	}
	
	/**
	 * Do the simulation
	 */
	public void runSimulation() {
		
		double time; 
		ProgressbarGUI pg = new ProgressbarGUI("app.gui.analysis.progressbar");
		pg.updateProgressbar(0);
		FluidCircuitSolver fluidSolver;
		
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
		
		logger.info("initializing model");
		time = 0.0;
		
		/* Init simulation control objects. */
		MachineState mstate = machineState.getState(time);
		for(ASimulationControl sc:simulators) {
			sc.setState(mstate);
			sc.update(); // TODO: write init method.
		}
				
		/* Init machine component objects */
		for(MachineComponent mc : machineComponentList)
			mc.getComponent().preSimulation();
				
		/* Create solver for fluid circuits */
		ArrayList<FluidCircuitProperties> fluidPropertyList = new ArrayList<FluidCircuitProperties>();
		for(MachineComponent mc: Machine.getInstance().getFloodableMachineComponentList())
			fluidPropertyList.addAll(((Floodable) (mc.getComponent())).getFluidPropertiesList());
		fluidSolver = new FluidCircuitSolver(fluidPropertyList, Machine.getInstance().getFluidConnectionList());
		
		logger.info("starting simulation");
		
		

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
				try{
					mc.getComponent().update();
				} catch (Exception e){
					System.out.println("EModSimulationMain.runSimulation(): update component not working for: " + mc.getComponent().toString());
				}
			
			/* Solve fluid circuits */
			try {
				fluidSolver.solve();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
			/*	Log data of the actual time sample */
			simoutput.logData(time);
			
			/* Update Progress bar */
			pg.updateProgressbar(time/machineState.simEndTime());
		}
		/* Close progress bar */
		pg.updateProgressbar(100);
		/* Close simulation output */
		simoutput.close();
	}
	
	/**
	 * sets all inputs
	 */
	private void setInputs() {
		for(IOConnection ioc : connectionList) {
			//ioc.getTarget().setValue(ioc.getSource().getValue() * ioc.getGain());
			//TODO 
			ioc.update();
		}
	}
	
	/**
	 * Sets the simulation period for all components
	 */
	public void updateSimulationPeriod() {
		for(ASimulationControl sc: simulators) 
			sc.setSimulationPeriod(sampleperiod);
		
		for(MachineComponent mc : machineComponentList)
			mc.getComponent().setSimulationTimestep(sampleperiod);
	}
	
	/**
	 * @return the simulation period [s]
	 */
	public double getSampleperiod() {
		return sampleperiod;
	}
}
