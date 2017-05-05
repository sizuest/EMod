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

import ch.ethz.inspire.emod.model.Revolver;

/**
 * @author Simon Züst
 *
 */
public class RevolverTest {
	
	/**
	 * 
	 */
	@Test
	public void testRevolver(){
		Revolver rev = new Revolver("Example");
		
		rev.setSimulationTimestep(0.2);
		
		rev.getInput("Tool").setValue(2);
		rev.update();
		
		assertEquals("Total torque", 10, rev.getOutput("Torque").getValue(), 0);
		
		for (int i=1; i<10; i++)
			rev.update();
		
		assertEquals("Tool", 2, rev.getOutput("ToolReal").getValue(), 0);
		assertEquals("Speed", 0, rev.getOutput("RotSpeed").getValue(), 0);
		
		// Rotate backward
		rev.getInput("Tool").setValue(9);
		
		for (int i=1; i<15; i++)
			rev.update();
		
		assertEquals("Tool", 9, rev.getOutput("ToolReal").getValue(), 0);
		assertEquals("Speed", 0, rev.getOutput("RotSpeed").getValue(), 0);
		
	}
	

}
