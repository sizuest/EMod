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

import java.lang.Math;

import ch.ethz.inspire.emod.model.thermal.ThermalArray;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Spindle model class.
 * Implements the physical model Spindels.
 * 
 * 
 * Inputlist:
 *   1: RotSpeed      : [rpm] : Actual rotational speed
 *   2: Torque        : [Nm]  : Actual torque
 *   3: TemperatureIn : [K]   : Coolant inlet temperature
 *   4: CoolantFlow   : [kg/s]: Coolant flow rate
 * Outputlist:
 *   1: PTotal         : [W]  : Calculated total energy demand
 *   2: PLoss          : [W]  : Calculated power loss
 *   3: PUse           : [W]  : Calculated mechanical power
 *   4: TemperatureOut : [K]  : Coolant outlet temperature
 *   5: CAirFlow       : [l/min]: Compressed air flow rate
 *   
 * Config parameters:
 *   TODO
 * 
 * @author sizuest
 *
 */
@XmlRootElement
public class Spindle extends APhysicalComponent implements Floodable{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer rotspeed;
	private IOContainer torque;
	// Output parameters:
	private IOContainer pmech;
	private IOContainer ploss;
	private IOContainer pel;
	private IOContainer cairFlow;
	private IOContainer temperature;
	//Fluid
	private FluidContainer fluidIn, fluidOut;
	// Fluid Properties
	FluidCircuitProperties fluidProperties;
	
	
	// Parameters used by the model. 
	private String[] bearingType;
	private String motorType;
	private double[] preloadForce;
	private double massStructure;
	private double alphaCoolant, volumeCoolant;
	private double lastTemperatureIn = Double.NaN;
	private double[] agFrictCoeff;
	private double 	pressureLossCoeff;
	private double lastPressureDrop = 0;
	
	// Submodels
	private MotorAC motor;
	private Bearing[] bearings;
	private ThermalElement structure;
	private ThermalArray coolant;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Spindle() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Linear Motor constructor
	 * 
	 * @param type
	 */
	public Spindle(String type) {
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
		inputs = new ArrayList<IOContainer>();
		rotspeed      = new IOContainer("RotSpeed",      Unit.RPM, 0, ContainerType.MECHANIC);
		torque        = new IOContainer("Torque",        Unit.NEWTONMETER, 0, ContainerType.MECHANIC);
		inputs.add(rotspeed);
		inputs.add(torque);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pmech          = new IOContainer("PUse",           Unit.WATT,   0, ContainerType.MECHANIC);
		ploss          = new IOContainer("PLoss",          Unit.WATT,   0, ContainerType.THERMAL);
		pel            = new IOContainer("PTotal",         Unit.WATT,   0, ContainerType.ELECTRIC);
		cairFlow       = new IOContainer("CAirFlow",       Unit.NONE,   0, ContainerType.FLUIDDYNAMIC);
		temperature    = new IOContainer("Temperature",    Unit.KELVIN, 293, ContainerType.THERMAL);
		outputs.add(pel);
		outputs.add(ploss);
		outputs.add(pmech);
		outputs.add(cairFlow);
		outputs.add(temperature);
		
		/* Define fluid in-/outputs */
		fluidIn        = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);
		fluidOut        = new FluidContainer("FluidOut", Unit.NONE, ContainerType.FLUIDDYNAMIC);
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
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			
			motorType         = params.getString("MotorType");
			preloadForce      = params.getDoubleArray("PreloadForce");
			bearingType       = params.getStringArray("BearingType");
			massStructure     = params.getDoubleValue("StructureMass");
			volumeCoolant     = params.getDoubleValue("CoolantVolume");
			alphaCoolant      = params.getDoubleValue("CoolantHTC");
			agFrictCoeff      = params.getDoubleArray("AirGapFrictionCoeff");
			pressureLossCoeff = params.getDoubleValue("PressureLossCoefficient");
			
			
			
			// Create Sub Elements
			motor = new MotorAC(motorType);
			
