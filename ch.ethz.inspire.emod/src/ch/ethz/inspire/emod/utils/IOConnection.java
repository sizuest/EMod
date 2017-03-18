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

package ch.ethz.inspire.emod.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.graph.GraphElementPosition;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.fluid.FluidCircuit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * contains information on simulation input sources and targets through
 * references to IOContainers of MachineComponents and SimulationControls.
 * 
 * @author dhampl
 * 
 */
@XmlRootElement
public class IOConnection {
	protected IOContainer source;
	protected IOContainer target;
	
	@XmlElement
	protected String sourceName;
	@XmlElement
	protected String targetName;
	@XmlElement
	private ArrayList<GraphElementPosition> points = new ArrayList<GraphElementPosition>();
	
	
	/**
	 * post xml init method (loading physics data)
	 * 
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
	}

	/**
	 * IOConnection
	 */
	public IOConnection() {}

	/**
	 * 
	 * @param source
	 * @param target
	 * @throws Exception
	 *             thrown if units don't match
	 */
	public IOConnection(IOContainer source, IOContainer target)
			throws Exception {
		this.source = source;
		this.target = target;

		if (!source.getUnit().equals(target.getUnit()))
			throw new Exception("units do not match " + source.getName() + ": "
					+ source.getUnit() + " <-> " + target.getName() + ": "
					+ target.getUnit());
		
		sourceName = getFullName(source);
		targetName = getFullName(target);
	}

	/**
	 * gets the Source IOContainer of the Connection
	 * 
	 * @return the Source
	 */
	public IOContainer getSource() {
		return source;
	}

	/**
	 * gets the Target IOContainer of the Connection
	 * 
	 * @return the Target
	 */
	public IOContainer getTarget() {
		return target;
	}

	/**
	 * update the connection, i.e. get the value of the source and write it to
	 * the target
	 */
	public void update() {
		this.getTarget().setValue(this.getSource().getValue());
	}
	
	/**
	 * Returns the list of corners in the graph, this list is
	 * empty for straight connection lines
	 * @return
	 */
	public ArrayList<GraphElementPosition> getPoints(){
		return this.points;
	}
	
	/**
	 * @return the sourceName
	 */
	public String getSourceName() {
		sourceName = getFullName(source);
		return sourceName;
	}	

	/**
	 * @return the targetName
	 */
	public String getTargetName() {
		targetName = getFullName(target);
		return targetName;
	}
	
	/**
	 * Creates the ioc for the given names
	 * @return 
	 */
	public boolean createIOConnection(){
		
		if (targetName == null | sourceName == null) {
			Exception ex = new Exception("IOConnection: Traget and/or source name null");
			ex.printStackTrace();
			return false;
		}
		
		/* Get input component and input object */
		StringTokenizer stin = new StringTokenizer(targetName, ".");
		String inObj = stin.nextToken();
		String inVar = stin.nextToken();
		
		
		MachineComponent inmc = Machine.getMachineComponent(inObj);
		if (inmc == null) {
			Exception ex = new Exception("Undefined input component '" + inObj);
			ex.printStackTrace();
			return false;
		}

		// when a fluidconnection is necessary ->
		// create two FluidContainers instead
		target = inmc.getComponent().getInput(inVar);
		if (target == null) {
			Exception ex = new Exception("Undefined input '" + inVar + "' of component '" + inObj);
			ex.printStackTrace();
			return false;
		}

		/* Get output component and output object */
		String outstruct = sourceName;
		StringTokenizer stout = new StringTokenizer(outstruct, ".");
		String outObj = stout.nextToken();
		MachineComponent outmc = null;

		if (stout.hasMoreTokens()) {

			/* Output object comes from a component */
			String outVar = stout.nextToken();

			// MachineComponent outmc = getMachineComponent(outObj);
			outmc = Machine.getMachineComponent(outObj);
			if (outmc == null) {
				Exception ex = new Exception("Undefined output component '" + outObj);
				ex.printStackTrace();
				return false;
			}

			source = outmc.getComponent().getOutput(outVar);
			if (source == null) {
				Exception ex = new Exception("Undefined output '" + outVar + "' of component '" + outObj);
				ex.printStackTrace();
				return false;
			}

		} else {
			/*
			 * If no output component, it must be a simulation
			 * object
			 */
			ASimulationControl sc = Machine.getInputObject(outstruct);
			if(null != sc)
				source = sc.getOutput();
			if (source == null) {
				Exception ex = new Exception("Undefined simulation object '" + outstruct);
				ex.printStackTrace();
				return false;
			}
		}
		
		if(this instanceof FluidConnection)
			FluidCircuit.floodCircuit((FluidContainer) source, (FluidContainer) target);
		
		if (target == null | source == null) {
			System.err.println("IOConnection: Traget and/or source name not valid: "+sourceName+"->"+targetName);
			return false;
		}
		
		return true;
	}
	
	private String getFullName(IOContainer io){
		
		List<MachineComponent> components   = Machine.getInstance().getMachineComponentList();
		List<ASimulationControl> simulators = Machine.getInstance().getInputObjectList();
		
		for (ASimulationControl sc : simulators) {
			if (sc.getOutput().equals(io.getReference())) {
				return sc.getName();
			}
		}
		// Components
		for (MachineComponent mc : components) {
			if ( mc.getComponent().getInputs().contains(io.getReference()) ||
				 mc.getComponent().getOutputs().contains(io.getReference()))
				return mc.getName()+"."+io.getReference().getName();
		}
		
		return null;
	}
}
