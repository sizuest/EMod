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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.simulation.ComponentState;

/**
 * @author dhampl
 * 
 */
public class SimulationConfigReader extends ConfigReader {

	String fileName;

	/**
	 * 
	 * @param type
	 *            the simulator's class type (randomsimulationcontrol,
	 *            static..., kienzle..., etc)
	 * @param component
	 *            the component name (80mmfan, x axis, etc)
	 * @throws Exception
	 */
	public SimulationConfigReader(String type, String component)
			throws Exception {

		String path = EModSession.getRootPath()
				+ File.separator
				+ Defines.MACHINECONFIGDIR
				+ File.separator
				+ EModSession.getMachineConfig()
				+ File.separator;;
		fileName = type + "_" + component + ".xml";
		filePath = path + fileName;

		ConfigReaderOpen();
	}

	/**
	 * Returns the file name
	 * 
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Returns the component state for a given machine state
	 * 
	 * @param machineState
	 * @return
	 */
	public ComponentState getComponentState(String machineState) {
		if(props.containsKey(machineState + "_state"))
			return ComponentState.valueOf(props.getProperty(machineState + "_state"));
		else
			return ComponentState.OFF;
	}

	/**
	 * Check if simulation control file exists.
	 * 
	 * @param type
	 *            Class name of simulation control
	 * @param component
	 *            name of simulation control
	 * @return true if config file exists, false otherwise.
	 */
	public static boolean SimulationConfigReaderExist(String type,
			String component) {
		String path = EModSession.getRootPath()
				+ File.separator
				+ Defines.MACHINECONFIGDIR
				+ File.separator
				+ EModSession.getMachineConfig()
				+ File.separator;
		String fname = path + "/" + type + "_" + component + ".xml";

		try {
			InputStream iostream = new FileInputStream(fname);
			iostream.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
