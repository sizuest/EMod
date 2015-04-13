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

import ch.ethz.inspire.emod.model.thermal.ThermalArray;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Pump model class.
 * Implements the physical model of a pump with reservoir.
 * From the input parameter mass flow, the electrical power
 * and the supply mass flow are calculated.
 * 
 * Assumptions:
 * Perfect gas
 * 
 * 
 * Inputlist:
 *   1: MassFlowOut : [kg/s] : Demanded mass flow out
 *   2: FluidIn     : [-]    : Fluid flowing into Pump
 * Outputlist:
 *   1: PTotal      : [W]    : Demanded electrical power
 *   2: PLoss       : [W]    : Thermal pump losses
 *   3: PUse        : [W]    : Power in the pluid
 *   4: FluidOut    : [-]    : Fluid flowing out of the Pump
 *   
 * Config parameters:
 *   PressureSamples      : [Pa]    : Pressure samples for liner interpolation
 *   FlowRateSamples      : [m^3/s] : Volumetric flow samples for liner interpolation
 *   ElectricalPower	  : [W]     : Nominal power if operating
 * 
 * @author manick
 *
 */
@XmlRootElement
public class PumpFluid extends APhysicalComponent implements Floodable{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer temperatureAmb;
	private FluidContainer fluidIn;
	private IOContainer flowRateOut;
	
	// Output parameters:
	private IOContainer pel;
	private IOContainer pth;
	private IOContainer pmech;
	private FluidContainer fluidOut;
	private IOContainer temperaturePump;
	
	// Parameters used by the model. 
	private double[] pressureSamples;  	// Samples of pressure [Pa]
	private double[] massFlowSamples;  	// Samples of mass flow [kg/s]
	private double[] flowRateSamples;   // Samples of flow rate [m^3/s]
	private double[] pressureSamplesR, massFlowSamplesR, flowRateSamplesR;
	private double pelPump;				// Power demand of the pump if on [W]
	
	private double temperatureInit;
	private String fluidType;
	private ThermalArray fluid;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public PumpFluid() {
		super();
		
		//TODO manick: change init
		this.type = "Example";
		this.temperatureInit = 293;
		this.fluidType = "Monoethylenglykol_34";
		
		init();
	}
	
