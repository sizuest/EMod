package ch.ethz.inspire.emod.model;

import org.junit.Test;

import ch.ethz.inspire.emod.utils.FluidContainer;

public class TankTest {
	
	@Test
	public void testCreateTank(){
		Tank tank1 = new Tank("Example");
		System.out.println(tank1.getType());
		System.out.println(tank1.getVolume());
		
		Tank tank2 = new Tank("Schaublin42L");
		System.out.println(tank2.getType());
		System.out.println(tank2.getVolume());
	}
	
	@Test
	public void testTank(){
		//create new Tank of Type Schaublin42L
		Tank tank1 = new Tank("Schaublin42L", 293);
		tank1.setSimulationTimestep(1);
		tank1.getInput("TemperatureAmb").setValue(293);
		//tank1.getInput("PressureAmb").setValue(105000);
		
		//set ambient temperature and fluidin/fluidout values (8.65 l/min equals 0.000144166667 m^3/s)
		((FluidContainer)tank1.getInput("FluidIn")).  setValues(293, 1);
		((FluidContainer)tank1.getOutput("FluidOut")).setValues(293, 1);
		tank1.getFluidProperties().setFlowRateIn(0.000144);
		
		//tank1.getInput("HeatFlowIn").setValue(100);
		
		for(int i=0; i<60; i++){
			tank1.update();
			//tank1.getFluid().toString();
		}
	}
	
	@Test
	public void testTank2(){
		//create new Tank with 1m^3
		Tank tank = new Tank("Test",293);
		tank.setSimulationTimestep(10);
		tank.getInput("TemperatureAmb").setValue(293);
		
		((FluidContainer)tank.getInput("FluidIn")).setValues(304, 100000);
		((FluidContainer)tank.getOutput("FluidOut")).setValues(303, 100000);
		tank.getFluidProperties().setFlowRateIn(0.00014);
		
		//tank.getInput("HeatFlowIn").setValue(1);
		
		for(int i=0; i<10000; i++){
			tank.update();
		}
	}
}
