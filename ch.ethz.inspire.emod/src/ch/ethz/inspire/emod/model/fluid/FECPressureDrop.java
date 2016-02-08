package ch.ethz.inspire.emod.model.fluid;

import ch.ethz.inspire.emod.utils.IOContainer;

public class FECPressureDrop extends AFluidElementCharacteristic{

	IOContainer pressure;
	
	public FECPressureDrop(IOContainer pressure){
		this.pressure = pressure;
	}
	
	@Override
	public double getA0(double flowRate, double pressure) {
		return this.pressure.getValue();
	}

	@Override
	public double getA1(double flowRate, double pressure) {
		return 0;
	}

	@Override
	public double getEp(double flowRate, double pressure) {
		return 1;
	}

}