	public PumpFluid(String type, double temperatureInit, String fluidType){
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		this.fluidType = fluidType;
		init();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Pump constructor
	 * 
	 * @param type
	 */
	public PumpFluid(String type) {
		super();
		
		this.type=type;
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
		/* Define Input parameters */
		inputs      = new ArrayList<IOContainer>();
		temperatureAmb = new IOContainer("TemperatureAmb", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		flowRateOut    = new IOContainer("FlowRateOut", Unit.KG_S, 0.00, ContainerType.FLUIDDYNAMIC);
		inputs.add(temperatureAmb);
		inputs.add(flowRateOut);
		
		/* Define output parameters */
		outputs    = new ArrayList<IOContainer>();
		pel        = new IOContainer("PTotal",     Unit.WATT, 0.00, ContainerType.ELECTRIC);
		pth        = new IOContainer("PLoss",      Unit.WATT, 0.00, ContainerType.THERMAL);
		pmech      = new IOContainer("PUse",       Unit.WATT, 0.00, ContainerType.FLUIDDYNAMIC);
		temperaturePump = new IOContainer("TemperaturePump", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		outputs.add(pel);
		outputs.add(pth);
		outputs.add(pmech);
		outputs.add(temperaturePump);

		
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
			pressureSamples = params.getDoubleArray("PressureSamples");
			flowRateSamples = params.getDoubleArray("FlowRateSamples");
			pelPump         = params.getDoubleValue("ElectricalPower");
			//fluid           = params.getMaterial("Fluid");
			//int numElements = 1;
			//fluid           = new ThermalArray(fluidType, .0001, 1);
			//fluid.getTemperature().setInitialCondition(temperatureInit);
			
			/*
			 * Revert arrays;
			 */
			pressureSamplesR = new double[pressureSamples.length];
			for (int i=0; i<pressureSamples.length; i++) {
				pressureSamplesR[i] = pressureSamples[pressureSamples.length-1-i];
			}
			flowRateSamplesR = new double[flowRateSamples.length];
			for (int i=0; i<flowRateSamples.length; i++) {
				flowRateSamplesR[i] = flowRateSamples[flowRateSamples.length-1-i];
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
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
		
		if(fluidType != null){
			setFluid(fluidType);
		} else{
			setFluid("Example");
		}
		
		/* Define FluidIn parameter */
		fluidIn        = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);

		/* Define FluidOut parameter */
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
		// Check model parameters:
		// Check dimensions:
		if (pressureSamples.length != flowRateSamples.length) {
			throw new Exception("Pump, type:" +type+ 
					": Dimension missmatch: Vector 'pressureSamples' must have same dimension as " +
					"'flowRateSamples' (" + pressureSamples.length + "!=" + flowRateSamples.length + ")!");
		}
		// Check if sorted:
		for (int i=1; i<pressureSamplesR.length; i++) {
			if (pressureSamplesR[i] <= pressureSamplesR[i-1]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'pressureSamples' must be sorted!");
			}
		}
		
		// Check if sorted:
		for (int i=1; i<flowRateSamples.length; i++) {
			if (flowRateSamples[i] <= flowRateSamples[i-1]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'flowRateSamples' must be sorted!");
			}
		}
				
		if (pelPump<=0){
			throw new Exception("Pump, type:" +type+ 
					": Negative or zero value: Pump power must be strictly positive!");
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		double density, viscosity, massFlowOut;

		//TODO manick:
		/*
		 * folgendes Vorgehen:
		 * Inputs: fluidIn, (Volumetric)FlowRateOut
		 * Outputs: fluidOut
		 * 
		 * wenn: FlowRateOut != 0 --> check ob FlowRate möglich (FlowRateSamples/PressureSamples?) --> Pressure >= 0
		 *       --> FlowRateIn rechnen, PressureOut rechnen
		 * 
		 */
		
		/* ************************************************************************/
		/*         Update inputs, direction of calculation:                       */
		/*         temperature [K]    : fluidIn --> fluid                         */
		/*         pressure    [Pa]   : fluidIn --> fluid                         */
		/*         flowRate:   [m^3/s]: fluid   <-- fluidOut                      */
		/* ************************************************************************/
		try{
			fluid.setTemperatureIn(fluidIn.getTemperature());
		} catch(Exception e) {
			fluid.setTemperatureIn(293);
		}
		if(fluidIn.getPressure() > 0){
			fluid.setPressure(fluidIn.getPressure());
		} else {
			fluid.setPressure(100000);
		}
		//fluid.setFlowRate(fluidOut.getFlowRate()); --> according to massFlowOut
		
		density   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue(),  fluid.getPressure());
		viscosity = fluid.getMaterial().getViscosity(fluid.getTemperature().getValue(), fluid.getPressure());
		massFlowOut = flowRateOut.getValue() * density;
		
		if(flowRateSamples[flowRateSamples.length-1] < flowRateOut.getValue()){
			System.out.println("pump can not provide requested massflow!");
		}

		// Pump has Input flowRateOut. if flowRateOut can be provided by the pump, then calculate
		if(flowRateOut.getValue() !=0 && flowRateSamples[flowRateSamples.length-1] >= flowRateOut.getValue()){
			// Pump is ON
			pel.setValue(pelPump);
			
			// set flowRate of fluid and calculate according pressure
			fluid.setFlowRate(flowRateOut.getValue());
			fluid.setPressure(Algo.linearInterpolation(flowRateOut.getValue(), flowRateSamples, pressureSamples));
		}
		else { // Pump is OFF
			pel.setValue(0);
			
			//therefore set flowRate to zero
			fluid.setFlowRate(0);
			fluid.setPressure(100000);
		}
		
		/* The mechanical power is given by the pressure and the voluminal flow:
		 * Pmech = pFluid [Pa] * Vdot [m3/s]
		 */
		pmech.setValue( Math.abs(fluid.getFlowRate()*fluid.getPressure()/fluid.getMaterial().getDensity(fluid.getTemperature().getValue(), fluid.getPressure())) );
		
		/* The Losses are the difference between electrical and mechanical power
		 */
		pth.setValue(pel.getValue()-pmech.getValue());		
		
		/*
		 * Settings for the fluid:
		 * Temperature External, Thermal Resistance, Heat Source
		 * Thermal Resistance: R_th = 1 with assumptions, that effect is neglectable for the small volume
		 * Heat Source: pump losses (pth)
		 */
		fluid.setTemperatureExternal(temperatureAmb.getValue());
		fluid.setThermalResistance(1);
		fluid.setHeatSource(pth.getValue());
		
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
		
		temperaturePump.setValue(fluid.getTemperature().getValue());
		
		System.out.println("Pump: " + fluid.getPressure() + " " + fluid.getFlowRate() + " " + fluid.getTemperature().getValue() + " " + fluid.getHeatLoss());
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
	/**
	 * set Type of the Pump
	 * @param type: the type of the pump to set
	 */
	public void setType(String type) {
		this.type = type;
		init();
	}

	/**
	 * get the fluid type
	 * @return the fluid type
	 */
	public String getFluidType(){
		return fluid.getMaterial().getType();
	}
	
	/**
	 * set the fluid type, init the fluid
	 * @param type: the type of the fluid to set
	 */
	public void setFluid(String type){
		/* ThermalArray */
		this.fluidType = type;
		this.fluid = new ThermalArray(type, 0.0001, 1);
		
		/* Initialize Thermal Array */
		if (temperatureAmb.getValue() > 0) {
			fluid.setInitialTemperature(temperatureAmb.getValue());
			fluid.getTemperature().setInitialCondition(temperatureAmb.getValue());
			fluid.setTemperatureExternal(temperatureAmb.getValue());
		} else {
			fluid.setInitialTemperature(293);
			fluid.getTemperature().setInitialCondition(293);
			fluid.setTemperatureExternal(293);
		}
		fluid.setThermalResistance(1);
		
		/* State */
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid.getTemperature());
	}
	
	/**
	 * set the fluid
	 * @param fluid
	 */
	public void setFluid(ThermalArray fluid) {
		this.fluid = fluid;
	}
}
