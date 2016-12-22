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
 * Implements the hydraulic properties of a rectangular profile
 * 
 * @author sizuest
 * 
 */
@XmlRootElement
public class HPRectangular extends AHydraulicProfile {
	@XmlElement
	private double height;
	@XmlElement
	private double width;

	/**
	 * 
	 */
	public HPRectangular() {
	}

	/**
	 * @param l
	 * @param b
	 */
	public HPRectangular(double l, double b) {
		this.height = l;
		this.width = b;
	}

	@Override
	public double getArea() {
		return height * width;
	}

	@Override
	public double getPerimeter() {
		return 2 * (height + width);
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public String toString() {
		return height + "x" + width;
	}

	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet("Rectangular");
		ps.setPhysicalValue("Height", this.height, new SiUnit("m"));
		ps.setPhysicalValue("Width", this.width, new SiUnit("m"));
		return ps;
	}

	@Override
	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.height = ps.getPhysicalValue("Height").getValue();
		this.width = ps.getPhysicalValue("Width").getValue();
	}

	@Override
	public AHydraulicProfile clone() {
		return new HPRectangular(this.height, this.width);
	}

}
