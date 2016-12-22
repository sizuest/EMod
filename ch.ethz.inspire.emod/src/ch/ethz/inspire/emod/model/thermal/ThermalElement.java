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
package ch.ethz.inspire.emod.model.thermal;

import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.utils.ShiftProperty;

/**
 * General thermal element class
 * 
 * @author simon
 * 
 */
public class ThermalElement extends AThermalIntegrator {

	private double thermalResistance;
	private double tempAmb;
	private double tempIn;
	private ShiftProperty<Double> heatInput = new ShiftProperty<Double>(0.0);

	/**
	 * ThermalElement
	 * 
	 * @param materialName
	 * @param mass
	 */
	public ThermalElement(String materialName, double mass) {
		super();
		this.massState.setInitialCondition(mass);
		this.material = new Material(materialName);
	}

	/**
	 * ThermalElement
	 * 
	 * @param material
	 * @param mass
	 */
	public ThermalElement(Material material, double mass) {
		super();
		this.material = material;
		getMass().setInitialCondition(mass);
	}

	/**
	 * Set the heat input [W]
	 * 
	 * @param heatInput
	 */
	public void setHeatInput(double heatInput) {
		if (!(heatInput == 0 | Double.isInfinite(heatInput) | Double
				.isNaN(heatInput)))
			this.heatInput.set(heatInput);
		else
			this.heatInput.set(0.0);
	}

	/**
	 * Add to current heat input [W]
	 * 
	 * @param heatInput
	 */
	public void addHeatInput(double heatInput) {
		if (!(heatInput == 0 | Double.isInfinite(heatInput) | Double
				.isNaN(heatInput)))
			setHeatInput(this.heatInput.getCurrent() + heatInput);
	}

	/**
	 * @param volume
	 */
	public void setVolume(double volume) {
		this.massState.setValue(volume
				* this.material.getDensity(this.getTemperature().getValue(),
						100000));
	}

	/**
	 * @param mass
	 */
	public void setMass(double mass) {
		this.massState.setValue(mass);
	}

	/**
	 * @return [m3]
	 */
	public double getVolume() {
		return massState.getValue()
				/ material.getDensity(temperatureState.getValue());
	}

	@Override
	public double getA(int i) {
		// A[k] = -mDotIn[k]/m[k] - Rth[k]/m[k]/cp[k]
		double cp = material.getHeatCapacity();
		return -getMassFlowIn().getCurrent() / massState.getValue()
				- thermalResistance / massState.getValue() / cp;
	}

	@Override
	public double getB(int i) {
		// B[k] = (mDotIn[k]*cpIn[k]*Tin[k] +
		// Tamb[k]*Rth[k]+heatInput[k])/m[k]/cp[k]
		double cp = material.getHeatCapacity();
		double cpIn = material.getHeatCapacity();
		return (getMassFlowIn().getCurrent() * cpIn * tempIn + tempAmb
				* thermalResistance + heatInput.getCurrent())
				/ massState.getValue() / cp;
	}

	/**
	 * Set inlet temperature
	 * 
	 * @param tempIn
	 *            [K]
	 */
	public void setTemperatureIn(double tempIn) {
		this.tempIn = tempIn;
	}

	/**
	 * Set ambient temperature
	 * 
	 * @param tempAmb
	 *            [K]
	 */
	public void setTemperatureAmb(double tempAmb) {
		this.tempAmb = tempAmb;
	}

	/**
	 * Set thermal resistance
	 * 
	 * @param thermalResistance
	 *            [W/K]
	 */
	public void setThermalResistance(double thermalResistance) {
		this.thermalResistance = thermalResistance;
	}

	/**
	 * Calculate the heat flux over the boundary
	 * @return
	 */
	public double getBoundaryHeatFlux() {
		return (temperature.get(0).getCurrent() - tempAmb) * thermalResistance;
	}

}
