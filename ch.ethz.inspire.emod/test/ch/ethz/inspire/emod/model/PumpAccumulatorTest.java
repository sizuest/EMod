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
import ch.ethz.inspire.emod.model.HydraulicAccumulator;
import ch.ethz.inspire.emod.model.material.Material;

/**
 * @author sizuest
 *
 */
public class PumpAccumulatorTest {
		
	/**
	 * 
	 */
	@Test
	public void testPumpAccumulator(){
		HydraulicAccumulator pump = new HydraulicAccumulator("Example");
		

		
		// Set Mass flow to 1kg/s and sample time to 1s
		pump.getFluidPropertiesList().get(0).setMaterial(new Material("Water"));
		pump.getFluidPropertiesList().get(1).setFlowRatesIn(new double[]{0.01/1000});
		pump.setSimulationTimestep(1);
		pump.update();
		
		// After 1s, pump is on
		assertEquals("Pump state @ 1s", 1, pump.getOutput("State").getValue(), 0);
		
		
		/*
		 * after t_switch = Vg0/FlowOut * (pg0/pmin-1)  = 40s
		 * the pump must be on
		 */
		for(int i=0; i < 31; i++) {
			pump.update();
			assertEquals("Pump state @ t="+i, 1, pump.getOutput("State").getValue(), 0 );
		}
	
		pump.update();
		assertEquals("Pump state @ 32s", 0, pump.getOutput("State").getValue(), 1);
		
		/*
		 * Turning off the inflow, the pump must switch off at pmax
		 */
		pump.getFluidPropertiesList().get(1).setFlowRatesIn(new double[]{0.0});
		pump.getFluidPropertiesList().get(0).setFlowRatesIn(new double[]{0.01/1000});
		while( 0<pump.getOutput("State").getValue() )
			pump.update();
		
		assertEquals("Pressure @ switch off", 200000, pump.getOutput("PressureGas").getValue(), 10000);
	}

}
