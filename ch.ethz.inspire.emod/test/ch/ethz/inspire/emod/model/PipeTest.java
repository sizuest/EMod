/***********************************
 * $Id: PipTest.java 101 2013-10-24 11:36:24Z sizuest $
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
import ch.ethz.inspire.emod.model.Pipe;
import ch.ethz.inspire.emod.utils.FluidConnection;
import ch.ethz.inspire.emod.utils.FluidContainer;

public class PipeTest {
	
	/**
	 * Test Pipe class
	 * @throws Exception 
	 */
	@Test
	public void testPipe() throws Exception{
		//ThermalArray fluid = new ThermalArray("Example", 01, 1);
		Pipe pip = new Pipe("Example", 303, "Example");
		//Pipe pip = new Pipe("Example", 303);
		
		// TODO stimmt noch nicht
		
		pip.setSimulationTimestep(1);
		
		//set temperature of fluid flowing into pipe1
		((FluidContainer)pip.getInput("FluidIn")).setTemperature(293);
		//set pressure needed out of pipe2
		((FluidContainer)pip.getInput("FluidIn")).setPressure(2000000);
		//set flowrate needed out of pipe2
		((FluidContainer)pip.getOutput("FluidOut")).setFlowRate(0.1);
		
		
		//((FluidContainer)pip.getInput("FluidIn")).setPressure(2000000);
		//pip.getInput("PressureOut").setValue(2000000);
		//((FluidContainer)pip.getInput("FluidIn")).setFlowRate(0.1);
		//pip.getInput("MassFlowOut").setValue(0.1);
		//((FluidContainer)pip.getInput("FluidIn")).setInitialTemperature(303);
		//pip.getInput("TemperatureIn").setValue(303);
		//temperature external should be a machinecomponent value
		//(in order to bulk machinecomponents: inside machine/outside(different temps possible))?
		//or even a machine value?
		//((FluidContainer)pip.getInput("FluidIn")).getFluid().setTemperatureExternal(293);		
		pip.getInput("TemperatureAmb").setValue(293);
		pip.update();
		pip.update();
		
		assertEquals("PressureLoss", 30580, pip.getOutput("PressureLoss").getValue(), 10);
		assertEquals("TemperatureOut", 303.3, ((FluidContainer)pip.getOutput("FluidOut")).getTemperature(), .5);
		//assertEquals("TemperatureOut", 303.3, pip.getOutput("TemperatureOut").getValue(), .5);
		//assertEquals("Ploss", 3.2, pip.getOutput("PLoss").getValue(), .1);
		assertEquals("massFlow", 0.1, ((FluidContainer)pip.getInput("FluidIn")).getFlowRate(), 0);
		//assertEquals("massFlow", 0.1, pip.getOutput("MassFlowIn").getValue(), 0);
	}
	
	@Test
	public void testPipeConnection() throws Exception{
		Pipe pip1 = new Pipe("Example", 303, "Example");
		Pipe pip2 = new Pipe("Example", 202, "Example");
		

		//List<? extends Floodable> fluidComponents;
		
		//fluidComponents.add(0, pip1);;
		
		
		//pip1.getFluid().setPressure(20000);
		
		pip1.setSimulationTimestep(1);
		pip2.setSimulationTimestep(1);
		
		
		//how handel different computation direction?
		//maybe change from ThermalArray to FluidArray=????
		//((FluidContainer)pip1.getInput("FluidIn")).setPressure(20000);
		//((FluidContainer)pip1.getInput("FluidIn")).setFlowRate(10);
		
		//((FluidContainer)pip1.getInput("FluidIn")).getFluid().setInitialTemperature(303);
		//((FluidContainer)pip1.getInput("FluidIn")).getFluid().setTemperatureExternal(293);		

		//what values can be overridden to the next component? when connecting?
		//
		//((FluidContainer)pip2.getInput("FluidIn")).setPressure(20000);
		//((FluidContainer)pip2.getInput("FluidIn")).setFlowRate(10);
		//((FluidContainer)pip2.getInput("FluidIn")).getFluid().setInitialTemperature(100);
		//((FluidContainer)pip2.getInput("FluidIn")).getFluid().setTemperatureExternal(293);		

		
		//pip2 should get all settings from fluid1 when initiating connection?
		
		FluidConnection fc = new FluidConnection(pip1, pip2);
		fc.init(293, 2000000, 0.1);
		
		//set temperature of fluid flowing into pipe1
		((FluidContainer)pip1.getInput("FluidIn")).setTemperature(293);
		//set pressure needed out of pipe2
		((FluidContainer)pip1.getInput("FluidIn")).setPressure(2000000);
		//set flowrate needed out of pipe2
		((FluidContainer)pip2.getOutput("FluidOut")).setFlowRate(0.1);
		
		pip1.getInput("TemperatureAmb").setValue(293);
	
		for(int i=0; i<10; i++){
			//what first??
			fc.update();
			System.out.print("pipe1 ");
			pip1.update();
			System.out.print("pipe2 ");
			pip2.update();

			//fc2.update();
			//System.out.println("pip1 tempOut: " + pip1.getFluid().getTemperatureOut() + " pip2 tempIn: " + pip2.getFluid().getTemperature().getValue());			
		}
		/*
		//Pipe<ThermalArray> pipeFluid = new Pipe<ThermalArray>("Example", 303);
		Pipe pipeFluid = new Pipe("Example", 303, new ThermalArray("Water", 0.2, 10));
		pipeFluid.setSimulationTimestep(1);
		
		
		FluidContainer fIn = (FluidContainer) pipeFluid.getInput("FluidIn");
		
		//fIn.setFluid(fluid);
		//fIn.setFluid(pipeFluid.getFluid());
		//pipeFluid.getInput("FluidIn").setFluid(pipeFluid.getFluid());

		pipeFluid.getInput("FluidIn");
		
		fIn.getFluid().setFlowRate(0.3);		
		//pipeFluid.getInput("FluidIn").getValue().setFlowRate(0.3);
		fIn.getFluid().setInitialTemperature(303);
		//pipeFluid.getInput("FluidIn").getValue().setInitialTemperature(303.00);
		fIn.getFluid().setTemperatureExternal(293);
		//pipeFluid.getInput("FluidIn").getValue().setTemperatureExternal(293.00);
		fIn.getFluid().setHeatSource(9999999);
		//pipeFluid.getInput("FluidIn").getValue().setHeatSource(999999999);
		
		FluidContainer fOut = (FluidContainer) pipeFluid.getOutput("FluidOut");
		
		fOut.setFluid(fIn.getFluid());
		
		pipeFluid.getOutput("FluidOut").setValue(pipeFluid.getInput("FluidIn").getValue());
		
		for(int i = 0; i<10; i++){
			pipeFluid.update();
			System.out.println("pipe2 Massflowout: " + fOut.getFluid().getFlowRate() + " temp " + fOut.getFluid().getTemperatureOut());
		
		}*/
	}
}
