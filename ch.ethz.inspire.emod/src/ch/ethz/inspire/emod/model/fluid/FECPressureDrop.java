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

import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * Element with fixed pressure drop
 * 
 * @author sizuest
 *
 */
public class FECPressureDrop extends AFluidElementCharacteristic {

	IOContainer pressure;

	/**
	 * @param pressure
	 */
	public FECPressureDrop(IOContainer pressure) {
		this.pressure = pressure;
	}

	@Override
	public double getA0(double flowRate, double pressureIn, double pressureOut) {
		return this.pressure.getValue();
	}

	@Override
	public double getA1(double flowRate, double pressureIn, double pressureOut) {
		return 0;
	}

	@Override
	public double getEp(double flowRate, double pressureIn, double pressureOut) {
		return 1;
	}

}
