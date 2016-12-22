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
package ch.ethz.inspire.emod.femexport;

import ch.ethz.inspire.emod.model.units.SiUnit;

/**
 * Generic implementation of a boundary condition
 * @author simon
 *
 */
public class BoundaryCondition {

	private String name;
	private SiUnit unit;
	private double value;

	private BoundaryConditionType type;

	/**
	 * New boundary condition 
	 * @param name
	 * @param unit
	 * @param value
	 * @param type
	 */
	public BoundaryCondition(String name, SiUnit unit, double value,
			BoundaryConditionType type) {
		this.name = name;
		this.unit = unit;
		this.value = value;
		this.type = type;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the unit
	 */
	public SiUnit getUnit() {
		return unit;
	}

	/**
	 * @return the type
	 */
	public BoundaryConditionType getType() {
		return type;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
