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

package ch.ethz.inspire.emod.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConstantComponentTest {

	/**
	 * Test LinearMotor class
	 */
	@Test
	public void testConstantComponent() {
		ConstantComponent cc = new ConstantComponent("80mmFan");
		
		// Check ptotal for input level 0
		cc.getInput("level").setValue(0);
		cc.update();
		assertEquals("Level0", 0.0, cc.getOutput("PTotal").getValue(), 0.0001);
		
		// Check ptotal for input level 1
		cc.getInput("level").setValue(1);
		cc.update();
		assertEquals("Level1", 50.0, cc.getOutput("PTotal").getValue(), 0.0001);
		
		// Check ptotal for input level 2
		cc.getInput("level").setValue(2);
		cc.update();
		assertEquals("Level2", 250.0, cc.getOutput("PTotal").getValue(), 0.0001);
		
	}
}
