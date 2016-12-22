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
package ch.ethz.inspire.emod.model.material;

/**
 * Implements a general material property
 * 
 * @author simon
 * 
 */
public abstract class AMaterialProperty {

	private String name;

	/**
	 * @param name
	 */
	public AMaterialProperty(String name) {
		this.name = name;
	}

	/**
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public abstract String toString();

}
