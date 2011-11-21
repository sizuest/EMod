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
		element.getInput("massFlow").setValue(0);
		element.getInput("PThermal").setValue(0);
		element.update();
		
		assertEquals("Heat flow out", 0, element.getOutput("PLoss").getValue(),   0);
		assertEquals("Power demnd", 0, element.getOutput("PTotal").getValue(),  0);
		
		// Still off but with heat flow
		element.getInput("PThermal").setValue(1);
		element.update();
		
		assertEquals("Heat flow out", 0, element.getOutput("PLoss").getValue(),   0);	
	}
	
	@Test
	public void testHeatExchangerOn(){
		HeatExchanger element = new HeatExchanger("Example");
		
		// Turn element on, no heat flow
		element.getInput("massFlow").setValue(5);
		element.getInput("PThermal").setValue(0);
		element.update();
		
		assertEquals("Heat flow out",     0.3*element.getOutput("PPump").getValue(), element.getOutput("PLoss").getValue(),    1);
		assertEquals("Power demand pump", 0.005*(10000-5*10000/10)/0.7,              element.getOutput("PPump").getValue(),  1);
		assertEquals("Power demand",      element.getOutput("PPump").getValue(),     element.getOutput("PTotal").getValue(), 1);
		
		// Still on but with heat flow
		element.getInput("PThermal").setValue(1000);
		element.update();
		assertEquals("Heat flow out",     1000+0.3*element.getOutput("PPump").getValue(), element.getOutput("PLoss").getValue(),    1);
		assertEquals("Power demand",      element.getOutput("PPump").getValue()+1000/2,   element.getOutput("PTotal").getValue(), 1);
	}
	
}
