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

import ch.ethz.inspire.emod.model.IOContainer;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * generic simulation control object. 
 * 
 * @author dhampl
 *
 */
public abstract class ASimulationControl {

	protected IOContainer simulationOutput;
	protected String name;
	protected MachineState state=MachineState.ON;
	
	public ASimulationControl(String name, Unit unit) {
		simulationOutput = new IOContainer(name, unit, 0);
		this.name = name;
	}
	
	/**
	 * updates the simulationOutput {@link IOContainer} according to the {@link MachineState}
	 * and simulation logic for the next simulation cycle.
	 */
	public abstract void update();
	
	/**
	 * sets the state. each implementation of {@link ASimulationControl} has to 
	 * implement a mapping of {@link MachineState} to a valid component simulation
	 * state.
	 */
	public abstract void setState(MachineState state);
		
	public MachineState getState() {
		return state;
	}
	
	public String getName() {
		return name;
	}
	
	public IOContainer getOutput() {
		return simulationOutput;
	}
}
