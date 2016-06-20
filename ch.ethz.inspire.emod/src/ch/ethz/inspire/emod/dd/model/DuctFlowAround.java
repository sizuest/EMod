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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.ParameterSet;

/**
 * Implements the hydrodynamic properties of a circular flow around
 * @author sizuest
 *
 */
@XmlRootElement
public class DuctFlowAround extends ADuctElement{
	@XmlElement
	double radius;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 */
	public DuctFlowAround() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Constructor by name 
	 * 
	 * @param name
	 */
	public DuctFlowAround(String name){
		super();
		this.name     = name;
	}
	
	/**
	 * constructor for testing
	 * 
	 * @param name 
	 * @param r 
	 * @param h  
	 * @param d
	 * @param profile
	 */
	public DuctFlowAround(String name, double r, double h, AHydraulicProfile profile){
		this.name     = name;
		this.radius   = r;
		this.profile  = profile;
		init();
	}
	
	/**
	 * Initializes the element
	 */
	private void init(){		
		this.length = Math.PI*this.radius;
	}
	
	@Override
	public double getSurface(){
		return 2*super.getSurface();
	}
	
	@Override
	public double getHydraulicSurface(){
		return 2*super.getHydraulicSurface();
	}
	
	@Override
	public double getVolume(){
		return 2*super.getVolume();
	}

	@Override
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {
		return Fluid.convectionForcedCoil(getMaterial(), temperatureWall, temperatureFluid, profile, radius*2, 0, flowRate/2);
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure,
			double temperatureFluid) {		
		return Fluid.pressureLossFrictionCoil(getMaterial(), temperatureFluid, length, profile, 2*radius, 0, flowRate/2);
	}
	
	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet(this.name);
		ps.setParameter("Radius", this.radius, new SiUnit("m"));
		return ps;
	}

	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.radius   = ps.getParameter("Radius").getValue();
	}
	
	@Override
	public DuctFlowAround clone() {
		DuctFlowAround clone = new DuctFlowAround();
		
		clone.setParameterSet(this.getParameterSet());
		if(null==this.isolation)
			clone.setIsolation(null);
		else
			clone.setIsolation(this.isolation.clone());
		clone.setName(this.getName());
		return clone;
	}

}
