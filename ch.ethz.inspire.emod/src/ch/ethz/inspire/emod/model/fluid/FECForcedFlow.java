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
 * Element with fixed flow rate
 * @author sizuest
 *
 */
public class FECForcedFlow extends AFluidElementCharacteristic {

	IOContainer flowRate;

	/**
	 * @param flowRate
	 */
	public FECForcedFlow(IOContainer flowRate) {
		this.flowRate = flowRate;
	}

	@Override
	public double getA0(double flowRate, double pressureIn, double pressureOut) {
		// TODO Auto-generated method stub
		return -this.flowRate.getValue();
	}

	@Override
	public double getA1(double flowRate, double pressureIn, double pressureOut) {
		return 1;
	}

	@Override
	public double getEp(double flowRate, double pressureIn, double pressureOut) {
		return 0;
	}

}
