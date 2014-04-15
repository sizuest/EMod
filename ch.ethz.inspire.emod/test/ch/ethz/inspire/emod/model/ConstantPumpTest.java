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

import ch.ethz.inspire.emod.model.ConstantPump;

public class ConstantPumpTest {
	
	@Test
	public void testConstantPump(){
		ConstantPump cpump = new ConstantPump("Example");
		
		
		cpump.getInput("MassFlowOut").setValue(0.4928);
		cpump.getInput("PumpCtrl").setValue(1);
		cpump.getInput("RotSpeed").setValue(3200);
		cpump.getInput("DemandedPressure").setValue(700000);
		cpump.update();
		
		assertEquals("Pressure ",                   1500000, cpump.getOutput("PressureOut").getValue(), 1.1);
		assertEquals("Pump losses through bypass ", 65.8,    cpump.getOutput("PBypass").getValue(),     0.1);
		assertEquals("Hydraulic power",             739,     cpump.getOutput("PUse").getValue(),        1);
		assertEquals("Electric power consumption",  1040,    cpump.getOutput("PTotal").getValue(),      1);
		assertEquals("Thermal power losses",        301,     cpump.getOutput("PLoss").getValue(),       1);
	}
}
