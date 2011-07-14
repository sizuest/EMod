package ch.ethz.inspire.emod.simulation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.MachineState.MachineStateEnum;

public class StaticSimulationControlTest {

	StaticSimulationControl tester;
	
	@Before
	public void init() {
		tester = new StaticSimulationControl("test", Unit.NONE, "test/ch/ethz/inspire/emod/simulation/StaticSimulationControl_tester.txt");
	}
	
	@Test
	public void testUpdate() {
		tester.setState(MachineStateEnum.ON);
		tester.update();
		assertEquals("update: ON", 20, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineStateEnum.OFF);
		tester.update();
		assertEquals("update: OFF", 0, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineStateEnum.STANDBY);
		tester.update();
		assertEquals("update: STANDBY", 30, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineStateEnum.READY);
		tester.update();
		assertEquals("update: READY", 20, tester.simulationOutput.getValue(), 0.0001);
		tester.setState(MachineStateEnum.CYCLE);
		tester.update();
		assertEquals("update: CYCLE", 20, tester.simulationOutput.getValue(), 0.0001);
	}

	@Test
	public void testSetState() {
		tester.setState(MachineStateEnum.ON);
		assertEquals("set state: ON", MachineStateEnum.ON, tester.getState());
		tester.setState(MachineStateEnum.OFF);
		assertEquals("set state: OFF", MachineStateEnum.OFF, tester.getState());
		tester.setState(MachineStateEnum.STANDBY);
		assertEquals("set state: STANDBY", MachineStateEnum.STANDBY, tester.getState());
		tester.setState(MachineStateEnum.READY);
		assertEquals("set state: READY", MachineStateEnum.ON, tester.getState());
		tester.setState(MachineStateEnum.CYCLE);
		assertEquals("set state: CYCLE", MachineStateEnum.ON, tester.getState());
	}

}
