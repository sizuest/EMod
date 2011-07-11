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

import ch.ethz.inspire.emod.model.units.Unit;

/**
 * reads static simulation samples from a file and loops while active.
 * may be only one value.
 * 
 * @author dhampl
 *
 */
public class StaticSimulationControl extends ASimulationControl {

	protected int simulationStep;
	protected List<double[]> samples;
	
	/**
	 * @param name
	 * @param unit
	 * @param samplesFile
	 */
	public StaticSimulationControl(String name, Unit unit, String samplesFile) {
		super(name, unit);
		simulationStep=0;
		readSamplesFromFile(samplesFile);
		stateMap.put(MachineState.CYCLE, MachineState.ON);
		stateMap.put(MachineState.OFF, MachineState.OFF);
		stateMap.put(MachineState.ON, MachineState.ON);
		stateMap.put(MachineState.READY, MachineState.ON);
		stateMap.put(MachineState.STANDBY, MachineState.STANDBY);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.simulation.ASimulationControl#update()
	 */
	@Override
	public void update() {
		if(state==MachineState.ON) {
			simulationOutput.setValue(samples.get(state.ordinal())[simulationStep]);
			simulationStep++;
			if(simulationStep>=samples.get(state.ordinal()).length)
				simulationStep=0;
		}
	}

	/**
	 * reads samples from file
	 * 
	 * @param file one line per state. e.g.: ON=10 20 30
	 */
	private void readSamplesFromFile(String file) {
		samples = new ArrayList<double[]>();
		
		try {
			Properties p = new Properties();
			InputStream is = new FileInputStream(file);
			p.load(is);
			for(MachineState ms : MachineState.values()) {
				String line = p.getProperty(ms.name());
				StringTokenizer st = new StringTokenizer(line);
				double[] vals = new double[st.countTokens()];
				int i = 0;
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
