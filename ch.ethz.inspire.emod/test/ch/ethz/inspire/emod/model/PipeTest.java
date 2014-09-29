/***********************************
 * $Id: PipTest.java 101 2013-10-24 11:36:24Z sizuest $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/test/ch/ethz/inspire/emod/model/CylinderTest.java $
 * $Author: sizuest $
 * $Date: 2013-10-24 13:36:24 +0200 (Do, 24 Okt 2013) $
 * $Rev: 101 $
 *
 * Copyright (c) 2013 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/

package ch.ethz.inspire.emod.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import ch.ethz.inspire.emod.model.Pipe;

public class PipeTest {
	
	/**
	 * Test Pipe class
	 * @throws Exception 
	 */
	@Test
	public void testPipe() throws Exception{
		Pipe pip = new Pipe("Example", 303);
		
		// TODO stimmt noch nicht
		
		pip.setSimulationPeriod(1);
		
		pip.getInput("PressureOut").setValue(2000000);
		pip.getInput("MassFlowOut").setValue(0.1);
		pip.getInput("TemperatureIn").setValue(303);
		pip.getInput("TemperatureAmb").setValue(293);
		pip.update();
		pip.update();
		
		assertEquals("PressureLoss", 30580, pip.getOutput("PressureLoss").getValue(), 1);
		assertEquals("TemperatureOut", 303.3, pip.getOutput("TemperatureOut").getValue(), .1);
		assertEquals("Ploss", 3.2, pip.getOutput("PLoss").getValue(), .1);
		assertEquals("massFlow", 0.1, pip.getOutput("MassFlowIn").getValue(), 0);
		
	}

}
