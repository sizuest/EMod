/***********************************
 * $Id: ValveTest.java 101 2013-10-24 11:36:24Z sizuest $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/test/ch/ethz/inspire/emod/model/CylinderTest.java $
 * $Author: sizuest $
 * $Date: 2013-10-24 13:36:24 +0200 (Do, 24 Okt 2013) $
 * $Rev: 101 $
 *
 * Copyright (c) 2013 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/

package ch.ethz.inspire.emod.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import ch.ethz.inspire.emod.model.Valve;

/**
 * @author Simon Züst
 *
 */
public class ValveTest {
	
	/**
	 * Test Valve class
	 */
	@Test
	public void testValve(){
		Valve val = new Valve("Example");
		
		// TODO
		val.getFluidPropertiesList().get(0).setFlowRatesIn(new double[]{.25/1000});
		val.getInput("ValveCtrl").setValue(1);
		val.update();
		
		assertEquals("PressureLoss", 312500, val.getPressure(val.getFluidPropertiesList().get(0).getFlowRate()), 30000);		
	}

}
