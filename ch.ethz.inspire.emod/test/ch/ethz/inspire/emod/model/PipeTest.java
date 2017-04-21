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
import ch.ethz.inspire.emod.model.Pipe;
import ch.ethz.inspire.emod.utils.FluidConnection;
import ch.ethz.inspire.emod.utils.FluidContainer;

public class PipeTest {
	
	@Test
	public void testPipeConnection() throws Exception{
		Pipe pip1 = new Pipe("Example", 303, "Example");
		Pipe pip2 = new Pipe("Example", 202, "Example");
				
		pip1.setSimulationTimestep(0.01);
		pip2.setSimulationTimestep(0.01);
		
		
		FluidConnection fc = new FluidConnection(pip1, pip2);
		fc.init(293, 100000);
	
		
		pip1.getInput("TemperatureAmb").setValue(293);
		
		for(int i=0; i<100; i++){
			//what first??
			fc.update();
			System.out.print("pipe1 ");
			pip1.update();
			System.out.print("pipe2 ");
			pip2.update();
		}
	}
}


