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

import ch.ethz.inspire.emod.model.thermal.ThermalArray;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
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
 * Inputlist:
 *   1: FluidIn       : [-]    : Fluid Container with temperature, pressure, massflow
 *   2: PressureAmb   : [Pa]   : Ambient Pressure (assuming free surface of fluid in tank)
 *   3: TemperatureAmb: [K]    : Ambient temperature
 * Outputlist:
 *   1: FluidOut      : [-]    : Fluid Container with temperature, pressure, massflow
 *   
 * Config parameters:
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
	private IOContainer levelHeatExchanger;
	
	// Parameters used by the model.
	private String material;
	private double volume;
	private double length = 0.00;
	private double width = 0.00;
	private double height = 0.00;
	private ThermalArray fluid;
	private double lasttemp[];
	double temperatureInit = 293.00;
	
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
		
		//TODO level HeatExchanger correct?
		levelHeatExchanger = new IOContainer("LevelHeatExchanger", Unit.NONE, 0, ContainerType.CONTROL);
		outputs.add(levelHeatExchanger);
		
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
			material     = params.getString("Material");
			
			/* Thermal Array */
			System.out.println("tank.init: setting the fluid " + material);
			if(material != null){
				setFluid(material);
			}
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
		if(material==null){
			throw new Exception("Tank, type: " + type + ": empty Material!");
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		double density, heatCapacity, alphaFluid;
		density   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue(),  fluid.getPressure());
		heatCapacity = fluid.getMaterial().getHeatCapacity();
		
		lasttemp[1] = fluid.getTemperature().getValue();
		/* ************************************************************************/
		/*         Update inputs, direction of calculation:                       */
		/*         temperature [K]    : fluidIn --> fluid                         */
		/*         pressure    [Pa]   : fluidIn --> fluid                         */
		/*         flowRate:   [m^3/s]: fluid   <-- fluidOut                      */
		/* ************************************************************************/
		fluid.setTemperatureIn(fluidIn.getTemperature());
		if(pressureAmb.getValue() > 0){
			fluid.setPressure(pressureAmb.getValue());
		} else {
			fluid.setPressure(100000); //DIN 1343 normpressure = 1.01325 bar = 101325
		}
		fluid.setFlowRate(fluidOut.getFlowRate());
		
		/* ************************************************************************/
		/*         Calculate and set fluid values:                                */
		/*         TemperatureIn, Pressure, FlowRate, ThermalResistance,          */
		/*         HeatSource, TemperatureExternal                                */
		/* ************************************************************************/
		//assumption Tank is cubic: S = 6 * (volume)^(2/3)
		double surface = 6 * Math.pow(volume, 2/3);
		//alpha = ln((T0-T_amb)/(T1-T_amb))*m*cp/surface
		if(lasttemp[1] - temperatureAmb.getValue() <= 0 || lasttemp[0] - temperatureAmb.getValue() <= 0){
			alphaFluid = 1;
		} else { //calculated for one timestep, could also be done over more than one
			alphaFluid = Math.log((lasttemp[0]-temperatureAmb.getValue())/(lasttemp[1]-temperatureAmb.getValue()) * volume * density * heatCapacity/surface);
		}
		
		double thermalResistance = 1/(alphaFluid*surface);
		fluid.setThermalResistance(thermalResistance);
		fluid.setHeatSource(-heatExchangerIn.getValue());
		fluid.setTemperatureExternal(temperatureAmb.getValue());
				
		/* ************************************************************************/
		/*         Integration step:                                              */
		/* ************************************************************************/
		fluid.integrate(timestep);
		
		/* ************************************************************************/
		/*         Update outputs, direction of calculation:                      */
		/*         temperature [K]    : fluid   --> fluidOut                      */
		/*         pressure    [Pa]   : fluid   --> fluidOut                      */
		/*         flowRate:   [m^3/s]: fluidIn <-- fluid                         */
		/* ************************************************************************/
		fluidOut.setTemperature(fluid.getTemperatureOut());
		fluidOut.setPressure(fluid.getPressure());
		fluidIn.setFlowRate(fluid.getFlowRate());
		
		temperatureTank.setValue(fluid.getTemperature().getValue());
		
		if(temperatureTank.getValue() > 303.5){
			levelHeatExchanger.setValue(1);
		} else if(temperatureTank.getValue() < 299.5){
			levelHeatExchanger.setValue(0);
		}
		
		lasttemp[0] = lasttemp[1];
		
		System.out.println("Tank: " + fluid.getPressure() + " " + fluid.getFlowRate() + " " + fluid.getTemperature().getValue() + " " + thermalResistance + " " + fluid.getHeatLoss());
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
	
	/*
	public ThermalArray getFluid(){
		return fluid;
	}
	*/
	/**
	 * get the type of the Fluid
	 * @return type of the fluid
	 */
	public String getFluidType(){
		return fluid.getMaterial().getType();
	}

	/*
	public void setFluid(ThermalArray fluid) {
		this.fluid = fluid;
	}
	*/
	/**
	 * set Fluid to type and initialize it
	 * @param type of the fluid
	 */
	public void setFluid(String type){
		/* ThermalArray */
		this.fluid = new ThermalArray(type, volume, 1);
		
		lasttemp = new double[2];
		
		/* Initialize Thermal Array */
		if (temperatureAmb.getValue() > 0) {
			fluid.setInitialTemperature(temperatureAmb.getValue());
			fluid.getTemperature().setInitialCondition(temperatureAmb.getValue());
			fluid.setTemperatureExternal(temperatureAmb.getValue());
			lasttemp[0] = temperatureAmb.getValue();
			lasttemp[1] = temperatureAmb.getValue();
		} else {
			fluid.setInitialTemperature(293);
			fluid.getTemperature().setInitialCondition(293);
			fluid.setTemperatureExternal(293);
			lasttemp[0] = 293;
			lasttemp[1] = 293;
		}
		fluid.setThermalResistance(1);
		
		/* State */
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid.getTemperature());
		
		
	}
	
	public double getVolume(){
		return volume;
	}
}
