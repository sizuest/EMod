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
public class PumpFluid extends APhysicalComponent /*implements Floodable*/{
//public class PumpFluid<T> extends APhysicalComponent<T>{

	@XmlElement
	protected String type;
	
	// Input parameters:
	//private IOContainer/*<ThermalArray>*/ fluidIn;
	private IOContainer/*<Double>*/ temperatureAmb;
	private FluidContainer fluidIn;
	//private IOContainer massFlowOut;
	private IOContainer pressureOut;
	//private IOContainer<Double> massFlowOut;
	//private IOContainer<Double> tempIn;
	
	//combine to object?
	private double lastpressure  = 0.00;
	//private double lastmassflow  = 0.00;
	private double lasttemperature  = 293.00;
	//private double Reynolds		 = 0.00;
	//private double lambda		 = 0.00;
	

	
	// Output parameters:
	private IOContainer/*<Double>*/ pel;
	private IOContainer/*<Double>*/ pth;
	private IOContainer/*<Double>*/ pmech;
	//private IOContainer/*<ThermalArray>*/ fluidOut;
	private FluidContainer fluidOut;
	//private IOContainer<Double> massFlowIn;
	//private IOContainer<Double> pFluid;
	
	
	// Parameters used by the model. 
	private double[] pressureSamples;  	// Samples of pressure [Pa]
	private double[] massFlowSamples;  	// Samples of mass flow [kg/s]
	private double[] pressureSamplesR, massFlowSamplesR;
	private double pelPump;				// Power demand of the pump if on [W]
	//private Material fluid;
	
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
		inputs      = new ArrayList<IOContainer/*<ThermalArray>*/>();
		temperatureAmb = new IOContainer/*<Double>*/("TemperatureAmb", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		pressureOut    = new IOContainer("PressureOut", Unit.PA, 0.00, ContainerType.FLUIDDYNAMIC);
		inputs.add(temperatureAmb);
		inputs.add(pressureOut);
		
		/* Define output parameters */
		outputs    = new ArrayList<IOContainer/*<T>*/>();
		pel        = new IOContainer/*<Double>*/("PTotal",     Unit.WATT, 0.00, ContainerType.ELECTRIC);
		pth        = new IOContainer/*<Double>*/("PLoss",      Unit.WATT, 0.00, ContainerType.THERMAL);
		pmech      = new IOContainer/*<Double>*/("PUse",       Unit.WATT, 0.00, ContainerType.FLUIDDYNAMIC);
		outputs.add(/*(IOContainer<T>) */pel);
		outputs.add(/*(IOContainer<T>) */pth);
		outputs.add(/*(IOContainer<T>) */pmech);

		
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
			int numElements = 1;
			fluid           = new ThermalArray(fluidType, .001, numElements);
			fluid.getTemperature().setInitialCondition(temperatureInit);
			
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
		
		//TODO manick: test for Fluid
		//fluidIn       = new IOContainer/*<ThermalArray>*/("FluidIn", Unit.NONE,    fluid, ContainerType.FLUIDDYNAMIC);
		fluidIn        = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(/*(IOContainer<T>)*/ fluidIn);
		
		fluidIn.setPressure(100000);
		fluidIn.setTemperature(293);
		
		//TODO manick: test for Fluid
		//fluidOut        = new IOContainer/*<ThermalArray>*/("FluidOut",      Unit.NONE,   fluid, ContainerType.FLUIDDYNAMIC);
		fluidOut        = new FluidContainer("FluidOut", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		outputs.add(/*(IOContainer<T>)*/ fluidOut);
		
		fluidOut.setFlowRate(0);
		
		/* State */
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid.getTemperature());
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
		double density, viscosity;

		// Update inputs
		//direction of calculation
		//temperature [K]    : fluidIn --> fluidOut
		//pressure    [Pa]   : fluidIn --> fluidOut
		//flowRate:   [m^3/s]: fluidIn <-- fluidOut
		fluid.setTemperatureIn(fluidIn.getTemperature());
		//fluid.setPressure(fluidIn.getPressure());
		//fluid.setFlowRate(fluidOut.getFlowRate());
		
		lastpressure = fluid.getPressure();
		//TODO manick: change of pressure! ....
		if(pressureOut.getValue() != lastpressure)
			System.out.println("pump: change of pressure");

		
		if(pressureOut.getValue() != 0){
			pel.setValue(pelPump);
			fluid.setPressure(pressureOut.getValue());
			
			//TODO manick: interpolation does not work this way round! pressureSamples should be sorted other way round
			//fluid.setFlowRate(1.4);
			fluid.setFlowRate(Algo.linearInterpolation(pressureOut.getValue(), pressureSamplesR, massFlowSamplesR));
			
			fluidIn.setFlowRate(fluid.getFlowRate());
		}
		else{
			pel.setValue(0);
			fluid.setPressure(fluidIn.getPressure());
			fluid.setFlowRate(fluidOut.getFlowRate());
			fluidIn.setFlowRate(fluid.getFlowRate());
		}
		
		/* The mechanical power is given by the pressure and the voluminal flow:
		 * Pmech = pFluid [Pa] * Vdot [m3/s]
		 */
		pmech.setValue( Math.abs(fluid.getFlowRate()*fluid.getPressure()/fluid.getMaterial().getDensity(fluidIn.getTemperature(), fluid.getPressure())) );
		
		/* The Losses are the difference between electrical and mechanical power
		 */
		pth.setValue(pel.getValue()-pmech.getValue());		
		
		// Get current fluid properties
		density   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue(),  fluid.getPressure());
		viscosity = fluid.getMaterial().getViscosity(fluid.getTemperature().getValue(), fluid.getPressure());
		
		fluid.setThermalResistance(1);
		fluid.setHeatSource(pth.getValue());
		fluid.setTemperatureExternal(temperatureAmb.getValue());
		fluid.setTemperatureIn(fluidIn.getTemperature());
		
		// Integration step
		fluid.integrate(timestep);

		// Update outputs
		//direction of calculation
		//temperature [K]    : fluidIn --> fluidOut
		//pressure    [Pa]   : fluidIn --> fluidOut
		//flowRate:   [m^3/s]: fluidIn <-- fluidOut
		fluidOut.setTemperature(fluid.getTemperatureOut());
		fluidOut.setPressure(fluid.getPressure());
		fluidIn.setFlowRate(fluid.getFlowRate());

		System.out.println("pump fluidvalues: " + fluidOut.getPressure() + " " + pth.getValue() + " " + fluid.getFlowRate() + " " + lasttemperature + " " + density + " " + viscosity + " " + pmech.getValue());
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
		init();
	}
	
	public ThermalArray getFluid(){
		return fluid;
	}

	public void setFluid(ThermalArray fluid) {
		this.fluid = fluid;
	}
}
