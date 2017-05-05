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

import ch.ethz.inspire.emod.model.thermal.FreeHeatTransfere;

/**
 * @author simon
 *
 */
public class FreeHeatTransfereTest {
	
	/**
	 * Test FreeHeatTransfer class
	 */
	@Test
	public void testFreeHeatTransfere(){
		FreeHeatTransfere fht = new FreeHeatTransfere("Example","ThermalTest");
		
		// No temperature gradient
		fht.getInput("Temperature1").setValue(293);
		fht.getInput("Temperature2").setValue(293);
		fht.update();
		
		assertEquals("Heatflow 1->2", 0, fht.getOutput("PThermal12").getValue(), 0);
		assertEquals("Heatflow 1->2", 0, fht.getOutput("PThermal21").getValue(), 0);
		
		// 10K temperature gradient
		fht.getInput("Temperature1").setValue(293);
		fht.getInput("Temperature2").setValue(283);
		fht.update();
		
		assertEquals("Heatflow 1->2",  2.22, fht.getOutput("PThermal12").getValue(), 0.1);
		assertEquals("Heatflow 1->2", -2.22, fht.getOutput("PThermal21").getValue(), 0.1);
		
	}
	

}
