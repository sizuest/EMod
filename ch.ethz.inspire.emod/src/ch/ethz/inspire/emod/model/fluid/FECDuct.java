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

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.simulation.DynamicState;


public class FECDuct extends AFluidElementCharacteristic{
	
	Duct duct;
	DynamicState temperature;
	
	public FECDuct(Duct duct, DynamicState temperature){
		this.duct = duct;
		this.temperature = temperature;
	}

	@Override
	public double getA0(double flowRate, double pressureIn, double pressureOut) {		
		double a0 = duct.getPressureDrop(flowRate, (pressureIn+pressureOut)/2, temperature.getValue())-flowRate*duct.getPressureLossDrivative(flowRate, pressureIn, temperature.getValue());
		return a0;
	}

	@Override
	public double getA1(double flowRate, double pressureIn, double pressureOut) {
		double a1;
		a1 = duct.getPressureLossDrivative(flowRate, (pressureIn+pressureOut)/2, temperature.getValue());
		return a1;
	}

	@Override
	public double getEp(double flowRate, double pressureIn, double pressureOut) {
		return 1;
	}

}
