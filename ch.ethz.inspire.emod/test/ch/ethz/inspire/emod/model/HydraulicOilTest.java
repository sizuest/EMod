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

import ch.ethz.inspire.emod.model.HydraulicOil;

public class HydraulicOilTest {
	
	/**
	 * Test HydraulicOil class
	 */
	@Test
	public void testHydraulicOil(){
		HydraulicOil hoi = new HydraulicOil("Example");
		
		// TODO stimmt noch nicht
		
		
		
		hoi.getInput("Temperature").setValue(323);
		hoi.getInput("Pressure").setValue(30000000);
		hoi.update();
		
		assertEquals("Viscosity", 30, hoi.getOutput("Viscosity").getValue(), 5);
		assertEquals("Density", 188, hoi.getOutput("Density").getValue(), 10);
		
	}

}
