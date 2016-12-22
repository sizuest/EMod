/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2015 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.utils;

import java.util.ArrayList;

import ch.ethz.inspire.emod.model.fluid.FluidCircuitProperties;

/**
 * Interface to define common methods of Floodable Machine Components, such as:
 * - Pipe, Tank, Pump
 * 
 * Usage: - in ch.ethz.inspire.emod.utils.FluidCircuit to flood components with
 * fluid - in Machine class to get all the Floodable components
 * 
 * @author manick
 * 
 */
public interface Floodable {

	/**
	 * method to get the fluidtype of a floodable component needs to be
	 * overriden by the component
	 * 
	 * @return fluidType
	 */
	public ArrayList<FluidCircuitProperties> getFluidPropertiesList();

	/**
	 * Method to be called pre-simulation
	 */
	public void flood();

}
