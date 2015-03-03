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
	protected double heatInput;
	protected double lastHeatInput;
	
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
		this.lastHeatInput = 0;
	}
	
	/**
	 * Set the heat input [W]
	 * @param heatInput
	 */
	public void setHeatInput(double heatInput){
		this.heatInput = heatInput;
	}
	
	/**
	 * Add to current heat input [W]
	 * @param heatInput
	 */
	public void addHeatInput(double heatInput){
		this.heatInput += heatInput;
	}
	
	/**
	 * Perform an integration step of length timestep [s]
	 * @param timestep
	 */
	public void integrate(double timestep){
		temperature.setTimestep(timestep);
		/* Integration step:
		 * T(k+1) [K] = T(k) [K]+ SampleTime[s]/2*(P_in(k) [W]+P_in(k+1) [W]) / cp [J/kg/K] / m [kg] 
		 */	
		temperature.addValue((heatInput+lastHeatInput)/2/mass/material.getHeatCapacity()*timestep);
		
		// Shift Heat inputs
		this.lastHeatInput = this.heatInput;
		this.heatInput     = 0;
	}
	
	/**
	 * Returns the temperature state
	 * @return {@link DynamicState} temperature
	 */
	public DynamicState getTemperature(){
		return temperature;
	}

}
