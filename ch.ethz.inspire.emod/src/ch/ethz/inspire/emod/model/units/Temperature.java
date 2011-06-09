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
package ch.ethz.inspire.emod.model.units;

/**
 * Unit type for temperature in Kelvin
 * 
 * @author dhampl
 *
 */
public class Temperature {

	private double temperature;
	
	public Temperature(double tempInKelvin) {
		if(tempInKelvin<0)
			throw new IllegalArgumentException("Temperature in Kelvin must be >=0");
		this.temperature = tempInKelvin;
	}

	/**
	 * @return the temperature
	 */
	public double getTemperature() {
		return temperature;
	}

	/**
	 * @param temperature the temperature to set
	 */
	public void setTemperature(double temperature) {
		if(temperature<0)
			throw new IllegalArgumentException("Temperature in Kelvin must be >=0");
		this.temperature = temperature;
	}
}
