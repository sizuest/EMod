/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.utils;

import java.io.FileInputStream;
import java.io.InputStream;

import ch.ethz.inspire.emod.simulation.ComponentState;

/**
 * @author dhampl
 *
 */
public class SimulationConfigReader extends ConfigReader {

	/**
	 * 
	 * @param type the simulator's class type (randomsimulationcontrol, static..., kienzle..., etc)
	 * @param component the component name (80mmfan, x axis, etc)
	 * @throws Exception 
	 */
	public SimulationConfigReader(String type, String component) throws Exception {
		
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix")+
				"/"+PropertiesHandler.getProperty("sim.MachineName")+"/"+Defines.MACHINECONFIGDIR+"/"+
				PropertiesHandler.getProperty("sim.MachineConfigName");
		fileName = path+"/"+type+"_"+component+".xml";
		
		ConfigReaderOpen();
	}
	
	public ComponentState getComponentState(String machineState) {
		return ComponentState.valueOf(props.getProperty(machineState+"_state"));
	}
	
	/**
	 * Check if simulation control file exists.
	 * 
	 * @param type Class name of simulation control
	 * @param component name of simulation control
	 * @return true if config file exists, false otherwise.
	 */
	public static boolean SimulationConfigReaderExist(String type, String component)
	{
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix")+
		"/"+PropertiesHandler.getProperty("app.MachineName")+"/"+Defines.MACHINECONFIGDIR+"/"+
		PropertiesHandler.getProperty("app.MachineConfigName");
		String fname = path+"/"+type+"_"+component+".xml";
		 
		try {
			InputStream iostream = new FileInputStream(fname);
			iostream.close();
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
}
