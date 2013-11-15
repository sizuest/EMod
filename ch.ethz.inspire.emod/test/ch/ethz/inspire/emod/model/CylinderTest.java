/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2013 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/

package ch.ethz.inspire.emod.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import ch.ethz.inspire.emod.model.Cylinder;

public class CylinderTest {
	
	/**
	 * Test Cylinder class
	 */
	@Test
	public void testCylinder(){
		Cylinder cyl = new Cylinder("Example");
		
		// TODO
		
		cyl.getInput("Force").setValue(0);
		cyl.getInput("Speed").setValue(0);
		cyl.update();
		
		assertEquals("No Force/movement", 0, cyl.getOutput("Pressure").getValue(), 0);
		
		cyl.getInput("Force").setValue(1000);
		cyl.getInput("Speed").setValue(0);
		cyl.update();
		
		assertEquals("Clamping", 509392, cyl.getOutput("Pressure").getValue(), 100);
		
	}

}
