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
package ch.ethz.inspire.emod.model.thermal;

import ch.ethz.inspire.emod.model.Material;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;

/**
 * General thermal element class
 * 
 * @author simon
 *
 */
public class ThermalElement {
	
	protected DynamicState temperature;
	protected Material material;
	protected double mass;
	protected ThermalShiftProperty<Double> heatInput;
	//protected double heatInput;
	//protected double lastHeatInput;
	
	/**
	 * ThermalElement
	 * 
	 * @param materialName
	 * @param mass
	 */
	public ThermalElement(String materialName, double mass){
		this.mass          = mass;
		this.material      = new Material(materialName);
		this.temperature   = new DynamicState("Temperature", Unit.KELVIN);
		this.heatInput     = new ThermalShiftProperty<Double>(0.0);
		//this.lastHeatInput = 0;
	}
	
	/**
	 * Set the heat input [W]
	 * @param heatInput
	 */
	public void setHeatInput(double heatInput){
		this.heatInput.set(heatInput);
	}
	
	/**
	 * Add to current heat input [W]
	 * @param heatInput
	 */
	public void addHeatInput(double heatInput){
		this.heatInput.set(this.heatInput.getCurrent()+heatInput);
	}
	
	/**
	 * Perform an integration step of length timestep [s]
	 * @param timestep
	 */
	public void integrate(double timestep){
		this.temperature.setTimestep(timestep);
		/* Integration step:
		 * T(k+1) [K] = T(k) [K]+ SampleTime[s]/2*(P_in(k) [W]+P_in(k+1) [W]) / cp [J/kg/K] / m [kg] 
		 */	
		this.temperature.addValue((this.heatInput.getCurrent()+this.heatInput.getLast())/2/mass/material.getHeatCapacity()*timestep);
		
		// Shift Heat inputs
		//this.lastHeatInput = this.heatInput;
		//this.heatInput     = 0;
		this.heatInput.update(0.0);
	}
	
	/**
	 * Returns the temperature state
	 * @return {@link DynamicState} temperature
	 */
	public DynamicState getTemperature(){
		return temperature;
	}

	public Material getMaterial() {
		return this.material;
	}

	public void setMaterial(String type) {
		this.material.setMaterial(type);
	}
	
	public void setVolume(double volume){
		this.mass = volume*this.material.getDensity(this.getTemperature().getValue(), 100000);
	}

}
