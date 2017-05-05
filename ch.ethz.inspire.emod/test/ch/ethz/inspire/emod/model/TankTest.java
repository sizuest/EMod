package ch.ethz.inspire.emod.model;

import org.junit.Test;

/**
 * @author Simon Züst
 *
 */
public class TankTest {
	
	/**
	 * 
	 */
	@Test
	public void testCreateTank(){
		Tank tank1 = new Tank("Example");
		System.out.println(tank1.getType());
		System.out.println(tank1.getVolume());
		
		Tank tank2 = new Tank("Schaublin42L");
		System.out.println(tank2.getType());
		System.out.println(tank2.getVolume());
	}
	
	/**
	 * 
	 */
	@Test
	public void testTank(){
		//create new Tank of Type Schaublin42L
		Tank tank1 = new Tank("Schaublin42L", 293);
		tank1.setSimulationTimestep(1);
		tank1.getInput("TemperatureAmb").setValue(293);
		//tank1.getInput("PressureAmb").setValue(105000);
		
		//set ambient temperature and fluidin/fluidout values (8.65 l/min equals 0.000144166667 m^3/s)

		//tank1.getInput("HeatFlowIn").setValue(100);
		
		for(int i=0; i<60; i++){
			tank1.update();
			//tank1.getFluid().toString();
		}
	}
}
