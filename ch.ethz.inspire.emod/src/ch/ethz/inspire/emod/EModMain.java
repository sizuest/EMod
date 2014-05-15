/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod;


import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.eclipse.swt.widgets.*;

import ch.ethz.inspire.emod.gui.EModGUI;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * energy model main class
 * 
 * @author dhampl
 *
 */
public class EModMain {
	private static Logger rootlogger = Logger.getLogger("");
	private static Logger logger = Logger.getLogger(EModMain.class.getName());
	
	public static void main(String[] args) {
		Display disp = new Display();
		
		// init logging
		LogManager logManager = LogManager.getLogManager();
		logManager.reset();
		try {
			// Let the loggers write to file:
			FileHandler fh = new FileHandler(PropertiesHandler.getProperty("app.logfile"), 100000, 1, true);
			fh.setFormatter(new SimpleFormatter());
			rootlogger.addHandler(fh);
			// Set log level:
			// The following loglevel are available (from lowest to highest):
			// OFF, FINEST, FINER, FINE=DEBUG, CONFIG, INFO, WARNING, SEVERE, ALL
			rootlogger.setLevel(LogLevel.FINER);
			
			// Add stdout to logger: All logging output is written to stdout too.
			SimpleFormatter fmt = new SimpleFormatter();
			StreamHandler sh = new StreamHandler(System.out, fmt);
			rootlogger.addHandler(sh);

			// Redirect stderr to logger (and then to stdout as stdout is a handler of logger)
			//OutputStream los;
			//los = new LoggingOutputStream(Logger.getLogger("stderr"), LogLevel.SEVERE);
			//System.setErr(new PrintStream(los,true));
			
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//start program
		new EModMain();
		new EModGUI(disp);
		
/*		Shell shell = new Shell(disp);
		shell.setLayout(new FillLayout());
		//new MachineComponentSelectGUI(shell);
		
		
		shell.open();
		
		while(!shell.isDisposed()) {
			if(!disp.readAndDispatch()) {
				disp.sleep();
			}
		}
*/		
		//test loading configs
		//Machine.initMachineFromFile("L:/misc_praktikum_david/xml/testmachine_new.xml");
		//EModSimulationMain sim = EModSimulationMain.initSimulationFromFile("L:/misc_praktikum_david/xml/sim.xml");
		//sim.runSimulation();
		
		//shut down
		disp.dispose();
	}

	/**
	 * Energy Model main method.
	 */
	public EModMain() {
		
		logger.info("Start EModMain");
		
		
		/* manick: run simulation just when tab "Analysis" is opened.
		 * following code part therefore moved to: ch.ethz.inspire.emod.simulation.EModSimulationRun.java
		 * EModSimulationRun.EModSimRun() is called from EModGUI -> tabFolder.addSelectionListener

		
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
		
		// Build machine: Read and check machine configuration
		Machine.buildMachine(machineName, machineConfigName);
		
		// Setup the simulation: Read simulation config 
		EModSimulationMain sim = new EModSimulationMain(machineName, simulationConfigName);
		
		// Connect simulation with machine config
		sim.setMachineComponentList(Machine.getInstance().getMachineComponentList());
		sim.setIOConnectionList(Machine.getInstance().getIOLinkList());
		sim.setInputparamObjectList(Machine.getInstance().getInputObjectList());
		
		// Setup the process
		Process process = new Process(PropertiesHandler.getProperty("sim.ProcessName"));
		
		// Set process parameters for simulation
		sim.setProcessParamsforSimulation(process);
		
		// Set simulation period for all simulation objects
		sim.updateSimulationPeriod();
		
		// Run the simulation
		sim.runSimulation();
		
		// Write the machine configuration
		//Machine.saveMachineToFile("machineexportallinone.xml");
		
		*/
		
	}
}
