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
import static org.junit.Assert.assertTrue;

import ch.ethz.inspire.emod.model.Pump;

public class PumpTest {
	
	@Test
	public void testPumpOnly(){
		Pump pump = new Pump("Example");
		
		// Set Mass flow to zero
		pump.getInput("massFlowOut").setValue(0);
		pump.update();
		
		assertEquals("Pump power if off", 0, pump.getOutput("Ptotal").getValue(),     0);
		assertEquals("Mass flow if off",  0, pump.getOutput("massFlowIn").getValue(), 0);
		assertEquals("Pressure if off",   0, pump.getOutput("Ptotal").getValue(),     0);
		
		// Set Mass flow to 1kg/s
		pump.getInput("massFlowOut").setValue(3.6);
		pump.update();
		
		assertEquals("Pump power after ", 1500, pump.getOutput("Ptotal").getValue(), 0);
		assertEquals("Mass flow if on",  3.6, pump.getOutput("massFlowIn").getValue(), 0);
		assertEquals("Pressure if on",   1000000, pump.getOutput("Pressure").getValue(), 1);
	}
	
	@Test
	public void testPumpReservoir(){
		Pump pump = new Pump("ExampleReservoir");
		
		// Set Mass flow to zero
		pump.getInput("massFlowOut").setValue(0);
		pump.setSimulationPeriod(0);
		pump.update();
		
		assertEquals("Pump power @ 0s", 0, pump.getOutput("Ptotal").getValue(), 0);
		assertEquals("Mass flow @ 0s",  0, pump.getOutput("massFlowIn").getValue(), 0);
		assertEquals("Pressure @ 0s",   1650000, pump.getOutput("Pressure").getValue(), 0);
		
		// Set Mass flow to 1kg/s and sample time to 1s
		pump.getInput("massFlowOut").setValue(1);
		pump.setSimulationPeriod(1);
		pump.update();
		
		// After 1s, pump is still off
		assertTrue("Pump power @ 1s", 0 == pump.getOutput("Ptotal").getValue());
		
		/*
		 * after t_switch = rho/mdot_out *(V0g+V0f-V0g*p0g/p_min)  = 360s
		 * the pump must be on
		 */
		for(int i=0; i < 360; i++)
			pump.update();
		
		assertTrue("Pump power @ 361s", 0 < pump.getOutput("Ptotal").getValue());
		
		/*
		 * Turning off the inflow, the pump must switch off at pmax
		 */
		pump.getInput("massFlowOut").setValue(0);
		while( 0<pump.getOutput("Ptotal").getValue() )
			pump.update();
		
		assertEquals("Pressure @ switch off", 2000000, pump.getOutput("Pressure").getValue(), 10000);
	}

}
