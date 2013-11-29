/***********************************
 * $Id: PumpTest.java 96 2012-04-05 08:10:57Z sizuest $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/test/ch/ethz/inspire/emod/model/PumpTest.java $
 * $Author: sizuest $
 * $Date: 2012-04-05 10:10:57 +0200 (Do, 05 Apr 2012) $
 * $Rev: 96 $
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
		
		
		// Set Mass flow to 0.2 kg/s
		cpump.getInput("MassFlowOut").setValue(0.2);
		cpump.getInput("PumpCtrl").setValue(1);
		cpump.update();
		
		assertEquals("Pump losses through bypass ",   0, cpump.getOutput("PBypass").getValue(),      0.1);
		assertEquals("Hydraulic power",           286, cpump.getOutput("PHydr").getValue(),  1);
		assertEquals("Electric power consumption",   942, cpump.getOutput("PEl").getValue(),    1);
		assertEquals("Thermal power losses",   1477, cpump.getOutput("PTh").getValue(),    1);
	}
}
