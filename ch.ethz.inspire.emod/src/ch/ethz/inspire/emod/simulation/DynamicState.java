/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2014 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/

package ch.ethz.inspire.emod.simulation;

import java.lang.reflect.Method;

import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.PropertiesHandler;


/**
 * Implements an initial condition for a {@link APhysicalComponent} 
 * @author sizuest
 *
 */
public class DynamicState {
	private String name;
	private double value, lastValue;
	private double initialValue;
	private double timestep;
	private Unit unit;
	private String parent;
	private Method initFnct;
	private Object initFnctObj;
	
	/**
	 * Initial Condition for {@link APhysicalComponent}
	 * @param name	Name of the initial condition
	 * @param unit  Unit
	 */
	public DynamicState(String name, Unit unit) {
		this.name = name;
		this.value = Double.NaN;
		this.lastValue = Double.NaN;
		this.initialValue = Double.NaN;
		this.unit = unit;
		this.parent = "";
		this.timestep = 0;
		
		this.initFnct    = null;
		this.initFnctObj = null;
	}
	
	/**
	 * Set state name
	 * @param name 
	 */
	public void setName(String name){
		this.name=name;
	}
	
	/**
	 * Sets the value of the state
	 * @param value
	 */
	public void setValue(double value){
		this.lastValue = this.value;
		this.value     = value;
	}
	
	/**
	 * Adds the value to the current state
	 * @param value
	 */
	public void addValue(double value){
		this.lastValue = this.value;
		this.value     += value;
	}
	
	/**
	 * Sets the value of the initial condition
	 * @param initialValue
	 */
	public void setInitialCondition(double initialValue){
		this.initialValue = initialValue;
		setInitialCondition();
	}
	
	/**
	 * sets the current and last value to the initial condition
	 */
	public void setInitialCondition(){
		this.value     = this.initialValue;
		this.lastValue = this.initialValue;
		
		// If available, run init function
		if (initFnct!=null){
			try {
				initFnct.invoke(initFnctObj, this.initialValue);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param initFnct
	 * @param initFnctObj 
	 */
	public void setInitialConditionFunction(Method initFnct, Object initFnctObj){
		this.initFnct    = initFnct;
		this.initFnctObj = initFnctObj;
	}
	
	/**
	 * Sets the parent of the initial condition
	 * @param parent
	 */
	public void setParent(String parent){
		this.parent = parent;
	}
	
	/**
	 * Sets the timestep duration [s]
	 * @param timestep
	 */
	public void setTimestep(double timestep){
		this.timestep = timestep;
	}
	
	/**
	 * @return current value
	 */
	public double getValue(){
		return this.value;
	}
	
	/**
	 * @return initial condition value
	 */
	public double getInitialValue(){
		return initialValue;
	}
	
	/**
	 * @return numerical derivate of value
	 */
	public double getTimeDerivate(){
		return (value-lastValue)/timestep;
	}
	
	/**
	 * @return initial condition name
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * @return initial condition parent name
	 */
	public String getParent(){
		return this.parent;
	}
	
	/**
	 * @return initial condition unit
	 */
	public Unit getUnit(){
		return this.unit;
	}
	
	/**
	 * Generates a unique IC name containing the parent and the IC names
	 * @return parent.name
	 */
	public String getInitialConditionName(){
		return "InitialValue_"+getParent()+"_"+getName();
	}
	
	/**
	 * Loads the initial condition from the simulation file
	 * @throws Exception 
	 */
	public void loadInitialCondition() throws Exception{
		
		if(this.parent.equals("")) 
			throw new Exception("Can't load initial condition: No parent set!");
		
		try {
			ConfigReader initCond = new ConfigReader( configPath() );
			setInitialCondition(initCond.getDoubleValue(getInitialConditionName()));
			initCond.Close();
		}
		catch (Exception e) {
			e.printStackTrace();
			value = Double.NaN;
		}
	}
	
	
	/**
	 * Saves the initial condition
	 * @throws Exception 
	 */
	public void saveInitialCondition() throws Exception{
		
		if(this.parent.equals(""))
			throw new Exception("Can't save initial condition: No parent set!");
		
		try {
			ConfigReader initCond = new ConfigReader( configPath() );
			initCond.setValue(getInitialConditionName(), this.value);
			initCond.Close();
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
	
	private String configPath(){
		return PropertiesHandler.getProperty("app.MachineDataPathPrefix")+
			      "/"+PropertiesHandler.getProperty("sim.MachineName")+"/"+Defines.SIMULATIONCONFIGDIR+"/"+
			      PropertiesHandler.getProperty("sim.SimulationConfigName")+"/"+Defines.SIMULATIONCONFIGFILE;
	}
	
}
