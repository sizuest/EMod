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

package ch.ethz.inspire.emod;

import ch.ethz.inspire.emod.model.IOContainer;

/**
 * contains information on simulation input sources and targets
 * through references to IOContainers of MachineComponents and
 * SimulationControls.
 * 
 * @author dhampl
 *
 */
public class IOConnection {

	private IOContainer source;
	private IOContainer target;
		
	/**
	 * 
	 * @param source
	 * @param target 
	 * @throws Exception thrown if units don't match
	 */
	public IOConnection(IOContainer source, IOContainer target) throws Exception {
		this.source = source;
		this.target = target;
		if(source.getUnit()!=target.getUnit())
			throw new Exception("units do not match "+source.getName()+
					": "+source.getUnit()+" <-> "+target.getName()+": "+
					target.getUnit());
	}
	
	public IOContainer getSoure() {
		return source;
	}
	
	public IOContainer getTarget() {
		return target;
	}
}
