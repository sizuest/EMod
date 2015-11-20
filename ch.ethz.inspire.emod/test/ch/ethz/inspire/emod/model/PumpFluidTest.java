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
import ch.ethz.inspire.emod.model.fluid.PressureReference;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidConnection;
import ch.ethz.inspire.emod.utils.FluidContainer;

public class PumpFluidTest {
	
	@Test
	public void testPump() throws Exception{
		//PumpFluid pump = new PumpFluid("Example", 293, "Example");
		PressureReference pref = new PressureReference(0);
		Pump pump = new Pump("Example");
		FluidConnection fc = new FluidConnection(pump, pref);
		
		pump.getInput("TemperatureAmb").setValue(293);
		pump.getInput("State").setValue(0);
		pump.getFluidPropertiesList().get(0).setMaterial(new Material("Example"));
		
		fc.init(293, 100000);
		
		// Set pressure out to pressure_amb
		//pump.getInput("PressureOut").setValue(0);
		((Floodable)pump).getFluidPropertiesList().get(0).setFlowRateIn(0.0);
		for(int i=0; i<4; i++){
			pump.update();
			pref.update();
			fc.update();
		}
		
		assertEquals("Pump power if off", 0, pump.getOutput("PTotal").getValue(),     0);
		assertEquals("Flow if off",       0, pump.getFluidPropertiesList().get(0).getFlowRate(), 0);
		assertEquals("Pressure if off",   0, ((FluidContainer)pump.getOutput("FluidOut")).getPressure(),   100000);
		
		// Set pressure out to 2 bar
		pref.setPressureDrop(2E5);
		pump.getInput("State").setValue(1);
		for(int i=0; i<4; i++){
			pump.update();
			pref.update();
			fc.update();
		}
		
		assertEquals("Pump power after ",   1088, pump.getOutput("PTotal").getValue(),      1);
		assertEquals("Flow if on",          5.6E-6, ((Floodable)pump).getFluidPropertiesList().get(0).getFlowRateIn(),  .1E-6);
	}

	@Test
	public void testPumpFluid(){
		Pump pump = new Pump("Hyfra_VWK_21_1S");
		
		pump.getInput("TemperatureAmb").setValue(293);
		
		pump.getFluidPropertiesList().get(0).setMaterial(new Material("Monoethylenglykol_34"));
		
		pump.getFluidPropertiesList().get(0).setFlowRateIn(0.00014);
		for(int i=0; i<1000; i++){
			pump.update();
		}
		
		System.out.println("PTotal: " + pump.getOutput("PTotal").getValue() + " PLoss: " + pump.getOutput("PLoss").getValue());
	}

}
