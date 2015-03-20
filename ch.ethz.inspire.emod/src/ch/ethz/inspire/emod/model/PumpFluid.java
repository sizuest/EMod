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
 * Outputlist:
 *   1: PTotal      : [W]    : Demanded electrical power
 *   2: PLoss       : [W]    : Thermal pump losses
 *   3: PUse        : [W]    : Power in the pluid
 *   4: MassFlowIn  : [m3/s] : Current mass flow in
 *   5: pressure    : [Pa]   : Pressure in the tank
 *   
 * Config parameters:
 *   PressureSamples      : [Pa]    : Pressure samples for liner interpolation
 *   MassFlowSamples      : [kg/s]  : Mass flow samples for liner interpolation
 *   DensityFluid         : [kg/m3] : Working fluid density
 *   ElectricalPower	  : [W]     : Nominal power if operating
 * 
 * @author simon
 *
 */
@XmlRootElement
public class PumpFluid extends APhysicalComponent implements Floodable{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer temperatureAmb;
	private FluidContainer fluidIn;
	//private IOContainer pressureOut; --> would this be useful?
	private IOContainer flowRateOut;
	
	//private double lastpressure  = 0.00;
	//private double lasttemperature  = 293.00;
	
	// Output parameters:
	private IOContainer pel;
	private IOContainer pth;
	private IOContainer pmech;
	private FluidContainer fluidOut;
	
	// Parameters used by the model. 
	private double[] pressureSamples;  	// Samples of pressure [Pa]
	private double[] massFlowSamples;  	// Samples of mass flow [kg/s]
	private double[] pressureSamplesR, massFlowSamplesR;
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
		//pressureOut    = new IOContainer("PressureOut", Unit.PA, 0.00, ContainerType.FLUIDDYNAMIC);
		flowRateOut    = new IOContainer("FlowRateOut", Unit.KG_S, 0.00, ContainerType.FLUIDDYNAMIC);
		inputs.add(temperatureAmb);
		//inputs.add(pressureOut);
		inputs.add(flowRateOut);
		
		/* Define output parameters */
		outputs    = new ArrayList<IOContainer>();
		pel        = new IOContainer("PTotal",     Unit.WATT, 0.00, ContainerType.ELECTRIC);
		pth        = new IOContainer("PLoss",      Unit.WATT, 0.00, ContainerType.THERMAL);
		pmech      = new IOContainer("PUse",       Unit.WATT, 0.00, ContainerType.FLUIDDYNAMIC);
		outputs.add(pel);
		outputs.add(pth);
		outputs.add(pmech);

		
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
			massFlowSamples = params.getDoubleArray("MassFlowSamples");
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
			massFlowSamplesR = new double[massFlowSamples.length];
			for (int i=0; i<massFlowSamples.length; i++) {
				massFlowSamplesR[i] = massFlowSamples[massFlowSamples.length-1-i];
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
		if (pressureSamples.length != massFlowSamples.length) {
			throw new Exception("Pump, type:" +type+ 
					": Dimension missmatch: Vector 'pressureSamples' must have same dimension as " +
					"'massFlowSamples' (" + pressureSamples.length + "!=" + massFlowSamples.length + ")!");
		}
		// Check if sorted:
		for (int i=1; i<pressureSamplesR.length; i++) {
			if (pressureSamplesR[i] <= pressureSamplesR[i-1]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'pressureSamples' must be sorted!");
			}
		}
		
		// Check if sorted:
		for (int i=1; i<massFlowSamples.length; i++) {
			if (massFlowSamples[i] <= massFlowSamples[i-1]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'massFlowSamples' must be sorted!");
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
		double density;

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
		fluid.setTemperatureIn(fluidIn.getTemperature());
		if(fluidIn.getPressure() > 0){
			fluid.setPressure(fluidIn.getPressure());
		} else {
			fluid.setPressure(100000);
		}
		//fluid.setFlowRate(fluidOut.getFlowRate()); --> according to massFlowOut
		
		density   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue(),  fluid.getPressure());
		//viscosity = fluid.getMaterial().getViscosity(fluid.getTemperature().getValue(), fluid.getPressure());
		double massFlowOut = flowRateOut.getValue() * density;
		
		if(massFlowSamples[massFlowSamples.length-1] < massFlowOut){
			System.out.println("pump can not provide requested massflow!");
		}

		// Pump has Input flowRateOut. if flowRateOut can be provided by the pump, then calculate
		if(flowRateOut.getValue() !=0 && massFlowSamples[massFlowSamples.length-1] >= massFlowOut){ // Pump is ON
			pel.setValue(pelPump);
			// set flowRate of fluidOut --> Pump creates flowRate
			fluidOut.setFlowRate(flowRateOut.getValue());
			
			// set flowRate of fluid and calculate according pressure
			fluid.setFlowRate(flowRateOut.getValue());
			fluid.setPressure(Algo.linearInterpolation(massFlowOut, massFlowSamples, pressureSamples));
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
		 * Thermal Resistance: R_th = l / (lambda * A) = 2.55 with assumptions:
		 *     l = length                    = 0.01     [m]
		 *     lambda = thermal Conductivity = 50       [W/(m K)] (Steel)
		 *     A = Area                      = 0.0001 [m^2]
		 * Heat Source: pump losses (pth)
		 */
		fluid.setTemperatureExternal(temperatureAmb.getValue());
		fluid.setThermalResistance(0.2);
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
		
		System.out.println("pump: " + fluid.getPressure() + " " + fluid.getFlowRate() + " " + fluid.getTemperature().getValue());
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
		} else {
			fluid.setInitialTemperature(293);
			fluid.getTemperature().setInitialCondition(293);
		}
		
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
