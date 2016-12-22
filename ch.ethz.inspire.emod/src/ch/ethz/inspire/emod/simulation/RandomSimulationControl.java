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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.units.SiUnit;

/**
 * Testclass with random input samples
 * 
 * @author dhampl
 * 
 */
@XmlRootElement
public class RandomSimulationControl extends ASimulationControl {

	/**
	 * @param name
	 * @param unit
	 */
	public RandomSimulationControl(String name, SiUnit unit) {
		super(name, unit);
	}

	/**
	 * Empty constructor for JABX
	 */
	public RandomSimulationControl() {
		super();
	}

	@Override
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		super.afterUnmarshal(u, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.simulation.SimulationControl#update()
	 */
	@Override
	public void update() {
		if (state != ComponentState.OFF) {
			Random rnd = new Random();
			simulationOutput.setValue(rnd.nextDouble() * 500);
		} else {
			simulationOutput.setValue(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.inspire.emod.simulation.ASimulationControl#setState(ch.ethz.inspire
	 * .emod.simulation.MachineState)
	 */
	@Override
	public void setState(MachineState state) {
		if (stateMap != null) {
			this.state = stateMap.get(state);
		} else {
			if (state == MachineState.READY || state == MachineState.STANDBY)
				this.state = ComponentState.ON;
			else
				this.state = ComponentState.OFF;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.inspire.emod.simulation.ASimulationControl#setSimulationPeriod
	 * (double)
	 */
	@Override
	public void setSimulationPeriod(double periodLength) {
		// do nothing, no sample period needed in this class
	}

}
