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

import ch.ethz.inspire.emod.model.parameters.ParameterSet;
import ch.ethz.inspire.emod.model.units.SiUnit;

/**
 * Implements the hydraulic properties of a circular profile
 * 
 * @author sizuest
 * 
 */
@XmlRootElement
public class HPCircular extends AHydraulicProfile {
	@XmlElement
	private double radius = .01;

	/**
	 * 
	 */
	public HPCircular() {
	}

	/**
	 * @param radius
	 */
	public HPCircular(double radius) {
		this.radius = radius;
	}

	@Override
	public double getArea() {
		return Math.pow(radius, 2) * Math.PI;
	}

	@Override
	public double getPerimeter() {
		return 2 * Math.PI * radius;
	}

	@Override
	public double getHeight() {
		return 2 * radius;
	}

	@Override
	public double getWidth() {
		return 2 * radius;
	}

	@Override
	public String toString() {
		return "R=" + radius;
	}

	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet("Circular");

		ps.setPhysicalValue("Radius", this.radius, new SiUnit("m"));
		return ps;
	}

	@Override
	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.radius = ps.getPhysicalValue("Radius").getValue();

	}

	@Override
	public AHydraulicProfile clone() {
		return new HPCircular(this.radius);
	}

}
