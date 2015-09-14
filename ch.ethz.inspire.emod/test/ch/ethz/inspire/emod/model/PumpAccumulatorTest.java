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

import ch.ethz.inspire.emod.model.HydraulicAccumulator;

public class PumpAccumulatorTest {
		
	@Test
	public void testPumpAccumulator(){
		HydraulicAccumulator pump = new HydraulicAccumulator("Example");
		
		// Set Mass flow to zero
		pump.getInput("Level").setValue(1);
		pump.getInput("MassFlowOut").setValue(0);
		pump.setSimulationTimestep(1);
		pump.update();
		
		assertEquals("Pump power @ 0s", 0,       pump.getOutput("PTotal").getValue(),     0);
		assertEquals("Mass flow @ 0s",  0,       pump.getOutput("MassFlowIn").getValue(), 0);
		assertEquals("Pressure @ 0s",   7000000, pump.getOutput("Pressure").getValue(),   0);
		
		// Set Mass flow to 1kg/s and sample time to 1s
		pump.getInput("MassFlowOut").setValue(0.01);
		pump.setSimulationTimestep(1);
		pump.update();
		
		// After 1s, pump is still off
		assertEquals("Pump power @ 1s", 0, pump.getOutput("PTotal").getValue(), 0);
		
		
		/*
		 * after t_switch = Vg0/FlowOut * (pg0/pmin-1)  = 40s
		 * the pump must be on
		 */
		for(int i=0; i < 31; i++) {
			pump.update();
			assertTrue("Pump power @ t="+i , 0 == pump.getOutput("PTotal").getValue() );
		}
	
		pump.update();
		assertEquals("Pump power @ 32s", 2216, pump.getOutput("PTotal").getValue(), 1);
		
		/*
		 * Turning off the inflow, the pump must switch off at pmax
		 */
		pump.getInput("MassFlowOut").setValue(0);
		while( 0<pump.getOutput("PTotal").getValue() )
			pump.update();
		
		assertEquals("Pressure @ switch off", 7000000, pump.getOutput("Pressure").getValue(), 10000);
	}

}
