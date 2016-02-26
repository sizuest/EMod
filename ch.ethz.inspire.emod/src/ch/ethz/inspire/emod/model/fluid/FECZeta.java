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

public class FECZeta extends AFluidElementCharacteristic{

	double[] zetaSamples, pressureSamples;
	
	public void setZeta(double zeta){
		this.zetaSamples = new double[]{zeta};
	}
	
	public void setZeta(double[] zeta, double[] pressure){
		this.zetaSamples = zeta;
		this.pressureSamples = pressure;
	}
	
	public FECZeta(double zeta){
		this.zetaSamples = new double[]{zeta};
	}
	
	public FECZeta(double[] zeta, double[] pressure){
		this.zetaSamples = zeta;
		this.pressureSamples = pressure;
	}
	
	@Override
	public double getA0(double flowRate, double pressureIn, double pressureOut) {
		return -getZeta(pressureIn-pressureOut)*Math.pow(flowRate, 2);
	}

	@Override
	public double getA1(double flowRate, double pressureIn, double pressureOut) {
		return 2*flowRate*getZeta(pressureIn-pressureOut);
	}

	@Override
	public double getEp(double flowRate, double pressureIn, double pressureOut) {
		return 1;
	}
	
	public double getZeta(double pressure){
		if(this.zetaSamples.length == 1)
			return this.zetaSamples[0];
		else
			return Algo.linearInterpolation(pressure, pressureSamples, zetaSamples);
	}

}
