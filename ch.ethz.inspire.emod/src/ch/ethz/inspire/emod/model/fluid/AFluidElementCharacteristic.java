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
package ch.ethz.inspire.emod.model.fluid;

/**
 * Abstract class for general fluid element characteristics
 * 
 * A fluid element is assumed to have the following characteristics: pin-pout =
 * [ep -ep] [pin pout]' = f(V) where f(V) is f(V) = a0(V0) + V*a1(V0) + O(V²)
 * 
 * @author simon
 * 
 */

public abstract class AFluidElementCharacteristic {

	/**
	 * Returns the 0th order coefficient of f(V)
	 * 
	 * @param flowRate
	 * @param pressureIn
	 * @param pressureOut
	 * @return
	 */
	public abstract double getA0(double flowRate, double pressureIn,
			double pressureOut);

	/**
	 * Returns the 1st order coefficient of f(V)
	 * 
	 * @param flowRate
	 * @param pressureIn
	 * @param pressureOut
	 * @return
	 */
	public abstract double getA1(double flowRate, double pressureIn,
			double pressureOut);

	/**
	 * Returns the elements of the pressure difference matrix
	 * 
	 * @param flowRate
	 * @param pressureIn
	 * @param pressureOut
	 * @return
	 */
	public abstract double getEp(double flowRate, double pressureIn,
			double pressureOut);
}
