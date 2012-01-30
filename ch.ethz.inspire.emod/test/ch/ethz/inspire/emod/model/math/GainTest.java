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

package ch.ethz.inspire.emod.model.math;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import ch.ethz.inspire.emod.model.math.Gain;
import ch.ethz.inspire.emod.model.units.Unit;

public class GainTest {
	
	@Test
	public void testGain(){
		Gain g = new Gain(Unit.WATT, 3);
		
		// Set input to 5
		g.getInput("Input").setValue(5);
		g.update();
		
		assertEquals("Result", 15, g.getOutput("Output").getValue(), 0);
		
	}
	
}