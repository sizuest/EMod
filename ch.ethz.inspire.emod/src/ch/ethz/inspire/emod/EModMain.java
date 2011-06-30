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
import java.util.*;

import org.eclipse.swt.widgets.*;

import ch.ethz.inspire.emod.gui.EModGUI;
import ch.ethz.inspire.emod.model.LinearMotor;
import ch.ethz.inspire.emod.model.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.EModSimulationMain;

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
			LoggingOutputStream los;
			los = new LoggingOutputStream(Logger.getLogger("stderr"), LogLevel.STDERR);
			System.setErr(new PrintStream(los,true));
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

	public EModMain() {
		
		ArrayList<MachineComponent> l1 = new ArrayList<MachineComponent>();
		MachineComponent mc1 = new MachineComponent("spindel");
		mc1.setComponent(new LinearMotor("siemens123"));
		MachineComponent mc2 = new MachineComponent("x");
		mc2.setComponent(new LinearMotor("siemens1234"));
		MachineComponent mc3 = new MachineComponent("y");
		mc3.setComponent(new LinearMotor("siemens12345"));
		l1.add(mc3);
		l1.add(mc2);
		l1.add(mc1);
		Machine.getInstance().setArrayList(l1);
		for(MachineComponent mc : Machine.getInstance().getComponentList()) {
			mc.getComponent().update();
			System.out.println(mc.getName()+" "+mc.getComponent().getOutput(0));
		}
		EModSimulationMain sim = new EModSimulationMain();
		//sim.generateSimulation(20);
		sim.readSimulationFromFile("sim.txt");
		sim.runSimulation();
		Machine.saveMachineToFile("testmach.xml");
	}
}
