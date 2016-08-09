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
import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.parameters.ParameterSet;
import ch.ethz.inspire.emod.model.units.SiUnit;

public class DuctDefinedValues  extends ADuctElement {
	@XmlElement
	double zeta;
	@XmlElement
	double alpha;
	@XmlElement
	double surface;
	@XmlElement
	double length;
	@XmlElement
	double volume;
	
	public DuctDefinedValues(){
		super();
	}
	
	public DuctDefinedValues(String name){
		super();
		this.name     = name;
	}
	
	
	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet();
		
		ps.setPhysicalValue("PressureLossCoefficient", this.zeta, new SiUnit("Pa s^2 m^-4"));
		ps.setPhysicalValue("HeatTransferCoefficient", this.alpha, new SiUnit("W K^-1"));
		ps.setPhysicalValue("Surface", this.surface, new SiUnit("m^2"));
		ps.setPhysicalValue("Length", this.length, new SiUnit("m"));
		ps.setPhysicalValue("Volume", this.length, new SiUnit("m^3"));
		
		return ps;
	}

	@Override
	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.zeta    = ps.getPhysicalValue("PressureLossCoefficient").getValue();
		this.alpha   = ps.getPhysicalValue("HeatTransferCoefficient").getValue();
		this.surface = ps.getPhysicalValue("Surface").getValue();
		this.length  = ps.getPhysicalValue("Length").getValue();
		this.volume  = ps.getPhysicalValue("Volume").getValue();
		
		super.length = this.length;
	}

	@Override
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {
		
		return alpha/getSurface();
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure,
			double temperatureFluid) {
		return zeta*Math.pow(flowRate,2)*Fluid.sign(flowRate);
	}
	
	@Override
	public double getSurface() {
		return surface;
	}
	
	@Override
	public double getVolume() {
		return volume;
	}
	
	@Override
	public double getHydraulicSurface() {
		return getSurface();
	}

	@Override
	public DuctDefinedValues clone() {
		DuctDefinedValues clone = new DuctDefinedValues();
		
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
