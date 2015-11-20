/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2013 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/

package ch.ethz.inspire.emod.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import ch.ethz.inspire.emod.model.Cylinder;
import ch.ethz.inspire.emod.utils.FluidContainer;

public class CylinderTest {
	
	/**
	 * Test Cylinder class
	 */
	@Test
	public void testCylinder(){
		Cylinder cyl = new Cylinder("Example");
		
		cyl.getDynamicState("Position").setInitialCondition(0);
		
		// TODO
		
		cyl.getInput("Force").setValue(0);
		cyl.getInput("Velocity").setValue(0);
		cyl.setSimulationTimestep(1);
		cyl.update();
		
		assertEquals("No Force/movement", 0, ((FluidContainer)cyl.getOutput("FluidOut")).getFluidCircuitProperties().getFlowRateIn(), 0);
		
		cyl.getInput("Force").setValue(1000);
		cyl.getInput("Velocity").setValue(0);
		cyl.update();
		
		assertEquals("Clamping", 606419, ((FluidContainer)cyl.getOutput("FluidOut")).getFluidCircuitProperties().getPressureDrop(), 1000);
		assertEquals("Clamping", 0, ((FluidContainer)cyl.getOutput("FluidOut")).getFluidCircuitProperties().getFlowRateIn(), 1E-6);
		
	}

}
