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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * @author simon
 *
 */
public class StaticSimulationControlTest {

	StaticSimulationControl tester;
	
	/**
	 * Init a new simulation contorl element
	 */
	@Before
	public void init() {
		EModSession.newSession("Test", "test", "test", "test");
		//tester = new StaticSimulationControl("test", new SiUnit(Unit.NONE));
		tester = (StaticSimulationControl) Machine.addNewInputObject("StaticSimulationControl", new SiUnit(Unit.NONE));
		tester.setSimulationPeriod( 0.2);
	}
	
	/**
	 * Test function for {@link ASimulationControl#setState(MachineState)}
	 */
	@Test
	public void testUpdate() {
		tester.setState(MachineState.ON);
		tester.update();
		assertEquals("update: ON", 1, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.OFF);
		tester.update();
		assertEquals("update: OFF", 0, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.STANDBY);
		tester.update();
		assertEquals("update: STANDBY", 0, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.READY);
		tester.update();
		assertEquals("update: READY", 0, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.PROCESS);
		tester.update();
		assertEquals("update: CYCLE", 1, tester.simulationOutput.getValue(), 0.0001);
	}

	/**
	 * Test function for {@link ASimulationControl#getState()}
	 */
	@Test
	public void testSetState() {
		tester.setState(MachineState.ON);
		assertEquals("set state: ON", ComponentState.ON, tester.getState());
		tester.setState(MachineState.OFF);
		assertEquals("set state: OFF", ComponentState.OFF, tester.getState());
		tester.setState(MachineState.STANDBY);
		assertEquals("set state: STANDBY", ComponentState.STANDBY, tester.getState());
		tester.setState(MachineState.READY);
		assertEquals("set state: READY", ComponentState.READY, tester.getState());
		tester.setState(MachineState.PROCESS);
		assertEquals("set state: PROCESS", ComponentState.CONTROLLED, tester.getState());
	}

}
