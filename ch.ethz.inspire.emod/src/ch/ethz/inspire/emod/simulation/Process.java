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

package ch.ethz.inspire.emod.simulation;

import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Read the process definition from file.
 * 
 * @author andreas
 *
 */
public class Process extends ConfigReader {
	
	/**
	 * Read the process parameters from file
	 * 
	 * @param name Name of process
	 */
	public Process(String name) {
		
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
		PropertiesHandler.getProperty("app.MachineName") + "/" + Defines.SIMULATIONCONFIGDIR + "/" +
		PropertiesHandler.getProperty("app.SimulationConfigName");
		fileName = path + "/" + Defines.PROCESSDEFFILE_PREFIX + name + ".xml";

		try {
			ConfigReaderOpen();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		
		/* If the sampleperiod of the process is not equal to the sampleperiod of the simulation
		 * we have to resample the time series. */
		// TODO
	}
	
}
