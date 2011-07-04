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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.ethz.inspire.emod.model.IOContainer;
import ch.ethz.inspire.emod.model.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;

/**
 * Main simulation class
 * 
 * @author dhampl
 *
 */
public class EModSimulationMain {
	
	private static Logger logger = Logger.getLogger(EModSimulationMain.class.getName());

	private int iterationStep;
	private double sampleperiod;
	private List<IOConnection> connectionList;
	private List<ASimulationControl> simulators;
	
	public EModSimulationMain() {
		super();
		iterationStep=0;
		sampleperiod = 0.2; // seconds
		connectionList = new ArrayList<IOConnection>();
		simulators = new ArrayList<ASimulationControl>();
	}
	
	public void addSimulator(ASimulationControl sim) {
		simulators.add(sim);
	}
	
	public void readSimulationFromFile(String file) {
		logger.info("reading simulation setup from file: "+file);
		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;
			
			while((line=input.readLine())!=null) {
				//tokenize & append
				
				StringTokenizer st = new StringTokenizer(line, "=");
				
				StringTokenizer stin = new StringTokenizer(st.nextToken(), ".");
				String inObj=stin.nextToken();
				String inVar=stin.nextToken();
				StringTokenizer stout = new StringTokenizer(st.nextToken(), ".");
				String outObj=stout.nextToken();
				String outVar=stout.nextToken();
				
				IOContainer tempTar = Machine.getInstance().getComponent(inObj).getComponent().getInput(inVar);
				IOContainer tempSource = null;
				if(Machine.getInstance().getComponent(outObj)!=null)
					tempSource = Machine.getInstance().getComponent(outObj).getComponent().getOutput(outVar);
				else {
					for(ASimulationControl sc : simulators) {
						if(sc.getName().equals(outObj)) {
							tempSource = sc.getOutput();
						}
					}
				}
				try {
					connectionList.add(new IOConnection(tempSource, tempTar));
				} catch (Exception e) {
					logger.log(Level.WARNING, inObj+" "+outObj);
					e.printStackTrace();
					System.exit(-1);
				}
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	/**
	 * starts the simulation
	 */
	public void runSimulation() {
		initSimulation();
		
		logger.info("starting simulation");
		logData(); // Log data at time 0.0 s
		while (iterationStep < 1) {
		
			for(ASimulationControl sc:simulators) 
				sc.update();
			
			setInputs();
			
			for(MachineComponent mc : Machine.getInstance().getComponentList())
				mc.getComponent().update();
			
			iterationStep++;
			
			logData();
		}
	}
	
	/**
	 * initialize the Simulation
	 */
	private void initSimulation() {
		logger.info("init simulation");
		Random rnd = new Random();
		for(MachineComponent mc : Machine.getInstance().getComponentList()) {
			mc.getComponent().setInput(0, rnd.nextDouble()*10);
			mc.getComponent().setInput(1, rnd.nextDouble()*10);
		}
	}
	
	/**
	 * sets all inputs
	 */
	private void setInputs() {
		for(IOConnection ioc : connectionList) {
			ioc.getTarget().setValue(ioc.getSoure().getValue());
		}
	}
	
	/**
	 * logging data for analysis
	 */
	private void logData() {
		System.out.println("Iteration: " + iterationStep + "  Time: " + iterationStep*sampleperiod + " s");
		for(MachineComponent mc : Machine.getInstance().getComponentList()) {
			System.out.println("  " + mc.getName());
			int i=0;
			for(IOContainer ioc : mc.getComponent().getInputs()) {
				System.out.println("    i" + i + ": " + ioc.toString());
				i++;
			}
			i=0;
			for(IOContainer ioc : mc.getComponent().getOutputs()) {
				System.out.println("    o" + i + ": " + ioc.toString());
				i++;
				logger.fine(ioc.toString());
			}
		}
	}
	
	/**
	 * contains information on simulation input sources and targets
	 * through references to IOContainers of MachineComponents and
	 * SimulationControls.
	 * 
	 * @author dhampl
	 *
	 */
	protected class IOConnection {

		private IOContainer source;
		private IOContainer target;
		
		/**
		 * 
		 * @param source
		 * @param target 
		 * @throws Exception thrown if units don't match
		 */
		public IOConnection(IOContainer source, IOContainer target) throws Exception {
			this.source = source;
			this.target = target;
			if(source.getUnit()!=target.getUnit())
				throw new Exception("units do not match "+source.getName()+
						": "+source.getUnit()+" <-> "+target.getName()+": "+
						target.getUnit());
		}
		
		public IOContainer getSoure() {
			return source;
		}
		
		public IOContainer getTarget() {
			return target;
		}
	}
	
}
