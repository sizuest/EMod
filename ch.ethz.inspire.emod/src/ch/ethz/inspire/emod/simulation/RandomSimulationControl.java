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
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.simulation.SimulationControl#update()
	 */
	@Override
	public void update() {
		// TODO dummy method
		Random rnd = new Random();
		simulationOutput.setValue(rnd.nextDouble()*100);
	}

}
