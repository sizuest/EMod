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
import ch.ethz.inspire.emod.simulation.EModSimulationMain;


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
			rootlogger.setLevel(LogLevel.CONFIG);
			
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
		
		//shut down
		disp.dispose();
	}

	/**
	 * Energy Model main method.
	 */
	public EModMain() {
		
		logger.info("Start EModMain");
		
		// Get name of machine 
		String machineName = PropertiesHandler.getProperty("app.MachineName");
		if (machineName == null) {
			Exception e = new Exception("No machine name defined in the application configuration (app.config)!");
			e.printStackTrace();
			System.exit(-1);
		}
		// Get name of the machine configuration
		String machineConfigName = PropertiesHandler.getProperty("app.MachineConfigName");
		if (machineConfigName == null) {
			Exception e = new Exception("No machine config name defined in the application configuration (app.config)!");
			e.printStackTrace();
			System.exit(-1);
		}
		// Get name of the simulation configuration
		String simulationConfigName = PropertiesHandler.getProperty("app.SimulationConfigName");
		if (simulationConfigName == null) {
			Exception e = new Exception("No simulation config name defined in the application configuration (app.config)!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read and check machine configuration */
		MachineConfig machineConfig = new MachineConfig(machineName, machineConfigName);
		
		/* Setup the simulation */
		EModSimulationMain sim = new EModSimulationMain(machineName, simulationConfigName);
		sim.setIOConnectionList(machineConfig.getIOLinkList());
		sim.setInputparamObjectList(machineConfig.getInputObjectList());

		/* Run the simulation */
		sim.runSimulation();
		
		//Machine.getInstance().saveMachineToFile("test_machinecomponents.xml");
		//machineConfig.saveInputSimulatorsToFile("test_inputsimulators.xml");
	}
}
