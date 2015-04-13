/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2013 by Inspire AG, ETHZ
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
 * General Pipe model class.
 * Implements the physical model of a hydraulic pipe
 * 
 * Assumptions:
 * -No leakage
 * -Separation between laminar and turbulent flow
 * -Smooth surface
 * -Pipe wall is rigid
 * 
 * Inputlist:
 *   1: PressureOut   : [Pa]   : Pressure at the end
 *   2: MassflowOut   : [kg/s] : Mass flow in the pipe
 *   3: TemperatureIn : [K]    : Inlet temperature
 *   4: TemperatureAmb: [K]    : Ambient temperature
 * Outputlist:
 *   1: PressureIn    : [Pa]   : Pressure in the cylinder chamber
 *   2: MassFlowIn    : [kg/s] : Mass flow into the cylinder chamber
 *   3: TemperatureOut: [K]    : Outlet temperature
 *   4: PLoss		  : [W]	   : Power loss
 *   5: PressureLoss  : [Pa]   : Pressure difference over the pipe
 *   
 * Config parameters:
 *   PipeDiameter   : [m] 
 *   PipeLength		: [m]
 *   
 * 
 * @author kraandre, sizuest
 *
 */
@XmlRootElement
public class Pipe extends APhysicalComponent implements Floodable{
//public class Pipe<T> extends APhysicalComponent<T>{

	@XmlElement
	protected String type;
	
	// Input parameters:
	//private IOContainer/*<Double>*/ pressureOut;
	//private IOContainer/*<Double>*/ massflowOut;
	//private IOContainer/*<Double>*/ temperatureIn;
	private IOContainer temperatureAmb;
	private IOContainer heatFlowIn;
	private FluidContainer fluidIn;
	
	//Saving last input values:
	//private double lastpressure  = 0.00;
	//private double lastmassflow  = 0.00;
	//private double lasttemperature  = 293.00;
	private double Reynolds		 = 0.00;
	private double Nusselt       = 0.00;
	private double lambda		 = 0.00;
		
	// Output parameters:
	//private IOContainer/*<Double>*/ pressureIn;
	//private IOContainer/*<Double>*/ massflowIn;
	private IOContainer/*<Double>*/ ploss;
	//private IOContainer/*<Double>*/ temperatureOut;
	private IOContainer/*<Double>*/ pressureloss;
	private IOContainer temperaturePipe;
	private FluidContainer fluidOut;
	
	// Parameters used by the model. 
	private double pipeDiameter;
	private double pipeLength;
	private double pipeHTC;
	private double volume;
	private String fluidType;
	private ThermalArray fluid;
	
