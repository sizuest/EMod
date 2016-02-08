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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.utils.Parameterizable;

/**
 * Implements the abstract class for a hydraulic duct element
 * @author simon
 *
 */
@XmlRootElement
public abstract class ADuctElement implements Parameterizable {
	
	protected String name;
	@XmlTransient
	protected Material material;
	protected AHydraulicProfile profile = new HPCircular(.01);
	protected double length;
	protected Isolation isolation = null;
	
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
	 * @param flowRate			[kg/s]
	 * @param pressureIn		[Pa]
	 * @param temperatureFluid	[K]
	 * @param temperatureWall	[K]
	 * @return [Pa]
	 */
	public double getPressureOut(double flowRate, double pressureIn, double temperatureFluid){
		return pressureIn - getPressureDrop(flowRate, pressureIn, temperatureFluid);
	}
	
	/**
	 * Returns the heat transfer coefficient for the given operational condition
	 * 
	 * @param flowRate			[kg/s]
	 * @param pressure 			[Pa]
	 * @param temperatureFluid	[K]
	 * @param temperatureWall	[K]
	 * @return [W/K/m²]
	 */
	public abstract double getHTC(double flowRate, double pressure, double temperatureFluid, double temperatureWall);
	
	/**
	 * Returns the heat pressure drop for the given operational condition
	 * 
	 * @param flowRate			[kg/s]
	 * @param pressure 			[Pa]
	 * @param temperatureFluid	[K]
	 * @param temperatureWall	[K]
	 * @return [Pa]
	 */
	public abstract double getPressureDrop(double flowRate, double pressure, double temperatureFluid);

	/**
	 * Returns the hydraulic profile object
	 * 
	 * @return {@link AHydraulicProfile.java}
	 */
	public AHydraulicProfile getProfile() {
		return this.profile;
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
		return this.isolation;
	}

}
