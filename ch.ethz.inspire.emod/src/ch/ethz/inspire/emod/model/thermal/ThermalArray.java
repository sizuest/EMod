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

import ch.ethz.inspire.emod.model.Material;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;

/**
 * General thermal array class
 * 
 * @author simon
 *
 */
public class ThermalArray {
	
	protected DynamicState temperatureBulk;
	protected double[] temperature;
	protected double temperatureExt = Double.NaN, temperatureIn = Double.NaN;
	protected Material material;
	protected double volume;
	protected double flowRate = 0;
	protected double pressure = 0;
	protected double thermalResistance = Double.NaN;
	protected double heatSource = 0;
	protected int numElements;
	
	/**
	 * ThermalElement
	 * 
	 * @param materialName
	 * @param volume
	 * @param numElements 
	 */
	public ThermalArray(String materialName, double volume, int numElements){
		this.volume          = volume;
		this.material        = new Material(materialName);
		this.temperatureBulk = new DynamicState("Temperature", Unit.KELVIN);
		this.numElements     = numElements;
		
		// Default values
		flowRate = 0;
		
		this.temperature  = new double[numElements];
		
		// Set the initialization method
		try {
			temperatureBulk.setInitialConditionFunction(this.getClass().getDeclaredMethod("setInitialTemperature", double.class), this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Sets the temperature vector to the initial value
	 * @param temperatureInit 
	 */
	public void setInitialTemperature(double temperatureInit){
		for(int i=0;i<this.numElements;i++)
			temperature[i] = temperatureInit;
	}
	
	/**
	 * Sets the input temperature [K]
	 * @param temperatureIn
	 */
	public void setTemperatureIn(double temperatureIn){
		this.temperatureIn = temperatureIn;
	}
	
	/**
	 * Sets the ambient temperature [K]
	 * @param temperatureExt
	 */
	public void setTemperatureExternal(double temperatureExt){
		this.temperatureExt = temperatureExt;
	}
	
	/**
	 * Sets the fluid pressure
	 * @param pressure
	 */
	public void setPressure(double pressure){
		this.pressure = pressure;
	}
	
	/**
	 * Sets the thermal resistance [K/W]
	 * @param thermalResistance
	 */
	public void setThermalResistance(double thermalResistance){
		this.thermalResistance = thermalResistance;
	}
	
	/**
	 * Set the flow rate [l/min]
	 * @param flowRate
	 */
	public void setFlowRate(double flowRate){
		this.flowRate = flowRate;
	}
	
	/**
	 * Set the flow rate according to the mass flow rate
	 * @param massFlowRate [kg/s]
	 * @param temperature [K]
	 * @param pressure [Pa]
	 */
	public void setMassFlowRate(double massFlowRate, double temperature, double pressure){
		double rho = material.getDensity(temperature, pressure);
		this.flowRate = massFlowRate/rho*1000*60;
	}
	
	
	/**
	 * Set the internal heat sources
	 * @param heatSource
	 */
	public void setHeatSource(double heatSource){
		this.heatSource = heatSource;
	}
	
	/**
	 * Bulk temperature
	 * @return bulk temperature
	 */
	private double getTemperatureBulk(){
		double m = 0; // Mass
		double H = 0; // Enthalpy
		
		double rho, cp;
		
		cp  = material.getHeatCapacity();
		
		for(int i=0;i<numElements;i++){
			rho = material.getDensity(temperature[i], pressure);
			
			m += volume/numElements*rho;
			H += volume/numElements*rho*cp*temperature[i];
		}

		return H/m/cp;
	}
	
	/**
	 * Output temperature
	 * @return temperature(end)
	 */
	public double getTemperatureOut(){
		return temperature[numElements-1];
	}
	
	/**
	 * @return heat loss to ambient
	 */
	public double getHeatLoss(){
		return (getTemperatureBulk()-temperatureExt)/thermalResistance;
	}
	
	/**
	 * Perform an integration step of length timestep [s]
	 * @param timestep
	 */
	public void integrate(double timestep){
		temperatureBulk.setTimestep(timestep);
		
		// Get fluid properties
		double rho      = material.getDensity(getTemperatureBulk(), pressure);
		double cp       = material.getHeatCapacity();
		double mass     = volume*rho;
		double massFlow = flowRate/1000/60*rho;
		
		
		/* For each element the change in temperature is
		 * Tdot_i [K/s] = N [-] /m [kg] * mDot [kg/s] *(T_i-1 - T_i) [K] - S [m2]/m [kg] /cp [J/kg/K] * k [W/m2/K] * (T_i-T_amb) [K]
		 * 
		 * where T_-1 = T_in
		 */	
		for (int i=numElements-1; i>0; i--) {
			temperature[i] += timestep * ( numElements / mass * massFlow * (temperature[i-1]-temperature[i]) -
					                           		1 / mass / cp / thermalResistance * (temperature[i]-temperatureExt) + 
					                           		heatSource / numElements);
		}
		temperature[0] += timestep * ( numElements / mass * massFlow * (temperatureIn-temperature[0]) -
                							   1 / mass / cp / thermalResistance * (temperature[0]-temperatureExt) +
                							   heatSource / numElements);
		
		/* State Temperature */
		temperatureBulk.setValue(getTemperatureBulk());
	}
	
	/**
	 * Returns the temperature state
	 * @return {@link DynamicState} temperature
	 */
	public DynamicState getTemperature(){
		return temperatureBulk;
	}
	
	/**
	 * @return Material
	 */
	public Material getMaterial(){
		return material;
	}

}
