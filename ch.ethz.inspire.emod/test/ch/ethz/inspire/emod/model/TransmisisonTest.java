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

import ch.ethz.inspire.emod.model.LinTransmission;

public class TransmisisonTest {
	
	/**
	 * Test Fan class
	 */
	@Test
	public void testTransmisison(){
		LinTransmission transm = new LinTransmission("Example");
		
		// Set fan to "off"
		transm.getInput("RotSpeed").setValue(0);
		transm.getInput("Torque").setValue(0);
		transm.update();
		// 
		assertEquals("Resulting speed", 	0, transm.getOutput("RotSpeed").getValue(), 0);
		assertEquals("Resulting torque", 	0, transm.getOutput("Torque").getValue(),   0);
		assertEquals("Heat loss", 			0, transm.getOutput("PLoss").getValue(),    0);
		
		// Set fan to "on"
		transm.getInput("RotSpeed").setValue(600);
		transm.getInput("Torque").setValue(10);
		transm.update();
		// 
		assertEquals("Resulting speed", 	60, transm.getOutput("RotSpeed").getValue(), 0);
		assertEquals("Resulting torque", 111.1, transm.getOutput("Torque").getValue(),  0.1);
		assertEquals("Heat loss", 		 69.8, transm.getOutput("PLoss").getValue(),    0.1);
	}
	
}