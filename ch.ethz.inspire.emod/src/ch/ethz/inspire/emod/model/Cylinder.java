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

import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.ShiftProperty;

/**
 * General Cylinder model class.
 * Implements the physical model of a hydraulic cylinder
 * 
 * Assumptions:
 * -3 States (idle, moving, extended&hold)
 * -Leakage only occurs internally and is treated as annular passage flow.
 * -Fitting between cylinder and piston is assumed as H7/h6.
 * -Friction is taken into account by hydraulic-mechanic efficiency
 * 
 * 
 * Inputlist:
 *   1: Force   	      : [N]      : Required force
 *   2: Velocity   	      : [mm/min] : Required displacement velocity
 *   3: FluidIn           : [-]      : Fluid input
 * Outputlist:
 *   1: PUse		  	  : [W]	 	 : Mechanical power
 *   2: PLoss		  	  : [W]	 	 : Power loss
 *   3: PHydraulic   	  : [W]	 	 : Hydraulic power
 *   4: FluidOut          : [-]      : Fluid output
 *   
 * Config parameters:
 *   PistonDiameter 	: [m] 
 *   PistonThickness	: [m]
 *   Stroke				: [m]
 *   Efficiency			: [] 	 : Hydraulic-mechanic
 *   PistonRodDiameter	: [m]
 *   ConnectionDiameter : [m]    : Dyameter of the connection
 *   PMax				: [bar]	 : Maximum allowed pressure in the cylinder
 *   CylinderType	    : []	 : According to this parameter, the cylinder type is chosen.
 *   							   1 = single-action cylinder, 2 = double action cylinder 
 * 
 * @author kraandre
 *
 */
@XmlRootElement
public class Cylinder extends APhysicalComponent implements Floodable {

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer force;
	private IOContainer velocity;
	private FluidContainer fluidIn;
	
	// Output parameters:
	private IOContainer pmech;
	private IOContainer ploss;
	private IOContainer phydr;
	private FluidContainer fluidOut;
	
	// Parameters used by the model. 
	private double pistonDiameter;
	private double pistonThickness;
	private double stroke;
	private double H_7;					//Tolerance Cylinder
	private double h_6;					//Tolerance Piston
	private double efficiency;
	private double pistonRodDiameter;
	private double k = 0.5;				//Geometric loss koefficient
	private double pmax;
	private int cylinderType;
	private double area;
	private double connectionDiameter;
	private double flowRateLeak = 0;
	private ShiftProperty<Double> flowRate = new ShiftProperty<Double>(0.0);
	
	/* Fluid Properties */
	FluidCircuitProperties fluidProperties;
	
