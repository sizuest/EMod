/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2013 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.model.material;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import ch.ethz.inspire.emod.model.units.SiConstants;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.MaterialConfigReader;

/**
 * General Material model class. Provides fluid properties
 * 
 * Assumptions: -Newtonian fluid -Pressure influence on viscosity neglected
 * -Density influenced by pressure and temperature
 * 
 * 
 * Config parameters: 
 * 	TemperaturSamples: [Kelvin] 
 * 	PressureSamples: [Pa]
 * 	Viscosity: [mm2/s]
 *  Density: [kg/m3]
 * 
 * @author sizuest, based on HydraulicOil
 * 
 */
public class Material {
	@XmlElement
	protected String type;

	private double heatCapacity;
	private double[] temperatureSamples;
	private double[] viscositySamples;
	private double[] densitySamples;
	private double thermalConductivity = 0.00;

	// Parameters for viscosity
	private double viscosityEta0 = Double.NaN;
	private double viscosityEa = Double.NaN;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Material() {
		this.type = null;
	}

	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		// post xml init method (loading physics data)
		init();
	}

	/**
	 * Fluid constructor
	 * 
	 * @param type
	 */
	public Material(String type) {
		this.type = type;
		init();
	}

	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init() {
		if (null == this.type)
			return;

		/* *********************************************************************** */
		/* Read configuration parameters: */
		/* *********************************************************************** */
		MaterialConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new MaterialConfigReader("Material", type);
			
			// Remove old variables
			params.deleteValue("PressureSamples");
			params.saveValues();

			
			temperatureSamples  = params.getPhysicalValue("TemperatureSamples", new SiUnit("K")).getValues();
			viscositySamples    = params.getPhysicalValue("ViscositySamples", new SiUnit("Pa s")).getValues();
			densitySamples      = params.getPhysicalValue("DensityMatrix", new SiUnit("kg m^-3")).getValues();
			heatCapacity        = params.getPhysicalValue("HeatCapacity", new SiUnit("J kg^-1 K^-1")).getValue();
			thermalConductivity = params.getPhysicalValue("ThermalConductivity", new SiUnit("W m^-1 K^-1")).getValue();

			params.Close(); /* Model configuration file not needed anymore. */
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Validate the parameters:
		try {
			checkConfigParams();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {

		// Check dimensions
		/*
		 * if(pressureSamples.length != temperatureSamples.length) throw new
		 * Exception("Valve, type:" +type+
		 * ": PressureSamples and TemperatureSamples must have the same dimensions!"
		 * );
		 */

		if (viscositySamples.length != temperatureSamples.length)
			throw new Exception(
					"Valve, type:"
							+ type
							+ ": ViscositySamples and TemperatureSamples must have the same dimensions!");

		for (int i = 1; i < temperatureSamples.length; i++) {
			if (temperatureSamples[i] <= temperatureSamples[i - 1]) {
				throw new Exception(
						"Valve, type:"
								+ type
								+ ": Sample vector 'TemperatureSamples' must be sorted!");
			}
		}
		/*
		 * for (int i=1; i<viscositySamples.length; i++) { if
		 * (viscositySamples[i] >= viscositySamples[i-1]) { throw new
		 * Exception("Valve, type:" +type+
		 * ": Sample vector 'ViscositySamples' must be sorted!"); } }
		 */

		// Check physical value
		if (heatCapacity < 0) {
			throw new Exception("Valve, type:" + type
					+ ": Heat capacity must be bigger than zero!");
		}


		for (int i = 0; i < temperatureSamples.length; i++) {
			if (temperatureSamples[i] < 0) {
				throw new Exception("Valve, type:" + type
						+ ": Temperature must be bigger than zero!");
			}
		}
		for (int i = 0; i < viscositySamples.length; i++) {
			if (viscositySamples[i] < 0) {
				throw new Exception("Valve, type:" + type
						+ ": Viscosity must be bigger than zero!");
			}
		}

	}

	/**
	 * Viscosity (dyn)
	 * 
	 * @param temperature [K]
	 * @return viscosity  [Pa s]
	 */
	public double getViscosityDynamic(double temperature) {
		double eta;
		/*
    	 * 
    	 */
		if (2 == viscositySamples.length & Double.isNaN(viscosityEa) & Double.isNaN(viscosityEta0)) {
			double[][] H = new double[viscositySamples.length][2];
			double[] y = new double[viscositySamples.length];

			for (int i = 0; i < viscositySamples.length; i++) {
				H[i][0] = 1;
				H[i][1] = 1 / SiConstants.R.getValue() / temperatureSamples[i];
				y[i] = Math.log(viscositySamples[i]);
			}

			double[] p = Algo.findLeastSquares(H, y);

			viscosityEta0 = Math.exp(p[0]);
			viscosityEa = p[1];
		}

		if (!Double.isNaN(viscosityEa) & !Double.isNaN(viscosityEta0))
			eta = viscosityEta0 * Math.exp(viscosityEa / SiConstants.R.getValue() / temperature);
		else
			eta = Algo.logInterpolation(temperature, temperatureSamples, viscositySamples);

		return eta;
	}

	/**
	 * Viscosity (kin)
	 * 
	 * @param temperature
	 * @return viscosity [m2/s]
	 */
	public double getViscosityKinematic(double temperature) {
		double rho, eta;

		rho = getDensity(temperature);
		eta = getViscosityDynamic(temperature);

		return eta / rho;
	}

	/**
	 * Density
	 * 
	 * @param temperature
	 *            [K]
	 * @return density [kg/m3]
	 */
	public double getDensity(double temperature) {
		return Algo.linearInterpolation(temperature, temperatureSamples, densitySamples);
	}

	/**
	 * Returns the apparent heat capacity hat the given temperature
	 * 
	 * @return apparent heat capacity [J/kg/K]
	 * 
	 *         TODO check for temperature / pressure dependency
	 */
	public double getHeatCapacity() {
		return heatCapacity;
	}

	/**
	 * @return type of material
	 */
	public String getType() {
		if (null == type)
			return "none";

		return type;
	}

	/**
	 * @return thermal conductivity [W/(m K)]
	 */
	public double getThermalConductivity() {
		return thermalConductivity;
	}

	/**
	 * @param type
	 */
	public void setMaterial(String type) {
		this.type = type;
		init();
	}

	/**
	 * @param material
	 */
	public void setMaterial(Material material) {
		this.type = material.getType();
		init();
	}

	/**
	 * Compares to materials, return true if the same type is used
	 * 
	 * @param material
	 * @return true/false
	 */
	public boolean equals(Material material) {
		return this.type.equals(material.getType());
	}
}
