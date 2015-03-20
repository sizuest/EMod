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
import ch.ethz.inspire.emod.utils.FluidContainer;

public class PumpFluidTest {
	
	@Test
	public void testPump(){
		//PumpFluid pump = new PumpFluid("Example", 293, "Example");
		PumpFluid pump = new PumpFluid("Example");
		pump.getInput("TemperatureAmb").setValue(293);
		pump.setFluid("Example");
		// Set pressure out to pressure_amb
		//pump.getInput("PressureOut").setValue(0);
		pump.getInput("FlowRateOut").setValue(0);
		for(int i=0; i<4; i++)
			pump.update();
		
		assertEquals("Pump power if off", 0, pump.getOutput("PTotal").getValue(),     0);
		assertEquals("Flow if off",       0, ((FluidContainer)pump.getInput("FluidIn")).getFlowRate(), 0);
		assertEquals("Pressure if off",   0, ((FluidContainer)pump.getOutput("FluidOut")).getPressure(),   100000);
		
		// Set pressure out to 20 bar
		//pump.getInput("PressureOut").setValue(2000000);
		pump.getInput("FlowRateOut").setValue(0.00014);
		for(int i=0; i<4; i++)
			pump.update();
		
		assertEquals("Pump power after ",   1500, pump.getOutput("PTotal").getValue(),      0);
		assertEquals("Flow if on",           /*3.6*//*1.44*/ 0.00014, ((FluidContainer)pump.getInput("FluidIn")).getFlowRate(),  0);
		assertEquals("Pressure if on",   2000000, ((FluidContainer)pump.getOutput("FluidOut")).getPressure(),    400000);
	}

	@Test
	public void testPumpFluid(){
		PumpFluid pump = new PumpFluid("Hyfra_VWK_21_1S");
		
		pump.getInput("TemperatureAmb").setValue(293);
		
		pump.setFluid("Water");
		
		pump.getInput("FlowRateOut").setValue(0.00014);
		for(int i=0; i<1000; i++){
			pump.update();
		}
	}

}
