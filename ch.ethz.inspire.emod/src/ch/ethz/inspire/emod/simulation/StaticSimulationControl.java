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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.bind.Unmarshaller;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * reads static simulation samples from a file and loops while active.
 * may be only one value.
 * 
 * @author dhampl
 *
 */
public class StaticSimulationControl extends ASimulationControl {

	private static Logger logger = Logger.getLogger(StaticSimulationControl.class.getName());
	protected int simulationStep;
	protected List<double[]> samples;
	
	/**
	 * @param name
	 * @param unit
	 * @param configFile
	 */
	public StaticSimulationControl(String name, Unit unit, String configFile) {
		super(name, unit, configFile);
		simulationStep=0;
		readSamplesFromFile(configFile);
	}
	
	/**
	 * JAXB constructor
	 */
	public StaticSimulationControl() {
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.simulation.ASimulationControl#update()
	 */
	@Override
	public void update() {
		logger.log(LogLevel.DEBUG, "update on "+getName()+" step: "+simulationStep);
		//as samples are in the same order as machinestates, the machinestate index (ordinal) can be used.
		simulationOutput.setValue(samples.get(state.ordinal())[simulationStep]);
		simulationStep++;
		if(simulationStep>=samples.get(state.ordinal()).length)
			simulationStep=0;
		
	}

	/**
	 * reads samples from file
	 * 
	 * @param file one line per state. e.g.: ON=10 20 30
	 */
	private void readSamplesFromFile(String file) {
		// TODO: change machine states to componentstate
		samples = new ArrayList<double[]>();
		logger.log(LogLevel.DEBUG, "reading samples from: "+file);
		try {
			//load file to properties
			Properties p = new Properties();
			InputStream is = new FileInputStream(file);
			p.load(is);
			is.close();
			// loop over all machine states
			for(MachineState ms : MachineState.values()) {
				String line = p.getProperty(ms.name());
				StringTokenizer st = new StringTokenizer(line);
				double[] vals = new double[st.countTokens()];
				int i = 0;
				//parse samples
				while(st.hasMoreTokens()) {
					vals[i] = Double.parseDouble(st.nextToken());
					i++;
				}
				samples.add(vals);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * sets and maps the {@link MachineState} to the appropriate {@link SimulationState}
	 * 
	 * @param state
	 */
	@Override 
	public void setState(MachineState state) {
		super.setState(state);
		simulationStep=0;
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		readSamplesFromFile(configFile);
		super.readConfig(configFile);
	}
}
