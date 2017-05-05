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

/**
 * @author Simon Züst
 *
 */
public class HeatExchangerTest {
	
	/**
	 * Test Fan class
	 */
	@Test
	public void testHeatExchangerOff(){
		Cooler element = new Cooler("Example");
		
		// Turn element off
		element.getInput("State").setValue(0);
		element.update();
		
		assertEquals("Heat flow out", 0, element.getOutput("PThermal").getValue(),   0);
		assertEquals("Power demand",  0, element.getOutput("PTotal").getValue(),  0);
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testHeatExchangerOn(){
		Cooler element = new Cooler("Example");
		
		// Turn element on, no heat flow
		element.getInput("State").setValue(1);
		element.getInput("Temperature").setValue(200);
		element.update();
		
		assertEquals("Heat flow out",     0, element.getOutput("PThermal").getValue(), 0);
		assertEquals("Power demand",      0,   element.getOutput("PTotal").getValue(), 0);
		
		// Turn element on, heat flow
		element.getInput("State").setValue(1);
		element.getInput("Temperature").setValue(400);
		element.update();
		
		assertEquals("Heat flow out",     2*500, element.getOutput("PThermal").getValue(),  0);
		assertEquals("Power demand",      500,   element.getOutput("PTotal").getValue(), 0);
	}
}
