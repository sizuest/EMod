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

/**
 * ThermalArrayStatus
 * Preserves the property of the thermal array at a specific time step
 * @author sizuest
 * @param <T> Type of the property
 *
 */
public class ShiftProperty<T> {
	private T last, current;
	
	/**
	 * Creates a new object with current and last property value equal to the argument
	 * @param value
	 */
	public ShiftProperty(T value){
		this.last    = value;
		this.current = value;
	}
	
	/**
	 * Sets the current value to the argument value
	 * @param value
	 */
	public void set(T value){
		this.current = value;
	}
	
	/**
	 * Shifts the current value to the last value field
	 */
	public void shift(){
		this.last    = this.current;
	}
	
	/**
	 * Shifts the current value to the last field and sets the current value equal to the arguement
	 * @param value
	 */
	public void update(T value){
		// Shift
		shift();
		// Update
		set(value);
	}
	
	/**
	 * Returns the current value
	 * @return Current value
	 */
	public T getCurrent(){
		return current;
	}
	
	/**
	 * Returns the last value
	 * @return Last value
	 */
	public T getLast(){
		return last;
	}
}
