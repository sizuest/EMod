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

import ch.ethz.inspire.emod.model.CompressedFluid;

/**
 * @author Simon Züst
 *
 */
public class CompressedFluidTest {
	
	/**
	 * Test Amplifier class
	 */
	@Test
	public void testCompressedFluid(){
		CompressedFluid cf = new CompressedFluid("Example");
		
		// No flow, nominal conditions 
		cf.getInput("Flow").setValue(0);
		cf.getInput("TemperatureAmb").setValue(273);
		cf.getInput("PressureAmb").setValue(101300);
		cf.update();
		assertEquals("Power", 0, cf.getOutput("PTotal").getValue(), 0);
		
		// flow 1m3/s
		cf.getInput("Flow").setValue(1.0);
		cf.update();
		assertEquals("Power", 500000, cf.getOutput("PTotal").getValue(), 1e4);
	}
	

}
