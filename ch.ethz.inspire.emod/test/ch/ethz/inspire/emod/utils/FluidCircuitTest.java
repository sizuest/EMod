package ch.ethz.inspire.emod.utils;

import org.junit.Test;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;

public class FluidCircuitTest {
	@Test
	public void testFluidConnection() throws Exception{
		Machine.clearMachine();
		
		System.out.println("*** testFluidCircuit ***");
		
		//add components to the Machine
		MachineComponent pip1 = Machine.addNewMachineComponent("Pipe", "Example");
		MachineComponent pip2 = Machine.addNewMachineComponent("Pipe", "Example");
		MachineComponent tank = Machine.addNewMachineComponent("Tank", "Example");
		
		Machine.addIOLink(tank.getComponent().getOutput("FluidOut"), pip1.getComponent().getInput("FluidIn"));
		Machine.addIOLink(pip1.getComponent().getOutput("FluidOut"), pip2.getComponent().getInput("FluidIn"));
		Machine.addIOLink(pip2.getComponent().getOutput("FluidOut"), tank.getComponent().getInput("FluidIn"));
		
		FluidCircuit fc = new FluidCircuit();
		fc.checkCircuit();
	}
}