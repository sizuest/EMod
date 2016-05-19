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
import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.ParameterSet;

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
		
		ps.setParameter("PressureLossCoefficient", this.zeta, new SiUnit("Pa s^2 m^-4"));
		ps.setParameter("HeatTransferCoefficient", this.alpha, new SiUnit("W K^-1"));
		ps.setParameter("Surface", this.surface, new SiUnit("m^2"));
		ps.setParameter("Length", this.length, new SiUnit("m"));
		ps.setParameter("Volume", this.length, new SiUnit("m^3"));
		
		return ps;
	}

	@Override
	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.zeta = ps.getParameter("PressureLossCoefficient").getValue();
		this.alpha = ps.getParameter("HeatTransferCoefficient").getValue();
		this.surface = ps.getParameter("Surface").getValue();
		this.length = ps.getParameter("Length").getValue();
		this.volume = ps.getParameter("Volume").getValue();
		
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

}
