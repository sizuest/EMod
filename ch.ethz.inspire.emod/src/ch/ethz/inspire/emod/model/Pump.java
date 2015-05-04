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

import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
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
 *   1: State       : [-]    : State of the Pump
 *   2: FluidIn     : [-]    : Fluid flowing into Pump
 * Outputlist:
 *   1: PTotal      : [W]    : Demanded electrical power
 *   2: PLoss       : [W]    : Thermal pump losses
 *   3: PUse        : [W]    : Power in the pluid
 *   4: Temperature : [K]    : Pump structural Temperature
 *   5: FluidOut    : [-]    : Fluid flowing out of the Pump
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
public class Pump extends APhysicalComponent implements Floodable{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer pumpCtrl;
	private IOContainer temperatureAmb;
	private FluidContainer fluidIn;
	
	// Output parameters:
	private IOContainer pel;
	private IOContainer pth;
	private IOContainer pmech;
	private FluidContainer fluidOut;
	private IOContainer temperaturePump;
	
	// Parameters used by the model. 
	private double[] pressureSamples;  	// Samples of pressure [Pa]
	private double[] flowRateSamples;   // Samples of flow rate [m^3/s]
	private double[] powerSamples;		// Samples of power demand [W]
	private double[] pressureSamplesR, powerSamplesR, flowRateSamplesR;
	
	private double temperatureInit;
	
	// Pump Structure
	private ThermalElement structure;
	
	
	// Fluid Properties
	FluidCircuitProperties fluidProperties;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Pump() {
		super();
		
		//TODO manick: change init
		this.type = "Example";
		this.temperatureInit = 293;
		init();
		this.fluidProperties.getMaterial().setMaterial("Monoethylenglykol_34");
	}
	
	public Pump(String type, double temperatureInit, String fluidType){
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		init();
		this.fluidProperties.getMaterial().setMaterial(fluidType);
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
	public Pump(String type) {
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
		pumpCtrl       = new IOContainer("State", Unit.NONE, 0, ContainerType.CONTROL);
		temperatureAmb = new IOContainer("TemperatureAmb", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		inputs.add(pumpCtrl);
		inputs.add(temperatureAmb);
		
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
			powerSamples    = params.getDoubleArray("PowerSamples");
						
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
			powerSamplesR = new double[powerSamples.length];
			for (int i=0; i<powerSamplesR.length; i++) {
				powerSamplesR[i] = powerSamples[powerSamplesR.length-1-i];
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read material parameters */
		try {
			structure = new ThermalElement(params.getString("Material"), params.getDoubleValue("Mass"));
			dynamicStates = new ArrayList<DynamicState>();
			dynamicStates.add(structure.getTemperature());
		}
		catch (Exception e){ 
			structure = null;
			System.out.println("Pump type:" +type+ 
					": No material or mass provided! Assuming zero mass.");
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
		
		/* Define FluidIn parameter */
		fluidIn        = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);

		/* Define FluidOut parameter */
		fluidOut        = new FluidContainer("FluidOut", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		outputs.add(fluidOut);
		
		/* Define FlowRate */
		fluidProperties = new FluidCircuitProperties();
		fluidProperties.setMaterial(new Material("Example"));
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
		if (pressureSamples.length != powerSamples.length) {
			throw new Exception("Pump, type:" +type+ 
					": Dimension missmatch: Vector 'pressureSamples' must have same dimension as " +
					"'powertSamples' (" + pressureSamples.length + "!=" + powerSamples.length + ")!");
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
		
		// Check size
		for(int i=1; i<powerSamples.length;i++)
			if (powerSamples[i]<=0){
				throw new Exception("Pump, type:" +type+ 
						": Negative or zero value: Pump power must be strictly positive!");
			}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		double heat2Fluid = 0;
		
		/* Calculate fluid properties */
		fluidProperties.setPressureDrop(fluidProperties.getSumFront());//fluidOut.getPressure() - fluidIn.getPressure();
		
		
		/* If pump is running calculate flow rate and power demand */
		if(pumpCtrl.getValue()>0){
			// Resulting flow rate
			fluidProperties.setFlowRate(Algo.linearInterpolation(fluidProperties.getPressureDrop(), pressureSamplesR, flowRateSamplesR));
			// Resulting power demand
			pel.setValue(Algo.linearInterpolation(fluidProperties.getPressureDrop(), pressureSamplesR, powerSamplesR));
		}
		else{
			fluidProperties.setFlowRate(0.0);
			pel.setValue(0);
		}
			
		
		/* 
		 * The mechanical power is given by the pressure and the voluminal flow:
		 * Pmech = pFluid [Pa] * Vdot [m3/s]
		 */
		pmech.setValue( fluidProperties.getFlowRate() * fluidProperties.getPressureDrop() );
		
		/* 
		 * The Losses are the difference between electrical and mechanical power
		 */
		pth.setValue(pel.getValue()-pmech.getValue());		
				
		/* Losses */
		// Losses go to structure if structure exist
		if(null != structure){
			// Motor
			structure.setHeatInput(pth.getValue());
			// To Fluid
			//TODO HTC Model
			heat2Fluid = 1*(structure.getTemperature().getValue()-fluidIn.getTemperature());
			structure.addHeatInput(-heat2Fluid);
			// Integrate
			structure.integrate(timestep);
		}
		else
			heat2Fluid = pth.getValue();
		
		// Add losses to the fluid
		if(fluidProperties.getFlowRate()!=0)
			fluidOut.setTemperature(fluidIn.getTemperature() + 
					heat2Fluid/(fluidProperties.getFlowRate()*fluidProperties.getMaterial().getDensity(fluidIn.getTemperature(), fluidIn.getPressure())*fluidProperties.getMaterial().getHeatCapacity()));
		else
			fluidOut.setTemperature(fluidIn.getTemperature());
		
		
		temperaturePump.setValue(fluidIn.getTemperature());
		fluidOut.setPressure(fluidIn.getPressure()+fluidProperties.getPressureDrop());
		
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
	 * @param type the type of the pump to set
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
		return fluidProperties.getMaterial().getType();
	}
	

	@Override
	public FluidCircuitProperties getFluidProperties() {
		return fluidProperties;
	}
}
