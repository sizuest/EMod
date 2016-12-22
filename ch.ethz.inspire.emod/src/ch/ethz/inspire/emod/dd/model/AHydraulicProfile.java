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

import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.parameters.ParameterSet;
import ch.ethz.inspire.emod.model.parameters.Parameterizable;

/**
 * Abstract calss for a hydraulic diameter
 * 
 * @author sizuest
 * 
 */
@XmlRootElement
public abstract class AHydraulicProfile implements Parameterizable, Cloneable {

	/**
	 * Constructor for unmarshaler
	 */
	public AHydraulicProfile() {
	}

	/**
	 * Clone
	 */
	@Override
	public abstract AHydraulicProfile clone();

	/**
	 * Constructor for {@link ParameterSet} input
	 * 
	 * @param ps
	 */
	public AHydraulicProfile(ParameterSet ps) {
		setParameterSet(ps);
	}

	/**
	 * Returns the configuration as string
	 * 
	 */
	@Override
	public abstract String toString();

	/**
	 * Returns the profile height
	 * 
	 * @return [m]
	 */
	public abstract double getHeight();

	/**
	 * Returns the profile width
	 * 
	 * @return [m]
	 */
	public abstract double getWidth();

	/**
	 * Returns the cross sectional area A
	 * 
	 * @return [m^2]
	 */
	public abstract double getArea();

	/**
	 * Returns the wetted perimeter U
	 * 
	 * @return [m]
	 */
	public abstract double getPerimeter();

	/**
	 * Returns the hydraulic diameter defined by Dh = 4 A/U
	 * 
	 * @return [m]
	 */
	public double getDiameter() {
		return Fluid.hydraulicDiameter(this);
	}

}
