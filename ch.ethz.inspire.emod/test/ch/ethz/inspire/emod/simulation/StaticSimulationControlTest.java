package ch.ethz.inspire.emod.simulation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.ethz.inspire.emod.model.units.Unit;

public class StaticSimulationControlTest {

	StaticSimulationControl tester;
	
	@Before
	public void init() {
		tester = new StaticSimulationControl("test", Unit.NONE, "test/ch/ethz/inspire/emod/simulation/StaticSimulationControl_tester.txt");
	}
	
	@Test
	public void testUpdate() {
		tester.setState(MachineState.ON);
		tester.update();
		assertEquals("update: ON", 20, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.OFF);
		tester.update();
		assertEquals("update: OFF", 0, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.STANDBY);
		tester.update();
		assertEquals("update: STANDBY", 30, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.READY);
		tester.update();
		assertEquals("update: READY", 20, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineState.PROCESS);
		tester.update();
		assertEquals("update: CYCLE", 20, tester.simulationOutput.getValue(), 0.0001);
	}

	@Test
	public void testSetState() {
		tester.setState(MachineState.ON);
		assertEquals("set state: ON", MachineState.ON, tester.getState());
		tester.setState(MachineState.OFF);
		assertEquals("set state: OFF", MachineState.OFF, tester.getState());
		tester.setState(MachineState.STANDBY);
		assertEquals("set state: STANDBY", MachineState.STANDBY, tester.getState());
		tester.setState(MachineState.READY);
		assertEquals("set state: READY", MachineState.ON, tester.getState());
		tester.setState(MachineState.PROCESS);
		assertEquals("set state: PROCESS", MachineState.ON, tester.getState());
	}

}
