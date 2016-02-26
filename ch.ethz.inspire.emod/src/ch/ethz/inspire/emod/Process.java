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

import java.util.ArrayList;

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
	
	private static Process process = null;
	
	private static double samplePeriod;
	
	
	private Process(){};
	
	/**
	 * @return Current Process object
	 */
	public static Process getInstance(){
		if(null==process)
			process = new Process();
		
		return process;
	}
	
	/**
	 * @param name
	 */
	public static void loadProcess(String name){
		Process.process = new Process(name);
	}
	
	/**
	 * 
	 */
	public static void loadProcess(){
		Process.process = new Process(PropertiesHandler.getProperty("sim.ProcessName"));
	}
	
	/**
	 * Read the process parameters from file
	 * 
	 * @param name Name of process
	 */
	private Process(String name) {
		
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
		PropertiesHandler.getProperty("sim.MachineName") + "/" + Defines.SIMULATIONCONFIGDIR + "/" +
		PropertiesHandler.getProperty("sim.SimulationConfigName");
		fileName = path + "/" + Defines.PROCESSDEFFILE_PREFIX + name + ".xml";

		try {
			ConfigReaderOpen();
			samplePeriod = getDoubleValue("SamplePeriod");
		}
		catch (Exception ex) {
			ex.printStackTrace();
			//System.exit(-1);
		}
		
		
		/* If the sampleperiod of the process is not equal to the sampleperiod of the simulation
		 * we have to resample the time series. */
		// TODO
	}
	
	/**
	 * @return Array of all variable names
	 */
	public static ArrayList<String> getVariableNames(){
		ArrayList<String> keys = getInstance().getKeys();
		keys.remove("SamplePeriod");
		
		return keys;
	}
	
	/**
	 * Delete an existing process variable
	 * @param name
	 */
	public static void deleteProcessVariable(String name){		
		if(getInstance().existsProcessVariable(name))
			getInstance().props.remove(name);
		else
			System.out.println("Delete process variable '"+name+"' failed: No such variable found!");
	}
	
	/**
	 * Add a new process variable initialized ad {0}
	 * @param name
	 * @throws Exception 
	 */
	public static void addProcessVariable(String name) throws Exception{
		double[] values = {0};
		addProcessVariable(name, values);
	}
	
	/**
	 * Add a new process variable initionized at 'values'
	 * @param name
	 * @param values
	 * @throws Exception 
	 */
	public static void addProcessVariable(String name, double[] values) throws Exception{
		getInstance().setValue(getInstance().getUniqueVariableName(name), values);
	}
	
	/**
	 * Set values of an existing process variable
	 * @param name
	 * @param values
	 * @throws Exception 
	 */
	public static void setProcessVariable(String name, double[] values) throws Exception{
		if(getInstance().existsProcessVariable(name))
			getInstance().setValue(name, values);
		else
			System.out.println("Setting process variable '"+name+"' failed: No such variable found!");
	}
	
	/**
	 * Set the name of an existing process variable
	 * @param name
	 * @param newName
	 */
	public static void setProcessVariableName(String name, String newName){	
		if(getInstance().existsProcessVariable(name)){
			newName = getInstance().getUniqueVariableName(newName);
			getInstance().props.put(newName, getInstance().props.get(name));
			getInstance().props.remove(name);
		}
		else
			System.out.println("Renaming process variable '"+name+"' failed: No such variable found!");
	}
	

	private boolean existsProcessVariable(String name){
		return props.containsKey(name);
	}
	
	private String getUniqueVariableName(String name){
		if(!existsProcessVariable(name))
			return name;
		
		// Add numer s.t. name becomes unique
		int i=1;
		while(existsProcessVariable(name+"_"+i))
			i++;
		
		return name+"_"+i;
	}

	
	/**
	 * @return Number of samples
	 */
	public static int getNumberOfTimeStamps() {
		try {
			return getProcessVariable(getVariableNames().get(0)).length;
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * @param name
	 * @return Process variable with key 'name'
	 */
	public static double[] getProcessVariable(String name){
		try {
			return getInstance().getDoubleArray(name);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return Sample period in [s]
	 */
	public static double getSamplePeriod() {
		return samplePeriod;
	}
	
}
