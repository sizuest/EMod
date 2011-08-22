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

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.SimulationConfigReader;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * generic simulation control object. 
 * 
 * @author dhampl
 *
 */
@XmlRootElement
public abstract class ASimulationControl {

	private static Logger logger = Logger.getLogger(ASimulationControl.class.getName());
	
	/* Attributes for JABX*/
	@XmlElement
	protected String name;
	@XmlElement
	protected Unit unit;
	@XmlElement
	protected double simulationPeriod;
	
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
	public ASimulationControl(String name, Unit unit) {
		this.name = name;
		this.unit = unit;
		
		simulationOutput = new IOContainer(name, unit, 0);
		readConfig();
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
	public void afterJABX()
	{
		readConfig();
	}
	
	/**
	 * reads the machine states and maps them to simulation states
	 * 
	 * @param file each {@link MachineState} is represented by one line MachineState_state=SimState ; e.g. READY_state=ON
	 */
	protected void readConfig() {
		logger.log(LogLevel.DEBUG, "reading state mapping for: "+this.getClass().getSimpleName()+"_"+name);
		
		stateMap = new EnumMap<MachineState, ComponentState>(MachineState.class);
		if(name!=null) {
			try {
				SimulationConfigReader scr=null;
				try {
					scr = new SimulationConfigReader(this.getClass().getSimpleName(), name);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				/*Properties p = new Properties();
				InputStream is = new FileInputStream(file);
				p.load(is);
				is.close();*/
				for(MachineState ms : MachineState.values()) {
					//String line = p.getProperty(ms.name()+"_state");
					stateMap.put(ms, scr.getComponentState(ms.name()));
				}
			} catch(Exception e) {
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
	
	public void setSimulationPeriod(double simulationPeriod) {
		this.simulationPeriod=simulationPeriod;
	}
	
	public IOContainer getOutput() {
		return simulationOutput;
	}
}