	/* Position */
	DynamicState position;

	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Cylinder() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Cylinder constructor
	 * 
	 * @param type
	 */
	public Cylinder(String type) {
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
		inputs    = new ArrayList<IOContainer>();
		force     = new IOContainer("Force",     new SiUnit(Unit.NEWTON),     0, ContainerType.MECHANIC);
		velocity  = new IOContainer("Velocity",  new SiUnit(Unit.M_S),     0, ContainerType.MECHANIC);
		fluidIn   = new FluidContainer("FluidIn", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC);
		inputs.add(force);
		inputs.add(velocity);
		inputs.add(fluidIn);
		
		/* Define output parameters */
		outputs     = new ArrayList<IOContainer>();
		pmech       = new IOContainer("PUse",       new SiUnit(Unit.WATT), 0, ContainerType.MECHANIC);
		ploss       = new IOContainer("PLoss",      new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		phydr       = new IOContainer("PTotal",     new SiUnit(Unit.WATT), 0, ContainerType.FLUIDDYNAMIC);
		fluidOut    = new FluidContainer("FluidOut", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC);
		
		outputs.add(pmech);
		outputs.add(ploss);
		outputs.add(phydr);
		outputs.add(fluidOut);
		
		/* Define state */
		position = new DynamicState("Position", new SiUnit(Unit.M));
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(position);
		
		/* Fluid Properties */
		fluidProperties = new FluidCircuitProperties();

			
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
			pistonDiameter 	    = params.getDoubleValue("PistonDiameter");
			pistonThickness		= params.getDoubleValue("PistonThickness");
			stroke				= params.getDoubleValue("CylinderStroke");
			efficiency			= params.getDoubleValue("Efficiency");
			pistonRodDiameter	= params.getDoubleValue("PistonRodDiameter");
			pmax				= params.getDoubleValue("PMax");
			cylinderType		= params.getIntValue("CylinderType");
			connectionDiameter  = params.getDoubleValue("ConnectionDiameter");
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
		
		/* Area */
		if(cylinderType == 1)
			area = Math.PI/4*(Math.pow(pistonDiameter,2));
		else if(cylinderType == 2)
			area = Math.PI/4*(Math.pow(pistonDiameter, 2)-Math.pow(pistonRodDiameter, 2));
		
		
		/* Choosing of the fitting according to the piston diameter */
		if(pistonDiameter > 0.03 && pistonDiameter <= 0.05)
		{
			H_7 = 12.5*Math.pow(10,-6);
			h_6 = 8*Math.pow(10,-6);
		}
		
		else if(pistonDiameter > 0.05 && pistonDiameter <= 0.065)
		{
			H_7 = 15*Math.pow(10,-6);
			h_6 = 19/2*Math.pow(10,-6);
		}
		
		else if(pistonDiameter > 0.065 && pistonDiameter <= 0.08)
		{
			H_7 = 15*Math.pow(10,-6);
			h_6 = 19/2*Math.pow(10,-6);
		}
		
		else if(pistonDiameter > 0.08 && pistonDiameter <= 0.1)
		{
			H_7 = 17.5*Math.pow(10,-6);
			h_6 = 11*Math.pow(10,-6);
		}
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
		if(0>pistonDiameter){
			throw new Exception("Cylinder, type:" +type+ 
					": Non physical value: Variable 'PistonDiameter' must be bigger than zero!");
		}
		
		if(0>pistonThickness){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'PistonThickness' must be bigger than zero!");
		}
		
		if(0>efficiency || efficiency>1){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'Efficiency' must reach from bigger than zero to 1!");
		}
		
		if(0>pistonRodDiameter){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'PistonRodDiameter' must be bigger than zero!");
		}
		
		if(0>pmax){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'PMax' must be bigger than zero!");
		}
		
		if(cylinderType != 1 && cylinderType != 2){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'PMax' must be 1 or 2!");
		}
				
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		double pressureDrop = 0,
		       viscosity,
		       density,
		       velocity,
		       heatCapacity, 
		       deltaPosition,
		       deltaTemperature;
		
		/* Material properties */
		viscosity    = fluidProperties.getMaterial().getViscosityDynamic(fluidIn.getTemperature());
		density      = fluidProperties.getMaterial().getDensity(fluidIn.getTemperature(), fluidIn.getPressure());
		heatCapacity = fluidProperties.getMaterial().getHeatCapacity();
		
		/* Position */
		velocity = this.velocity.getValue();
		deltaPosition = velocity*timestep;
		
		if(position.getValue()+deltaPosition<0){
			deltaPosition = -position.getValue();
		}
		else if(position.getValue()+deltaPosition>stroke){
			deltaPosition = stroke-position.getValue();
		}
		
		position.addValue(deltaPosition);
		velocity = position.getTimeDerivate();
		
		
		/* Flow Rate */
		flowRate.update(Math.abs(velocity)*area);
		
		/* Pressure drop */
		if(0==velocity){
			pressureDrop = Math.abs(force.getValue())/(Math.PI/4*(Math.pow(pistonDiameter,2)-Math.pow(pistonRodDiameter, 2)));
		}
		else {
			if(1==cylinderType && 0<velocity)
				pressureDrop = Math.abs(force.getValue()) / (efficiency*area) +    (Math.pow(velocity, 2)*Math.pow(Math.PI/4*(Math.pow(pistonDiameter,2)-Math.pow(pistonRodDiameter, 2)), 2)+ Math.pow(flowRateLeak, 2)*density)/(2*Math.pow(Math.PI/4*Math.pow(connectionDiameter,2)*k, 2))*Math.pow(pistonDiameter, 2)-Math.pow(pistonRodDiameter, 2)/Math.pow(pistonDiameter, 2);
			else if(2==cylinderType)
				pressureDrop = Math.abs(force.getValue()) / (efficiency*area) + 875*Math.pow(velocity, 2)*Math.pow(Math.PI/4*(Math.pow(pistonDiameter,2)-Math.pow(pistonRodDiameter, 2)), 2)/(2*Math.pow(Math.PI/4*k*Math.pow(0.01, 2), 2));
			else
				pressureDrop = fluidProperties.getPressureBack()-fluidProperties.getPressureFront();
		}

		/* Leak flow */
		flowRateLeak = Math.PI * pressureDrop *Math.pow(pistonDiameter/2+H_7-(pistonDiameter/2-h_6), 3)*(pistonDiameter/2+H_7+pistonDiameter/2-h_6)/(12*viscosity*Math.pow(10,-6)*pistonThickness)/density;
		
		
				
		/* Set outputs */
		pmech.setValue(Math.abs(force.getValue())*Math.abs(velocity));
		phydr.setValue(pressureDrop*((flowRate.getCurrent()+flowRate.getLast())/2+flowRateLeak));
		ploss.setValue(phydr.getValue()-pmech.getValue());
		
		/* Temperature raise */
		if((flowRate.getCurrent()+flowRate.getLast())/2+flowRateLeak>0)
			deltaTemperature = ploss.getValue() / ((flowRate.getCurrent()+flowRate.getLast())/2+flowRateLeak) / density / heatCapacity;
		else
			deltaTemperature = 0;
		
		/* Fluid properties */
		fluidProperties.setFlowRateIn(flowRateLeak+(flowRate.getCurrent()+flowRate.getLast())/2);
		fluidProperties.setPressureDrop(pressureDrop);
		
		fluidOut.setTemperature(fluidIn.getTemperature()+deltaTemperature);
		fluidIn.setPressure(fluidOut.getPressure()+pressureDrop);
	
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
		// TODO Auto-generated method stub
		return fluidProperties;
	}
	
}
