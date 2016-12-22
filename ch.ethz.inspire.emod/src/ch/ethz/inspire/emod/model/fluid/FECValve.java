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

import ch.ethz.inspire.emod.model.Valve;

/**
 * Element with zeta according to valve model
 * @author sizuest
 *
 */
public class FECValve extends AFluidElementCharacteristic {

	Valve valve;

	/**
	 * @param valve
	 */
	public FECValve(Valve valve) {
		this.valve = valve;
	}

	@Override
	public double getA0(double flowRate, double pressureIn, double pressureOut) {
		if (valve.isClosed())
			return pressureIn - pressureOut;

		return -valve.getPressure(flowRate);
	}

	@Override
	public double getA1(double flowRate, double pressureIn, double pressureOut) {
		if (valve.isClosed())
			return 1e24;

		return 2 * valve.getPressureLossCoefficient() * flowRate
				* Fluid.sign(flowRate);
	}

	@Override
	public double getEp(double flowRate, double pressureIn, double pressureOut) {
		return 1;
	}

}
