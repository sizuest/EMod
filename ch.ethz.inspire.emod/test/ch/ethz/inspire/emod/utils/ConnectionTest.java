package ch.ethz.inspire.emod.utils;

import java.util.List;

import org.junit.Test;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.Pipe;
import ch.ethz.inspire.emod.model.Tank;

public class ConnectionTest {

	@Test
	public void testAddConnection() throws Exception{
		Machine.clearMachine();
		
		System.out.println("*** testAddConnection ***");
		
		MachineComponent mc1 = Machine.addNewMachineComponent("PumpFluid", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Pipe", "Example");
		
		Machine.addFluidLink((FluidContainer)mc1.getComponent().getOutput("FluidOut"), (FluidContainer)mc2.getComponent().getInput("FluidIn"));
		
		MachineComponent mc3 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc4 = Machine.addNewMachineComponent("Amplifier", "Example");
		
		// the following connection should not work, since the in and output are not Fluid
		Machine.addIOLink(mc3.getComponent().getOutput("PTotal"), mc4.getComponent().getInput("PDmd"));
		
		List<FluidConnection> listFC = Machine.getInstance().getFluidConnectionList();
		for(FluidConnection fc:listFC){
			System.out.println("fluidconnection: " + fc.getSource().getName().toString());
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
	public void testPumpFluid() throws Exception{
		Machine.clearMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("PumpFluid", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Pipe", "Example");
		
		Machine.addIOLink(mc1.getComponent().getOutput("FluidOut"), mc2.getComponent().getInput("FluidIn"));
		Machine.addIOLink(mc2.getComponent().getOutput("FluidOut"), mc1.getComponent().getInput("FluidIn"));
		
		
	}
}
