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
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;

public class PumpFluidTest {
	
	@Test
	public void testPump(){
		//PumpFluid pump = new PumpFluid("Example", 293, "Example");
		Pump pump = new Pump("Example");
		pump.getInput("TemperatureAmb").setValue(293);
		pump.getInput("State").setValue(0);
		pump.getFluidProperties().setMaterial(new Material("Example"));
		// Set pressure out to pressure_amb
		//pump.getInput("PressureOut").setValue(0);
		((Floodable)pump).getFluidProperties().setFlowRate(0.0);
		for(int i=0; i<4; i++)
			pump.update();
		
		assertEquals("Pump power if off", 0, pump.getOutput("PTotal").getValue(),     0);
		assertEquals("Flow if off",       0, pump.getFluidProperties().getFlowRate(), 0);
		assertEquals("Pressure if off",   0, ((FluidContainer)pump.getOutput("FluidOut")).getPressure(),   100000);
		
		// Set pressure out to 20 bar
		((FluidContainer)pump.getOutput("FluidOut")).setPressure(2000000);
		pump.getInput("State").setValue(1);
		for(int i=0; i<4; i++)
			pump.update();
		
		assertEquals("Pump power after ",   135, pump.getOutput("PTotal").getValue(),      0);
		assertEquals("Flow if on",          0.00014, ((Floodable)pump).getFluidProperties().getFlowRate(),  0);
	}

	@Test
	public void testPumpFluid(){
		Pump pump = new Pump("Hyfra_VWK_21_1S");
		
		pump.getInput("TemperatureAmb").setValue(293);
		
		pump.getFluidProperties().setMaterial(new Material("Monoethylenglykol_34"));
		
		pump.getFluidProperties().setFlowRate(0.00014);
		for(int i=0; i<1000; i++){
			pump.update();
		}
		
		System.out.println("PTotal: " + pump.getOutput("PTotal").getValue() + " PLoss: " + pump.getOutput("PLoss").getValue());
	}

}
