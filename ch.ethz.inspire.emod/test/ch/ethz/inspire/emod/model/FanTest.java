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
		assertEquals("Fan power", 0, fan.getOutput("PTotal").getValue(),   0);
		assertEquals("Fan loss",  0, fan.getOutput("PLoss").getValue(),    0);
		assertEquals("Fan mech.", 0, fan.getOutput("PUse").getValue(),     0);
		assertEquals("Mass flow", 0, fan.getOutput("MassFlow").getValue(), 0);
	}
	
	@Test
	public void testFanNominal(){
		Fan fan = new Fan("Example");
		
		// Set fan to nominal power
		fan.getInput("level").setValue(1);
		fan.update();
		// Check if power is zero, and no voluminal flow occurs
		assertEquals("Fan power", 1500, fan.getOutput("PTotal").getValue(), 0);
		assertEquals("Fan loss",  1450, fan.getOutput("PLoss").getValue(),  0);
		assertEquals("Fan mech.", 50,   fan.getOutput("PUse").getValue(),   0);
		assertEquals("Mass flow", 0.5*1.2 , fan.getOutput("MassFlow").getValue(), 0);
	}
	
	@Test
	public void testFanDemandCtrl(){
		Fan fan = new Fan("Example");
		
		// Set fan to nominal power
		fan.getInput("level").setValue(0.5);
		fan.update();
		// Check if power is zero, and no voluminal flow occurs
		assertEquals("Fan power", 1500*.5*.5*.5, fan.getOutput("PTotal").getValue(),  0);
		assertEquals("Fan loss",  181.25,        fan.getOutput("PLoss").getValue(),   0.1);
		assertEquals("Fan mech.", 6.25,          fan.getOutput("PUse").getValue(),    0);
		assertEquals("Mass flow", 0.5*0.5*1.2 ,  fan.getOutput("MassFlow").getValue(), 0);
	}
	
}
