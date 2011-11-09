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

import ch.ethz.inspire.emod.model.LinAxis;

public class LinAxisTest {
	
	@Test
	public void testHorizontal(){
		LinAxis la = new LinAxis("ExampleH");
		
		// Moving axis horizontal, no force
		la.getInput("Speed").setValue(1);
		la.getInput("ProcessForce").setValue(0);
		la.update();
		
		assertEquals("Translation speed", 0.02, la.getOutput("RotSpeed").getValue(), 0.001);
		assertEquals("Torque", 0, la.getOutput("Torque").getValue(), 0.001);
		
		// Holding axis horizontal, with force
		la.getInput("Speed").setValue(0);
		la.getInput("ProcessForce").setValue(1);
		la.update();
		
		assertEquals("Translation speed", 0, la.getOutput("RotSpeed").getValue(), 0.001);
		assertEquals("Torque", 0.05, la.getOutput("Torque").getValue(), 0.001);
	}
	
	@Test
	public void testVertical(){
		LinAxis la = new LinAxis("ExampleV");
		
		// Moving axis vertical, no force
		la.getInput("Speed").setValue(1);
		la.getInput("ProcessForce").setValue(0);
		la.update();
		
		assertEquals("Translation speed", 0.02, la.getOutput("RotSpeed").getValue(), 0.001);
		assertEquals("Torque", -98.1*50/1000, la.getOutput("Torque").getValue(), 0.001);
		
		// Holding axis vertical, with force
		la.getInput("Speed").setValue(0);
		la.getInput("ProcessForce").setValue(1);
		la.update();
		
		assertEquals("Translation speed", 0, la.getOutput("RotSpeed").getValue(), 0.001);
		assertEquals("Torque", (1-98.1)*50/1000, la.getOutput("Torque").getValue(), 0.001);
	}
	
	@Test
	public void testDiagonal(){
		LinAxis la = new LinAxis("Example45");
		
		// Moving axis diagonal, with force
		la.getInput("Speed").setValue(1);
		la.getInput("ProcessForce").setValue(1);
		la.update();
		
		assertEquals("Translation speed", 0.02, la.getOutput("RotSpeed").getValue(), 0.001);
		assertEquals("Torque", (1-98.1/1.4142)*50/1000, la.getOutput("Torque").getValue(), 0.001);
	}
}
