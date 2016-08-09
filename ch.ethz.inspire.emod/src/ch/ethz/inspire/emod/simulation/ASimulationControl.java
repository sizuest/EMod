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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.gui.graph.GraphElementPosition;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.SimulationConfigReader;

/**
 * generic simulation control object. 
 * 
 * @author dhampl
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ASimulationControl {

	private static Logger logger = Logger.getLogger(ASimulationControl.class.getName());
	
	/* Attributes for JABX*/
	@XmlElement(name="name")
	protected String name;
	@XmlElement
	protected SiUnit unit;
	@XmlElement
	protected GraphElementPosition position;
	
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
	public ASimulationControl(String name, SiUnit unit) {
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
		/* name and unit are set by JABX */
	}
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		simulationOutput = new IOContainer(name, unit, 0);
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
				
				for(MachineState ms : MachineState.values())
					stateMap.put(ms, scr.getComponentState(ms.name()));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * updates the simulationOutput {@link IOContainer} according to the {@link SimulationState}
	 * and simulation logic for the next simulation cycle.
	 */
	public abstract void update();
	
	/**
	 * sets the simulation period length in seconds. if required, samples are re-
	 * sampled to the given rate.
	 * 
	 * @param periodLength
	 */
	public abstract void setSimulationPeriod(double periodLength);
	
	/**
	 * sets the state. the state is mapped through the stateMap to valid states for 
	 * the simulator.
	 * @param state 
	 */
	public void setState(MachineState state) {
		this.state = stateMap.get(state);
	}
	
	public void setUnit(SiUnit unit) {
		this.unit = unit;
		simulationOutput.setUnit(unit);
	}
	
	public ComponentState getState() {
		return state;
	}
	
	public String getName() {
		return this.name;
	}
	
	public SiUnit getUnit() {
		return this.unit;
	}
	
	/**
	 * Sets a new name
	 * @param name New name to be set
	 */
	public void setName(String name) {
		this.name = name;
	}
		
	public IOContainer getOutput() {
		return simulationOutput;
	}

	public GraphElementPosition getPosition() {
		return position;
	}
	
	public void setPosition(GraphElementPosition position){
		this.position = position;
	}
}
