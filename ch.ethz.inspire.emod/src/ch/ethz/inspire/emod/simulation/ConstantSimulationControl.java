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

import javax.xml.bind.Unmarshaller;

import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.SimulationConfigReader;

/**
 * Implements a constant simulation control, which is independent from the machine state
 * @author sizuest
 *
 */
public class ConstantSimulationControl extends ASimulationControl {

	private double value;

	/**
	 * @param name
	 * @param unit
	 */
	public ConstantSimulationControl(String name, SiUnit unit) {
		this.name = name;
		this.unit = unit;

		init();
	}

	/**
	 * JAXB constructor
	 */
	public ConstantSimulationControl() {
		init();
	}

	@Override
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		init();
	}

	@Override
	public void update() {
		simulationOutput.setValue(value);
	}

	@Override
	public void setSimulationPeriod(double periodLength) {
		// TODO Auto-generated method stub

	}

	private void init() {
		/* Output */
		simulationOutput = new IOContainer(name, unit, 0);
		/*
		 * Make default mapping: Component state ON for all Machine states. The
		 * samples and state for during the process is set after reading the
		 * process definition.
		 */
		stateMap = new EnumMap<MachineState, ComponentState>(MachineState.class);
		for (MachineState ms : MachineState.values())
			stateMap.put(ms, ComponentState.ON);

		/* Init state: OFF */
		state = ComponentState.ON;
		readValueFromFile();

	}

	private void readValueFromFile() {
		if (null == name)
			return;

		SimulationConfigReader scr = null;
		try {
			scr = new SimulationConfigReader(this.getClass().getSimpleName(),
					name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			value = scr.getDoubleValue("Value");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
