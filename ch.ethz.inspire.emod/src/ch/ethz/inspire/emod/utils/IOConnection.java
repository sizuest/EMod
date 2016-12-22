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

import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * contains information on simulation input sources and targets through
 * references to IOContainers of MachineComponents and SimulationControls.
 * 
 * @author dhampl
 * 
 */
public class IOConnection {
	protected IOContainer source;
	protected IOContainer target;

	/**
	 * IOConnection
	 */
	public IOConnection() {

	}

	/**
	 * 
	 * @param source
	 * @param target
	 * @throws Exception
	 *             thrown if units don't match
	 */
	public IOConnection(IOContainer source, IOContainer target)
			throws Exception {
		this.source = source;
		this.target = target;

		if (!source.getUnit().equals(target.getUnit()))
			throw new Exception("units do not match " + source.getName() + ": "
					+ source.getUnit() + " <-> " + target.getName() + ": "
					+ target.getUnit());
	}

	/**
	 * gets the Source IOContainer of the Connection
	 * 
	 * @return the Source
	 */
	public IOContainer getSource() {
		return source;
	}

	/**
	 * gets the Target IOContainer of the Connection
	 * 
	 * @return the Target
	 */
	public IOContainer getTarget() {
		return target;
	}

	/**
	 * update the connection, i.e. get the value of the source and write it to
	 * the target
	 */
	public void update() {
		this.getTarget().setValue(this.getSource().getValue());
	}
}
