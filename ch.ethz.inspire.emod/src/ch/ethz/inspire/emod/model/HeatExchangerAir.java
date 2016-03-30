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


package ch.ethz.inspire.emod.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.fluid.FECZeta;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General Water-Air Heat exchanger model class.
 * Implements the physical model of a heat exchanger with external
 * coolant supply.
 * 
 * 
 * Inputlist:
 *   1: State          : [-]  : State of the HE
 *   2: TemperatureAmb : [-]  : Ambient temperature
 *   3: FluidIn        : [-]  : Fluid 1 flowing into the HE
 * Outputlist:
 *   1: PTotal      : [W]    : Demanded electrical power
 *   2: PLoss       : [W]    : Losses
 *   3: PThermal    : [W]    : Heat flux out
 *   4: FluidOut    : [-]    : Fluid 1 flowing out of the HE
 *   
 * Config parameters:
 *   PressureSamples      : [Pa]    : Pressure samples for liner interpolation
 *   FlowRateSamples      : [m^3/s] : Volumetric flow samples for liner interpolation
 *   ElectricalPower	  : [W]     : Power samples for linear interpolation
 * 
 * @author sizuest
 *
 */

@XmlRootElement
public class HeatExchangerAir extends APhysicalComponent implements Floodable{
	@XmlElement
	protected String type;
	
	// Input parameters
	private IOContainer level;
	private IOContainer tempAmb;
	private FluidContainer fluidIn;
	
	// Output parameters
	private IOContainer ptotal, ploss, pth;
	private FluidContainer fluidOut;
	
	// Fluid properties
	private FluidCircuitProperties fluidProperties;
	
	private FECZeta zeta;
	
	private ThermalElement fluid;
	
	// Parameters
	private double zetaValue;
	private double htc;
	private double tempOn, tempOff;
	private double power;
	private double volume;
	
	private boolean wasCooling = false;
	
	private boolean isInitialized = false;
	
	
	public HeatExchangerAir(){
		super();
	}
	
	/**
	 * HeatExchanger constructor
	 * 
	 * @param type
	 */
	public HeatExchangerAir(String type){
		super();
		
		this.type=type;
		init();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	private void init(){
		zeta = new FECZeta(1);
		
		fluidProperties = new FluidCircuitProperties(zeta);
		
		// Inputs
		inputs  = new ArrayList<IOContainer>();
		level   = new IOContainer("State",          new SiUnit(Unit.NONE),   0,      ContainerType.CONTROL);
		tempAmb = new IOContainer("TemperatureAmb", new SiUnit(Unit.KELVIN), 293.15, ContainerType.THERMAL);
		fluidIn = new FluidContainer("FluidIn",     new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
		inputs.add(level);
		inputs.add(tempAmb);
		inputs.add(fluidIn);
		
		// Outputs
		outputs = new ArrayList<IOContainer>();
		ptotal    = new IOContainer("PTotal",      new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		ploss     = new IOContainer("PLoss",       new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		pth       = new IOContainer("PThermal",    new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		fluidOut  = new FluidContainer("FluidOut", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
		outputs.add(ptotal);
		outputs.add(ploss);
		outputs.add(pth);
		outputs.add(fluidOut);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader(getModelType(), type);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Read the config parameter: */
		try {
			zetaValue = params.getDoubleValue("PressureLossCoefficient");
			
			htc = params.getDoubleValue("HeatTransferCoefficient");
			
			tempOn  = params.getDoubleValue("TemperatureHigh");
			tempOff = params.getDoubleValue("TemperatureLow");
			
			power = params.getDoubleValue("Power");
			
			volume = params.getDoubleValue("Volume");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Set pressure loss coeff. */
		zeta.setZeta(zetaValue);
		
		/* Create thermal elements */
		fluid = new ThermalElement("Example", 1.0);
		
		fluid.setVolume(volume);
		
		fluid.getTemperature().setName("Temperature");
		
		fluid.getMass().setName("Mass");
		
		fluid.setMaterial(fluidProperties.getMaterial());
		
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid.getTemperature());
	
		
		fluidProperties.setTemperature(fluid.getTemperature());
		
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void update() {
		fluid.setMaterial(fluidProperties.getMaterial());
		
		if(!isInitialized){
			fluid.getMass().setInitialCondition(volume*fluid.getMaterial().getDensity(fluid.getTemperature().getValue()));
			isInitialized = true;
		}

		if(level.getValue() == 1){
			
			/* Controlled Temperature: tempOff=tempOn */
			if(tempOff==tempOn){
				double heatFlux;
				
				heatFlux = Math.max(0, (fluidIn.getTemperature()-tempOn) * fluidProperties.getMassFlowRate() * fluidProperties.getMaterial().getHeatCapacity());
				
				fluid.setHeatInput(-heatFlux);
				
				ptotal.setValue(power);
				ploss.setValue(power);
			}
			else {					
				double htc = 0;
			
				if( (wasCooling & fluidProperties.getTemperatureIn()>tempOff) |
					(!wasCooling & fluidProperties.getTemperatureIn()>=tempOn) ){
					wasCooling = true;
					htc = this.htc;
					
					ptotal.setValue(power);
					ploss.setValue(power);
				}
				else{
					wasCooling = false;
					
					
					ptotal.setValue(0);
					ploss.setValue(0);
				}
				
				fluid.setThermalResistance(htc);
			}
						
		}
		else{
			wasCooling = false;
			fluid.setHeatInput(0);
			ptotal.setValue(0);
			ploss.setValue(0);
			fluid.setThermalResistance(0);
		}
			
		
		fluid.setTemperatureIn(fluidIn.getTemperature());
		fluid.integrate(timestep, fluidProperties.getFlowRate(), fluidProperties.getFlowRate(), fluidProperties.getPressure());		
		fluid.setTemperatureAmb(tempAmb.getValue());
		
		pth.setValue(fluidProperties.getEnthalpyChange()+ploss.getValue());
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public ArrayList<FluidCircuitProperties> getFluidPropertiesList() {
		ArrayList<FluidCircuitProperties> out = new ArrayList<FluidCircuitProperties>();
		out.add(fluidProperties);
		return out;
	}
	
	@Override
	public void flood(){/* Not used */}

}
