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
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.*;

import org.eclipse.swt.widgets.*;

import ch.ethz.inspire.emod.gui.EModGUI;
import ch.ethz.inspire.emod.model.LinearMotor;
import ch.ethz.inspire.emod.model.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.EModSimulationMain;
import ch.ethz.inspire.emod.simulation.RandomSimulationControl;

/**
 * energy model main class
 * 
 * @author dhampl
 *
 */
public class EModMain {
	private static Logger logger = Logger.getLogger("");
	
	public static void main(String[] args) {
		Display disp = new Display();
		
		//init logging
		LogManager logManager = LogManager.getLogManager();
		logManager.reset();
		try {
			FileHandler fh = new FileHandler(PropertiesHandler.getProperty("app.logfile"), 100000, 1, true);
			fh.setFormatter(new SimpleFormatter());
			logger.addHandler(fh);
			logger.setLevel(LogLevel.DEBUG);
			//LoggingOutputStream los;
			//los = new LoggingOutputStream(Logger.getLogger("stderr"), LogLevel.STDERR);
			//System.setErr(new PrintStream(los,true));
			FileHandler fhsim = new FileHandler("simlogdata.txt",1000000,1,true);
			fhsim.setFormatter(new SimpleFormatter());
			Logger.getLogger(EModSimulationMain.class.getName()).addHandler(fhsim);
			Logger.getLogger(EModSimulationMain.class.getName()).setLevel(Level.FINE);
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
		
		ArrayList<MachineComponent> mclist = new ArrayList<MachineComponent>();
		
		/* Create machine components and add to mclist */
		MachineComponent mc1 = new MachineComponent("spindel");
		mc1.setComponent(new LinearMotor("siemens123"));
		mclist.add(mc1);
		
		MachineComponent mc2 = new MachineComponent("x");
		mc2.setComponent(new LinearMotor("siemens1234"));
		mclist.add(mc2);
		
		MachineComponent mc3 = new MachineComponent("y");
		mc3.setComponent(new LinearMotor("siemens12345"));
		mclist.add(mc3);
		
		Machine.getInstance().setArrayList(mclist);
		
		EModSimulationMain sim = new EModSimulationMain();
		//sim.generateSimulation(20);
		sim.addSimulator(new RandomSimulationControl("spindelRPM", Unit.RPM));
		sim.addSimulator(new RandomSimulationControl("spindelTorque", Unit.NEWTONMETER));
		sim.addSimulator(new RandomSimulationControl("xRPM", Unit.RPM));
		sim.addSimulator(new RandomSimulationControl("xTorque", Unit.NEWTONMETER));
		sim.addSimulator(new RandomSimulationControl("yRPM", Unit.RPM));
		sim.addSimulator(new RandomSimulationControl("yTorque", Unit.NEWTONMETER));
		sim.readSimulationFromFile("initSim.txt");
		sim.runSimulation();
		Machine.saveMachineToFile("testmach.xml");
	}
}
