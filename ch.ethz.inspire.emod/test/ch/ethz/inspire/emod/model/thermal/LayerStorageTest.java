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

import ch.ethz.inspire.emod.model.thermal.LayerStorage;

public class LayerStorageTest {
	
	/**
	 * Test Amplifier class
	 */
	@Test
	public void testLayerStorageTest(){
		LayerStorage ls = new LayerStorage("Example","ThermalTest");
		ls.setSimulationPeriod(0.2);
		
		// No temperature gradient
		ls.getInput("TemperatureIn").setValue(293);
		ls.getInput("TemperatureAmb").setValue(293);
		ls.getInput("MassFlow").setValue(0);
		ls.update();
		
		assertEquals("Outflow temperature", 293, ls.getOutput("TemperatureOut").getValue(), 0);
		assertEquals("Thermal Loss",        0, ls.getOutput("PLoss").getValue(), 0);
		
		ls.getInput("TemperatureAmb").setValue(0);
		ls.getInput("MassFlow").setValue(1);
		ls.update();
		
		// Loss is equal to average temperature drop * thermal resistance * surace
		assertEquals("Thermal Loss",        293*.5*1, ls.getOutput("PLoss").getValue(), 1);
		
		// After 10s steady state is reached
		for (int i=0; i<50; i++)
			ls.update();
		
		assertEquals("Outflow temperature", 292.8, ls.getOutput("TemperatureOut").getValue(), .1);

		
	}
	

}
