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

import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Fan model class.
 * Implements the physical model of a fan.
 * From the input parameter fan level, the mass flow and the 
 * electrical power consumption are calculated.
 * 
 * Assumptions:
 * Validity of the fan laws, constant fan efficiency, negligible dynamics
 * 
 * Inputlist:
 *   1: level	    : [1]    : Fan level
 * Outputlist:
 *   1: Pel       	: [W]    : Calculated mechanical power
 *   2: massFlow       : [kg/s] : Calculated power loss
 *   
 * Config parameters:
 *   RhoFluid         : [kg/m3] : Desity of the moved fluid
 *   PelRef           : [W]     : Electrical power, reference point
 *   VdotRef          : [m3/s]  : Voluminal flow, reference point,
 *   							  corresponding point for PmechRef
 * 
 * @author simon
 *
 */
@XmlRootElement
public class Fan extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer u;
	// Output parameters:
	private IOContainer pel;
	private IOContainer mdot;
	
	// Parameters used by the model. 
	private double rhoFluid; // Fluid density [kg/m3]
	private double pelRef;   // Electrical power reference point [W]
	private double vdotRef;  // Voluminal flow reference point [m3/s]
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Fan() {
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
	public Fan(String type) {
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
		inputs   = new ArrayList<IOContainer>();
		u    = new IOContainer("level", Unit.NONE, 0);
		inputs.add(u);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pel     = new IOContainer("Pel", Unit.WATT, 0);
		mdot    = new IOContainer("massFlow", Unit.KG_S, 0);
		outputs.add(pel);
		outputs.add(mdot);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Fan", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			rhoFluid = params.getDoubleValue("DensityFluid");
			pelRef   = params.getDoubleValue("PowerRef");
			vdotRef  = params.getDoubleValue("VoluminalFlowRef");
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
    	
    	// Strictly positive
    	if (rhoFluid <= 0){
    		throw new Exception("Fan, type:" +type+ 
					": Negative or zero: Fluid density must be strictly positive");
    	}
    	if (pelRef <= 0){
    		throw new Exception("Fan, type:" +type+ 
					": Negative or zero: Reference power must be strictly positive");
    	}
    	if (vdotRef <= 0){
    		throw new Exception("Fan, type:" +type+ 
					": Negative or zero: Reference voluminal flow must be strictly positive");
    	}		
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		/*
		 * mdot [kg/s] = Vdot_ref [m^3/s] * rho [kg/m^3] * u [1]
		 */
		mdot.setValue(u.getValue()*vdotRef*rhoFluid);
		
		/*
		 * Pel [W] = Pel_ref [W] * u^3 [1]
		 */
		pel.setValue(pelRef*Math.pow(u.getValue(), 3));
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
