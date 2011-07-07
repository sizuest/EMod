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

import ch.ethz.inspire.emod.model.units.Unit;

/**
 * Testclass with random input samples
 * 
 * @author dhampl
 *
 */
public class RandomSimulationControl extends ASimulationControl {

	/**
	 * @param name
	 * @param unit
	 */
	public RandomSimulationControl(String name, Unit unit) {
		super(name, unit);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.simulation.SimulationControl#update()
	 */
	@Override
	public void update() {
		// TODO dummy method
		if(state!=MachineState.OFF){
			Random rnd = new Random();
			simulationOutput.setValue(rnd.nextDouble()*100);
		} else {
			simulationOutput.setValue(0);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.simulation.ASimulationControl#setState(ch.ethz.inspire.emod.simulation.MachineState)
	 */
	@Override
	public void setState(MachineState state) {
		if(state == MachineState.READY || state == MachineState.STANDBY)
			this.state=MachineState.ON;
		else
			this.state=state;
		
	}

}
