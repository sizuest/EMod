package ch.ethz.inspire.emod.simulation;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.utils.ProgressbarGUI;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class EModSimulationRun {
	
// manick: Method to run the simulation, copied from EModMain.java
public static void EModSimRun(){
	// Get name of machine 
	String machineName = PropertiesHandler.getProperty("sim.MachineName");
	
	if (machineName == null) {
		Exception e = new Exception("No machine name defined in the application configuration (app.config)!");
		e.printStackTrace();
		System.exit(-1);
	}
	
	// Get name of the machine configuration
	String machineConfigName = PropertiesHandler.getProperty("sim.MachineConfigName");
	if (machineConfigName == null) {
		Exception e = new Exception("No machine config name defined in the application configuration (app.config)!");
		e.printStackTrace();
		System.exit(-1);
	}
	
	// Get name of the simulation configuration
	String simulationConfigName = PropertiesHandler.getProperty("sim.SimulationConfigName");
	if (simulationConfigName == null) {
		Exception e = new Exception("No simulation config name defined in the application configuration (app.config)!");
		e.printStackTrace();
		System.exit(-1);
	}
	
	/* Build machine: Read and check machine configuration */
	Machine.buildMachine(machineName, machineConfigName);
	
	/* Setup the simulation: Read simulation config  */
	EModSimulationMain sim = new EModSimulationMain(machineName, simulationConfigName);
	
	/* Connect simulation with machine config */
	sim.setMachineComponentList(Machine.getInstance().getMachineComponentList());
	sim.setIOConnectionList(Machine.getInstance().getIOLinkList());
	sim.setInputparamObjectList(Machine.getInstance().getInputObjectList());
	
	/* Setup the process */
	Process process = new Process(PropertiesHandler.getProperty("sim.ProcessName"));
	
	/* Set process parameters for simulation */
	sim.setProcessParamsforSimulation(process);
	
	/* Set simulation period for all simulation objects */
	sim.updateSimulationPeriod();
	
	/* Run the simulation */
	sim.runSimulation();
	
	/* Write the machine configuration */
	//Machine.saveMachineToFile("machineexportallinone.xml");
	}
}