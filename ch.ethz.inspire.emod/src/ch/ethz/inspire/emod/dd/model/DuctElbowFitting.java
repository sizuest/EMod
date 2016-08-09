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
import ch.ethz.inspire.emod.model.parameters.ParameterSet;
import ch.ethz.inspire.emod.model.units.SiUnit;

/**
 * Implements the hydrodynamic properties of a elbow fitting
 * @author sizuest
 *
 */
@XmlRootElement
public class DuctElbowFitting extends ADuctElement{
	
	@XmlElement
	double count=1;
	
	/**
	 * Constructor for XmlUnmarshaller
	 */
	public DuctElbowFitting(){}
	
	public DuctElbowFitting(String name){
		this.name = name;
	}

	@Override
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {
		return this.profile.getDiameter()*Math.PI/2;
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure,
			double temperatureFluid) {
		return Fluid.pressureLoss90Angle(getMaterial(), temperatureFluid, getProfileIn(), flowRate/count);
	}

	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet();
		ps.setPhysicalValue("Count", this.count, new SiUnit());
		return ps;
	}

	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.count = ps.getPhysicalValue("Count").getValue();
	}

	@Override
	public DuctElbowFitting clone() {
		DuctElbowFitting clone = new DuctElbowFitting();
		
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
