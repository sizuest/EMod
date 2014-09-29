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

import ch.ethz.inspire.emod.model.Material;
import ch.ethz.inspire.emod.model.thermal.LayerStorage;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
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
public class Pipe extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer pressureOut;
	private IOContainer massflowOut;
	private IOContainer temperatureIn;
	private IOContainer temperatureAmb;
	
	//Saving last input values:
	
	private double lastpressure  = 0;
	private double lastmassflow  = 0;
	private double lasttemperature  = 293;
	private double Reynolds		 = 0;
	private double lambda		 = 0;
	
	// Fluid properties
	private Material fluid;
	private LayerStorage layerStorage;
	
	// Output parameters:
	private IOContainer pressureIn;
	private IOContainer massflowIn;
	private IOContainer ploss;
	private IOContainer temperatureOut;
	private IOContainer pressureloss;
	
	// Parameters used by the model. 
	private double pipeDiameter;
	private double pipeLength;
	private String fluidType;
	
	// Initial temperature
	double temperatureInit = 0;
	
	// Dynamic State
	private DynamicState temperatureState;
	
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
	public Pipe(String type, double temperatureInit) {
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
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
		pressureOut    = new IOContainer("PressureOut",    Unit.PA,       0, ContainerType.FLUIDDYNAMIC);
		massflowOut    = new IOContainer("MassFlowOut",    Unit.KG_S,     0, ContainerType.FLUIDDYNAMIC);
		temperatureIn  = new IOContainer("TemperatureIn",  Unit.KELVIN, 293, ContainerType.THERMAL);
		temperatureAmb = new IOContainer("TemperatureAmb", Unit.KELVIN, 293, ContainerType.THERMAL);
		inputs.add(pressureOut);
		inputs.add(massflowOut);
		inputs.add(temperatureIn);
		inputs.add(temperatureAmb);
		
		/* Define output parameters */
		outputs        = new ArrayList<IOContainer>();
		pressureIn     = new IOContainer("PressureIn",         Unit.PA,      0, ContainerType.FLUIDDYNAMIC);
		massflowIn     = new IOContainer("MassFlowIn",         Unit.KG_S,    0, ContainerType.FLUIDDYNAMIC);
		ploss          = new IOContainer("PLoss",              Unit.WATT,    0, ContainerType.THERMAL);
		pressureloss   = new IOContainer("PressureLoss",       Unit.PA,      0, ContainerType.FLUIDDYNAMIC);
		temperatureOut = new IOContainer("TemperatureOut",     Unit.KELVIN,293, ContainerType.THERMAL);
		outputs.add(pressureIn);
		outputs.add(massflowIn);
		outputs.add(temperatureOut);
		outputs.add(ploss);
		outputs.add(pressureloss);
		
		/* State */
		dynamicStates    = new ArrayList<DynamicState>();
		temperatureState = newDynamicState("Temperature", Unit.KELVIN);
		
		if(temperatureInit!=0)
			temperatureState.setInitialCondition(temperatureInit);
		else
			try {
				temperatureState.loadInitialCondition();
			} catch (Exception e1) {
				System.err.print("Failed to load initial condition for "+temperatureState.getInitialConditionName()+": "+e1.getMessage());
			}


			
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
			fluidType    = params.getString("Material");
		
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
		
		/* Create new layer storage object */
		try {
			layerStorage = new LayerStorage(type, "Pipe", temperatureState.getInitialValue());
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		/* Create new fluid object: */
		try {
			fluid = new Material(fluidType);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
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
		
		layerStorage.update();
		
		lastpressure    = pressureOut.getValue();
		lastmassflow    = massflowOut.getValue();
		lasttemperature = (layerStorage.getInput("TemperatureIn").getValue()+layerStorage.getOutput("TemperatureOut").getValue())/2;
		
		// Update Losses
		ploss.setValue(layerStorage.getOutput("PLoss").getValue());
		
		// Get current fluid properties
		density   = fluid.getDensity(lasttemperature,  lastpressure);
		viscosity = fluid.getViscosity(lasttemperature, lastpressure);
		
		Reynolds = pipeDiameter*lastmassflow/(density*viscosity*Math.pow(10,-6)*Math.PI/4*Math.pow(pipeDiameter, 2));
		
		if (Reynolds < 2300)
			lambda = 64/Reynolds;
		else
			lambda = 0.3164/Math.pow(Reynolds, 0.25);
		
		if(lastmassflow!=0)
			pressureloss.setValue(lambda*pipeLength*Math.pow(lastmassflow, 2)/(pipeDiameter*2*density*Math.pow(Math.PI/4*Math.pow(pipeDiameter, 2), 2)));
		else
			pressureloss.setValue(0);
		
		// Update mass flow and pressure
		massflowIn.setValue(massflowOut.getValue());				
		pressureIn.setValue(pressureOut.getValue()+pressureloss.getValue());
		
		temperatureOut.setValue(layerStorage.getOutput("TemperatureOut").getValue());
		
		// Update Layer Model
		layerStorage.getInput("TemperatureIn").setValue(temperatureIn.getValue());
		layerStorage.getInput("TemperatureAmb").setValue(temperatureAmb.getValue());
		layerStorage.getInput("MassFlow").setValue(massflowOut.getValue());
		layerStorage.getInput("HeatSource").setValue(pressureloss.getValue()*massflowOut.getValue()/density);	
		layerStorage.getInput("Pressure").setValue(lastpressure);
		
		// Update State
		temperatureState.setValue(layerStorage.getOutput("TemperatureAvg").getValue());
	
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public void setSimulationPeriod(double sampleperiod){
		this.sampleperiod = sampleperiod;
		layerStorage.setSimulationPeriod(sampleperiod);
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
}
