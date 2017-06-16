/***********************************
 * $Id: EModSession.java xxx 21.03.2017 13:35:43 sizuest $
 *
 * $URL:  $
 * $Author: sizuest $
 * $Date: 21.03.2017 13:35:43 $
 * $Rev: xxx $
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.simulation.MachineState;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Generic class to save a modeling sessions parameters
 * @author sizuest
 *
 */
@XmlRootElement
public class EModSession {
	
	private static EModSession session;
	
	@XmlElement
	private String machineName = "";
	@XmlElement
	private String processName = "";
	@XmlElement
	private String machineConfig = "";
	@XmlElement
	private String simulationConfig = "";
	@XmlElement
	private String notes = "";
	
	private String path = null;
	
	/**
	 * @return the path
	 */
	public static String getPath() {
		return getInstance().path;
	}

	/**
	 * @param path the path to set
	 */
	public static void setPath(String path) {
		getInstance().path = path;
	}

	/* 
	 * Private constructor for singelton implementation
	 */
	private EModSession() {}
	
	/**
	 * Returns the current session object
	 * @return
	 */
	protected static EModSession getInstance(){
		if(null==session)
			session = new EModSession();
		
		return session;
	}
	
	/**
	 * Saves the config according to the session settings
	 */
	public static void save(){
		saveSessionToFile(getSessionConfigPath());
	}
	
	
	/**
	 * Loads the session from a file
	 * @param file
	 */
	public static void initSessionFromFile(String file) {
		session = null;
		try {
			JAXBContext context = JAXBContext.newInstance(EModSession.class);
			Unmarshaller um = context.createUnmarshaller();
			session = (EModSession) um.unmarshal(new FileReader(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the session to a file
	 * @param file
	 */
	public static void saveSessionToFile(String file) {
		// Save Machine Configuration
		try {
			JAXBContext context = JAXBContext.newInstance(EModSession.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			Writer w = new FileWriter(file);
			m.marshal(session, w);
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the machineName
	 */
	public static String getMachineName() {
		return getInstance().machineName;
	}

	/**
	 * @param machineName the machineName to set
	 */
	public static void setMachineName(String machineName) {
		getInstance().machineName = machineName;
		addNote("Set machine name: '"+machineName+"'");
	}

	/**
	 * @return the processName
	 */
	public static String getProcessName() {
		return getInstance().processName;
	}

	/**
	 * @param processName the processName to set
	 */
	public static void setProcessName(String processName) {
		if(-1<Arrays.binarySearch(getProcessNames(), processName))
			getInstance().processName = processName;
		else
			newProcess(processName);
		
		addNote("Set process name: '"+processName+"'");
	}

	/**
	 * @return the machineConfig
	 */
	public static String getMachineConfig() {
		return getInstance().machineConfig;
	}

	/**
	 * @param machineConfig the machineConfig to set
	 */
	public static void setMachineConfig(String machineConfig) {
		if(-1<Arrays.binarySearch(getMachineConfigs(), machineConfig))
			getInstance().machineConfig = machineConfig;
		else
			newMachineConfig(machineConfig);
		addNote("Set machine config name: '"+machineConfig+"'");
	}

	/**
	 * @return the simulationConfig
	 */
	public static String getSimulationConfig() {
		return getInstance().simulationConfig;
	}

	/**
	 * @param simulationConfig the simulationConfig to set
	 */
	public static void setSimulationConfig(String simulationConfig) {
		if(-1<Arrays.binarySearch(getSimulationConfigs(), simulationConfig))
			getInstance().simulationConfig = simulationConfig;
		else
			newSimulationConfig(simulationConfig, "default");
		
		addNote("Set simulation config name: '"+simulationConfig+"'");
	}

	/**
	 * @return the notes
	 */
	public static String getNotes() {
		return getInstance().notes;
	}

	/**
	 * @param text the notes to add
	 */
	public static void addNote(String text) {
		getInstance().notes += "[+] "+text+" ("+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date())+", "+System.getProperty("user.name")+")\n";
	}
	
	/**
	 * Returns the roots path of the current machine cfg
	 * @return
	 */
	public static String getRootPath(){
		return PropertiesHandler.getProperty("app.MachineDataPathPrefix") + File.separator + getMachineName();
	}
	
	
	/**
	 * Returns the path to the machine config file
	 * @return
	 */
	public static String getMachineConfigPath(){
		 return getMachineConfigDirPath() + File.separator + Defines.MACHINEFILENAME;
	}
	
	/**
	 * Returns the path of the current machine config directory
	 * @return
	 */
	public static String getMachineConfigDirPath(){
		return  getRootPath() + File.separator + 
				Defines.MACHINECONFIGDIR + File.separator +
				getMachineConfig();
	}
	
	/**
	 * Returns the path to the simulation config file
	 * @return
	 */
	public static String getSimulationConfigPath(){
		return  getRootPath() + File.separator +
				Defines.SIMULATIONCONFIGDIR + File.separator +
				getSimulationConfig() + File.separator + Defines.SIMULATIONCONFIGFILE;
	}
	
	/**
	 * Returns the path to the process file
	 * @return
	 */
	public static String getProcessConfigPath(){
		return  getRootPath() + File.separator +
				Defines.SIMULATIONCONFIGDIR + File.separator +
				getSimulationConfig() + File.separator + Defines.PROCESSDEFFILE_PREFIX + getProcessName() + ".xml";
	}
	
	/**
	 * Returns the path to the state sequence file
	 * @return
	 */
	public static String getStateSequenceConfigPath(){
		return  getRootPath() + File.separator +
				Defines.SIMULATIONCONFIGDIR + File.separator +
				getSimulationConfig() + File.separator + Defines.MACHINESTATEFNAME;
	}
	
	/**
	 * Returns the path to the session file
	 * @return
	 */
	public static String getSessionConfigPath(){
		return  getRootPath() + File.separator +
				Defines.SESSIONFILE;
	}
	
	/**
	 * Returns the path to the simulation output
	 * @return
	 */
	public static String getResultFilePath(){
		return getRootPath() + File.separator +
				Defines.RESULTDIR + File.separator +
				getMachineConfig() + "_" +
				getSimulationConfig() + "_" +
				getProcessName() + ".dat";
	}
	
	/**
	 * Returns the path to the FEM export file
	 * @return
	 */
	public static String getFEMExportFilePath(){
		return getRootPath() + File.separator +
				Defines.RESULTDIR + File.separator +
				getMachineConfig() + "_" +
				getSimulationConfig() + "_" +
				getProcessName() + "_FEM.dat";
	}
	
	/**
	 * Creates a new machine configuration with the stated name
	 * @param machineConfig
	 */
	public static void newMachineConfig(String machineConfig){
		EModSession.getInstance().machineConfig = machineConfig;
		
		File machinexml = new File(EModSession.getMachineConfigPath());
		try {
			machinexml.getParentFile().mkdirs();
			machinexml.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Create empty machine
		Machine.clearMachine();
		Machine.saveMachineToFile(EModSession.getMachineConfigPath());
		
	}
	
	
	/**
	 * Creates a new simulation configuration with the stated name
	 * @param simConfig
	 * @param processName
	 */
	public static void newSimulationConfig(String simConfig, String processName){
		EModSession.getInstance().simulationConfig = simConfig;
		EModSession.getInstance().processName = processName;
		
		File simxml     = new File(EModSession.getSimulationConfigPath());
		//File processxml = new File(EModSession.getProcessConfigPath());
		File stateseq   = new File(EModSession.getStateSequenceConfigPath());
		try {
			simxml.getParentFile().mkdirs();
			simxml.createNewFile();
			//processxml.createNewFile();
			stateseq.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		States.removeAllStates();
		States.appendState(10, MachineState.ON);
		States.saveStates(getMachineName(), getSimulationConfig());
		
		newProcess(processName);
		
		
	}
	
	/**
	 * Returns the available machine configurations
	 * @return
	 */
	public static String[] getMachineConfigs(){
		String path = getRootPath() + File.separator + Defines.MACHINECONFIGDIR;
		File subdir = new File(path);

		// check if subdirectory exists, then show possible configurations
		if (subdir.exists()) {
			String[] subitems = subdir.list();
			Arrays.sort(subitems);
			return subitems;
		}
		
		return new String[] {};
	}
	
	/**
	 * Returns the available simulation configurations
	 * @return
	 */
	public static String[] getSimulationConfigs(){
		String path = getRootPath() + File.separator + Defines.SIMULATIONCONFIGDIR;
		File subdir = new File(path);

		// check if subdirectory exists, then show possible configurations
		if (subdir.exists()) {
			String[] subitems = subdir.list();
			Arrays.sort(subitems);
			return subitems;
		}
		
		return new String[] {};
	}
	
	/**
	 * Returns the available processes for the current simulation config
	 * @return
	 */
	public static String[] getProcessNames(){
		return getProcessNames(getSimulationConfig());
	}
	
	/**
	 * Returns the available processes for the given simulation config
	 * @param simConfig 
	 * @return
	 */
	public static String[] getProcessNames(String simConfig){
		String path = getRootPath() + File.separator + Defines.SIMULATIONCONFIGDIR + File.separator +  simConfig;
		File subdir = new File(path);
		
		ArrayList<String> names = new ArrayList<String>();
		
		for (File f : subdir.listFiles()) {
			if (f.getName().startsWith("process_")) {
				names.add(f.getName().substring(8, f.getName().length() - 4));
			}
		}
		
		String[] ret = names.toArray(new String[] {});
		Arrays.sort(ret);

		return ret;
	}
	
	/**
	 * Creates a new process with the stated name
	 * @param processName
	 */
	public static void newProcess(String processName){
		getInstance().processName = processName;
		Process.newProcess(processName);	
	}
		
	
	/**
	 * Creates a new machine with the given parameters
	 * @param machineName
	 * @param machineConfig
	 * @param simConfig
	 * @param processName
	 */
	public static void newSession(String machineName, String machineConfig, String simConfig, String processName){
		
		getInstance().notes = "";
		
		getInstance();
		// Exit library mode
		EModSession.setLibrary(false);
		EModFileHandling.clearTempPath();

		// create the according folders and files (machine.xml, iolinking.txt)
		EModSession.setMachineName(machineName);
		newMachineConfig(machineConfig);
		newSimulationConfig(simConfig, processName);
		
		EModSession.setPath(null);
	}
	
	
	
	/**
	 * Toggles whether to use the library or not
	 * @param b
	 */
	public static void setLibrary(boolean b){
		if(b){
			PropertiesHandler.setProperty("app.MachineDataPathPrefix", Defines.LIBFILESPACE);
			addNote("Enabled library mode");
		}
		else{
			PropertiesHandler.setProperty("app.MachineDataPathPrefix", Defines.TEMPFILESPACE);
			addNote("Disabled library mode");
		}
		
	}
	
	/**
	 * @param name Name of the machine config to delete
	 */
	public static void removeMachineConfig(String name){
		String path = getRootPath() + File.separator + Defines.MACHINECONFIGDIR + File.separator + name;
		File dir = new File(path);
		
		if(dir.exists() && dir.isDirectory())
			deleteFolder(dir);
		
		if(getMachineConfigs().length == 0)
			newMachineConfig("MachineConfig1");
		else
			setMachineConfig(getMachineConfigs()[0]);
	}
	
	/**
	 * @param name Name of the sim config to delete
	 */
	public static void removeSimulationConfig(String name){
		String path = getRootPath() + File.separator + Defines.SIMULATIONCONFIGDIR + File.separator + name;
		File dir = new File(path);
				
		if(dir.exists() && dir.isDirectory())
			deleteFolder(dir);
		
		
		if(getSimulationConfigs().length == 0)
			newSimulationConfig("SimConfig1", "default");
		else{
			setSimulationConfig(getSimulationConfigs()[0]);
			setProcessName(getProcessNames()[0]);
		}
		
		
	}

	/**
	 * @param name Name of the process to delete
	 */
	public static void removeProcess(String name){
		String path = getRootPath() + File.separator + Defines.MACHINECONFIGDIR + File.separator + "process_" + name;
		File file = new File(path);
		
		if(file.exists() && !file.isDirectory())
			file.delete();
		
		if(getProcessNames().length == 0)
			newProcess("default");
		else
			setProcessName(getProcessNames()[0]);
	}
	
	
	private static void deleteFolder(File folder) {
        for(File f: folder.listFiles()) {
            f.delete();
            
            System.out.println(f.canWrite());
        }
		
		folder.delete();
	}


}
