package ch.ethz.inspire.emod.model.fluid;

import ch.ethz.inspire.emod.simulation.DynamicState;


public class FECDuct extends AFluidElementCharacteristic{
	
	Duct duct;
	DynamicState temperature;
	
	public FECDuct(Duct duct, DynamicState temperature){
		this.duct = duct;
		this.temperature = temperature;
	}

	@Override
	public double getA0(double flowRate, double pressure) {		
		double a0 = duct.getPressureDrop(flowRate, pressure, temperature.getValue());
		return a0;
	}

	@Override
	public double getA1(double flowRate, double pressure) {
		double a1;
		double flowRateCrit = 1E-9;
		/*if(flowRateCrit>flowRate)
			a1 = -getA0(flowRateCrit, pressure)/flowRateCrit;
		else
			//a1 = -2*getA0(flowRate, pressure)/flowRate;
			a1 = 2*6E10;*/
		//flowRate = Math.max(flowRate, flowRateCrit);
		a1 = duct.getPressureLossDrivative(flowRate, pressure, temperature.getValue());
		return a1;
	}

	@Override
	public double getEp(double flowRate, double pressure) {
		return 1;
	}

}
