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
	 */
	@Test
	public void testPipe(){
		Pipe pip = new Pipe("Example");
		
		// TODO stimmt noch nicht
		
		
		
		pip.getInput("PressureOut").setValue(1000000);
		pip.getInput("MassFlowOut").setValue(0.44);
		pip.update();
		
		assertEquals("PressureLoss", 87843, pip.getOutput("PressureLoss").getValue(), 1);
		assertEquals("Ploss", 39, pip.getOutput("PLoss").getValue(), 1);
		assertEquals("massFlow", 0.44, pip.getOutput("MassFlowIn").getValue(), 0.1);
		
	}

}
