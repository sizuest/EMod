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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.simulation.MachineState;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Generic class to save a modelling sessions parameters
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
		getInstance().processName = processName;
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
		getInstance().machineConfig = machineConfig;
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
		getInstance().simulationConfig = simulationConfig;
	}

	/**
	 * @return the notes
	 */
	public static String getNotes() {
		return getInstance().notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public static void setNotes(String notes) {
		getInstance().notes = notes;
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
		EModSession.setMachineConfig(machineConfig);
		
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
		EModSession.setSimulationConfig(simConfig);
		EModSession.setProcessName(processName);
		
		File simxml     = new File(EModSession.getSimulationConfigPath());
		File processxml = new File(EModSession.getProcessConfigPath());
		File stateseq   = new File(EModSession.getStateSequenceConfigPath());
		try {
			simxml.getParentFile().mkdirs();
			simxml.createNewFile();
			processxml.createNewFile();
			stateseq.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		States.removeAllStates();
		States.appendState(10, MachineState.ON);
		States.saveStates(getMachineName(), getSimulationConfig());
		
		try {
			Process.setTimeVector(new double[]{0,1,2,3,4,5});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Creates a new process with the stated name
	 * @param processName
	 */
	public static void newProcess(String processName){
		EModSession.setProcessName(processName);
		
		File processxml = new File(EModSession.getProcessConfigPath());
		try {
			processxml.getParentFile().mkdirs();
			processxml.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			Process.setTimeVector(new double[]{0,1,2,3,4,5});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	
	/**
	 * Creates a new machine with the given parameters
	 * @param machineName
	 * @param machineConfig
	 * @param simConfig
	 * @param processName
	 */
	public static void newSession(String machineName, String machineConfig, String simConfig, String processName){
		
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
	 * Toogles whether to use the library or not
	 * @param b
	 */
	public static void setLibrary(boolean b){
		if(b)
			PropertiesHandler.setProperty("app.MachineDataPathPrefix", Defines.LIBFILESPACE);
		else
			PropertiesHandler.setProperty("app.MachineDataPathPrefix", Defines.TEMPFILESPACE);
		
	}


}
