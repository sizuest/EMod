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

import ch.ethz.inspire.emod.model.Amplifier;

public class AmplifierTest {
	
	/**
	 * Test Amplifier class
	 */
	@Test
	public void testAmplifier(){
		Amplifier amp = new Amplifier("Example");
		
		// Amp off 
		amp.getInput("State").setValue(0);
		amp.getInput("PDmd").setValue(1);
		amp.update();
		assertEquals("Control power", 0, amp.getOutput("PUse").getValue(), 0);
		assertEquals("Supply power",  0, amp.getOutput("PAmp").getValue(), 0);
		
		// Amp just running
		amp.getInput("State").setValue(1);
		amp.getInput("PDmd").setValue(0);
		amp.update();
		assertEquals("Control power", 10, amp.getOutput("PUse").getValue(), 0);
		assertEquals("Supply power",  0,  amp.getOutput("PAmp").getValue(),  0);
		
		// Amp running with power demand
		amp.getInput("State").setValue(1);
		amp.getInput("PDmd").setValue(1);
		amp.update();
		assertEquals("Control power",       10, amp.getOutput("PUse").getValue(),  0);
		assertEquals("Supply power",     1/.95, amp.getOutput("PAmp").getValue(),   0);
		assertEquals("Total power",   1/.95+10, amp.getOutput("PTotal").getValue(), 0.1);
		assertEquals("Power Loss",  1/.95-1+10, amp.getOutput("PLoss").getValue(),  0.1);
		
	}
	

}
