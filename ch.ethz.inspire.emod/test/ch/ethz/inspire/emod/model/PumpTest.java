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
import ch.ethz.inspire.emod.model.Pump;

public class PumpTest {
	
	@Test
	public void testPump(){
		Pump pump = new Pump("Example");
		
		// Set Mass flow to zero
		pump.getInput("MassFlowOut").setValue(0);
		pump.update();
		
		assertEquals("Pump power if off", 0, pump.getOutput("PTotal").getValue(),     0);
		assertEquals("Flow if off",       0, pump.getOutput("MassFlowIn").getValue(), 0);
		assertEquals("Pressure if off",   0, pump.getOutput("Pressure").getValue(),   0);
		
		// Set Mass flow to 3.6kg/s
		pump.getInput("MassFlowOut").setValue(3.6);
		pump.update();
		
		assertEquals("Pump power after ",   1500, pump.getOutput("PTotal").getValue(),      0);
		assertEquals("Flow if on",           3.6, pump.getOutput("MassFlowIn").getValue(),  0);
		assertEquals("Pressure if on",   1000000, pump.getOutput("Pressure").getValue(),    1);
	}
	
	
	@Test
	public void testHyfraPump(){
		Pump pump = new Pump("Hyfra_VWK_21_1S");
		
		//massflow out from: 8.65 l/min (measured schaublin) and 1055 kg/m^3 (glykol-water)
		pump.getInput("MassFlowOut").setValue(0.15);

		pump.update();
		
		System.out.println("PTotal: " + pump.getOutput("PTotal").getValue() + " PLoss: " + pump.getOutput("PLoss").getValue() + " PUse: " + pump.getOutput("PUse").getValue() + " MassFlowIn: " + pump.getOutput("MassFlowIn").getValue() + " Pressure: " + pump.getOutput("Pressure").getValue());
	}
}
