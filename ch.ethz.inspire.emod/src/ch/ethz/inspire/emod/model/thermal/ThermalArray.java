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
import ch.ethz.inspire.emod.utils.AThermalIntegrator;

/**
 * General thermal array class
 * 
 * @author simon
 *
 */
public class ThermalArray extends AThermalIntegrator {
	
	/* Input and Ambient temperatures */
	protected double tempAmb = 293.15, tempIn = 293.15;
	/* Volume of the array */
	protected double volume;
	/* Current flow rate [m3/s] / pressure [Pa] / th. Resistance [K/W] */
	protected double flowRate = 0;
	protected double massFlowRate = 0;
	protected double thermalResistance = Double.NaN;
	/* Internal heat source [W] */
	protected double heatSource = 0;
	
	/**
	 * ThermalElement
	 * 
	 * @param materialName
	 * @param volume 
	 * @param numElements 
	 */
	public ThermalArray(String materialName, double volume, int numElements){
		super(numElements);
		this.material = new Material(materialName);
		this.volume = volume;
		massState.setInitialCondition(volume*material.getDensity(293.15));
		// Default values
		flowRate = 0;
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
		this.temperature 		= that.temperature; //TODO copy?
		this.temperatureState 	= that.temperatureState; //TODO copy?
		this.tempAmb 	        = that.tempAmb;
		this.tempIn 		    = that.tempIn;
		this.thermalResistance 	= that.thermalResistance;
		this.volume 			= that.volume;
		this.massState          = that.massState;
		init();
	}
	
	/**
	 * Sets the input temperature [K]
	 * @param temperatureIn
	 */
	public void setTemperatureIn(double temperatureIn){
		this.tempIn = temperatureIn;
	}
	
	/**
	 * Sets the ambient temperature [K]
	 * @param temperatureExt
	 */
	public void setTemperatureAmb(double temperatureExt){
		this.tempAmb = temperatureExt;
	}
	
	/**
	 * Sets the thermal resistance [K/W]
	 * @param thermalResistance
	 */
	public void setThermalResistance(double thermalResistance){
		if(!(Double.isInfinite(thermalResistance) | Double.isNaN(thermalResistance)))
			this.thermalResistance = thermalResistance;
		else
			this.thermalResistance = 0.0;
	}
	
	/**
	 * Set the flow rate [m^3/s]
	 * @param flowRate
	 */
	public void setFlowRate(double flowRate){
		this.flowRate = flowRate;
		this.massFlowRate = flowRate*material.getDensity(getTemperatureBulk(), pressure.getCurrent());
	}
	/**
	 * get the flow rate [m^3/s]
	 * @return flowRate
	 */
	public double getFlowRate(){
		return flowRate;
	}	

	
	/**
	 * Set the internal heat sources
	 * @param heatSource
	 */
	public void setHeatSource(double heatSource){
		if(!(heatSource==0 | Double.isInfinite(heatSource) | Double.isNaN(heatSource)))
			this.heatSource = heatSource;
	}
	
	
	/**
	 * Output temperature
	 * @return temperature(end)
	 */
	public double getTemperatureOut(){
		return temperature.get(numElements-1).getCurrent();
	}
	
	/**
	 * @return heat loss to ambient
	 */
	public double getHeatLoss(){
		if(thermalResistance==0 | Double.isInfinite(thermalResistance) | Double.isNaN(thermalResistance))
			return 0;
		else
			return (getTemperatureBulk()-tempAmb)*thermalResistance;
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
		return tempAmb + " " + tempIn + " " + material.toString() + " " + volume + " " + flowRate + " " + pressure + " " + heatSource + " " + numElements;
	}

	@Override
	public double getA(int i) {
		// A[k] = -(mDotIn[k]*cpIn[k]*N - Rth[k])/m[k]/cp[k]
		double cp   = material.getHeatCapacity();
		return -(cp*massFlowRate*numElements+thermalResistance)/massState.getValue()/cp;
	}

	@Override
	public double getB(int i) {
		// B[k] = (mDotIn[k]*cpIn[k]*Tin[k] + Tamb[k]*Rth[k]+heatInput[k])/m[k]/cp[k]
		double cp   = material.getHeatCapacity();
		double cpIn = material.getHeatCapacity();
		double tempIn = (0==i)? this.tempIn : temperature.get(i-1).getCurrent();
		
		return (cpIn*tempIn*massFlowRate + tempAmb*thermalResistance/numElements+heatSource/numElements)*numElements/massState.getValue()/cp;
	}
}


