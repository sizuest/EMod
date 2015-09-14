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
package ch.ethz.inspire.emod.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.fluid.FluidElement;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Tank model class.
 * Implements the physical model of a tank
 * 
 * Assumptions:
 * -No leakage
 * -Separation between laminar and turbulent flow
 * -Smooth surface
 * -Tank wall is rigid
 * 
 * Input list:
 *   1: FluidIn       : [-]    : Fluid Container with temperature, pressure, mass flow
 *   2: PressureAmb   : [Pa]   : Ambient Pressure (assuming free surface of fluid in tank)
 *   3: TemperatureAmb: [K]    : Ambient temperature
 * Output list:
 *   1: FluidOut      : [-]    : Fluid Container with temperature, pressure, mass flow
 *   
 * Configuration parameters:
 * 	 Volume			: [m^3]
 * 		or
 *   Length	    	: [m]
 *   Width      	: [m] 
 *   Height			: [m]
 *   
 *	 Material		: [-]
 *   
 * 
 * @author manick
 *
 */
@XmlRootElement
public class Tank extends APhysicalComponent implements Floodable {

	@XmlElement
	protected String type;
	
	// Input parameters:
	private FluidContainer fluidIn;
	private IOContainer temperatureAmb;
	private IOContainer pressureAmb;
	//private IOContainer heatFlowIn;
	

	private IOContainer heatExchangerIn;
		
	// Output parameters:
	private FluidContainer fluidOut;
	private IOContainer temperatureTank;
	private IOContainer massTank;
	
	// Fluid Properties
	FluidCircuitProperties fluidProperties;
	
	// Parameters used by the model.
	private double volume, surface;
	private double length = 0.00;
	private double width = 0.00;
	private double height = 0.00;
	private ThermalElement fluid;
	double temperatureInit = 293.00;
	private boolean materialSet = false;
	
	private FluidElement mass;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Tank() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 * @throws Exception 
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) throws Exception {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Tank constructor
	 * @param type
	 * @throws Exception 
	 */
	public Tank(String type) {
		super();
		
		this.type = type;
		init();
	}
	
	/**
	 * @param type
	 * @param TInit
	 */
	public Tank(String type, double TInit){
		super();
		this.type = type;
		init();
		fluid.getTemperature().setInitialCondition(TInit);
		fluid.getTemperature().setInitialCondition();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 * @throws Exception 
	 */
	private void init()
	{
		/* Define Input parameters */
		inputs         = new ArrayList<IOContainer>();
		temperatureAmb = new IOContainer("TemperatureAmb", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		pressureAmb    = new IOContainer("PressureAmb", Unit.PA, 0.00, ContainerType.FLUIDDYNAMIC);
		heatExchangerIn= new IOContainer("HeatExchangerIn", Unit.WATT, 0.00, ContainerType.THERMAL);
		inputs.add(temperatureAmb);
		inputs.add(pressureAmb);
		inputs.add(heatExchangerIn);
		
		/* Define output parameters */
		outputs         = new ArrayList<IOContainer>();		
		temperatureTank = new IOContainer("TemperatureTank", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		massTank        = new IOContainer("Content",         Unit.KG,     0,               ContainerType.FLUIDDYNAMIC);
		outputs.add(temperatureTank);
		outputs.add(massTank);
		
	
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
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			volume   = params.getDoubleValue("Volume");
			length   = Math.pow(volume, .333);
			width    = Math.pow(volume, .333);
			height   = Math.pow(volume, .333);
			surface  = 2*(length+width)*height + length*width;
		}
		catch (Exception e) {
			System.out.println("no property 'Volume', checking for 'Length'/'Depth'/'Height':");
			try{
				length   = params.getDoubleValue("Length");
				width    = params.getDoubleValue("Width");
				height   = params.getDoubleValue("Height");
				volume   = length * width * height;
				surface  = 2*(length+width)*height + length*width;
			}
			catch (Exception ee){
				e.printStackTrace();
				ee.printStackTrace();
				//System.exit(-1);
			}
		}
		try {
			
			/* Thermal Element */
			fluid = new ThermalElement(params.getString("Material"), volume);
			/* Fluid Element */
			mass  = new FluidElement(fluid.getMaterial(), volume/fluid.getMaterial().getDensity(293.15));
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
		// Validate the parameters:
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
		
		//TODO manick: test for Fluid
		fluidIn        = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);
		//TODO manick: test for Fluid
		fluidOut        = new FluidContainer("FluidOut", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		outputs.add(fluidOut);

		/* Dynamic state */
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid.getTemperature());
		dynamicStates.add(mass.getMass());
		
		/* FlowRate */
		fluidProperties = new FluidCircuitProperties();
		fluidProperties.setCuppledInAndOut(false);
		fluidProperties.setPressureReference(fluidOut);
		fluidProperties.setMaterial(fluid.getMaterial());

	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{
		if(0>volume){
			throw new Exception("Tank, type: " + type + ": Non physical value: Variable 'volume' must be bigger than zero!");
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		double mIn,
		       mOut,
		       density, 
		       heatCapacity, 
		       alphaFluid, 
		       thermalResistance;
		density   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue(),  fluidIn.getPressure());
		heatCapacity = fluid.getMaterial().getHeatCapacity();
		
		if(!materialSet)
			fluidProperties.setMaterial(fluid.getMaterial());
		
		/* ************************************************************************/
		/*         Calculate and set fluid values:                                */
		/*         TemperatureIn, Pressure, FlowRate, ThermalResistance,          */
		/*         HeatSource, TemperatureExternal                                */
		/* ************************************************************************/
		
		mIn  = fluidProperties.getFlowRateIn()*fluid.getMaterial().getDensity(fluidIn.getTemperature(), fluidOut.getPressure());
		mOut = fluidProperties.getFlowRateOut()*fluid.getMaterial().getDensity(fluid.getTemperature().getValue(), pressureAmb.getValue());
		
		
		alphaFluid = Fluid.convectionFreeCuboid(new Material("Air"), fluid.getTemperature().getValue(), temperatureAmb.getValue(), length, width, height, false);

		thermalResistance = 1/(alphaFluid*surface);
		
		/* Mass sum */
		mass.setMassInput(mIn-mOut);
		mass.integrate(timestep);
		
		
		/* Thermal sum */
		// Convection
		if(0!=alphaFluid)
			fluid.setHeatInput(0);
		else
			fluid.setHeatInput(-(fluid.getTemperature().getValue()-temperatureAmb.getValue())/thermalResistance);
		// Mass transfere
		fluid.addHeatInput((fluidIn.getTemperature()-fluidOut.getTemperature())*heatCapacity*density*fluidProperties.getFlowRate());
		// External
		fluid.addHeatInput(-heatExchangerIn.getValue());
		// Integrate
		fluid.integrate(timestep);
		// Adjust mass
		fluid.setMass(mass.getMass().getValue());
		
		
		// Outlet temperature is equal to the bulk temperature of the tank
		fluidOut.setTemperature(fluid.getTemperature().getValue());
		fluidOut.setPressure(pressureAmb.getValue());
		fluidIn.setPressure(pressureAmb.getValue());
		
		temperatureTank.setValue(fluid.getTemperature().getValue());
		massTank.setValue(mass.getMass().getValue());
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	

	/**
	 * get the type of the Fluid
	 * @return type of the fluid
	 */
	public String getFluidType(){
		return fluid.getMaterial().getType();
	}

		
	/**
	 * @return tank volume
	 */
	public double getVolume(){
		return volume;
	}

	@Override
	public FluidCircuitProperties getFluidProperties() {
		return this.fluidProperties;
	}
}
