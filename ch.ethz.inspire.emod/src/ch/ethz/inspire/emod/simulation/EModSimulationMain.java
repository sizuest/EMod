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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

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

	private int iterationStep;
	private List<double[]> simData;
	
	public EModSimulationMain() {
		super();
		iterationStep=0;
		simData = new ArrayList<double[]>();
	}
	
	public void readSimulationFromFile(String file) {
		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;
			
			while((line=input.readLine())!=null) {
				//tokenize & append
				int cnt = 0;
				StringTokenizer st = new StringTokenizer(line, ";");
				double[] tempT = new double[st.countTokens()];
				double[] tempR = new double[st.countTokens()];
				while(st.hasMoreTokens()) {
					StringTokenizer sto = new StringTokenizer(st.nextToken());
					tempT[cnt] = Double.parseDouble(sto.nextToken());
					tempR[cnt] = Double.parseDouble(sto.nextToken());
					cnt++;
				}
				simData.add(tempT);
				simData.add(tempR);
				
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
		for(;;) {
			setInputs();
			for(MachineComponent mc : Machine.getInstance().getComponentList())
				mc.getComponent().update();
			logData();
			iterationStep++;
		}
	}
	
	/**
	 * initialize the Simulation
	 */
	private void initSimulation() {
		
	}
	
	/**
	 * sets all inputs
	 */
	private void setInputs() {
		Random rnd = new Random();
		for(MachineComponent mc : Machine.getInstance().getComponentList()) {
			mc.getComponent().setInput(0, rnd.nextDouble()*100);
			mc.getComponent().setInput(1, rnd.nextDouble()*100);
		}
	}
	
	/**
	 * logging data for analysis
	 */
	private void logData() {
		for(MachineComponent mc : Machine.getInstance().getComponentList()) {
			System.out.println(mc.getName());
			for(IOContainer ioc : mc.getComponent().getOutputs()) {
				System.out.println(ioc.toString());
			}
		}
	}
	
	/**
	 * generate random input samples for a given machine config
	 * 
	 * @param iterations
	 */
	public void generateSimulation(int iterations) {
		Random rnd = new Random();
		for(int i=0; i<Machine.getInstance().getComponentList().size(); i++) {
			double[] tempT = new double[iterations];
			double[] tempR = new double[iterations];
			for(int j=0;j<iterations;j++){
				tempT[j]=rnd.nextDouble()*1000;
				tempR[j]=rnd.nextDouble()*10000;
			}
			simData.add(tempT);
			simData.add(tempR);
		}
	}
}
