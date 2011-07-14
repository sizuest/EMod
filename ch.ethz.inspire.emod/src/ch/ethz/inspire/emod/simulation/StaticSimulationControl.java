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

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.MachineState.MachineStateEnum;

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
	 * @param samplesFile
	 */
	public StaticSimulationControl(String name, Unit unit, String samplesFile) {
		super(name, unit, samplesFile);
		simulationStep=0;
		readSamplesFromFile(samplesFile);
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
		// TODO: BUG: simulationStep must be set to 0, if state changes!
		
	}

	/**
	 * reads samples from file
	 * 
	 * @param file one line per state. e.g.: ON=10 20 30
	 */
	private void readSamplesFromFile(String file) {
		samples = new ArrayList<double[]>();
		logger.log(LogLevel.DEBUG, "reading samples from: "+file);
		try {
			//load file to properties
			Properties p = new Properties();
			InputStream is = new FileInputStream(file);
			p.load(is);
			// loop over all machine states
			for(MachineStateEnum ms : MachineStateEnum.values()) {
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
}
