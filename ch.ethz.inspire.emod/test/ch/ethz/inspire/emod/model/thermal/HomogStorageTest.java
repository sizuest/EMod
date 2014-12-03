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

package ch.ethz.inspire.emod.model.thermal;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import ch.ethz.inspire.emod.model.thermal.HomogStorage;

public class HomogStorageTest {
	
	/**
	 * Test Amplifier class
	 */
	@Test
	public void testHomogStorage(){
		HomogStorage hs = new HomogStorage("Water",10, 293);
		hs.setSimulationTimestep(0.2);
		
		// Add two inflows and two outflows
		hs.getInput("In");
		hs.getInput("In");
		hs.getInput("Out");
		hs.getInput("Out");
		
		// No heat flow
		hs.getInput("In1").setValue(0);
		hs.getInput("In2").setValue(0);
		hs.getInput("Out1").setValue(0);
		hs.getInput("Out2").setValue(0);
		hs.update();
		
		assertEquals("Temperature", 293, hs.getOutput("Temperature").getValue(), 0);
		
		// Set heat flows
		hs.getInput("In1").setValue(10000);
		hs.getInput("In2").setValue(-2000);
		hs.getInput("Out1").setValue(3000);
		hs.getInput("Out2").setValue(-4000);
		for (int i=0; i<50; i++)
			hs.update();
		
		// Total heat flow: 10-2-3+4 W= 9 kW
		// New temperature : 293 + 9kW / 10kg / 4182 J/kg/K * 10s = 2.152K+293K
		assertEquals("Temperature", 295.152, hs.getOutput("Temperature").getValue(), .1);

	}
	

}
