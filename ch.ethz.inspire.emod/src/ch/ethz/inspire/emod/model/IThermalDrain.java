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

/**
 * Thermal model interface for drains
 * 
 * @author dhampl
 *
 */
public interface IThermalDrain {

	public void drainHeat(float val);
	
}
