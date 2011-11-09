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

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import ch.ethz.inspire.emod.model.Fan;

public class FanTest {
	
	/**
	 * Test Fan class
	 */
	@Test
	public void testFanOff(){
		Fan fan = new Fan("Example");
		
		// Set fan to "off"
		fan.getInput("level").setValue(0);
		fan.update();
		// Check if power is zero, and no voluminal flow occurs
		assertEquals("Fan power", 0, fan.getOutput("Ptotal").getValue(),   0);
		assertEquals("Mass flow", 0, fan.getOutput("massFlow").getValue(), 0);
	}
	
	@Test
	public void testFanNominal(){
		Fan fan = new Fan("Example");
		
		// Set fan to nominal power
		fan.getInput("level").setValue(1);
		fan.update();
		// Check if power is zero, and no voluminal flow occurs
		assertEquals("Fan power", 1500, fan.getOutput("Ptotal").getValue(), 0);
		assertEquals("Mass flow", 0.5*1.2 , fan.getOutput("massFlow").getValue(), 0);
	}
	
	@Test
	public void testFanDemandCtrl(){
		Fan fan = new Fan("Example");
		
		// Set fan to nominal power
		fan.getInput("level").setValue(0.5);
		fan.update();
		// Check if power is zero, and no voluminal flow occurs
		assertEquals("Fan power", 1500*.5*.5*.5, fan.getOutput("Ptotal").getValue(), 0);
		assertEquals("Mass flow", 0.5*0.5*1.2 , fan.getOutput("massFlow").getValue(), 0);
	}
	
}