			bearings = new Bearing[bearingType.length];
			for (int i=0; i<bearingType.length; i++)
				bearings[i] = new Bearing(bearingType[i]);
			
			// = new HomogStorage(materialStructure, massStructure);
			//bearingLosses = structure.getInput("In");
			//coilLosses    = structure.getInput("In");
			structure = new ThermalElement(params.getString("StructureMaterial"), massStructure);
			coolant = new ThermalArray("Example", volumeCoolant, 20);
			
			// Change state names
			structure.getTemperature().setName("TemperatureStructure");
			coolant.getTemperature().setName("TemperatureCoolant");
			
			// Add states
			dynamicStates = new ArrayList<DynamicState>();
			dynamicStates.add(0, structure.getTemperature());
			dynamicStates.add(1, coolant.getTemperature());
			
			/* Fluid circuit parameters */
			fluidProperties = new FluidCircuitProperties();
			fluidProperties.setMaterial(coolant.getMaterial());
			
			
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
		// TODO
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		//TODO Workarround
		if(fluidIn.getTemperature()<=0) {
			if (Double.isNaN(lastTemperatureIn))
				lastTemperatureIn = structure.getTemperature().getValue();
			fluidIn.setTemperature(lastTemperatureIn);
		}
		else
			lastTemperatureIn = fluidIn.getTemperature();
		
		double frictionTorque = 0;
		double frictionLosses = 0;
		
		// Bearings
		if(rotspeed.getValue()!=0)
			for (int i=0; i<bearings.length; i++){
				bearings[i].getInput("RotSpeed").setValue(rotspeed.getValue());
				bearings[i].getInput("ForceRadial").setValue(0);//TODO
				bearings[i].getInput("ForceAxial").setValue(preloadForce[i]);
				bearings[i].update();
				
				frictionTorque+=bearings[i].getOutput("Torque").getValue();
				frictionLosses+=bearings[i].getOutput("PLoss").getValue();
			}
		
		// Air Gap
		frictionTorque += Math.pow(rotspeed.getValue()/30*Math.PI, agFrictCoeff[0])*agFrictCoeff[1];
		frictionLosses += Math.pow(rotspeed.getValue()/30*Math.PI, agFrictCoeff[0])*agFrictCoeff[1]*rotspeed.getValue()/30*Math.PI;
		
		motor.getInput("RotSpeed").setValue(rotspeed.getValue());
		motor.getInput("Torque").setValue(frictionTorque+torque.getValue());
		motor.update();
		
		pmech.setValue(rotspeed.getValue()*torque.getValue()*Math.PI/30.0);
		pel.setValue(motor.getOutput("PTotal").getValue());
		// ploss.setValue(motor.getOutput("PLoss").getValue()+frictionLosses);
		ploss.setValue(0);
		
				
		// Coolant
		coolant.setThermalResistance(1/alphaCoolant);
		coolant.setFlowRate(fluidProperties.getFlowRate());
		coolant.setHeatSource(0);
		coolant.setPressure(100000); //TODO
		coolant.setTemperatureExternal(structure.getTemperature().getValue());
		coolant.setTemperatureIn(fluidIn.getTemperature());
		
		// Pressure loss
		fluidProperties.setPressureDrop(0.5*lastPressureDrop+0.5*pressureLossCoeff*Math.pow(fluidProperties.getFlowRate(), 2));
		lastPressureDrop = fluidProperties.getPressureDrop();
		
		// Thermal flows
		structure.setHeatInput(motor.getOutput("PLoss").getValue()+frictionLosses);
		structure.addHeatInput(coolant.getHeatLoss());
				
		// Update submodels
		structure.integrate(timestep);
		coolant.integrate(timestep);
		
		// Write outputs
		temperature.setValue(structure.getTemperature().getValue());
		fluidOut.setTemperature(coolant.getTemperatureOut());
		fluidOut.setPressure(fluidIn.getPressure()-fluidProperties.getPressureDrop());
		
		
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

	@Override
	public FluidCircuitProperties getFluidProperties() {
		return this.fluidProperties;
	}
	
}
