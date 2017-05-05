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

/**
 * @author Simon Züst
 *
 */
public class BearingTest {
	
	/**
	 * Test Clamp class
	 */
	@Test
	public void testBearing(){
		Bearing b = new Bearing("Example");
		
		// Set speed to zero
		b.getInput("RotSpeed").setValue(0);
		b.getInput("ForceAxial").setValue(0);
		b.getInput("ForceRadial").setValue(0);
		
		b.update();
		
		// Reaction force must be zero
		assertEquals("Friction torque", 0 , b.getOutput("Torque").getValue(), 0);
		assertEquals("Losses",          0,  b.getOutput("PLoss").getValue(),  0);
		
		// Set speed to 42000 rpm and Fa to 944N
		b.getInput("RotSpeed").setValue(700);
		b.getInput("ForceAxial").setValue(944);
		
		b.update();
		
		// Reaction force must be zero
		assertEquals("Friction torque", 0.0565 , b.getOutput("Torque").getValue(), 0.005);
		assertEquals("Losses",          250,     b.getOutput("PLoss").getValue(),   4);
		
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testBearing2(){
		Bearing b = new Bearing("SKF-7009");
		
		// Set speed to zero
		b.getInput("RotSpeed").setValue(0);
		b.getInput("ForceAxial").setValue(0);
		b.getInput("ForceRadial").setValue(0);
		
		b.update();
		
		// Reaction force must be zero
		assertEquals("Friction torque", 0 , b.getOutput("Torque").getValue(), 0);
		assertEquals("Losses",          0,  b.getOutput("PLoss").getValue(),  0);
		
		// Set speed to 42000 rpm and Fa to 944N
		b.getInput("RotSpeed").setValue(700);
		b.getInput("ForceAxial").setValue(944);
		
		b.update();
		
		// Reaction force must be zero
		assertEquals("Friction torque", 0.0265 , b.getOutput("Torque").getValue(), 0.005);
		assertEquals("Losses",          115,     b.getOutput("PLoss").getValue(),   4);
		
		
	}
	

}
