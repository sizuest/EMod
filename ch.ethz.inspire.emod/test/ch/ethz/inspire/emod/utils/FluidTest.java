package ch.ethz.inspire.emod.utils;

import org.junit.Test;

import ch.ethz.inspire.emod.model.Pipe;
import ch.ethz.inspire.emod.model.PumpFluid;
import ch.ethz.inspire.emod.model.Tank;

public class FluidTest {
	/**
	 * Test Pipe class
	 * @throws Exception 
	 */	
	@Test
	public void testPipePumpPipe() throws Exception{
		System.out.println("*** testPipePumpPipe ***");

		Pipe pip1 		= new Pipe("Example", 293, "Example");
		PumpFluid pf	= new PumpFluid("Example", 293, "Example");
		Pipe pip2 		= new Pipe("Example", 293, "Example");
		//pip1.getFluid().setPressure(20000);
		
		pip1.setSimulationTimestep(1);
		pf.setSimulationTimestep(1);
		pip2.setSimulationTimestep(1);
		
		FluidConnection fc1 = new FluidConnection(pip1, pf);
		fc1.init(293, 2000000, 0.1);
		
		FluidConnection fc2 = new FluidConnection(pf, pip2);
		fc2.init(293, 2000000, 0.1);
				
		//set temperature of fluid flowing into pipe1
		((FluidContainer)pip1.getInput("FluidIn")).setTemperature(293);
		//set pressure needed out of pipe2
		((FluidContainer)pip2.getOutput("FluidOut")).setPressure(2000000);
		//set flowrate needed out of pipe2
		((FluidContainer)pip2.getOutput("FluidOut")).setFlowRate(0.1);
		
		pip1.getInput("TemperatureAmb").setValue(293);
	
		for(int i=0; i<1; i++){
			//what first??
			fc1.update();
			fc2.update();
			pip1.update();
			pf.update();
			pip2.update();
		}
	}

	@Test
	public void testTankPumpPipe() throws Exception{
		System.out.println("*** testTankPumpPipe ***");

		Tank tank		= new Tank("Example", 293, "Example");
		PumpFluid pf	= new PumpFluid("Example", 293, "Example");
		Pipe pip 		= new Pipe("Example", 293, "Example");

		tank.setSimulationTimestep(1);
		pf.setSimulationTimestep(1);
		pip.setSimulationTimestep(1);
		
		FluidConnection fc1 = new FluidConnection(tank, pf);
		fc1.init(293, 100000, 0);
		
		FluidConnection fc2 = new FluidConnection(pf, pip);
		fc2.init(293, 100000, 0);
		
		FluidConnection fc3 = new FluidConnection(pip, tank);
		fc3.init(293, 100000, 0);
		
		//TODO manick: tempAmb should be set as Machine var!
		tank.getInput("TemperatureAmb").setValue(293);
		pf.getInput("TemperatureAmb").setValue(293);
		pip.getInput("TemperatureAmb").setValue(293);
		
	
		for(int i=0; i<10; i++){
			fc1.update();
			fc2.update();
			fc3.update();

			tank.update();
			pf.update();
			pip.update();
		}

		//--> set Pump to ON i.e. set its pressure out value to 20bar
		pf.getInput("PressureOut").setValue(2000000);
		
		for(int i=0; i<10; i++){
			//what first??

			fc1.update();
			fc2.update();
			fc3.update();

			tank.update();
			pf.update();
			pip.update();
		}	
	}
	
	@Test
	public void testPipePipe() throws Exception{
		System.out.println("*** testPipePipe ***");

		Pipe pip1 		= new Pipe("Example", 293, "Example");
		Pipe pip2		= new Pipe("Example", 293, "Example");
		
		pip1.setSimulationTimestep(1);
		pip2.setSimulationTimestep(2);
		
		FluidConnection fc1 = new FluidConnection(pip1, pip2);
		fc1.init(293, 100000, 0);

		pip1.getInput("TemperatureAmb").setValue(293);
		pip2.getInput("TemperatureAmb").setValue(293);
		
		for(int i=0; i<10; i++){
			//what first??

			fc1.update();

			pip1.update();
			pip2.update();
		}

		//
		((FluidContainer)pip1.getInput("FluidIn")).setPressure(2000000);
		
		for(int i=0; i<10; i++){
			//what first??

			fc1.update();

			pip1.update();
			pip2.update();
		}	
	}	
	
}
