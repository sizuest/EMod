package ch.ethz.inspire.emod;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;

import ch.ethz.inspire.emod.model.*;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;

import org.junit.Test;

public class MachineTest {
	
	@Test
	public void testAddNewMachineComponent() {
		
		Machine.deleteMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Motor", "Siemens_1FE1115-6WT11");
		MachineComponent mc2 = Machine.addNewMachineComponent("Motor", "Siemens_1FE1115-6WT11");

		try {
			assertEquals("get component by name", mc2, Machine.getMachineComponent("Motor_1"));
			assertSame("get component by name", mc2, Machine.getMachineComponent("Motor_1"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testAddNewSimulator() {
		
		Machine.deleteMachine();
		
		ASimulationControl sc1 = Machine.addNewSimulator("ProcessSimulationControl", Unit.WATT);
		ASimulationControl sc2 = Machine.addNewSimulator("ProcessSimulationControl", Unit.WATT);

		try {
			assertEquals("get component by name", sc2, Machine.getSimulator("ProcessSimulationControl_1"));
			assertSame("get component by name", sc2, Machine.getSimulator("ProcessSimulationControl_1"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testRemoveMachineComponent() {
		
		Machine.deleteMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc3 = Machine.addNewMachineComponent("Amplifier", "Example");
		ASimulationControl sc1 = Machine.addNewSimulator("ProcessSimulationControl", Unit.WATT);
		
		// Add some connections
		Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc2.getComponent().getInput("PDmd"));
		Machine.addIOLink(mc2.getComponent().getOutput("PTotal"), mc1.getComponent().getInput("PDmd"));
		Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc3.getComponent().getInput("PDmd"));
		Machine.addIOLink(sc1.getOutput(), mc3.getComponent().getInput("PDmd"));
		
		Machine.removeMachineComponent("Amplifier");

		try {
			assertEquals("get component by name", null, Machine.getMachineComponent("Amplifier"));
			assertSame("get component by name", null, Machine.getMachineComponent("Amplifier"));
			assertEquals("number of remaining connections", 2, Machine.getInstance().getIOLinkList().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRemoveSimulator() {
		
		Machine.deleteMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Amplifier", "Example");
		ASimulationControl sc1 = Machine.addNewSimulator("ProcessSimulationControl", Unit.WATT);
		
		// Add some connections
		Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc2.getComponent().getInput("PDmd"));
		Machine.addIOLink(sc1.getOutput(), mc1.getComponent().getInput("PDmd"));
		
		Machine.removeSimulator("ProcessSimulationControl");

		try {
			assertEquals("get component by name", null, Machine.getSimulator("ProcessSimulationControl"));
			assertSame("get component by name", null, Machine.getSimulator("ProcessSimulationControl"));
			assertEquals("number of remaining connections", 1, Machine.getInstance().getIOLinkList().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