	// Initial temperature
	double temperatureInit = 293;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Pipe() {
		super();
		
		this.type = "Example";
		this.temperatureInit = 293;
		this.fluidType = "Monoethylenglykol_34";
		
		init();
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
	 * Pipe constructor
	 * 
	 * @param type
	 * @throws Exception 
	 */
	//TODO manick: what to do with a pipe without fluid? only constructor with fluid allowed!
	public Pipe(String type) {
		super();
		
		this.type = type;
		init();
	}
	
	/**
	 * Pipe constructor
	 * 
	 * @param type
	 * @param temperatureInit 
	 * @throws Exception 
	 */
	//TODO manick: what to do with a pipe without fluid? only constructor with fluid allowed!
	/*
	public Pipe(String type) {
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		init();
	}
	*/
	
	/**
	 * Pipe constructor
	 * @param type
	 * @param temperatureInit
	 * @param material type
	 * @throws Exception
	 */
	public Pipe(String type, double temperatureInit, String fluidType) {
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		this.fluidType = fluidType;
		init();
	}
	
	/**
	 * Pipe constructor
	 * @param type
	 * @param temperatureInit
	 * @param fluid
	 * @throws Exception
	 */
	/*
	public Pipe(String type, double temperatureInit, ThermalArray fluid){
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		this.fluid = fluid;
		init();
	}
	*/
	
	/**
	 * Pipe constructor
	 * @param type
	 * @param temperatureInit
	 * @param fluid
	 * @throws Exception
	 */
	/*
	public Pipe(String type, double temperatureInit, String materialName, double volume, int numElements) {
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		this.fluid = new ThermalArray(materialName, volume, numElements);
		init();
	}
	*/
	
	/**
	 * Called from constructor or after unmarshaller.
	 * @throws Exception 
	 */
	private void init()
	{
		/* Define Input parameters */
		inputs         = new ArrayList<IOContainer/*<T>*/>();
		//pressureOut    = new IOContainer/*<Double>*/("PressureOut",    Unit.PA,       0.00, ContainerType.FLUIDDYNAMIC);
		//massflowOut    = new IOContainer/*<Double>*/("MassFlowOut",    Unit.KG_S,     0.00, ContainerType.FLUIDDYNAMIC);
		//temperatureIn  = new IOContainer/*<Double>*/("TemperatureIn",  Unit.KELVIN, 293.00, ContainerType.THERMAL);
		temperatureAmb = new IOContainer/*<Double>*/("TemperatureAmb", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		//pressureOut    = new IOContainer("PressureOut", Unit.PA, 0.00, ContainerType.FLUIDDYNAMIC);
		heatFlowIn     = new IOContainer("HeatFlowIn", Unit.WATT, 0.00, ContainerType.THERMAL);
		//inputs.add(/*(IOContainer<T>)*/ pressureOut);
		//inputs.add(/*(IOContainer<T>)*/ massflowOut);
		//inputs.add(/*(IOContainer<T>)*/ temperatureIn);
		inputs.add(/*(IOContainer<T>)*/ temperatureAmb);
		//inputs.add(pressureOut);
		inputs.add(heatFlowIn);
		
		/* Define output parameters */
		outputs        = new ArrayList<IOContainer/*<T>*/>();
		//pressureIn     = new IOContainer/*<Double>*/("PressureIn",         Unit.PA,      0.00, ContainerType.FLUIDDYNAMIC);
		//massflowIn     = new IOContainer/*<Double>*/("MassFlowIn",         Unit.KG_S,    0.00, ContainerType.FLUIDDYNAMIC);
		ploss          = new IOContainer/*<Double>*/("PLoss",              Unit.WATT,    0.00, ContainerType.THERMAL);
		pressureloss   = new IOContainer/*<Double>*/("PressureLoss",       Unit.PA,      0.00, ContainerType.FLUIDDYNAMIC);
		temperaturePipe= new IOContainer("TemperaturePipe", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		//temperatureOut = new IOContainer/*<Double>*/("TemperatureOut",     Unit.KELVIN,293.00, ContainerType.THERMAL);
		//outputs.add(/*(IOContainer<T>)*/ pressureIn);
		//outputs.add(/*(IOContainer<T>)*/ massflowIn);
		//outputs.add(/*(IOContainer<T>)*/ temperatureOut);
		outputs.add(/*(IOContainer<T>)*/ ploss);
		outputs.add(/*(IOContainer<T>)*/ pressureloss);
		outputs.add(temperaturePipe);

		
			
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
			pipeDiameter = params.getDoubleValue("PipeDiameter");
			pipeLength   = params.getDoubleValue("PipeLength");
			pipeHTC      = params.getDoubleValue("PipeHTC");
			//fluidType    = params.getString("Material");
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

		try{
			volume = Math.pow(pipeDiameter/2, 2)*Math.PI*pipeLength;
			if(fluidType != null){
				setFluid(fluidType);
			}
			//System.out.println("Pipe.init: adding fluid");
		}
		catch (Exception e){
			e.printStackTrace();
		}
		/* Thermal Array */
		/*
		volume = Math.pow(pipeDiameter/2,2)*Math.PI*pipeLength;
		int numElements = 10;
		fluid = new ThermalArray("Water", volume, numElements);
		fluid.getTemperature().setInitialCondition(temperatureInit);
		*/
		
		//add fluid In/Output
		fluidIn        = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);
		fluidOut        = new FluidContainer("FluidOut", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		outputs.add(fluidOut);
		
		/* State */
		/*
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid.getTemperature());
		*/
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
		if(0>pipeDiameter){
			throw new Exception("Pipe, type:" +type+ 
					": Non physical value: Variable 'pipeDiameter' must be bigger than zero!");
		}
		if(0>pipeLength){
			throw new Exception("Pipe, type:" +type+
					": Non physical value: Variable 'pistonLength' must be bigger than zero!");
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		double density, viscosity, heatCapacity, thermalConductivity, alphaFluid;
		
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
		fluid.setFlowRate(fluidOut.getFlowRate());
		
		/* ************************************************************************/
		/*         Calculate and set fluid values:                                */
		/*         TemperatureIn, Pressure, FlowRate, ThermalResistance,          */
		/*         HeatSource, TemperatureExternal                                */
		/* ************************************************************************/
		// Get current fluid properties
		density   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue(),  fluid.getPressure());
		viscosity = fluid.getMaterial().getViscosity(fluid.getTemperature().getValue(), fluid.getPressure());
		heatCapacity = fluid.getMaterial().getHeatCapacity();
		thermalConductivity = fluid.getMaterial().getThermalConductivity();

		/* calculate Reynolds-Number pipe: Re = velocity [m/s] * diameter [m] / kin_viscosity [m^2/s]
		 * Re = (flowRate [m^3/s] / area [m^2]) * diameter [m] / (dyn_viscosity [kg/(m s)] / density [kg/m^3])
		 * calculate Nusselt-Number (Source for equations: VDI-Waermeatlas, 2013, Springer, page 28 & 759
		 */
		Reynolds = (fluidOut.getFlowRate() / (Math.pow(pipeDiameter/2,2)*Math.PI)) * pipeDiameter * density /(viscosity/1000);
		
		if (Reynolds <= 0){
			Reynolds = 1;
			Nusselt  = 1;
		}
		
		if (Reynolds < 2300) { //laminar flow
			lambda = 64/Reynolds;
			Nusselt = 4.36;			
		} else { //turbulent flow
			lambda = 0.3164/Math.pow(Reynolds, 0.25);
			double Prandtl = viscosity * heatCapacity / thermalConductivity; //n * cp / lambda
			double xi = Math.pow(1.8 * Math.log10(Reynolds)-1.5, -2);
			Nusselt = (Reynolds * Prandtl * xi/8)/(1+12.7*Math.sqrt(xi/8)*(Math.pow(Prandtl, 2/3)-1))*(1+Math.pow(pipeDiameter/pipeLength, 2/3));
		}
		
		/* with Nusselt-Number: calculate alphaFluid
		 */
		alphaFluid = Nusselt * lambda / pipeLength;
		
		/* further needed: alphaAir and pipeArea
		 * and Rayleigh for air: Ra = beta * g * length^3 * DeltaT / (nu kappa)
		 *  where DeltaT assumed as fluidTemp-ambTemp
		 *  lambdaAir = 0.02587
		 *  Prandlt-Air= 0.7, f3(Pr) = 0.325
		 */
		double pipeArea = 2 * Math.PI * pipeDiameter/2 * pipeLength;
		double deltaT   = fluid.getTemperature().getValue()-temperatureAmb.getValue();
		double Rayleigh = (0.003421*9.81*Math.pow(pipeLength,3)*deltaT)/(153.2*Math.pow(10,-7)* 216.3*Math.pow(10,-7));
		//NusseltAir = (0.752 + 0.387(Ra * f3(Pr))^(1/6))^2
		double NusseltAir= Math.pow(0.752+0.387*Math.pow(Rayleigh*0.325,1/6),2);
		double alphaAir = NusseltAir * 0.02587 / pipeLength;
		
		/* calculate overall thermal Resistance of pipe
		 * 
		 * R_tot = 1/(alphaFluid*Area)      //convetion inside
		 *       + length/(lambda * Area)   //conduction in pipe wall, assumed as 0
		 *       + 1/(alphaAir*Area)        //convection outside
		 * assumptions: r_in = 0.005, r_out= 0.0075, lambda_pipe = 0.25 (plastic)
		 */
		double thermalResistance = 1/(alphaFluid * pipeArea) + pipeLength / (0.25 * pipeArea) + 1/(alphaAir * pipeArea);
		
		/* calculate pressureloss with given lambda
		 */
		if(fluid.getFlowRate()!=0) {
			pressureloss.setValue(lambda*pipeLength*Math.pow(fluid.getFlowRate(), 2)/(pipeDiameter*2*density*Math.pow(Math.PI/4*Math.pow(pipeDiameter, 2), 2)));
		} else {
			pressureloss.setValue(0.00);
		}
		
		// set array boundary conditions
		fluid.setThermalResistance(thermalResistance);
		fluid.setHeatSource(pressureloss.getValue()*fluid.getFlowRate()/density + heatFlowIn.getValue());
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
		
		ploss.setValue(fluid.getHeatLoss());
		temperaturePipe.setValue(fluid.getTemperature().getValue());
		
		System.out.println("Pipe: " + fluid.getPressure() + " " + fluid.getFlowRate() + " " + fluid.getTemperature().getValue() + " " + thermalResistance + " " + fluid.getHeatLoss());
	}

	/**
	 * if pipe goes into tank, the pressure difference leads to a flowRate!
	 * @param lastPipeBeforeTank
	 */
	public void update(boolean lastPipeBeforeTank) {
		
		double flowRate, pressureDifference, area, density, friction;

		pressureDifference = fluid.getPressure() - 100000;
		area = Math.PI * Math.pow(pipeDiameter/2, 2);
		density   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue(),  fluid.getPressure());
		friction = 1;

		// see: http://www.techniker-forum.de/thema/abhaengigkeit-von-volumenstrom-im-druckverlust.59218/
		flowRate = Math.sqrt(2*pressureDifference*Math.pow(area,2)/(density*friction));
		
		fluidOut.setFlowRate(flowRate);
		
		update();
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
	
	public String getFluidType(){
		return fluid.getMaterial().getType();
	}
	
	/*
	public void setFluid(ThermalArray fluid) {
		this.fluid = fluid;	
	}
	*/
	
	public void setFluid(String type){
		/* ThermalArray */
		this.fluid = new ThermalArray(type, volume, 1);
		this.fluidType = type;
		
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
}
