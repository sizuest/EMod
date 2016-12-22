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

import ch.ethz.inspire.emod.utils.Algo;

/**
 * Element with zeta from lookup table
 * 
 * @author sizuest
 *
 */
public class FECZeta extends AFluidElementCharacteristic {

	double[] zetaSamples, pressureSamples;

	/**
	 * Set constant value
	 * @param zeta
	 */
	public void setZeta(double zeta) {
		this.zetaSamples = new double[] { zeta };
	}

	/**
	 * Set pressure dependent value
	 * @param zeta
	 * @param pressure
	 */
	public void setZeta(double[] zeta, double[] pressure) {
		this.zetaSamples = zeta;
		this.pressureSamples = pressure;
	}

	/**
	 * @param zeta
	 */
	public FECZeta(double zeta) {
		this.zetaSamples = new double[] { zeta };
	}

	/**
	 * @param zeta
	 * @param pressure
	 */
	public FECZeta(double[] zeta, double[] pressure) {
		this.zetaSamples = zeta;
		this.pressureSamples = pressure;
	}

	@Override
	public double getA0(double flowRate, double pressureIn, double pressureOut) {
		return -getZeta(pressureIn - pressureOut) * Math.pow(flowRate, 2)
				* Fluid.sign(flowRate);
	}

	@Override
	public double getA1(double flowRate, double pressureIn, double pressureOut) {
		return 2 * flowRate * getZeta(pressureIn - pressureOut)
				* Fluid.sign(flowRate);
	}

	@Override
	public double getEp(double flowRate, double pressureIn, double pressureOut) {
		return 1;
	}

	/**
	 * Get zeta for the given pressure
	 * @param pressure
	 * @return
	 */
	public double getZeta(double pressure) {
		if (this.zetaSamples.length == 1)
			return this.zetaSamples[0];
		else
			return Algo.linearInterpolation(pressure, pressureSamples,
					zetaSamples);
	}

}
