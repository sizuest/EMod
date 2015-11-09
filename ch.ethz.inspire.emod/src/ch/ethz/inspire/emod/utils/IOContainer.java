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
package ch.ethz.inspire.emod.utils;

import ch.ethz.inspire.emod.model.units.*;

/**
 * Container for all model i/o
 * 
 * @author dhampl
 *
 */ 
public class IOContainer {

	private String name;
	private SiUnit unit;
	
	private double value;
	
	private ContainerType type;
	
	/**
	 * @param name
	 * @param unit
	 * @param value
	 */
	public IOContainer(String name, SiUnit unit, double value) {
		super();
		this.name  = name;
		this.unit  = unit;
		this.value = value;
		this.type  = ContainerType.NONE;
	}
	/**
	 * @param name
	 * @param unit
	 * @param value
	 * @param type
	 */
	public IOContainer(String name, SiUnit unit, double value, ContainerType type) {
		super();
		this.name  = name;
		this.unit  = unit;
		this.value = value;
		this.type  = type;
	}	
	
	public IOContainer() {
		
	}
	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
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
	public ContainerType getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(ContainerType type){
		this.type = type;
	}	
	public String toString() {
		return name + " " + value + " " + unit + " " + type;
	}
}