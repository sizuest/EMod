package ch.ethz.inspire.emod;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import ch.ethz.inspire.emod.model.*;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

import org.junit.Test;

/**
 * @author sizuest
 *
 */
public class MachineTest {
	
	/**
	 * 
	 */
	@Test
	public void testAddNewMachineComponent() {
		
		Machine.clearMachine();
		
		@SuppressWarnings("unused")
		MachineComponent mc1 = Machine.addNewMachineComponent("Motor", "Siemens_1FE1115-6WT11");
		MachineComponent mc2 = Machine.addNewMachineComponent("Motor", "Siemens_1FE1115-6WT11");

		try {
			assertEquals("get component by name", mc2, Machine.getMachineComponent("Motor_1"));
			assertSame("get component by name", mc2, Machine.getMachineComponent("Motor_1"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testAddNewSimulator() {
		
		Machine.clearMachine();
		
		@SuppressWarnings("unused")
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", new SiUnit(Unit.WATT));
		ASimulationControl sc2 = Machine.addNewInputObject("ProcessSimulationControl", new SiUnit(Unit.WATT));

		try {
			assertEquals("get component by name", sc2, Machine.getInputObject("ProcessSimulationControl_1"));
			assertSame("get component by name", sc2, Machine.getInputObject("ProcessSimulationControl_1"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testRemoveMachineComponent() {
		
		Machine.clearMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc3 = Machine.addNewMachineComponent("Amplifier", "Example");
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", new SiUnit(Unit.WATT));
		
		// Add some connections
		Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc2.getComponent().getInput("PDmd"));
		Machine.addIOLink(mc2.getComponent().getOutput("PTotal"), mc1.getComponent().getInput("PDmd"));
		Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc3.getComponent().getInput("PDmd"));
		Machine.addIOLink(sc1.getOutput(), mc3.getComponent().getInput("PDmd"));
		
		Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc2.getComponent().getInput("PDmd"));
		
		System.out.println(Machine.getInstance().getIOLinkList().toString());
		
		Machine.removeMachineComponent("Amplifier");

		try {
			assertEquals("get component by name", null, Machine.getMachineComponent("Amplifier"));
			assertSame("get component by name", null, Machine.getMachineComponent("Amplifier"));
			assertEquals("number of remaining connections", 2, Machine.getInstance().getIOLinkList().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void testRemoveSimulator() {
		
		Machine.clearMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Amplifier", "Example");
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", new SiUnit(Unit.WATT));
		
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
	
	/**
	 * 
	 */
	@Test
	public void testRename() {
		Machine.clearMachine();
		
		MachineComponent mc1 = Machine.addNewMachineComponent("Motor", "Siemens_1FE1115-6WT11");
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", new SiUnit(Unit.WATT));
		
		Machine.renameMachineComponent("Motor", "Spindel");
		Machine.renameInputObject("ProcessSimulationControl", "Leistung");
		
		try {
			assertEquals("get component by name", mc1, Machine.getMachineComponent("Spindel"));
			assertSame("get component by name", sc1, Machine.getInputObject("Leistung"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void testGetOutputs() {
		Machine.clearMachine();
		
		MachineComponent mc;
		
		mc = Machine.addNewMachineComponent("Motor", "Siemens_1FE1115-6WT11");
		Machine.addNewInputObject("ProcessSimulationControl", new SiUnit(Unit.WATT));
		
		try {
			assertEquals("number of outputs", 5, Machine.getOutputList().size());
			assertEquals("number of outputs in WATT", 4, Machine.getOutputList(new SiUnit(Unit.WATT)).size());
			assertEquals("number of outputs in WATT without the motor", 1, Machine.getOutputList(mc, new SiUnit(Unit.WATT)).size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void testNewMachine() {
		
		Machine.clearMachine();
		
		// Add some components
		MachineComponent mc1 = Machine.addNewMachineComponent("Amplifier", "Example");
		MachineComponent mc2 = Machine.addNewMachineComponent("Amplifier", "Example");
		ASimulationControl sc1 = Machine.addNewInputObject("ProcessSimulationControl", new SiUnit(Unit.WATT));
		
		// Add some connections
		Machine.addIOLink(mc1.getComponent().getOutput("PTotal"), mc2.getComponent().getInput("PDmd"));
		Machine.addIOLink(sc1.getOutput(), mc1.getComponent().getInput("PDmd"));
		
		
		String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix");
		
		assertEquals("File does not yet exists", false, (new File(prefix+"/Test/TestConfig1").exists()));
		
		try {
			Machine.newMachine("Test", "TestConfig1");
			assertEquals("File Exists", true, (new File(prefix+"/Test/MachineConfig/TestConfig1/Machine.xml").exists()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Machine.deleteMachine("Test", "TestConfig1");
			//assertEquals("File does not exists", false, (new File(prefix+"/Test/MachineConfig/TestConfig1").exists()));
			assertEquals("File does not exists", false, (new File(prefix+"/Test/TestConfig1").exists()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testSetType(){
		Machine.clearMachine();
		
		// Add some components
		MachineComponent mc = Machine.addNewMachineComponent("Amplifier", "Siemens_6SN1123_1AA00_0CA1");
		
		try {
			Machine.getMachineComponent(mc.getName()).getComponent().setType("Example");
			assertEquals("New name is Example: ", "Example", Machine.getMachineComponent(mc.getName()).getComponent().getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testFloodableComponent(){
		Machine.clearMachine();
		
		// Add several components to the machine
		Machine.addNewMachineComponent("Tank", "Example");
		Machine.addNewMachineComponent("Amplifier", "Example");
		Machine.addNewMachineComponent("Pipe", "Example");
		
		// Check if components are floodable
		ArrayList<MachineComponent> mcl = Machine.getInstance().getMachineComponentList();
		for(MachineComponent mc : mcl){
			if(mc.getComponent() instanceof ch.ethz.inspire.emod.utils.Floodable)
				System.out.println(mc.getComponent().getClass().toString() + " is floodable");
			else
				System.out.println(mc.getComponent().getClass().toString() + " is not floodable");
		}

		System.out.println("all floodable components from the getFloodableMachineComponentList:");
		ArrayList<MachineComponent> fmcl = Machine.getInstance().getFloodableMachineComponentList();
		for(MachineComponent mc : fmcl){
			System.out.println(mc.getComponent().toString());
		}
	}

}
