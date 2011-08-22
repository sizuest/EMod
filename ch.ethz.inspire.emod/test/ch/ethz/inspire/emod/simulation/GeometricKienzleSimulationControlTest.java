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

/**
 * @author dhampl
 *
 */
public class GeometricKienzleSimulationControlTest {

	GeometricKienzleSimulationControl tester;
	
	@Before
	public void before() {
		double[] n = {2000, 2200, 2300, 3000};
		double[] f = {0.1, 0.08, 0.9, 1};
		double[] ap = {3, 4, 9, 0.5};
		double[] d = {0.006, 0.02, 0.004, 0.0001};
		
		try {
			tester = new GeometricKienzleSimulationControl("test", n, f, ap, d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test method for {@link ch.ethz.inspire.emod.simulation.GeometricKienzleSimulationControl#update()}.
	 */
	@Test
	public void testUpdate() {
		tester.setState(MachineState.ON);
		tester.update();
		assertEquals("update: ON", 20, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.OFF);
		tester.update();
		assertEquals("update: OFF", 0, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.STANDBY);
		tester.update();
		assertEquals("update: STANDBY", 30, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.READY);
		tester.update();
		assertEquals("update: READY", 20, tester.simulationOutput.getValue(), 0.0001);
		
	}

	/**
	 * Test method for {@link ch.ethz.inspire.emod.simulation.GeometricKienzleSimulationControl#setState(ch.ethz.inspire.emod.simulation.MachineState)}.
	 */
	@Test
	public void testSetState() {
		
		tester.setState(MachineState.OFF);
		assertEquals("off", ComponentState.OFF, tester.getState());
		assertEquals(0, tester.simulationStep);
		
		tester.setState(MachineState.ON);
		assertEquals("on", ComponentState.ON, tester.getState());
		assertEquals(0, tester.simulationStep);
		
		tester.setState(MachineState.PROCESS);
		assertEquals("pr", ComponentState.PERIODIC, tester.getState());
		assertEquals(0, tester.simulationStep);
		
		tester.setState(MachineState.READY);
		assertEquals("rdy", ComponentState.ON, tester.getState());
		assertEquals(0, tester.simulationStep);
		
		tester.setState(MachineState.STANDBY);
		assertEquals("standby", ComponentState.STANDBY, tester.getState());
		assertEquals(0, tester.simulationStep);
	}

	/**
	 * Test method for {@link ch.ethz.inspire.emod.simulation.GeometricKienzleSimulationControl#readConfigFromFile(java.lang.String)}.
	 */
	@Test
	public void testReadConfigFromFile() {
		assertEquals("kappa", 1.570796, tester.kappa, 0.001);
		assertEquals("z", 0.25, tester.z, 0.0001);
		assertEquals("kc", 1950, tester.kc, 0.0001);
	}


	/**
	 * Test method for {@link ch.ethz.inspire.emod.simulation.GeometricKienzleSimulationControl#calculateMoments(double[], double[], double[])}.
	 */
	@Test
	public void testCalculateMoments() {
		tester.setState(MachineState.PROCESS);
		tester.update();
		assertEquals("update 1",3.12088,tester.getOutput().getValue(),0.05);
		tester.update();
		assertEquals("update 2",11.73308,tester.getOutput().getValue(),0.05);
		tester.update();
		assertEquals("update 3",32.43314,tester.getOutput().getValue(),0.05);
		tester.update();
		assertEquals("update 4",0.04875,tester.getOutput().getValue(),0.05);
	}
	
}
