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
//public class IOContainer<T> {

	private String name;
	private Unit unit;
	
	//TODO manick: create IOConnection and IOContainer with fluid!
	private double value;
	//private T value;
	
	private ContainerType type;
	
	/**
	 * @param name
	 * @param unit
	 * @param value
	 */
	public IOContainer(String name, Unit unit, double value) {
	//public IOContainer(String name, Unit unit, T value) {
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
	public IOContainer(String name, Unit unit, double value, ContainerType type) {
	//public IOContainer(String name, Unit unit, T value, ContainerType type) {
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
	//public T getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
	//public void setValue(T value) {
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