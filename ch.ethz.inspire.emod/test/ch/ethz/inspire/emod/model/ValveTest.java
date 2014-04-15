/***********************************
 * $Id: ValveTest.java 101 2013-10-24 11:36:24Z sizuest $
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

import ch.ethz.inspire.emod.model.Valve;

public class ValveTest {
	
	/**
	 * Test Valve class
	 */
	@Test
	public void testValve(){
		Valve val = new Valve("Example");
		
		// TODO
		
		val.getInput("PressureOut").setValue(200000);
		val.getInput("MassFlowOut").setValue(0.25);
		val.update();
		
		assertEquals("PressureLoss", 350000, val.getOutput("PressureLoss").getValue(), 30000);
		assertEquals("Ploss",        87.5,   val.getOutput("PLoss").getValue(),        0.1);
		assertEquals("MassFlow",     0.25,   val.getOutput("MassFlowIn").getValue(),   0.1);
		assertEquals("PressureIn",   550000, val.getOutput("PressureIn").getValue(),   30000);
		
	}

}
