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

import ch.ethz.inspire.emod.model.MovingMass;

public class MovingMassTest {

	/**
	 * Test MovingMass class
	 */
	@Test
	public void testMovingMass() {
		// New Mass with 75kg, moving at an 90° angle
		MovingMass mm = new MovingMass(75,1,0,0);
		
		// Set time step to 1s
		mm.setSimulationTimestep(1);
		
		// 1ST TEST
		// Set speed [m/s]
		mm.getInput("SpeedLin").setValue(0);
		// update outputs
		mm.update();
		assertEquals("Force", 735.75, mm.getOutput("Force").getValue(), 0.01);
		
		//2ND TEST
		// Set speed [m/s]
		mm.getInput("SpeedLin").setValue(1);
		// update outputs
		mm.update();
		assertEquals("Force", 810.75, mm.getOutput("Force").getValue(), 0.01);
		mm.update();
		assertEquals("Force", 735.75, mm.getOutput("Force").getValue(), 0.01);
		
	}
	
}
