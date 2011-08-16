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

import javax.xml.bind.Unmarshaller;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.SimulationConfigReader;

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
	public StaticSimulationControl(String name, Unit unit) {
		super(name, unit);
		simulationStep=0;
		readSamplesFromFile();
	}
	
	/**
	 * JAXB constructor
	 */
	public StaticSimulationControl() {
		super();
	}
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		super.afterUnmarshal(u, parent);
		simulationStep=0;
	}
	/**
	 * Path can not be given, when creating the objects by JABX.
	 * @param path Directory holding the configfiles.
	 */
	@Override
	public void afterJABX()
	{
		super.afterJABX();
		readSamplesFromFile();
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
	private void readSamplesFromFile() {
		samples = new ArrayList<double[]>();
		logger.log(LogLevel.DEBUG, "reading samples for: "+this.getClass().getSimpleName()+"_"+name);
		SimulationConfigReader scr=null;
		try {
			scr = new SimulationConfigReader(this.getClass().getSimpleName(), name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			
			// loop over all machine states
			for(ComponentState cs : ComponentState.values()) {
				samples.add(scr.getSamplesArray(cs.name()));
			}
		} catch(Exception e) {
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
}
