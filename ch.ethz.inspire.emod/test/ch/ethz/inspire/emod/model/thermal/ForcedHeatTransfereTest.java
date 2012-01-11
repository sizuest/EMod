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

import ch.ethz.inspire.emod.model.thermal.ForcedHeatTransfere;

public class ForcedHeatTransfereTest {
	
	/**
	 * Test Amplifier class
	 */
	@Test
	public void testForcedHeatTransfereTest(){
		ForcedHeatTransfere fht = new ForcedHeatTransfere("Example","ThermalTest");
		
		// No temperature gradient
		fht.getInput("Temperature1").setValue(293);
		fht.getInput("Temperature2").setValue(293);
		fht.getInput("MassFlow").setValue(0);
		fht.update();
		
		assertEquals("Heatflow 1->2", 0, fht.getOutput("PThermal12").getValue(), 0);
		assertEquals("Heatflow 1->2", 0, fht.getOutput("PThermal21").getValue(), 0);
		
		// 10K temperature gradient
		fht.getInput("Temperature1").setValue(293);
		fht.getInput("Temperature2").setValue(283);
		fht.update();
		
		assertEquals("Heatflow 1->2", 0, fht.getOutput("PThermal12").getValue(), 0);
		assertEquals("Heatflow 1->2", 0, fht.getOutput("PThermal21").getValue(), 0);
		
		// Turn on mass flow
		fht.getInput("MassFlow").setValue(1);
		fht.update();
		
		assertEquals("Heatflow 1->2",  10000, fht.getOutput("PThermal12").getValue(), 0);
		assertEquals("Heatflow 1->2", -10000, fht.getOutput("PThermal21").getValue(), 0);
		
	}
	

}
