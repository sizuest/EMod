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
 * All energy related machine states. Usually, a machine implements a subset of
 * the defined states only.
 * 
 * @author dhampl
 * 
 */
public enum MachineState {
	/**
	 * Machine is on
	 */
	ON,
	/**
	 * Machine is off
	 */
	OFF, 
	/**
	 * Machine is in standby (low ready)
	 */
	STANDBY, 
	/**
	 * Machine is ready for processing (high ready)
	 */
	READY, 
	/**
	 * Machine is processing
	 */
	PROCESS;
}
