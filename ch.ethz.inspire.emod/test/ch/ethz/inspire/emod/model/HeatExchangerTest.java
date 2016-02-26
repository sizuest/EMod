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

import ch.ethz.inspire.emod.model.Cooler;;

public class HeatExchangerTest {
	
	/**
	 * Test Fan class
	 */
	@Test
	public void testHeatExchangerOff(){
		Cooler element = new Cooler("Example");
		
		// Turn element off
		element.getInput("level").setValue(0);
		element.update();
		
		assertEquals("Heat flow out", 0, element.getOutput("PThermal").getValue(),   0);
		assertEquals("Power demand",  0, element.getOutput("PTotal").getValue(),  0);
		
	}
	
	@Test
	public void testHeatExchangerOn(){
		Cooler element = new Cooler("Example");
		
		// Turn element on, no heat flow
		element.getInput("level").setValue(1);
		element.update();
		
		assertEquals("Heat flow out",     2*500, element.getOutput("PThermal").getValue(),  0);
		assertEquals("Power demand",      500,   element.getOutput("PTotal").getValue(), 0);
	}
	
	@Test
	public void testHeatExchangerHyfra(){
		Cooler heex = new Cooler("Hyfra_VWK_21_1S");
		
		heex.getInput("level").setValue(1);
		heex.update();
		
		System.out.println("PThermal: " + heex.getOutput("PThermal").getValue() + " PTotal: " + heex.getOutput("PTotal").getValue());
	}
}
