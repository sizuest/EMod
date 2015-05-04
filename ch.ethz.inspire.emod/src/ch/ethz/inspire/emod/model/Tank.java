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
	
	//TODO where to set the level for the heatExchanger? could also be done in process configuration
	private IOContainer heatExchangerIn;
		
	// Output parameters:
	private FluidContainer fluidOut;
	private IOContainer temperatureTank;
	
	// Fluid Properties
	FluidCircuitProperties fluidProperties;
	
	// Parameters used by the model.
	private double volume;
	private double length = 0.00;
	private double width = 0.00;
	private double height = 0.00;
	private ThermalElement fluid = new ThermalElement("Example", 1);
	double temperatureInit = 293.00;
	private boolean materialSet = false;
	
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
		outputs        = new ArrayList<IOContainer>();		
		temperatureTank = new IOContainer("TemperatureTank", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		outputs.add(temperatureTank);
		
	
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
			volume		 = params.getDoubleValue("Volume");
		}
		catch (Exception e) {
			System.out.println("no property 'Volume', checking for 'Length'/'Depth'/'Height':");
			try{
				length   = params.getDoubleValue("Length");
				width    = params.getDoubleValue("Width");
				height   = params.getDoubleValue("Height");
				volume   = length * width * height;
			}
			catch (Exception ee){
				e.printStackTrace();
				ee.printStackTrace();
				//System.exit(-1);
			}
		}
		try {
			
			/* Thermal Array */
			fluid.setMaterial(params.getString("Material"));
			fluid.setVolume(volume);
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
		
		/* FlowRate */
		fluidProperties = new FluidCircuitProperties();
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
		double density, heatCapacity, alphaFluid;
		density   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue(),  fluidIn.getPressure());
		heatCapacity = fluid.getMaterial().getHeatCapacity();
		
		if(!materialSet)
			fluidProperties.setMaterial(fluid.getMaterial());
		
		/* ************************************************************************/
		/*         Calculate and set fluid values:                                */
		/*         TemperatureIn, Pressure, FlowRate, ThermalResistance,          */
		/*         HeatSource, TemperatureExternal                                */
		/* ************************************************************************/
		//assumption Tank is cubic: S = 6 * (volume)^(2/3)
		double surface = 6 * Math.pow(volume, 0.66);
		//alpha = ln((T0-T_amb)/(T1-T_amb))*m*cp/surface
		if(fluid.getTemperature().getValue() - temperatureAmb.getValue() <= 0 || fluid.getTemperature().getLastValue() - temperatureAmb.getValue() <= 0){
			alphaFluid = 1;
		} else { //calculated for one timestep, could also be done over more than one
			alphaFluid = Math.log((fluid.getTemperature().getLastValue() - temperatureAmb.getValue())/(fluid.getTemperature().getValue()-temperatureAmb.getValue()) * volume * density * heatCapacity/surface);
		}
		
		//System.out.println(alphaFluid+" W/K/m² on "+surface+" m²");
		
		//TODO
		double thermalResistance = 1/(alphaFluid*surface);
		
		// Convection
		fluid.setHeatInput(-(fluid.getTemperature().getValue()-temperatureAmb.getValue())/thermalResistance);
		// Mass transfere
		fluid.addHeatInput((fluidIn.getTemperature()-fluidOut.getTemperature())*heatCapacity*density*fluidProperties.getFlowRate());
		// External
		fluid.addHeatInput(-heatExchangerIn.getValue());
		// Integrate
		fluid.integrate(timestep);
		
		
		// Outlet temperature is equal to the bulk temperature of the tank
		fluidOut.setTemperature(fluid.getTemperature().getValue());
		fluidOut.setPressure(0);
		
		temperatureTank.setValue(fluid.getTemperature().getValue());
		
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
