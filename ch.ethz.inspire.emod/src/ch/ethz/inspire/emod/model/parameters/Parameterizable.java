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
 * Interface for a parametrizable (physical parameters!) class
 * 
 * @author sizuest
 * 
 */

public interface Parameterizable {

	/**
	 * Return the parameter set
	 * 
	 * @return {@link ParameterSet}
	 */

	public ParameterSet getParameterSet();

	/**
	 * Write the parameter set given as ps
	 * 
	 * @param ps {@link ParameterSet}
	 */

	public void setParameterSet(ParameterSet ps);

}
