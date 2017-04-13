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

import java.io.File;

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
		try{
			Machine.saveMachine(EModSession.getMachineName(), EModSession.getMachineConfig());
			States.saveStates(EModSession.getMachineName(), EModSession.getSimulationConfig());
			EModSession.save();
			ZipUtils.zipFolder(getMachinePath(), path);
		} catch (Exception e){
			System.err.println("SAVING FAILED: Saving the set-up at'"+path+"' was not successful: ");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Loads the config from the stated path
	 * @param path
	 */
	public static void open(String path){
		try{
			File file = new File(getMachineTempPath()+File.separator+"temp");
			clearTempPath();
			ZipUtils.unzipFolder(path, file.getPath());
			EModSession.initSessionFromFile(getMachineTempPath()+File.separator+"temp"+File.separator+Defines.SESSIONFILE);
			file.renameTo(new File(getMachineTempPath()+File.separator+EModSession.getMachineName()));
			PropertiesHandler.setProperty("app.MachineDataPathPrefix", Defines.TEMPFILESPACE);
			
			Machine.initMachineFromFile(EModSession.getMachineConfigPath());
			
			EModSession.setPath(path);
			
		} catch (Exception e){
			System.err.println("LOADING FAILED: Loading the set-up at'"+path+"' was not successful: ");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Returns the path to the current loaded machine
	 * @return
	 */
	public static String getMachinePath(){		
		return EModSession.getRootPath();
	}
	
	/**
	 * Returns the path to the directory for tempoary loaded machines
	 * @return
	 */
	public static String getMachineTempPath(){
		String path = Defines.TEMPFILESPACE;
		
		return path;
	}
	
	/**
	 * Clears the temp path for machine configs
	 * @return
	 */
	public static boolean clearTempPath(){
		try{
			clearFolder(new File(getMachineTempPath()));
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private static void clearFolder(File folder) {
		File[] files = folder.listFiles();
		if(files!=null) { 
	        for(File f: files) {
	            if(f.isDirectory())
	            	clearFolder(f);
	            
	           f.delete();
	        }
	    }
	}

}
