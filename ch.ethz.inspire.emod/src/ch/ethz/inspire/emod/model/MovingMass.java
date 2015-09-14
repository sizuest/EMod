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
	protected double angle;
	
	// Input parameters:
	private IOContainer speed;
	// Output parameters:
	private IOContainer force;
	
	// Save last input values
	private double lastspeed = 0;
	
	private DynamicState position;
	
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
	 * @param angle 
	 * 
	 * @param type
	 */
	public MovingMass(double mass, double angle) {
		super();
		
		this.angle=angle;
		this.mass =mass;
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		speed  = new IOContainer("Speed", Unit.MM_MIN, 0, ContainerType.MECHANIC);
		inputs.add(speed);
		
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		force   = new IOContainer("Force", Unit.NEWTON, 0, ContainerType.MECHANIC);
		outputs.add(force);
		
		/* State */
		position = new DynamicState("Position", Unit.M);
		
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(position);
		
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
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		// Get required speed in m/s (source:mm/min)
		double curspeed = speed.getValue()/60/1000;
		
		position.addValue(curspeed*timestep);
		
		/*
		 * Force is calculated by
		 * F =  ( Acceleration + sin(Angle)*g ) * Mass
		 * where the Acceleration is estimated by the velocity change:
		 * Acceleration = (v(t)-v(t-Ts))/Ts
		 */
		force.setValue( ( (curspeed-lastspeed)/timestep + Math.sin(angle*Math.PI/180)*9.81 ) * mass );
		
				
		// Update last speed
		lastspeed = curspeed;
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
