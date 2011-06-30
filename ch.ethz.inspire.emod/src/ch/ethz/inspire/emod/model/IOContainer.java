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
package ch.ethz.inspire.emod.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType; 
import javax.xml.bind.annotation.XmlAccessorType;  

import ch.ethz.inspire.emod.model.units.Unit;

/**
 * Container for all model i/o
 * 
 * @author dhampl
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)  
public class IOContainer {

	private String name;
	private Unit unit;
	private double value;
	/**
	 * @param name
	 * @param unit
	 * @param value
	 */
	public IOContainer(String name, Unit unit, double value) {
		super();
		this.name = name;
		this.unit = unit;
		this.value = value;
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
	public Unit getUnit() {
		return unit;
	}
	
	public String toString() {
		return name+" "+value+unit;
	}
}
