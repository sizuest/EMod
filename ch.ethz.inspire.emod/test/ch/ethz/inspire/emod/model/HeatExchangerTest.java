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

import ch.ethz.inspire.emod.model.HeatExchanger;;

public class HeatExchangerTest {
	
	/**
	 * Test Fan class
	 */
	@Test
	public void testHeatExchangerOff(){
		HeatExchanger element = new HeatExchanger("Example");
		
		// Turn element off
		element.getInput("level").setValue(0);
		element.update();
		
		assertEquals("Heat flow out", 0, element.getOutput("PThermal").getValue(),   0);
		assertEquals("Power demand",  0, element.getOutput("PTotal").getValue(),  0);
		
	}
	
	@Test
	public void testHeatExchangerOn(){
		HeatExchanger element = new HeatExchanger("Example");
		
		// Turn element on, no heat flow
		element.getInput("level").setValue(1);
		element.update();
		
		assertEquals("Heat flow out",     2*500, element.getOutput("PThermal").getValue(),  0);
		assertEquals("Power demand",      500,   element.getOutput("PTotal").getValue(), 0);
		
	}
	
}