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
 * Implements the hydrodynamic properties of a pipe
 * 
 * @author sizuest
 * 
 */
@XmlRootElement
public class DuctPipe extends ADuctElement {
	@XmlElement
	private double roughness;
	@XmlElement
	private double length;

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
		init();
	}

	/**
	 * Constructor by name
	 * 
	 * @param name
	 */
	public DuctPipe(String name) {
		super();
		this.name = name;
		init();
	}

	/**
	 * Constructor
	 * 
	 * @param length
	 * @param diameter
	 * @param roughness
	 */
	public DuctPipe(double length, double diameter, double roughness) {
		super();
		this.length = length;
		this.profile = new HPCircular(diameter / 2);
		this.roughness = roughness;
		init();
	}

	/**
	 * Constructor for testing
	 * 
	 * @param name
	 * @param length
	 * @param diameter
	 */
	public DuctPipe(String name, double length, AHydraulicProfile diameter) {
		super();
		this.name = name;
		this.profile = diameter;
		init();
	}

	/**
	 * Initializes the elemtn
	 */
	private void init() {
		super.length = this.length;
	}

	@Override
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {
		return Fluid.convectionForcedPipe(this.material, temperatureWall,
				temperatureFluid, this.length, this.profile, flowRate);
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure,
			double temperatureFluid) {
		return Fluid.pressureLossFrictionPipe(material, temperatureFluid,
				this.length, getDiameter(), flowRate, this.roughness);

	}

	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet(this.name);
		ps.setPhysicalValue("Length", this.length, new SiUnit("m"));
		ps.setPhysicalValue("Wall Roughness", this.roughness, new SiUnit("m"));
		return ps;
	}

	@Override
	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.length = ps.getPhysicalValue("Length").getValue();
		super.length = this.length;
		this.roughness = ps.getPhysicalValue("Wall Roughness").getValue();
	}

	@Override
	public DuctPipe clone() {
		DuctPipe clone = new DuctPipe();

		clone.setParameterSet(this.getParameterSet());
		if (null == this.isolation)
			clone.setIsolation(null);
		else
			clone.setIsolation(this.isolation.clone());
		clone.setName(this.getName());

		clone.setProfile(getProfile().clone());

		return clone;
	}

}
