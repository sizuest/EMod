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
import ch.ethz.inspire.emod.model.parameters.ParameterSet;
import ch.ethz.inspire.emod.model.units.SiUnit;

/**
 * Implements the hydrodynamic properties of a drill hole
 * @author sizuest
 *
 */
@XmlRootElement
public class DuctDrilling extends ADuctElement {
	@XmlElement
	private double length;
	@XmlElement
	private double count;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 */
	public DuctDrilling() {
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
	 * @param name
	 */
	public DuctDrilling(String name){
		super();
		this.name     = name;
	}
	
	/**
	 * Constructor for Testing
	 * 
	 * @param name 
	 * @param d 
	 * @param l 
	 * @param c 
	 */
	public DuctDrilling(String name, double d, double l, double c){
		this.name          = name;
		this.profile       = new HPCircular(d/2);
		this.length		   = l;
		this.count         = c;
		init();
	}
	
	/**
	 * Initializes the elemtn
	 */
	private void init(){
		super.length = this.length;
	}
	
	@Override
	public double getSurface(){
		return this.count*super.getSurface();
	}
	
	@Override
	public double getHydraulicSurface(){
		return this.count*super.getHydraulicSurface();
	}
	
	@Override
	public double getVolume(){
		return this.count*super.getVolume();
	}

	@Override
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {
		return Fluid.convectionForcedPipe(material, temperatureFluid, temperatureWall, length, this.profile, flowRate/this.count);
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure,
			double temperatureFluid) {
		return Fluid.pressureLossFrictionPipe(getMaterial(), temperatureFluid, length, getDiameter(), flowRate/this.count, .5E-3);
	}

	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet(this.name);
		ps.setPhysicalValue("Length", this.length, new SiUnit("m"));
		ps.setPhysicalValue("Count", this.count, new SiUnit(""));
		return ps;
	}

	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.length        = ps.getPhysicalValue("Length").getValue();
		super.length       = this.length;
		this.count         = ps.getPhysicalValue("Count").getValue();
	}
	
	@Override
	public DuctDrilling clone() {
		DuctDrilling clone = new DuctDrilling();
		
		clone.setParameterSet(this.getParameterSet());
		if(null==this.isolation)
			clone.setIsolation(null);
		else
			clone.setIsolation(this.isolation.clone());
		clone.setName(this.getName());
		
		clone.setProfile(getProfile().clone());
		
		return clone;
	}

}
