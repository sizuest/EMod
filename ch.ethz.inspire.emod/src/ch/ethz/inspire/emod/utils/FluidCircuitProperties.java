/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2015 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.utils;

import java.util.ArrayList;

import ch.ethz.inspire.emod.model.fluid.AFluidElementCharacteristic;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.simulation.DynamicState;

/**
 * General implementation of the properties of a component within a fluid circuit.
 * The FCP are implemented as a binary three, where as the leaves of a object
 * indicate the up and downstream elements.
 * @author sizuest
 *
 */
public class FluidCircuitProperties {
	/* Flow rate [kg/s] */
	private double[] flowRateIn = {0.0};
	/* Inlet and outlet pressures [Pa] */
	private double pressureIn = 0, pressureOut = 1;
	/* Temperature */
	private DynamicState temperature = null, temperatureIn = null;
	/* Material of the fluid flowing through the component {@link Material} */ 
	private Material material;
	/* Up- and downstream elements */
	private ArrayList<FluidCircuitProperties> post, pre;
	/* Pressure reference */
	private IOContainer pressureReferenceIn = null, pressureReferenceOut = null;
	/* Characteristics */
	private AFluidElementCharacteristic characteristic;
	/* Last flow rate in */
	private double[] lastFlowRateIn;
	
	/**
	 * Public constructor
	 * @param characteristic 
	 * @param temperature 
	 */
	public FluidCircuitProperties(AFluidElementCharacteristic characteristic, DynamicState temperature){
		this.characteristic = characteristic;
		this.temperature    = temperature;
		init();
	}
	
	/**
	 * Public constructor
	 * @param characteristic 
	 * @param temperature 
	 */
	public FluidCircuitProperties(AFluidElementCharacteristic characteristic){
		this.characteristic = characteristic;
		init();
	}
	
	private void init(){
		material = new Material("Example");
		post     = new ArrayList<FluidCircuitProperties>();
		pre      = new ArrayList<FluidCircuitProperties>();
	}
	
	
	/**
	 * Set the down-stream element
	 * This method will automatically set the the current element as
	 * the up-stream element of the indicated down-stream element.
	 * @param post {@link FluidCircuitProperties} Down-stream element
	 */
	public void setPost(FluidCircuitProperties post){
		if(!this.post.contains(post)){
			this.post.add(post);
			post.setPre(this);
		}
	}
	
	/**
	 * Set the upstream-stream element
	 * @param post {@link FluidCircuitProperties} Up-stream element
	 */
	private void setPre(FluidCircuitProperties pre){
		if(!this.pre.contains(pre)){
			this.pre.add(pre);
			flowRateIn = new double[this.pre.size()];
		}

	}
	
	/**
	 * Set the inlet flow rate according to the value [m³/s]
	 * @param value
	 */
	public void setFlowRatesIn(double[] value){	
		this.flowRateIn = value;
	}
	
	public double getTemperatureOut(){
		if(null==temperature)
			return getTemperatureIn();
		else
			return temperature.getValue();
	}
	
	public double getTemperatureIn(){
		double out = 0, flowRate = 0;
		if(null==temperatureIn){
			if(0!=getFlowRate())
				lastFlowRateIn = getFlowRates();
			else if(0==getFlowRate() & null==lastFlowRateIn){
				lastFlowRateIn = new double[pre.size()];
				for(int i=0; i<lastFlowRateIn.length; i++)
					lastFlowRateIn[i] = 1;
			}
			
			for(int i=0; i<lastFlowRateIn.length; i++)
				flowRate+=lastFlowRateIn[i];
			
			for(int i=0; i<pre.size(); i++)
				if(Double.isNaN(getFlowRates()[i]) & !Double.isNaN(pre.get(i).getTemperatureOut()))
					out+=pre.get(i).getTemperatureOut()/lastFlowRateIn.length;
				else if(Double.isNaN(pre.get(i).getTemperatureOut()))
					out+=293.15/lastFlowRateIn.length;
				else
					out+=pre.get(i).getTemperatureOut()*lastFlowRateIn[i]/flowRate;
			
			return out;
		}
		else
			return temperatureIn.getValue();
	}
	
	/**
	 * Set the material according to the value {@link Material}
	 * @param value
	 */
	public void setMaterial(Material value){
		this.material = value;
		for(FluidCircuitProperties fp: getAllConnectedElements(this))
			fp.material.setMaterial(this.material);
	}
	
