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

import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General Moving Mass Class.
 * TODO
 * 
 * Assumptions:
 * TODO
 * 
 * Inputlist:
 *   1: Speed    : [mm/min] : Required speed
 *   
 * Outputlist:
 *   1: Force 	 : [N]		: Resulting force
 *   
 * Config parameters:
 *   none
 * 
 * @author simon
 *
 */
@XmlRootElement
public class MovingMass extends APhysicalComponent{

	@XmlElement
	protected double mass;
	@XmlElement
	protected double inertia;
	@XmlElement
	protected double angle;
	
	// Input parameters:
	private IOContainer speedLin, speedRot;
	// Output parameters:
	private IOContainer force, torque;
	
	// Save last input values
	private double lastspeedLin = 0, lastspeedRot = 0;
	
	private DynamicState positionLin, positionAng;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public MovingMass() {
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
	 * Linear Motor constructor
	 * @param mass 
	 * @param inertia 
	 * @param angle 
	 * 
	 * @param type
	 */
	public MovingMass(double mass, double inertia, double angle) {
		super();
		
		this.angle   = angle;
		this.mass    = mass;
		this.inertia = inertia;
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		speedLin  = new IOContainer("SpeedLin", new SiUnit(Unit.M_S), 0, ContainerType.MECHANIC);
		speedRot  = new IOContainer("SpeedRot", new SiUnit(Unit.REVOLUTIONS_S), 0, ContainerType.MECHANIC);
		inputs.add(speedLin);
		inputs.add(speedRot);
		
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		force   = new IOContainer("Force", new SiUnit(Unit.NEWTON), 0, ContainerType.MECHANIC);
		torque  = new IOContainer("Torque", new SiUnit(Unit.NEWTONMETER), 0, ContainerType.MECHANIC);
		outputs.add(force);
		
		/* State */
		positionLin = new DynamicState("Position", new SiUnit(Unit.M));
		positionAng = new DynamicState("Angle",    new SiUnit(""));
		
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(positionLin);
		dynamicStates.add(positionAng);
		
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
		if (mass <= 0) {
			throw new Exception("MovingMass, mass:" +mass+ 
					": Negative value: Mass must be non negative" );
		}
		
		// Check dimensions:
		if (inertia <= 0) {
			throw new Exception("MovingMass, inertia:" +inertia+ 
					": Negative value: Inertia must be non negative" );
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		// Get required speed in m/s 
		double curspeedLin = speedLin.getValue(),
			   curspeedRot = speedRot.getValue();
		
		
		positionLin.addValue( (curspeedLin+lastspeedLin)/2*timestep);
		positionAng.addValue( (curspeedRot+lastspeedRot)/2*timestep);
		
		
		/*
		 * Force is calculated by
		 * F =  ( Acceleration + sin(Angle)*g ) * Mass
		 * where the Acceleration is estimated by the velocity change:
		 * Acceleration = (v(t)-v(t-Ts))/Ts
		 */
		force.setValue(  ( (curspeedLin-lastspeedLin)/timestep + Math.sin(angle*Math.PI/180)*9.81 ) * mass );
		torque.setValue( (curspeedRot-lastspeedRot)/timestep * inertia );
		
				
		// Update last speed
		lastspeedLin = curspeedLin;
		lastspeedRot = curspeedRot;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return "m="+mass+"-alpha="+angle;
	}
	
	public void setType(String type) {
		//TODO
	}
	
}
