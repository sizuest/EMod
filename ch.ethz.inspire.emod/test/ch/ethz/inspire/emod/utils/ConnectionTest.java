package ch.ethz.inspire.emod.utils;

import java.util.List;

import org.junit.Test;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;

/**
 * @author simon
 *
 */
public class ConnectionTest {

	/**
	 * Test function for {@link Machine#addIOLink(IOContainer, IOContainer)}
	 * @throws Exception
	 */
	@Test
	public void testAddConnection() throws Exception{
		Machine.clearMachine();
		
		System.out.println("*** testAddConnection ***");
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Pump", "Example");
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
}
