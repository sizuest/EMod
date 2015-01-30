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
package ch.ethz.inspire.emod.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * Abstract machine component. 
 * 
 * @author dhampl
 *
 */
@XmlRootElement
public abstract class APhysicalComponent {
//public abstract class APhysicalComponent <T> {

	protected List<IOContainer> inputs;
	//protected List<IOContainer<T>> inputs;
	protected List<IOContainer> outputs;
	//protected List<IOContainer<T>> outputs;
	protected double timestep;
	protected ArrayList<DynamicState> dynamicStates;
	
	/**
	 * @param id
	 * @param value
	 */
	public void setInput(int id, double value) {
	//public void setInput(int id, T value) {
		inputs.get(id).setValue(value);
	}	
	
	public double getOutput(int id) {
	//public T getOutput(int id) {
		return outputs.get(id).getValue();
	}
	
	public double getInput(int id) {
	//public T getInput(int id) {
		return inputs.get(id).getValue();
	}
	
	public List<IOContainer> getInputs() {
	//public List<IOContainer<T>> getInputs() {
		return inputs;
	}
	
	public List<IOContainer> getOutputs() {
	//public List<IOContainer<T>> getOutputs() {
		return outputs;
	}
	
	//public ArrayList<MachineComponent> getOutputs() {
	//	return outputs;
	//}
	
	public IOContainer getInput(String name) {
	//public IOContainer<T> getInput(String name) {
		IOContainer temp=null;
		//IOContainer<T> temp=null;
		for(IOContainer ioc:inputs){
		//for(IOContainer<T> ioc:inputs){
			if(ioc.getName().equals(name)) {
				temp=ioc;
				break;
			}
		}
		return temp;
	}
	
	/*
	public FluidContainer getFluidInput(String name){
		FluidContainer temp=null;
		for(FluidContainer fioc:inputs){
			if(fioc.getName().equals(name)){
				temp=(FluidContainer) fioc;
				break;
			}
		}
	}
	*/
	
	public IOContainer getOutput(String name) {
	//public IOContainer<T> getOutput(String name) {
		IOContainer temp=null;
		//IOContainer<T> temp=null;
		for(IOContainer ioc:outputs){
		//for(IOContainer<T> ioc:outputs){
			if(ioc.getName().equals(name)) {
				temp=ioc;
				break;
			}
		}
		return temp;
	}
	
	public abstract String getType();
	
	public abstract void update();
	
	/**
	 * To be executed before Simulation
	 */
	public void preSimulation(){
		if(null==dynamicStates)
			return;
		for(DynamicState ic : dynamicStates)
			try {
				ic.loadInitialCondition();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Sets the sample time for the model
	 * 
	 * @param timestep sample time in seconds
	 */
	public void setSimulationTimestep(double timestep){
		this.timestep = timestep;
		if(dynamicStates!=null)
			for(DynamicState ds : dynamicStates) 
				ds.setTimestep(timestep);
		
	}
	
	/**
	 * Sets the type of the component according to the argument
	 * @param type
	 */
	public abstract void setType(String type);
	
	/**
	 * Returns the type of the model used, referring to the folders in the
	 * MachineComponentDB. If not over-written, this is the class name.
	 * @return Model Type
	 */
	public String getModelType() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * Adds a new initial condition to the set
	 * @param name
	 * @param unit
	 * @return the created {@link DynamicState}
	 */
	public DynamicState newDynamicState(String name, Unit unit){
		DynamicState ic = new DynamicState(name, unit);
		dynamicStates.add(ic);
		
		return ic;
	}
	
	/**
	 * Returns the {@link DynamicState} with the given name and parent
	 * @param name
	 * @param parent 
	 * @return {@link DynamicState}
	 */
	public DynamicState getDynamicState(String name){
		for(DynamicState ds : this.getDynamicStateList()) 
			if(ds.getName().equals(name))
				return ds;
			
		return null;
	}
	
	/**
	 * List of all initial conditions in this set
	 * @return {@link DynamicState}
	 */
	public ArrayList<DynamicState> getDynamicStateList(){
		return dynamicStates;
	}
	
	/**
	 * Sets the parent of all initial conditions
	 * @param parent
	 */
	public void setDynamicStateParent(String parent){
		if(dynamicStates!=null)
			for(DynamicState ic : this.getDynamicStateList())
				ic.setParent(parent);
	}	
}