	/**
	 * Set the pressure reference
	 * @param p
	 */
	public void setPressureReferenceOut(IOContainer p){
		pressureReferenceOut = p;
	}
	
	/**
	 * Set the pressure reference
	 * @param p
	 */
	public void setPressureReferenceIn(IOContainer p){
		pressureReferenceIn = p;
	}
	
	/**
	 * Set the pressure reference
	 * @param p
	 */
	public void setPressureReferencIn(FluidContainer p){
		pressureReferenceIn = p;
	}
	
	public double getPressureIn(){
		return this.pressureIn;
	}
	
	public double getPressureOut(){
		return this.pressureOut;
	}
	
	
	/**
	 * Returns the pressure reference in Pa
	 * @return [Pa]
	 */
	public double getPressureReferenceOut(){
		if(null==pressureReferenceOut)
			return Double.NaN;
		else
			if(pressureReferenceOut instanceof FluidContainer)
				return ((FluidContainer) pressureReferenceOut).getPressure();
			else
				return pressureReferenceOut.getValue();
	}
	
	/**
	 * Returns the pressure reference in Pa
	 * @return [Pa]
	 */
	public double getPressureReferenceIn(){
		if(null==pressureReferenceIn)
			return Double.NaN;
		else
			if(pressureReferenceIn instanceof FluidContainer)
				return ((FluidContainer) pressureReferenceIn).getPressure();
			else
				return pressureReferenceIn.getValue();
	}
	
	public void setPressureIn(double pressureIn){
		this.pressureIn = pressureIn;
	}
	
	public void setPressureOut(double pressureOut){
		this.pressureOut = pressureOut;
	}
	
	/**
	 * getFlowRate()
	 * @return current flow rate [m³/s]
	 */
	public double getFlowRate(){		
		double ret = 0;
		
		for(int i=0; i<this.flowRateIn.length; i++)
			ret+=this.flowRateIn[i];
		
		return ret;
	}
	
	/**
	 * getMassFlowRate()
	 * @return current flow rate [kg/s]
	 */
	public double getMassFlowRate(){		
		double rho;
		
		rho = getMaterial().getDensity(getTemperature(), getPressure());
		
		return getFlowRate()*rho;
	}
	
	private double getTemperature() {
		return (getTemperatureIn()+getTemperatureOut())/2;
	}

	/**
	 * getFlowRate()
	 * @return current flow rate [m³/s]
	 */
	public double[] getFlowRates(){
		return this.flowRateIn;
	}
	
	/**
	 * getHeatLoss()
	 * @return current heat loss [W]
	 */
	public double getHeatLoss(){
		return getPressureDrop()*getFlowRate();
	}
	
	
	/**
	 * getMaterial()
	 * @return current material {@link Material}
	 */
	public Material getMaterial(){
		return this.material;
	}
	
	/**
	 * getPressureDrop
	 * @return Current pressure drop [Pa]
	 */
	public double getPressureDrop(){
		return this.pressureIn-this.pressureOut;
	}
	
	public ArrayList<FluidCircuitProperties> getPre(){
		return this.pre;
	}
	
	public ArrayList<FluidCircuitProperties> getPost(){
		return this.post;
	}
	
	public static ArrayList<FluidCircuitProperties> getAllConnectedElements(FluidCircuitProperties start){
		ArrayList<FluidCircuitProperties> list = new ArrayList<FluidCircuitProperties>(), 
				                          candidates = new ArrayList<FluidCircuitProperties>();
		
		list.add(start);
		
		// Build list of candidates
		candidates.addAll(start.post);
		for(FluidCircuitProperties fp: start.pre)
			if(!list.contains(fp))
				candidates.add(fp);
		
		while(0!=candidates.size()){
			if(!list.contains(candidates.get(0))){
				list.add(candidates.get(0));
				for(FluidCircuitProperties fp: candidates.get(0).post)
					if(!list.contains(fp))
						candidates.add(fp);
				for(FluidCircuitProperties fp: candidates.get(0).pre)
					if(!list.contains(fp))
						candidates.add(fp);
				candidates.remove(0);
			}
			else
				candidates.remove(0);
		}
		
		return list;
	}

	public AFluidElementCharacteristic getCharacterisitc() {		
		return characteristic;
	}

	public void setTemperature(DynamicState temperature) {
		this.temperature = temperature;
	}

	public double getPressure() {
		return (pressureOut+pressureIn)/2;
	}
}
