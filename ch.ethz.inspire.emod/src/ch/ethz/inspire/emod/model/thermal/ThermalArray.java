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
	
	/* Dynamic state object */
	protected DynamicState temperatureBulk;
	/* List of current [k+1] and past [k] node temperatures */
	protected double[] temperature;
	protected double[] lastTemperature;
	/* Input and Ambient temperatures */
	protected double temperatureExt = Double.NaN, temperatureIn = Double.NaN;
	/* Material of the array */
	protected Material material;
	/* Volume of the array */
	protected double volume;
	/* Current flow rate [l/min] / pressure [Pa] / th. Resistance [K/W] */
	protected double flowRate = 0;
	protected double pressure = 0;
	protected double thermalResistance = Double.NaN;
	/* Internal heat source [W] */
	protected double heatSource = 0;
	/* Number of nodes */
	protected int numElements;
	/* Static constant C1/2[k] */
	double lastC1 = Double.NaN, lastC2 = Double.NaN, lastC3 = Double.NaN, lastC4 = Double.NaN;
	
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
		
		this.temperature      = new double[numElements];
		this.lastTemperature  = new double[numElements];
		
		// Set the initialization method
		try {
			temperatureBulk.setInitialConditionFunction(this.getClass().getDeclaredMethod("setInitialTemperature", double.class), this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * copy constructor
	 * @param that
	 */
	public ThermalArray(ThermalArray that){
		this.flowRate 			= that.flowRate;
		this.heatSource 		= that.heatSource;
		this.material 			= that.material;
		this.numElements 		= that.numElements;
		this.pressure 			= that.pressure;
		this.temperature 		= that.temperature;
		this.temperatureBulk 	= that.temperatureBulk;
		this.temperatureExt 	= that.temperatureExt;
		this.temperatureIn 		= that.temperatureIn;
		this.thermalResistance 	= that.thermalResistance;
		this.volume 			= that.volume;
	}
	
	/**
	 * Sets the temperature vector to the initial value
	 * @param temperatureInit 
	 */
	public void setInitialTemperature(double temperatureInit){
		for(int i=0;i<this.numElements;i++) {
			temperature[i]     = temperatureInit;
			lastTemperature[i] = temperatureInit;
		}
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
	 * gets the fluid pressure
	 * @return pressure
	 */
	public double getPressure(){
		return pressure;
	}
	
	/**
	 * Sets the thermal resistance [K/W]
	 * @param thermalResistance
	 */
	public void setThermalResistance(double thermalResistance){
		this.thermalResistance = thermalResistance;
	}
	
	/**
	 * Set the flow rate [m^3/s]
	 * @param flowRate
	 */
	public void setFlowRate(double flowRate){
		this.flowRate = flowRate;
	}
	/**
	 * get the flow rate [m^3/s]
	 * @return flowRate
	 */
	public double getFlowRate(){
		return flowRate;
	}	
	/**
	 * Set the flow rate according to the mass flow rate
	 * @param massFlowRate [kg/s]
	 * @param temperature [K]
	 * @param pressure [Pa]
	 */
	public void setMassFlowRate(double massFlowRate, double temperature, double pressure){
		double rho = material.getDensity(temperature, pressure);

		//TODO manick: unit flowRate!
		//this.flowRate = massFlowRate/rho*1000*60;
		this.flowRate = massFlowRate/rho;
	}
	/**
	 * get the mass flow rate according
	 * @param id 
	 * @return massFlowRate [kg/s]
	 */
	public double getMassFlowRate(int id){
		if(id < numElements){
			double rho = material.getDensity(temperature[id], pressure);
			
			//TODO manick: unit flowRate!
			//return flowRate*rho/1000/60;
			return flowRate*rho;
		}
		else{
			return 0.0;
		}

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
	 * update in and outputs of fluid
	 */
	//TODO manick: not necessary!!
	// in component: fluid.integrate()
	// in fluidconnection: fluidIn = fluidOut and vise versa...
	/*
	public void update(FluidContainer fluidIn, FluidContainer fluidOut){
		temperatureIn = fluidIn.getTemperature();
		
		if(fluidIn.getPressure() == fluidOut.getPressure())
			pressure = fluidIn.getPressure();
		else {
			pressure = fluidIn.getPressure();
			this.integrate(1);
		}
		
		if(fluidIn.getFlowRate() == fluidOut.getFlowRate())
			flowRate = fluidIn.getFlowRate();
		else {
			flowRate = fluidIn.getFlowRate();
			this.integrate(1);
		}	
	}
	*/
	
	/**
	 * Perform an integration step of length timestep [s]
	 * @param timestep
	 */
	public void integrate(double timestep){
		temperatureBulk.setTimestep(timestep);
		

		
		/* Get fluid properties */
		double rho      = material.getDensity(getTemperatureBulk(), pressure);
		double cp       = material.getHeatCapacity();
		double mass     = volume*rho;
		
		//TODO manick: unit flowRate!
		//double massFlow = flowRate/1000/60*rho;
		double massFlow = flowRate*rho;
		
		//System.out.println("ThermalArray.integrate: " + rho + " " + cp + " " + mass + " " + massFlow);
		/* Calculate constantes for the current timestep:
		 * C1 = N*mDot + 1/Rth/cp
		 * C2 = T_amb/Rth/cp
		 * C3 = hsrc/cp
		 */
		double C1       = cp*thermalResistance;
		double C2       = heatSource*thermalResistance+temperatureExt;
		double C3       = massFlow;
		double C4       = temperatureIn;
		// If required initialize past constants
		if(Double.isNaN(lastC1))
			lastC1 = C1;
		if(Double.isNaN(lastC2))
			lastC2 = C2;
		if(Double.isNaN(lastC3))
			lastC3 = C3;
		if(Double.isNaN(lastC4))
			lastC4 = C4;
			
		
		/* Bilinear transformation */
		
		for(int i=0; i<numElements; i++){
			if(i==0)
				temperature[i] = ( timestep * (lastC1*C2+C1*lastC2) +
						           C1*( timestep*numElements*lastC1 * (lastC3*lastC4+C3*C4-lastC3*lastTemperature[i]) +
						               -timestep*lastTemperature[i] + 2*lastC1*lastTemperature[i]*mass)) / 
						         ( lastC1*(timestep+C1*(2*mass+numElements*timestep*C3)) );
			else
				temperature[i] = ( timestep * (lastC1*C2+C1*lastC2) +
						           C1*( timestep*numElements*lastC1 * (lastC3*lastTemperature[i-1]+C3*temperature[i-1]-lastC3*lastTemperature[i]) +
						               -timestep*lastTemperature[i] + 2*lastC1*lastTemperature[i]*mass)) / 
						         ( lastC1*(timestep+C1*(2*mass+numElements*timestep*C3)) );
		}
		//System.out.println("ThermalArray.integrate 0: " + temperature[0]);
		
		
		
		
		/* State Temperature */
		temperatureBulk.setValue(getTemperatureBulk());
		
		/* Shift new temperatures to old temperatures */
		lastTemperature = temperature.clone();
		
		/* Shift new C1/2 to old C1/2 */
		lastC1 = C1;
		lastC2 = C2;
		lastC3 = C3;
	}
	
	/**
	 * Returns the temperature state
	 * @return {@link DynamicState} temperature
	 */
	public DynamicState getTemperature(){
		return temperatureBulk;
	}
	
	/**
	 * @param material to set
	 */
	public void setMaterial(Material material){
		this.material = material;
	}
	
	/**
	 * @return Material
	 */
	public Material getMaterial(){
		return material;
	}
	
	/**
	 * @param volume to set
	 */
	public void setVolume(double volume){
		this.volume = volume;
	}
	
	/**
	 * @return volume
	 */
	public double getVolume(){
		return volume;
	}

	public String toString() {
		return temperatureExt + " " + temperatureIn + " " + material.toString() + " " + volume + " " + flowRate + " " + pressure + " " + heatSource + " " + numElements;
	}
}
