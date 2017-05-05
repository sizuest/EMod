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

import ch.ethz.inspire.emod.model.Clamp;

/**
 * @author Simon Züst
 *
 */
public class ClampTest {
	
	/**
	 * Test Clamp class
	 */
	@Test
	public void testClamp(){
		Clamp cl = new Clamp("Example");
		
		// Set clamp position outside of material (>300mm)
		cl.getInput("Position").setValue(400);
		cl.update();
		// Reaction force must be zero
		assertEquals("Reaction force outside", 0, cl.getOutput("Force").getValue(), 0);
		
		// Set clamp position inside material (-1mm)
		cl.getInput("Position").setValue(299);
		cl.update();
		// Reaction force must be -1mm * 1000 N/mm = 1000 N
		assertEquals("Reaction force inside", 1000, cl.getOutput("Force").getValue(), 0);
		
	}
	

}
