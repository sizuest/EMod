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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.model.IOContainer;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * generic simulation control object. 
 * 
 * @author dhampl
 *
 */
public abstract class ASimulationControl {

	private static Logger logger = Logger.getLogger(ASimulationControl.class.getName());
	
	/* Attributes for JABX*/
	@XmlElement
	protected String name;
	@XmlElement
	protected Unit unit;
	@XmlElement
	protected String configFile;
	
	protected IOContainer simulationOutput;
	protected ComponentState state=ComponentState.ON;
	protected Map<MachineState, ComponentState> stateMap = null;
	
	/**
	 * Constructor with name, unit and config file
	 * 
	 * @param name
	 * @param unit
	 * @param configFile maps machine states to simulator states.
	 */
	public ASimulationControl(String name, Unit unit, String configFile) {
		this.name = name;
		this.unit = unit;
		this.configFile=configFile;
		
		simulationOutput = new IOContainer(name, unit, 0);
		readConfig(configFile);
	}
	
	/**
	 * Constructor for JAXB
	 *   - Constructor can not have arguments.
	 */
	public ASimulationControl() {
		/* name, unit and configFile are set by JABX */
	}
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		simulationOutput = new IOContainer(name, unit, 0);
	}
	/**
	 * Path can not be given, when creating the objects by JABX.
	 * @param path Directory holding the configfiles.
	 */
	public void afterJABX(String path)
	{
		readConfig(path+configFile);
	}
	
	/**
	 * reads the machine states and maps them to simulation states
	 * 
	 * @param file each {@link MachineState} is represented by one line MachineState_state=SimState ; e.g. READY_state=ON
	 */
	protected void readConfig(String file) {
		logger.log(LogLevel.DEBUG, "reading state mapping from: "+file);
		
		stateMap = new EnumMap<MachineState, ComponentState>(MachineState.class);
		if(file!=null) {
			try {
				Properties p = new Properties();
				InputStream is = new FileInputStream(file);
				p.load(is);
				is.close();
				for(MachineState ms : MachineState.values()) {
					String line = p.getProperty(ms.name()+"_state");
					stateMap.put(ms, ComponentState.valueOf(line));
				}
			} catch(IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	/**
	 * updates the simulationOutput {@link IOContainer} according to the {@link SimulationState}
	 * and simulation logic for the next simulation cycle.
	 */
	public abstract void update();
	
	/**
	 * sets the state. the state is mapped through the stateMap to valid states for 
	 * the simulator.
	 */
	public void setState(MachineState state) {
		this.state = stateMap.get(state);
	}
		
	public ComponentState getState() {
		return state;
	}
	
	public String getName() {
		return name;
	}
	
	public IOContainer getOutput() {
		return simulationOutput;
	}
}
