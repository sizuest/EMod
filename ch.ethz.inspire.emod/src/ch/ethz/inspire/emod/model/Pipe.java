/***********************************
 * $Id: Cylinder.java 103 2013-10-31 13:39:36Z kraandre $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/src/ch/ethz/inspire/emod/model/Cylinder.java $
 * $Author: kraandre $
 * $Date: 2013-10-31 14:39:36 +0100 (Do, 31 Okt 2013) $
 * $Rev: 103 $
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
public class Pipe extends APhysicalComponent /*implements Floodable*/{
//public class Pipe<T> extends APhysicalComponent<T>{

	@XmlElement
	protected String type;
	
	// Input parameters:
	//private IOContainer/*<Double>*/ pressureOut;
	//private IOContainer/*<Double>*/ massflowOut;
	//private IOContainer/*<Double>*/ temperatureIn;
	private IOContainer temperatureAmb;
	private FluidContainer fluidIn;
	
	//Saving last input values:
	//private double lastpressure  = 0.00;
	//private double lastmassflow  = 0.00;
	//private double lasttemperature  = 293.00;
	private double Reynolds		 = 0.00;
	private double lambda		 = 0.00;
		
	// Output parameters:
	//private IOContainer/*<Double>*/ pressureIn;
	//private IOContainer/*<Double>*/ massflowIn;
	private IOContainer/*<Double>*/ ploss;
	//private IOContainer/*<Double>*/ temperatureOut;
	private IOContainer/*<Double>*/ pressureloss;
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
	public Pipe(String type, double temperatureInit) {
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
		//inputs.add(/*(IOContainer<T>)*/ pressureOut);
		//inputs.add(/*(IOContainer<T>)*/ massflowOut);
		//inputs.add(/*(IOContainer<T>)*/ temperatureIn);
		inputs.add(/*(IOContainer<T>)*/ temperatureAmb);
		//inputs.add(pressureOut);
		
		/* Define output parameters */
		outputs        = new ArrayList<IOContainer/*<T>*/>();
		//pressureIn     = new IOContainer/*<Double>*/("PressureIn",         Unit.PA,      0.00, ContainerType.FLUIDDYNAMIC);
		//massflowIn     = new IOContainer/*<Double>*/("MassFlowIn",         Unit.KG_S,    0.00, ContainerType.FLUIDDYNAMIC);
		ploss          = new IOContainer/*<Double>*/("PLoss",              Unit.WATT,    0.00, ContainerType.THERMAL);
		pressureloss   = new IOContainer/*<Double>*/("PressureLoss",       Unit.PA,      0.00, ContainerType.FLUIDDYNAMIC);
		//temperatureOut = new IOContainer/*<Double>*/("TemperatureOut",     Unit.KELVIN,293.00, ContainerType.THERMAL);
		//outputs.add(/*(IOContainer<T>)*/ pressureIn);
		//outputs.add(/*(IOContainer<T>)*/ massflowIn);
		//outputs.add(/*(IOContainer<T>)*/ temperatureOut);
		outputs.add(/*(IOContainer<T>)*/ ploss);
		outputs.add(/*(IOContainer<T>)*/ pressureloss);

		
			
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

		/* Thermal Array */
		volume = Math.pow(pipeDiameter/2,2)*Math.PI*pipeLength;
		int numElements = 10;
		fluid = new ThermalArray(fluidType, volume, numElements);
		fluid.getTemperature().setInitialCondition(temperatureInit);
		
		//add fluid In/Output
		fluidIn        = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);
		fluidOut        = new FluidContainer("FluidOut", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		outputs.add(fluidOut);
		
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
		double density, viscosity;
		
		// Update inputs
		//direction of calculation
		//temperature [K]    : fluidIn --> fluidOut
		//pressure    [Pa]   : fluidIn --> fluidOut
		//flowRate:   [m^3/s]: fluidIn <-- fluidOut
		fluid.setTemperatureIn(fluidIn.getTemperature());
		fluid.setPressure(fluidIn.getPressure());
		//fluid.setFlowRate(fluidOut.getFlowRate());

		// Get current fluid properties
		density   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue(),  fluid.getPressure());
		viscosity = fluid.getMaterial().getViscosity(fluid.getTemperature().getValue(), fluid.getPressure());
		
		Reynolds = pipeDiameter*fluid.getFlowRate()/(density*viscosity*Math.pow(10,-6)*Math.PI/4*Math.pow(pipeDiameter, 2));
		
		//TODO manick: what to do with lambda when pressure difference occurs -> massflow is starting? (Reynolds = 0 -> lambda = NAN)
		if (Reynolds == 0)
			Reynolds = 1;
		
		if (Reynolds < 2300)
			lambda = 64/Reynolds;
		else
			lambda = 0.3164/Math.pow(Reynolds, 0.25);
		
		if(fluid.getFlowRate()!=0)
			pressureloss.setValue(lambda*pipeLength*Math.pow(fluid.getFlowRate(), 2)/(pipeDiameter*2*density*Math.pow(Math.PI/4*Math.pow(pipeDiameter, 2), 2)));
		else
			pressureloss.setValue(0.00);
		
		
		if(fluidIn.getPressure() - fluidOut.getPressure() != 0){
			System.out.println("*** change of pressure! ***");
			//http://www.tlv.com/global/TI/calculator/water-pressure-loss-through-piping.html
			//fluid.setFlowRate(fluid.getFlowRate() + Math.sqrt(2*pipeDiameter*(fluidIn.getPressure()-fluidOut.getPressure())/(/*lambda**/pipeLength*density)));
			fluid.setFlowRate(0.1);
		}
				
		// set array boundary conditions
		fluid.setThermalResistance(1/(pipeHTC*pipeLength*Math.pow(pipeDiameter/2, 2)*Math.PI));//TODOs
		fluid.setHeatSource(pressureloss.getValue()*fluid.getFlowRate()/density);
		fluid.setTemperatureExternal(temperatureAmb.getValue());
		
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

		ploss.setValue(fluid.getHeatLoss());
		
		// Update Layer Model

	
		System.out.println("pipe fluidvalues: " + fluid.getPressure() + " " + pressureloss.getValue() + " " + fluid.getFlowRate() + " " + fluid.getTemperature().getValue() + " " + density + " " + viscosity + " " + Reynolds + " " + lambda);
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
	
	public ThermalArray getFluid(){
		return fluid;
	}

	public void setFluid(ThermalArray fluid) {
		this.fluid = fluid;
		
	}
}
