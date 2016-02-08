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
 * Implements the hydrodynamic properties of a helix
 * @author sizuest
 *
 */
@XmlRootElement
public class DuctHelix extends ADuctElement{
	@XmlElement
	double radius;
	@XmlElement
	double height;
	@XmlElement
	double distance;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 */
	public DuctHelix() {
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
	public DuctHelix(String name){
		super();
		this.name     = name;
	}
	
	/**
	 * Constructor for testing
	 * 
	 * @param name 
	 * @param r 
	 * @param h 
	 * @param d
	 * @param profile
	 */
	public DuctHelix(String name, double r, double h, double d, AHydraulicProfile profile){
		this.name     = name;
		this.radius   = r;
		this.height   = h;
		this.distance = d;
		this.profile  = profile;
		init();
	}
	
	/**
	 * Initializes the element
	 */
	private void init(){
		double N;  //Number of revolutions
		double dl; // Length per revolution
		
		N  = height/distance;
		dl = Math.sqrt(Math.pow(distance, 2)+Math.pow(2*Math.PI*radius, 2));
		
		this.length = N*dl;
	}
	

	@Override
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {
		return Fluid.convectionForcedCoil(getMaterial(), temperatureWall, temperatureFluid, profile, radius*2, distance, flowRate);
		//return Fluid.convectionForcedPipe(getMaterial(), temperatureWall, temperatureFluid, getLength(), getProfile(), flowRate);
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure,
			double temperatureFluid) {		
		return Fluid.pressureLossFrictionCoil(getMaterial(), temperatureFluid, length, profile, 2*radius, distance, flowRate);
	}
	
	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet(this.name);
		
		ps.setParameter("Radius", this.radius, new SiUnit("m"));
		ps.setParameter("Height", this.height, new SiUnit("m"));
		ps.setParameter("Distance", this.distance, new SiUnit("m"));
		return ps;
	}

	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.radius   = ps.getParameter("Radius").getValue();
		this.height   = ps.getParameter("Height").getValue();
		this.distance = ps.getParameter("Distance").getValue();
	}

}
