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

package ch.ethz.inspire.emod.simulation;

/**
 * States of machine components. A component implements a subset of the defined
 * states only.
 * 
 * @author andreas
 * 
 */
public enum ComponentState {
	/**
	 * Component is turned on
	 */
	ON,
	/**
	 * Component is turned off
	 */
	OFF, 
	/**
	 * Component is in standby
	 */
	STANDBY, 
	/**
	 * Component is ready
	 */
	READY, 
	/**
	 * Component is in periodic operation
	 */
	PERIODIC, 
	/**
	 * Component is in controlled operation
	 */
	CONTROLLED;
}
