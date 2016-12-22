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
package ch.ethz.inspire.emod.model.parameters;

/**
 * Implements a parameter with a unit and a describing comment
 * @author sizuest
 *
 * @param <T>
 */
public class Parameter<T> {
	private String name;
	private T value;
	private String comment;

	/**
	 * @param name
	 * @param value
	 */
	public Parameter(String name, T value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Get the value
	 * @return
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Set the value
	 * @param value
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * Get the description
	 * @return
	 */
	public String getComment() {
		return this.comment;
	}

	/**
	 * Set the description
	 * @param comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Get the name
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
}
