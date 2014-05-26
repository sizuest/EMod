package ch.ethz.inspire.emod;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Constructor;

import ch.ethz.inspire.emod.model.*;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

import org.junit.Test;

public class MachineTest {
	
	@Test
	public void testAddNewMachineComponent() {
		
		Machine.clearMachine();
		
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
		
		Machine.clearMachine();
		
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", Unit.WATT);
		ASimulationControl sc2 = Machine.addNewInputObject("ProcessSimulationControl", Unit.WATT);

		try {
			assertEquals("get component by name", sc2, Machine.getInputObject("ProcessSimulationControl_1"));
			assertSame("get component by name", sc2, Machine.getInputObject("ProcessSimulationControl_1"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testRemoveMachineComponent() {
		
		Machine.clearMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc3 = Machine.addNewMachineComponent("Amplifier", "Example");
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", Unit.WATT);
		
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
		
		Machine.clearMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Amplifier", "Example");
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", Unit.WATT);
		
		// Add some connections
		Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc2.getComponent().getInput("PDmd"));
		Machine.addIOLink(sc1.getOutput(), mc1.getComponent().getInput("PDmd"));
		
		Machine.removeInputObject("ProcessSimulationControl");

		try {
			assertEquals("get component by name", null, Machine.getInputObject("ProcessSimulationControl"));
			assertSame("get component by name", null, Machine.getInputObject("ProcessSimulationControl"));
			assertEquals("number of remaining connections", 1, Machine.getInstance().getIOLinkList().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRename() {
		Machine.clearMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Motor", "Siemens_1FE1115-6WT11");
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", Unit.WATT);
		
		Machine.renameMachineComponent("Motor", "Spindel");
		Machine.renameInputObject("ProcessSimulationControl", "Leistung");
		
		try {
			assertEquals("get component by name", mc1, Machine.getMachineComponent("Spindel"));
			assertSame("get component by name", sc1, Machine.getInputObject("Leistung"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetOutputs() {
		Machine.clearMachine();
		
		Machine.addNewMachineComponent("Motor", "Siemens_1FE1115-6WT11");
		Machine.addNewInputObject("ProcessSimulationControl", Unit.WATT);
		
		try {
			assertEquals("number of outputs", 5, Machine.getOutputList().size());
			assertEquals("number of outputs in WATT", 4, Machine.getOutputList(Unit.WATT).size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testNewMachine() {
		
		Machine.clearMachine();
		
		// Add some components
		MachineComponent mc1 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Amplifier", "Example");
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", Unit.WATT);
		
		// Add some connections
		Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc2.getComponent().getInput("PDmd"));
		Machine.addIOLink(sc1.getOutput(), mc1.getComponent().getInput("PDmd"));
		
		
		String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix");
		
		assertEquals("File does not yet exists", false, (new File(prefix+"/Test/TestConfig1").exists()));
		
		try {
			Machine.newMachine("Test", "TestConfig1");
			assertEquals("File Exists", true, (new File(prefix+"/Test/MachineConfig/TestConfig1/Machine.xml").exists()));
			assertEquals("File Exists", true, (new File(prefix+"/Test/MachineConfig/TestConfig1/IOLinking.txt").exists()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Machine.deleteMachine("Test", "TestConfig1");
			assertEquals("File does not exists", false, (new File(prefix+"/Test/TestConfig1").exists()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}