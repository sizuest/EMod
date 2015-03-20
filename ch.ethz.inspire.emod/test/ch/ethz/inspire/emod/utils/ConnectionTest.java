package ch.ethz.inspire.emod.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.Material;
import ch.ethz.inspire.emod.model.Pipe;
import ch.ethz.inspire.emod.model.PumpFluid;
import ch.ethz.inspire.emod.model.Tank;
import ch.ethz.inspire.emod.model.thermal.ThermalArray;

public class ConnectionTest {

	@Test
	public void testAddConnection(){
		Machine.clearMachine();
		
		System.out.println("*** testAddConnection ***");
				
		MachineComponent mc1 = Machine.addNewMachineComponent("Pipe", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Pipe", "Example");
		
		MachineComponent mc3 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc4 = Machine.addNewMachineComponent("Amplifier", "Example");
		
		// Add some connections
		// two pipes connected to each other in a circle, therefore pressurein is pressureout of the other pipe, same for the massflow
		Machine.addIOLink(mc1.getComponent().getOutput("PressureIn"), mc2.getComponent().getInput("PressureOut"));
		Machine.addIOLink(mc2.getComponent().getOutput("MassFlowIn"), mc1.getComponent().getInput("MassFlowOut"));
		
		Machine.addIOLink(mc3.getComponent().getOutput("PTotal"), mc4.getComponent().getInput("PDmd"));
		//Machine.addIOLink(mc2.getComponent().getOutput("PTotal"), mc1.getComponent().getInput("PDmd"));
		//Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc3.getComponent().getInput("PDmd"));

		
		// add Fluid connection
		// the temperature is linked with a fluid
		Machine.addIOLink(mc1.getComponent().getOutput("TemperatureOut"), mc2.getComponent().getInput("TemperatureIn"));
		
		// the following connection should not work, since the in and output are not Fluid
		Machine.addIOLink(mc3.getComponent().getOutput("PTotal"), mc4.getComponent().getInput("PDmd"));
	}
	
	@Test
	public void testConnection(){
		Machine.clearMachine();

		System.out.println("*** testConnection ***");
				
		Pipe pf1 = new Pipe("Example", 300, "Example");
		Pipe pf2 = new Pipe("Example", 250, "Example");
				
		Machine.addIOLink(pf1.getOutput("TemperatureOut"), pf2.getInput("TemperatureIn"));
		Machine.addIOLink(pf2.getOutput("TemperatureOut"), pf1.getInput("TemperatureIn"));
		
		pf1.setSimulationTimestep(999999);
		pf2.setSimulationTimestep(999999);
		
		for (int i = 0; i<100; i++){
		pf1.update();
		pf2.update();
		System.out.println(pf1.getDynamicState("Temperature").getValue() + ", " + pf2.getDynamicState("Temperature").getValue());
		}
	}

	
	
	@Test
	public void testFluidConnection() throws Exception{
		Machine.clearMachine();
		
		System.out.println("*** testFluidConnection ***");
		
		Tank tank = new Tank("Schaublin42L");
		Pipe pip1 = new Pipe("Example", 300, "Example");
		Pipe pip2 = new Pipe("Example", 250, "Example");
		//PumpFluid pf3 = new PumpFluid("Example");
		
		FluidConnection fc1 = new FluidConnection(tank, pip1);
		FluidConnection fc2 = new FluidConnection(pip1, pip2);
		
		//Add connection with ThermalArray fluid
		//Machine.addIOLink(pf1.getOutput("FluidOut"), pf3.getInput("FluidIn"));
		//Machine.addIOLink(pf3.getOutput("FluidOut"), pf2.getInput("FluidIn"));
		//Machine.addIOLink(pf2.getOutput("FluidOut"), pf1.getInput("FluidIn"));
		
		tank.setSimulationTimestep(1);
		pip1.setSimulationTimestep(1);
		pip2.setSimulationTimestep(1);
		//pf3.setSimulationTimestep(999999);
		
		for (int i = 0; i<100; i++){
			tank.update();
			pip1.update();
			pip2.update();
			//System.out.println(pf1.getDynamicState("Temperature").getValue() + ", " + pf2.getDynamicState("Temperature").getValue());
		}
	}
	
	@Test
	public void testConvertConnections() throws Exception{
		Machine.clearMachine();

		System.out.println("*** testConvertConnections ***");
				
		MachineComponent mc1 = Machine.addNewMachineComponent("Pipe", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Pipe", "Example");
				
		// Add some connections
		// two pipes connected to each other in a circle, therefore pressurein is pressureout of the other pipe, same for the massflow
		Machine.addIOLink(mc1.getComponent().getOutput("PressureIn"), mc2.getComponent().getInput("PressureOut"));
		Machine.addIOLink(mc2.getComponent().getOutput("MassFlowIn"), mc1.getComponent().getInput("MassFlowOut"));

		// add Fluid connection
		// the temperature is linked with a fluid
		Machine.addIOLink(mc1.getComponent().getOutput("TemperatureOut"), mc2.getComponent().getInput("TemperatureIn"));
		Machine.addIOLink(mc2.getComponent().getOutput("TemperatureOut"), mc1.getComponent().getInput("TemperatureIn"));
		
		System.out.println(Machine.getInstance().getIOLinkList().toString());
		
		//((FluidConnection) Machine.getInstance().getIOLinkList().get(2)).removeFluid();
		//Machine.getInstance().getIOLinkList().get(3).addFluid(new Material("Air"));

		System.out.println(Machine.getInstance().getIOLinkList().toString());
	}
	
	@Test
	public void testPumpFluid() throws Exception{
		Machine.clearMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("PumpFluid", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Pipe", "Example");
		
		Machine.addIOLink(mc1.getComponent().getOutput("FluidOut"), mc2.getComponent().getInput("FluidIn"));
		Machine.addIOLink(mc2.getComponent().getOutput("FluidOut"), mc1.getComponent().getInput("FluidIn"));
		
		
	}
}
