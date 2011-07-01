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

import java.util.Random;

import ch.ethz.inspire.emod.model.IOContainer;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * @author dhampl
 *
 */
public class SimulationControl {

	private IOContainer simulationOutput;
	private String name;
	private MachineState state=MachineState.ON;
	
	public SimulationControl(String name, Unit unit) {
		simulationOutput = new IOContainer(name, unit, 0);
		this.name = name;
	}
	
	public void update() {
		// TODO dummy method
		Random rnd = new Random();
		simulationOutput.setValue(rnd.nextDouble()*100);
	}
	
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
