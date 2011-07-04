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
	
	public abstract void update();
	
	public void setState(MachineState state) {
		this.state=state;
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
