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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.ParameterSet;

/**
 * Implements the hydrodynamic properties of a pipe
 * @author sizuest
 *
 */
@XmlRootElement
public class DuctPipe extends ADuctElement {
	@XmlElement
	private double roughness;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 */
	public DuctPipe() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		//todo
	}
	
	/**
	 * Constructor by name
	 * @param name
	 */
	public DuctPipe(String name){
		super();
		this.name     = name;
	}
	
	/**
	 * Constructor
	 * 
	 * @param name
	 * @param length 
	 * @param diameter 
	 */
	public DuctPipe(double length, double diameter, double roughness){
		super();
		this.length    = length;
		this.profile   = new HPCircular(diameter/2);
		this.roughness = roughness;
	}
		
	/**
	 * Constructor for testing
	 * 
	 * @param name
	 * @param length 
	 * @param diameter 
	 */
	public DuctPipe(String name, double length, AHydraulicProfile diameter){
		super();
		this.name     = name;
		this.profile = diameter;
	}

	@Override
	public double getHTC(double flowRate, double pressure, double temperatureFluid, double temperatureWall) {
		return Fluid.convectionForcedPipe(this.material, temperatureWall, temperatureFluid, this.length, this.profile, flowRate);
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure, double temperatureFluid) {
		return Fluid.pressureLossFrictionPipe(material, temperatureFluid, this.length, getDiameter(), flowRate, this.roughness);

	}
	
	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet(this.name);
		
		ps.setParameter("Wall Roughness", this.roughness/1000, new SiUnit("m"));
		return ps;
	}

	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.roughness = ps.getParameter("Wall Roughness").getValue()*1000;
	}

}
