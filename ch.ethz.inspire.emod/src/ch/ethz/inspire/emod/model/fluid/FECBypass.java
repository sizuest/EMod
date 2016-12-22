package ch.ethz.inspire.emod.model.fluid;

/**
 * Element representina a bypass with fixed zeta and opening pressure
 * @author sizuest
 *
 */
public class FECBypass extends AFluidElementCharacteristic {

	double pMax;
	double zeta;

	/**
	 * @param pMax
	 * @param zeta
	 */
	public FECBypass(double pMax, double zeta) {
		this.pMax = pMax;
		this.zeta = zeta;
	}

	/**
	 * Set opening pressure
	 * @param pMax
	 */
	public void setPressure(double pMax) {
		this.pMax = pMax;
	}

	/**
	 * Set zeta
	 * @param zeta
	 */
	public void setZeta(double zeta) {
		this.zeta = zeta;
	}

	@Override
	public double getA0(double flowRate, double pressureIn, double pressureOut) {
		if (pressureIn - pressureOut < pMax)
			return pressureIn - pressureOut;
		else
			return pMax - getZeta(pressureIn - pressureOut)
					* Math.pow(flowRate, 2) * Fluid.sign(flowRate);
	}

	@Override
	public double getA1(double flowRate, double pressureIn, double pressureOut) {
		if (pressureIn - pressureOut < pMax)
			return 1E20 * 2 * flowRate;
		else
			return 2 * flowRate * getZeta(pressureIn - pressureOut)
					* Fluid.sign(flowRate);
	}

	@Override
	public double getEp(double flowRate, double pressureIn, double pressureOut) {
		if (pressureIn - pressureOut < pMax)
			return 1;
		else
			return 1;
	}

	/**
	 * Get current zeta
	 * @param pressure
	 * @return
	 */
	public double getZeta(double pressure) {
		double zeta;

		if (pressure < pMax)
			zeta = Double.NaN;
		else
			zeta = this.zeta;

		return zeta;
	}

}
