package ch.ethz.inspire.emod.model.fluid;

public class FECIdeal extends AFluidElementCharacteristic{

	@Override
	public double getA0(double flowRate, double pressure) {
		return 0;
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
