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

package ch.ethz.inspire.emod.model.math;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import ch.ethz.inspire.emod.model.math.Sum;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * @author simon
 *
 */
public class SumTest {
	
	/**
	 * Test function for {@link Sum}
	 */
	@Test
	public void testServoMotor(){
		Sum s = new Sum(Unit.WATT);
		
		// Creat to positive and two negative inputs
		s.getInput("Plus");
		s.getInput("Plus");
		s.getInput("Minus");
		s.getInput("Minus");
		
		// set inputs
		s.getInput("Plus1").setValue(10);
		s.getInput("Plus2").setValue(-2);
		s.getInput("Minus1").setValue(3);
		s.getInput("Minus2").setValue(-4);
		s.update();
		
		// 10-2-3-(-4) = 9
		assertEquals("Sum", 9, s.getOutput("Sum").getValue(), 0);
		
		
		
		
	}
	
}
