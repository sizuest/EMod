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
package ch.ethz.inspire.emod;

import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.PropertiesHandler;
import ch.ethz.inspire.emod.utils.ZipUtils;

/**
 * @author sizuest
 *
 */
public class EModFileHandling {

	
	/**
	 * Save the machine at the given location (as zip)
	 * @param path
	 */
	public static void save(String path){
		ZipUtils.zipFolder(getMachinePath(), path);
	}
	
	
	/**
	 * Returns the path to the current loaded machine
	 * @return
	 */
	public static String getMachinePath(){
		String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix");
		String path = prefix + "/" + PropertiesHandler.getProperty("sim.MachineName");
		
		return path;
	}
	
	/**
	 * Returns the path to the directory for tempoary loaded machines
	 * @return
	 */
	public static String getMachineTempPath(){
		String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix");
		String path = prefix + "/" + Defines.TEMPFILESPACE;
		
		return path;
	}
	
	

}
