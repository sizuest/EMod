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
import java.io.IOException;

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
	protected double[] samples;
	
	/**
	 * @param name
	 * @param unit
	 */
	public StaticSimulationControl(String name, Unit unit, String samplesFile) {
		super(name, unit);
		simulationStep=0;
		readSamplesFromFile(samplesFile);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.simulation.ASimulationControl#update()
	 */
	@Override
	public void update() {
		simulationOutput.setValue(samples[simulationStep]);
		simulationStep++;
		if(simulationStep>=samples.length)
			simulationStep=0;

	}

	/**
	 * reads samples from file
	 * 
	 * @param file format: 1. line=number of samples, one sample per line
	 */
	private void readSamplesFromFile(String file) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			int samplesCount = Integer.parseInt(in.readLine());
			samples = new double[samplesCount];
			for(int i=0; i<samplesCount; i++) {
				samples[i] = Double.parseDouble(in.readLine());
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
