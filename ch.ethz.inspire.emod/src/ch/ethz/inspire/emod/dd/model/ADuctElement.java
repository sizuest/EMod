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

package ch.ethz.inspire.emod.dd.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.fluid.Isolation;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.parameters.Parameterizable;

/**
 * Implements the abstract class for a hydraulic duct element
 * @author simon
 *
 */
@XmlRootElement
public abstract class ADuctElement implements Parameterizable, Cloneable{
	
	protected String name;
	@XmlTransient
	protected Material material;
	protected AHydraulicProfile profile = new HPCircular(.01);
	protected double length;
	protected Isolation isolation = null;
	@XmlElement
	protected double heatSource = Double.NaN;
	@XmlElement
	protected double wallTemperature = Double.NaN;
	
	/**
	 * Sets the current fluid material
	 * 
	 * @param material {@link Material.java}
	 */
	@XmlTransient	
	public void setMaterial(Material material){
		this.material = material;
	}
	
	/**
	 * Returns the current material.
	 * 
	 * @return {@link Material.java}
	 */
	public Material getMaterial(){
		return this.material;
	}
	
	/**
	 * Returns the element name
	 * 
	 * @return element name
	 */
	public String getName(){
		return this.name;
	}
	
	/** 
	 * Sets the element name
	 * 
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Returns the (hydraulic) diameter
	 * 
	 * @return [m]
	 */
	public double getDiameter(){
		return this.profile.getDiameter();
	}
	
	/**
	 * Returns the length
	 * 
	 * @return [m]
	 */
	public double getLength(){
		return this.length;
	}
	
	/**
	 * Returns the free surface
	 * 
	 * @return [m²]
	 */
	public double getSurface(){
		return this.length*profile.getPerimeter();
	}
	
	/**
	 * Returns the hydraulic surface
	 * 
	 * @return [m²]
	 */
	public double getHydraulicSurface() {
		return this.length*Fluid.hydraulicPerimeter(this.profile);
	}
	
	/**
	 * Returns the total volume
	 * 
	 * @return [m³]
	 */
	public double getVolume() {
		return this.length*profile.getArea();
	}
	
	/**
	 * Sets the isolation
	 * 
	 * @param isolation {@link Isolation}
	 */
	public void setIsolation(Isolation isolation){
		this.isolation = isolation;
	}
	
	/**
	 * Returns the outlet pressure for the given operational condition
	 * @param flowRate			[m³/s]
	 * @param pressureIn		[Pa]
	 * @param temperatureFluid	[K]
	 * @param temperatureWall	[K]
	 * @return [Pa]
	 */
	public double getPressureOut(double flowRate, double pressureIn, double temperatureFluid){
		return pressureIn - Math.signum(flowRate)*getPressureDrop(Math.abs(flowRate), pressureIn, temperatureFluid);
	}
	
	/**
	 * Returns the heat transfer coefficient for the given operational condition
	 * 
	 * @param flowRate			[m³/s]
	 * @param pressure 			[Pa]
	 * @param temperatureFluid	[K]
	 * @param temperatureWall	[K]
	 * @return [W/K/m²]
	 */
	protected abstract double getHTC(double flowRate, double pressure, double temperatureFluid, double temperatureWall);
	
	public double getHTC(double flowRate, double pressure, double temperatureFluid){
		return getHTC(flowRate, pressure, temperatureFluid, this.getWallTemperature(temperatureFluid, flowRate, pressure));
	}
	
	/**
	 * Returns the heat pressure drop for the given operational condition
	 * 
	 * @param flowRate			[m³/s]
	 * @param pressure 			[Pa]
	 * @param temperatureFluid	[K]
	 * @param temperatureWall	[K]
	 * @return [Pa]
	 */
	public abstract double getPressureDrop(double flowRate, double pressure, double temperatureFluid);
	
	/**
	 * Returns the heat pressure drop for the given operational condition
	 * 
	 * @param flowRate			[m³/s]
	 * @param pressure 			[Pa]
	 * @param temperatureFluid	[K]
	 * @param temperatureWall	[K]
	 * @return [Pa]
	 */
	public double getPressureLossCoefficient(double flowRate, double pressure, double temperatureFluid){
		if(0==flowRate)
			return Double.NaN;
		
		return getPressureDrop(flowRate, pressure, temperatureFluid)/Math.pow(flowRate, 2)*Math.signum(flowRate);
	}
	
	/**
	 * Returns the hydraulic profile object
	 * 
	 * @return {@link AHydraulicProfile.java}
	 */
	public AHydraulicProfile getProfile() {
		return this.profile;
	}

	/**
	 * Returns the hydraulic profile object
	 * 
	 * @return {@link AHydraulicProfile.java}
	 */
	public AHydraulicProfile getProfileIn() {
		return this.profile;
	}
	
	/**
	 * Returns the hydraulic profile object
	 * 
	 * @return {@link AHydraulicProfile.java}
	 */
	public AHydraulicProfile getProfileOut() {
		return this.profile;
	}
	
	public boolean hasIsolation(){
		if(this.isolation == null)
			return false;
		if(this.isolation.getThickness() == 0)
			return false;
		else
			return true;
	}
	

