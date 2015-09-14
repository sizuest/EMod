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
package ch.ethz.inspire.emod.model.fluid;

import ch.ethz.inspire.emod.model.Material;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.ShiftProperty;

/**
 * General fluid element class
 * 
 * @author simon
 *
 */
public class FluidElement {
	
	protected DynamicState mass;
	protected Material material;
	protected ShiftProperty<Double> massInput;
	//protected double heatInput;
	//protected double lastHeatInput;
	
	/**
	 * ThermalElement
	 * 
	 * @param materialName
	 * @param mass
	 */
	public FluidElement(String materialName, double mass){
		this.material  = new Material(materialName);
		this.mass      = new DynamicState("Mass", Unit.KG);
		this.massInput = new ShiftProperty<Double>(0.0);
	}
	
	/**
	 * ThermalElement
	 * 
	 * @param material
	 * @param mass
	 */
	public FluidElement(Material material, double mass){
		this.material  = material;
		this.mass      = new DynamicState("Mass", Unit.KG);
		this.massInput = new ShiftProperty<Double>(0.0);
	}
	
	/**
	 * Set the mass input [kg/s]
	 * @param massInput 
	 */
	public void setMassInput(double massInput){
		this.massInput.set(massInput);
	}
	
	/**
	 * Add to current mass input [kg/s]
	 * @param massInput
	 */
	public void addMassInput(double massInput){
		this.massInput.set(this.massInput.getCurrent()+massInput);
	}
	
	/**
	 * Perform an integration step of length timestep [s]
	 * @param timestep
	 */
	public void integrate(double timestep){
		this.mass.setTimestep(timestep);
		/* Integration step:
		 * m(k+1) [kg] = m(k) [kg]+ SampleTime[s]/2*(m_in(k) [kg/s]+m_in(k+1) [ks/s])
		 */	
		this.mass.addValue((this.massInput.getCurrent()+this.massInput.getLast())/2*timestep);
		
		// Shift Mass inputs
		//this.lastHeatInput = this.heatInput;
		//this.heatInput     = 0;
		this.massInput.update(0.0);
	}
	
	/**
	 * Returns the mass state
	 * @return {@link DynamicState} mass
	 */
	public DynamicState getMass(){
		return mass;
	}
	
	/**
	 * Returns the volume
	 * @param T [K]
	 * @return V [m3]
	 */
	public double getVolume(double T){
		return mass.getValue()/material.getDensity(T);
	}

	public Material getMaterial() {
		return this.material;
	}

	public void setMaterial(String type) {
		this.material.setMaterial(type);
	}
	
	public void setMaterial(Material material){
		this.material = material;
	}

}
