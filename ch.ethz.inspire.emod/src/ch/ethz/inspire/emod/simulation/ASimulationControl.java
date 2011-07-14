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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.model.IOContainer;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.MachineState.MachineStateEnum;

/**
 * generic simulation control object. 
 * 
 * @author dhampl
 *
 */
public abstract class ASimulationControl {

	private static Logger logger = Logger.getLogger(ASimulationControl.class.getName());
	
	protected IOContainer simulationOutput;
	protected String name;
	protected MachineStateEnum state=MachineStateEnum.ON;
	protected Map<MachineStateEnum, MachineStateEnum> stateMap = null;
	
	/**
	 * Constructor with name and unit
	 * 
	 * @param name
	 * @param unit
	 */
	public ASimulationControl(String name, Unit unit) {
		simulationOutput = new IOContainer(name, unit, 0);
		this.name = name;
	}
	
	/**
	 * Constructor with name, unit and config file
	 * 
	 * @param name
	 * @param unit
	 * @param configFile maps machine states to simulator states.
	 */
	public ASimulationControl(String name, Unit unit, String configFile) {
		simulationOutput = new IOContainer(name, unit, 0);
		this.name = name;
		stateMap = new EnumMap<MachineStateEnum, MachineStateEnum>(MachineStateEnum.class);
		readConfig(configFile);
	}
	
	/**
	 * reads the machine states and maps them to simulation states
	 * 
	 * @param file
	 */
	private void readConfig(String file) {
		logger.log(LogLevel.DEBUG, "reading state mapping from: "+file);
		try {
			Properties p = new Properties();
			InputStream is = new FileInputStream(file);
			p.load(is);
			for(MachineStateEnum ms : MachineStateEnum.values()) {
				String line = p.getProperty(ms.name()+"_state");
				stateMap.put(ms, MachineStateEnum.valueOf(line));
				
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * updates the simulationOutput {@link IOContainer} according to the {@link MachineState}
	 * and simulation logic for the next simulation cycle.
	 */
	public abstract void update();
	
	/**
	 * sets the state. the state is mapped through the stateMap to valid states for 
	 * the simulator.
	 */
	public void setState(MachineStateEnum state) {
		this.state = stateMap.get(state);
	}
		
	public MachineStateEnum getState() {
		return state;
	}
	
	public String getName() {
		return name;
	}
	
	public IOContainer getOutput() {
		return simulationOutput;
	}
}