	/**
	 * Returns the wall temperature
	 * @param temperatureIn 
	 * @param flowRate 
	 * @param pressure 
	 * @return 
	 */
	public double getWallTemperature(double temperatureIn, double flowRate, double pressure){
		double T = Double.NaN;
		
		// Simplest case: No heat transfer
		if(Double.isNaN(this.heatSource) & Double.isNaN(this.wallTemperature))
			T = temperatureIn;
		
		// Wall temperature
		if(Double.isNaN(this.heatSource) & !Double.isNaN(this.wallTemperature))
			T = this.wallTemperature;
			
		// Wall heat flux
		if(!Double.isNaN(this.heatSource) & Double.isNaN(this.wallTemperature)){
			int i=0;
			double Tlast;
			T = 293;
			do {
				Tlast = T;
				T = (temperatureIn + this.heatSource/2/getMaterial().getHeatCapacity()/flowRate/getMaterial().getDensity(temperatureIn)) + this.heatSource/getHTC(flowRate, pressure, temperatureIn, Tlast)/getSurface();
				i++;
			} while (Math.abs(T/Tlast)>1E-3 & i<10);
		}
		
		return T;
	}
	
	/**
	 * Returns the outlet temperature
	 * @param temperatureIn 
	 * @param flowRate 
	 * @param pressure 
	 * @return 
	 */
	public double getTemperatureOut(double temperatureIn, double flowRate, double pressure){
		return getTemperature(temperatureIn, flowRate, pressure, getLength());
	}
	
	/**
	 * Returns the wall heat flux
	 * @param temperatureIn 
	 * @param flowRate 
	 * @param pressure 
	 * @return 
	 * 
	 */
	public double getWallHeatFlux(double temperatureIn, double flowRate, double pressure){
		double Qdot;
		
		// Simplest case: No heat transfer
		if(Double.isNaN(this.heatSource) & Double.isNaN(this.wallTemperature))
			Qdot = 0;
		
		// Wall temperature
		if(Double.isNaN(this.heatSource) & !Double.isNaN(this.wallTemperature))
			Qdot = (getTemperatureOut(temperatureIn, flowRate, pressure)-temperatureIn)*flowRate*getMaterial().getDensity(temperatureIn)*getMaterial().getHeatCapacity();
			
		// Wall heat flux
		if(!Double.isNaN(this.heatSource) & Double.isNaN(this.wallTemperature))
			Qdot =this.heatSource;
		
		else
			Qdot = Double.NaN;
		
		return Qdot;
	}
	
	/**
	 * Returns the temperature at position x
	 * @param temperatureIn 
	 * @param flowRate 
	 * @param pressure 
	 * @param x 
	 * @return 
	 */
	public double getTemperature(double temperatureIn, double flowRate, double pressure, double x){
		
		double T = Double.NaN;
		
		// Simplest case: No heat transfer
		if(Double.isNaN(this.heatSource) & Double.isNaN(this.wallTemperature))
			T = temperatureIn;
		
		// Wall temperature
		if(Double.isNaN(this.heatSource) & !Double.isNaN(this.wallTemperature))
			T = this.wallTemperature + Math.exp(-getProfile().getPerimeter()*x*getHTC(flowRate, pressure, temperatureIn, this.wallTemperature) / getMaterial().getHeatCapacity() / (flowRate*getMaterial().getDensity(temperatureIn))) * (temperatureIn-this.wallTemperature);
			
		// Wall heat flux
		if(!Double.isNaN(this.heatSource) & Double.isNaN(this.wallTemperature))
			T = temperatureIn + this.heatSource/(flowRate*getMaterial().getDensity(temperatureIn))/getLength()/getMaterial().getHeatCapacity()*x;
		
		return T;
	}
	
	/**
	 * Sets the wall heat flux
	 * and resets the wall temperature
	 * 
	 * @param heatSource [W]
	 */
	public void setHeatSource(double heatSource){
		this.heatSource = heatSource;
		this.wallTemperature = Double.NaN;
	}
	
	/**
	 * Sets the wall temperature
	 * and resets the wall heat flux
	 * 
	 * @param wallTemperature [K]
	 */
	public void setWallTemperature(double wallTemperature){
		this.wallTemperature = wallTemperature;
		this.heatSource = Double.NaN;
	}
	

	/**
	 * Sets the hydraulic profile
	 * 
	 * @param profile {@link AHydraulicProfile.java}
	 */
	@XmlElement
	public void setProfile(AHydraulicProfile profile) {
		this.profile = profile;
	}

	/**
	 * Returns the isolation object
	 * 
	 * @return {@link Isolation.java}
	 */
	@XmlElement
	public Isolation getIsolation() {
		if(this.isolation != null)
			if(this.isolation.getMaterial() == null)
				return null;
			else
				return this.isolation;
		else
			return null;
	}
	
	public abstract ADuctElement clone();

	/**
	 * Indicates if a wall temperature BC has been set
	 * @return
	 */
	public boolean hasWallTemperature() {
		return !(Double.isNaN(wallTemperature));
	}

	/**
	 * Indicates if a heat flux has been set
	 * @return
	 */
	public boolean hasHeatSource() {
		return !(Double.isNaN(heatSource));
	}

}
