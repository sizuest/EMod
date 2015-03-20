package ch.ethz.inspire.emod.utils;

import org.junit.Test;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.HeatExchanger;
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
		
		pip1.setSimulationTimestep(0.01);
		pf.setSimulationTimestep(0.01);
		pip2.setSimulationTimestep(0.01);
		
		((FluidContainer)pip1.getInput("FluidIn")).setTemperature(293);
		
		FluidConnection fc1 = new FluidConnection(pip1, pf);
		fc1.init(293, 100000, 0);
		
		FluidConnection fc2 = new FluidConnection(pf, pip2);
		fc2.init(293, 100000, 0);
				
		for(int i=0; i<3; i++){
			//what first??
			fc1.update();
			fc2.update();
			pip1.update();
			pf.update();
			pip2.update();
		}

		
		//set FlowRate needed out of pump
		pf.getInput("FlowRateOut").setValue(0.001);
		pf.update();
		
		for(int i=0; i<3; i++){
			//what first??
			fc1.update();
			fc2.update();
			pip1.update();
			pf.update();
			pip2.update();
		}
		
		System.out.println(pf.getInput("FlowRateOut").getValue());
	}

	@Test
	public void testTankPumpPipe() throws Exception{
		System.out.println("*** testTankPumpPipe ***");

		Tank tank = new Tank("Schaublin42L");
		tank.getInput("TemperatureAmb").setValue(293);
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
		pf.getInput("FlowRateOut").setValue(0.00014);
		
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
	
	@Test
	public void testSchaublin1() throws Exception{
		Machine.clearMachine();
		System.out.println("*** testSchaublin ***");
		Tank tank = new Tank("Schaublin42L");
		tank.getInput("TemperatureAmb").setValue(293);
		//
		PumpFluid pump = new PumpFluid("Example");
		//Pipe pip1 = new Pipe("Schaublin42LV", tank.getInput("TemperatureAmb").getValue(), tank.getFluidType());
		Pipe pip1 = new Pipe("Schaublin42LV");
		//Pipe piph = new Pipe("Example",       tank.getInput("TemperatureAmb").getValue(), tank.getFluidType());
		Pipe piph = new Pipe("Example");
		//Pipe pip2 = new Pipe("Schaublin42LR", tank.getInput("TemperatureAmb").getValue(), tank.getFluidType());
		Pipe pip2 = new Pipe("Schaublin42LR");
		
		pump.getInput("TemperatureAmb").setValue(293);
		pip1.getInput("TemperatureAmb").setValue(293);
		piph.getInput("TemperatureAmb").setValue(293);
		pip2.getInput("TemperatureAmb").setValue(293);
		
		double timestep = 0.01;
		tank.setSimulationTimestep(timestep);
		pump.setSimulationTimestep(timestep);
		pip1.setSimulationTimestep(timestep);
		piph.setSimulationTimestep(timestep);
		pip2.setSimulationTimestep(timestep);
		
		pip1.getInput("HeatFlowIn").setValue(0);
		piph.getInput("HeatFlowIn").setValue(0.5);
		pip2.getInput("HeatFlowIn").setValue(0);
		
		FluidConnection fc1 = new FluidConnection(tank, pump);
		fc1.init(293, 100000, 0);
		FluidConnection fc2 = new FluidConnection(pump, pip1);
		fc2.init(293, 100000, 0);
		FluidConnection fc3 = new FluidConnection(pip1, piph);
		fc2.init(293, 100000, 0);
		FluidConnection fc4 = new FluidConnection(piph, pip2);
		fc3.init(293, 100000, 0);
		FluidConnection fc5 = new FluidConnection(pip2, tank);
		fc4.init(293, 100000, 0);
		
		for(int i=0; i<10; i++){
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		}	
		
		//((FluidContainer)pump.getInput("FluidIn")).setPressure(20000000);
		//pump.getInput("PressureOut").setValue(2000000);
		pump.getInput("FlowRateOut").setValue(0.00014);
		//pump.getInput("FlowRateOut").setValue(8.65);
		
		for(int i=0; i<10; i++){
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		}
		
		piph.getInput("HeatFlowIn").setValue(10);
		//tank.getInput("HeatFlowIn").setValue(-50);
		
		for(int i=0; i<4; i++){
		
			//while(((FluidContainer)tank.getOutput("FluidOut")).getTemperature() <= 303){
				tank.update();
				fc1.update();
				pump.update();
				fc2.update();
				pip1.update();
				fc3.update();
				piph.update();
				fc4.update();
				pip2.update();
				fc5.update();
			//}
		
			tank.getInput("HeatFlowIn").setValue(-1);

			//while(((FluidContainer)tank.getOutput("FluidOut")).getTemperature() >= 299){
				tank.update();
				fc1.update();
				pump.update();
				fc2.update();
				pip1.update();
				fc3.update();
				piph.update();
				fc4.update();
				pip2.update();
				fc5.update();
			//}
			
			tank.getInput("HeatFlowIn").setValue(0);
		}
		
		piph.getInput("HeatFlowIn").setValue(0);
		
		for(int i=0; i<10000; i++){
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		}
	}
	
	@Test
	public void testSchaublin2() throws Exception{
		Machine.clearMachine();
		System.out.println("*** testSchaublin ***");
		Tank tank = new Tank("Schaublin42L");
		tank.getInput("TemperatureAmb").setValue(293);
		
		//
		PumpFluid pump = new PumpFluid("Example");
		//Pipe pip1 = new Pipe("Schaublin42LV", tank.getInput("TemperatureAmb").getValue(), tank.getFluidType());
		Pipe pip1 = new Pipe("Schaublin42LV");
		//Pipe piph = new Pipe("Example",       tank.getInput("TemperatureAmb").getValue(), tank.getFluidType());
		Pipe piph = new Pipe("Example");
		//Pipe pip2 = new Pipe("Schaublin42LR", tank.getInput("TemperatureAmb").getValue(), tank.getFluidType());
		Pipe pip2 = new Pipe("Schaublin42LR");


		
		pump.getInput("TemperatureAmb").setValue(293);
		pip1.getInput("TemperatureAmb").setValue(293);
		piph.getInput("TemperatureAmb").setValue(293);
		pip2.getInput("TemperatureAmb").setValue(293);
		
		double timestep = 0.01;
		tank.setSimulationTimestep(timestep);
		pump.setSimulationTimestep(timestep);
		pip1.setSimulationTimestep(timestep);
		piph.setSimulationTimestep(timestep);
		pip2.setSimulationTimestep(timestep);
		
		pip1.getInput("HeatFlowIn").setValue(0);
		piph.getInput("HeatFlowIn").setValue(0.5);
		pip2.getInput("HeatFlowIn").setValue(0);
		
		FluidConnection fc1 = new FluidConnection(tank, pump);
		fc1.init(293, 100000, 0);
		FluidConnection fc2 = new FluidConnection(pump, pip1);
		fc2.init(293, 100000, 0);
		FluidConnection fc3 = new FluidConnection(pip1, piph);
		fc2.init(293, 100000, 0);
		FluidConnection fc4 = new FluidConnection(piph, pip2);
		fc3.init(293, 100000, 0);
		FluidConnection fc5 = new FluidConnection(pip2, tank);
		fc4.init(293, 100000, 0);
		
		for(int i=0; i<10; i++){
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		}	
		
		pump.getInput("FlowRateOut").setValue(0.00014);
		
		for(int i=0; i<10; i++){
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		}
		
		piph.getInput("HeatFlowIn").setValue(10);
		
		//while(((FluidContainer)tank.getOutput("FluidOut")).getTemperature() <= 303){
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		//}
		
		piph.getInput("HeatFlowIn").setValue(0);
		tank.getInput("HeatFlowIn").setValue(-0.001);
		for(int i=0; i<10000; i++){
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		}
	}
	
	@Test
	public void testSchaublin3() throws Exception{
		Machine.clearMachine();
		System.out.println("*** testSchaublin ***");
		Tank tank = new Tank("Schaublin42L");
		tank.getInput("TemperatureAmb").setValue(293);
		
		//
		PumpFluid pump = new PumpFluid("Example");
		//Pipe pip1 = new Pipe("Schaublin42LV", tank.getInput("TemperatureAmb").getValue(), tank.getFluidType());
		Pipe pip1 = new Pipe("Schaublin42LV");
		//Pipe piph = new Pipe("Example",       tank.getInput("TemperatureAmb").getValue(), tank.getFluidType());
		Pipe piph = new Pipe("Example");
		//Pipe pip2 = new Pipe("Schaublin42LR", tank.getInput("TemperatureAmb").getValue(), tank.getFluidType());
		Pipe pip2 = new Pipe("Schaublin42LR");
		
		//TODO manick: test heatexchanger
		HeatExchanger heat = new HeatExchanger("Hyfra_VWK_21_1S");
		heat.getInput("level").setValue(0);
		IOConnection io = new IOConnection(heat.getOutput("PThermal"), tank.getInput("HeatExchangerIn"));
		
		pump.getInput("TemperatureAmb").setValue(293);
		pip1.getInput("TemperatureAmb").setValue(293);
		piph.getInput("TemperatureAmb").setValue(293);
		pip2.getInput("TemperatureAmb").setValue(293);
		
		double timestep = 0.01;
		tank.setSimulationTimestep(timestep);
		pump.setSimulationTimestep(timestep);
		pip1.setSimulationTimestep(timestep);
		piph.setSimulationTimestep(timestep);
		pip2.setSimulationTimestep(timestep);
		
		pip1.getInput("HeatFlowIn").setValue(0);
		piph.getInput("HeatFlowIn").setValue(50);
		pip2.getInput("HeatFlowIn").setValue(0);
		
		FluidConnection fc1 = new FluidConnection(tank, pump);
		fc1.init(293, 100000, 0);
		FluidConnection fc2 = new FluidConnection(pump, pip1);
		fc2.init(293, 100000, 0);
		FluidConnection fc3 = new FluidConnection(pip1, piph);
		fc2.init(293, 100000, 0);
		FluidConnection fc4 = new FluidConnection(piph, pip2);
		fc3.init(293, 100000, 0);
		FluidConnection fc5 = new FluidConnection(pip2, tank);
		fc4.init(293, 100000, 0);
		
		for(int i=0; i<10; i++){
			heat.update();
			
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		}	
		
		//((FluidContainer)pump.getInput("FluidIn")).setPressure(20000000);
		//pump.getInput("PressureOut").setValue(2000000);
		pump.getInput("FlowRateOut").setValue(0.00014);
		
		for(int i=0; i<10; i++){
			heat.update();
			
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		}
		
		piph.getInput("HeatFlowIn").setValue(500);
		//tank.getInput("HeatFlowIn").setValue(-50);
		for(int j=0; j<10000; j++){
			heat.update();
			
			tank.update();
			fc1.update();
			pump.update();
			fc2.update();
			pip1.update();
			fc3.update();
			piph.update();
			fc4.update();
			pip2.update();
			fc5.update();
		}
		
		
		for(int i=0; i<4; i++){
		
			int k = 0;
			while(((FluidContainer)tank.getOutput("FluidOut")).getTemperature() <= 303 && k <= 10000){
			//for(int j=0; j<10000; j++){
				heat.update();
				
				tank.update();
				fc1.update();
				pump.update();
				fc2.update();
				pip1.update();
				fc3.update();
				piph.update();
				fc4.update();
				pip2.update();
				fc5.update();
				k++;
			}
		
			//tank.getInput("HeatFlowIn").setValue(-1);
			heat.getInput("level").setValue(1);
			heat.update();
			tank.getInput("HeatExchangerIn").setValue(heat.getOutput("PThermal").getValue());

			k=0;
			while(((FluidContainer)tank.getOutput("FluidOut")).getTemperature() >= 299 && k <= 10000){
			//for(int j=0; j<10000; j++){
				heat.update();
				
				tank.update();
				fc1.update();
				pump.update();
				fc2.update();
				pip1.update();
				fc3.update();
				piph.update();
				fc4.update();
				pip2.update();
				fc5.update();
				k++;
			}
			
			heat.getInput("level").setValue(0);
			heat.update();
			tank.getInput("HeatExchangerIn").setValue(heat.getOutput("PThermal").getValue());
			//tank.getInput("HeatFlowIn").setValue(0);
		}
	}
}
