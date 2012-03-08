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

import ch.ethz.inspire.emod.model.ServoMotor;

public class ServoMotorTest {
	
	@Test
	public void testServoMotor(){
		ServoMotor servo = new ServoMotor("Example");

		// Disable apply torque and speed
		servo.getInput("Torque").setValue(10);
		servo.getInput("RotSpeed").setValue(10);
		servo.update();
		
		assertEquals("Servo power", 3*(10+1)/1*(0.1*10+2*(10+1)/1), 
				                        servo.getOutput("PTotal").getValue(), 0.1);
		assertEquals("Power Loss",  3*Math.pow((10+1)/1,2)*2, 
                                        servo.getOutput("PLoss").getValue(), 0.1);
		assertEquals("Efficiency",  servo.getOutput("PUse").getValue()/servo.getOutput("PTotal").getValue(),
										servo.getOutput("Efficiency").getValue(), 0);
		
		
	}
	
}
