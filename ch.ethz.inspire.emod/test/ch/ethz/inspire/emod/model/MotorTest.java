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

import ch.ethz.inspire.emod.model.Motor;

public class MotorTest {

	/**
	 * Test LinearMotor class
	 */
	@Test
	public void testLinearMotor() {
		Motor lm = new Motor("Siemens_1FE1115-6WT11");
		
		// Set rotational speed [rpm]
		lm.getInput("RotSpeed").setValue(1500);
		// Set torque [Nm]
		lm.getInput("Torque").setValue(264.83);
		// update outputs
		lm.update();
		assertEquals("Nominal Efficiency", 0.9024, lm.getOutput("Efficiency").getValue(), 0.0001);
		assertEquals("Nominal Pmech", 41600, lm.getOutput("Pmech").getValue(), 1);
		assertEquals("Nominal Ploss", 4500, lm.getOutput("Ploss").getValue(), 1);
		
		// Set rotational speed [rpm]
		lm.getInput("RotSpeed").setValue(2250);
		// Set torque [Nm]
		lm.getInput("Torque").setValue(2);
		// update outputs
		lm.update();
		assertEquals("Pmech", 471, lm.getOutput("Pmech").getValue(), 1);
		assertEquals("Ploss", 1307.5, lm.getOutput("Ploss").getValue(), 1);	
		assertEquals("Efficiency", 0.265, lm.getOutput("Efficiency").getValue(), 0.001);	
	}
	
}
