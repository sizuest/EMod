/** $Id$
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

public class FECIdeal extends AFluidElementCharacteristic{

	@Override
	public double getA0(double flowRate, double pressureIn, double pressureOut) {
		return 0;
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
