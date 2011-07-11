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

import java.util.EnumMap;
import java.util.Map;

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
	protected Map<MachineState, MachineState> stateMap;
	
	public ASimulationControl(String name, Unit unit) {
		simulationOutput = new IOContainer(name, unit, 0);
		this.name = name;
		stateMap = new EnumMap<MachineState, MachineState>(MachineState.class);
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
	public void setState(MachineState state) {
		this.state = stateMap.get(state);
	}
		
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
