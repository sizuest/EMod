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

import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General clamp model class.
 * Implements the physical model of a clamp.
 * From the input process force and clamp position, the requested 
 * linear speed and motor force are calculated
 * 
 * Assumptions:
 * The interia and frictional losses are negligible
 * 
 * Inputlist:
 *   1: Postion       : [mm]   : Actual translational speed
 * Outputlist:
 *   1: ActuatorForce : [N]    : Requested motor force
 *   
 * Config parameters:
 *   SpringStiffness  : [N/mm] : Spring constant of the material
 *   WorkPiecePostion : [mm]    : Position of the working piece
 * 
 * @author simon
 *
 */
@XmlRootElement
public class Clamp extends APhysicalComponent{
	
	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer position;
	// Output parameters:
	private IOContainer force;
	
	// Save last input values
	private double lastposition;
	
	// Parameters used by the model. 
	private double springconst;
	private double wpposition;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Clamp() {
		super();
	}
	
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Clamp constructor
	 * 
	 * @param type
	 */
	public Clamp(String type) {
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
		position  = new IOContainer("Position", Unit.MM, 0, ContainerType.MECHANIC);
		inputs.add(position);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		force   = new IOContainer("Force", Unit.NEWTONMETER, 0, ContainerType.MECHANIC);
		outputs.add(force);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Clamp", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			springconst = params.getDoubleValue("SpringStiffness");
			wpposition  = params.getDoubleValue("WorkPiecePostion");
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
		// Parameter must be non negative
    	if (springconst < 0) {
    		throw new Exception("Clamp, type:" + type +
    				": Negative value: Spring stiffness must be non negative");
    	}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		if ( lastposition == position.getValue() ) {
			// Input values did not change, nothing to do.
			return;
		}
		lastposition = position.getValue(); // [mm]
		
		/* Clamping force
		 * If the clamp is close enough, calculate a clamping force
		 */
		if ( lastposition < wpposition )
			force.setValue( springconst*(wpposition-lastposition) );
		else
			force.setValue(0);
	
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
}
