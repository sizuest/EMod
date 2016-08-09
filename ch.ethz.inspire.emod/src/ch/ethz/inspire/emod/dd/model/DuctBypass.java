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

package ch.ethz.inspire.emod.dd.model;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.parameters.ParameterSet;

@XmlRootElement
public class DuctBypass extends ADuctElement {
	
	@XmlElement
	Duct ductPrimary   = new Duct(this.name+": 1"),
		 ductSecondary = new Duct(this.name+": 2");
	
	double lastFlowRate, lastPressure, lastTemperatureFluid;
	double flowRatePrimary;
	
	public DuctBypass(){}
	
	/**
	 * Constructor by name
	 * @param name
	 */
	public DuctBypass(String name){
		super();
		this.name     = name;
		init();
	}
	
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		init();
	}


	private void init() {
		lastFlowRate = Double.NaN;
		lastPressure = Double.NaN;
		lastTemperatureFluid = Double.NaN;
		
		setMaterial(getMaterial());
		
		getPrimary().cleanUpFittings();
		getSecondary().cleanUpFittings();
	}

	@Override
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {
		
		double htcPrimary   = getPrimary().getHTC(getFlowRatePrimary(flowRate, pressure, temperatureFluid), pressure, temperatureFluid, temperatureWall); 
		
		return htcPrimary;
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure,
			double temperatureFluid) {
		
		double pressureDrop = getPrimary().getPressureDrop(getFlowRatePrimary(flowRate, pressure, temperatureFluid), pressure, temperatureFluid);
		
		return pressureDrop;
	}

	public double getFlowRatePrimary(double flowRate, double pressure,	double temperatureFluid) {
		updateFlowRates(flowRate, pressure, temperatureFluid);
		
		return this.flowRatePrimary;
	}
	
	public double getFlowRateSecondary(double flowRate, double pressure, double temperatureFluid) {
		updateFlowRates(flowRate, pressure, temperatureFluid);
		
		return flowRate-this.flowRatePrimary;
	}

	@Override
	public DuctBypass clone() {
		DuctBypass element = new DuctBypass();
		element.init();
		element.setPrimary(element.ductPrimary.clone(this.ductPrimary));
		element.setSecondary(element.ductSecondary.clone(this.ductSecondary));
		
		element.setParameterSet(this.getParameterSet());
		if(null==this.isolation)
			element.setIsolation(null);
		else
			element.setIsolation(this.isolation.clone());
		
		element.setName(this.getName());
		
		return element;
	}
	

	@Override
	public ParameterSet getParameterSet() {
		return new ParameterSet();
	}

	@Override
	public void setParameterSet(ParameterSet ps) {
		// Not used here
	}
	
	public Duct getPrimary(){
		return this.ductPrimary;
	}
	
	public Duct getSecondary(){
		return this.ductSecondary;
	}
	
	private void setPrimary(Duct duct){
		this.ductPrimary = duct;
	}
	
	private void setSecondary(Duct duct){
		this.ductSecondary = duct;
	}
	
	private void updateFlowRates(double flowRate, double pressure, double temperatureFluid) {
		if( flowRate == lastFlowRate &
		    pressure == lastPressure &
		    temperatureFluid == lastTemperatureFluid)
			return;
		
		lastFlowRate = flowRate;
		lastPressure = pressure;
		lastTemperatureFluid = temperatureFluid; 
		
		if(Double.isNaN(this.flowRatePrimary) | 0==this.flowRatePrimary | flowRate >= this.flowRatePrimary)
			this.flowRatePrimary = flowRate*.5;
		if(0==flowRate){
			this.flowRatePrimary = 0;
			return;
		}

		
		double lastFlowRatePrimary = this.flowRatePrimary;
		double k1 = 0, k2 = 0, kT1 = 0, kT2 = 0;
		double r=Double.POSITIVE_INFINITY;
		
		
		
		for(int i=0; i<10; i++){
			
			k1 = getPrimary().getPressureLossCoefficient(Math.max(this.flowRatePrimary, 1E-3*flowRate), pressure, temperatureFluid);
			k2 = getSecondary().getPressureLossCoefficient(Math.max(flowRate-this.flowRatePrimary, 1E-3*flowRate), pressure, temperatureFluid);
			
			kT1 = ( Fluid.pressureLossTBranchPrimary(getMaterial(),   temperatureFluid, getPrimary().getInletProfile(),  this.flowRatePrimary, flowRate) +
					Fluid.pressureLossTBranchSecondary(getMaterial(), temperatureFluid, getPrimary().getOutletProfile(), this.flowRatePrimary, flowRate)) / (flowRate*Math.abs(flowRate));
			kT2 = ( Fluid.pressureLossTBranchPrimary(getMaterial(),   temperatureFluid, getSecondary().getInletProfile(),  this.flowRatePrimary, flowRate) +
					Fluid.pressureLossTBranchSecondary(getMaterial(), temperatureFluid, getSecondary().getOutletProfile(), this.flowRatePrimary, flowRate)) / (flowRate*Math.abs(flowRate));

			
			if(k1+kT1 == k2+kT2)
				this.flowRatePrimary = flowRate*.5;
			else
				this.flowRatePrimary =flowRate * (kT2+k2)/(k1+kT1-k2-kT2) * (-1+Math.sqrt(1+(k1+kT1-k2-kT2)/(kT2+k2)));
			

			r = (this.flowRatePrimary-lastFlowRatePrimary)/this.flowRatePrimary;
			if(Math.abs(r)<1E-4)
				break;
			
			lastFlowRatePrimary = this.flowRatePrimary;
		}
				
	}
	
	@Override
	public void setMaterial(Material material){
		this.material = material;
		getPrimary().setMaterial(material);
		getSecondary().setMaterial(material);
	}
	
		
	/**
	 * Returns the (hydraulic) diameter
	 * 
	 * @return [m]
	 */
	@Override
	public double getDiameter(){
		if(getPrimary().getElements().size()>0)
			return getPrimary().getElement(0).getDiameter();
		else
			return 0.01;
	}
	
	/**
	 * Returns the length
	 * 
	 * @return [m]
	 */
	@Override
	public double getLength(){
		return  this.getPrimary().getLength();
	}
	
	/**
	 * Returns the free surface
	 * 
	 * @return [m²]
	 */
	@Override
	public double getSurface(){
		return this.getPrimary().getSurface();
	}
	
	/**
	 * Returns the hydraulic surface
	 * 
	 * @return [m²]
	 */
	@Override
	public double getHydraulicSurface() {
		return this.getPrimary().getSurface();
	}
	
	/**
	 * Returns the total volume
	 * 
	 * @return [m³]
	 */
	@Override
	public double getVolume() {
		return this.getPrimary().getVolume();
	}
	
	/**
	 * Returns the hydraulic profile object
	 * 
	 * @return {@link AHydraulicProfile.java}
	 */
	@Override
	public AHydraulicProfile getProfileIn() {
		if(getPrimary().getElements().size()>0)
			return getPrimary().getElement(0).getProfileIn();
		else
			return this.profile;
	}
	
	/**
	 * Returns the hydraulic profile object
	 * 
	 * @return {@link AHydraulicProfile.java}
	 */
	@Override
	public AHydraulicProfile getProfileOut() {
		if(getPrimary().getElements().size()>0)
			return getPrimary().getElement(getPrimary().getElements().size()-1).getProfileOut();
		else
			return this.profile;
	}
	

}
