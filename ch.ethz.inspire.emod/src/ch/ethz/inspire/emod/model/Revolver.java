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

import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General revolver model class.
 * Implements the physical model of a revolver.
 * From the input parameter tool position, 
 * the requested motor torque is calculated and speed
 * 
 * Assumptions:
 * Intertia forces are dominant. Simple bang-bang controller
 * for position changes. 
 * 
 * 
 * Inputlist:
 *   1: Tool        : [1] : Number of the tool which shall be used
 * Outputlist:
 *   1: Torque      : [Nm]   : Calculated torque
 *   2: RotSpeed	: [rpm]  : Requested rotational speed
 *   
 * Config parameters:
 *   Inertia        : [kgm3] : Revolver Interia
 *   NumberOfTools	: [1]    : Number of tool positions on the revolver
 *   MaxTorque	    : [Nm]   : Maximum torque acting on the revolver
 * 
 * @author simon
 *
 */
@XmlRootElement
public class Revolver extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer n;
	// Output parameters:
	private IOContainer torque;
	private IOContainer rotspeed;
	private IOContainer nReal;
	
	// Save last input values
	private int nDem;
	
	// Parameters used by the model. 
	private double theta;			// [kgm3] Intertia
	private int nTotal, nCurr;
	private double rotPosCurr, 
				   rotPosRel,
	               rotVelCurr, 
	               anglePerPos;
	private double torqueMax, torqueCurr; 
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Revolver() {
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
	 * Revolver constructor
	 * 
	 * @param type
	 */
	public Revolver(String type) {
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
		n      = new IOContainer("Tool", new SiUnit(Unit.NONE), 0, ContainerType.CONTROL);
		inputs.add(n);
		
		/* Define output parameters */
		outputs  = new ArrayList<IOContainer>();
		torque   = new IOContainer("Torque",   new SiUnit(Unit.NEWTONMETER),   0, ContainerType.MECHANIC);
		rotspeed = new IOContainer("RotSpeed", new SiUnit(Unit.REVOLUTIONS_S), 0, ContainerType.MECHANIC);
		nReal    = new IOContainer("ToolReal", new SiUnit(Unit.NONE),          1, ContainerType.INFORMATION);
		outputs.add(torque);
		outputs.add(rotspeed);
		outputs.add(nReal);

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
			theta     = params.getDoubleValue("Inertia");
			nTotal    = params.getIntValue("NumberOfTools");
			torqueMax = params.getDoubleValue("MaxTorque");
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
		
		/* Set initial position */
		nCurr   = 1;
		rotPosCurr = 0;
		rotVelCurr = 0;
		
		/* Define angle per position */
		anglePerPos = 2*Math.PI/nTotal;
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
    	if (theta < 0) {
    		throw new Exception("Revolver, type:" + type +
    				": Negative value: Inertia must be non negative");
    	}
    	// Torque must be positive
    	if (torqueMax <= 0) {
    		throw new Exception("Revolver, type:" + type +
    				": Negative value: At least 2 tools are required");
    	}
    	// Its not funny with less than 2 tools
    	if (nTotal < 2) {
    		throw new Exception("Revolver, type:" + type +
    				": Small value: At least 2 tools are required");
    	}
    	
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		// Get new position
		nDem = (int) n.getValue(); // [-]		
		
		// If no change is requested, or no tool is selected, do nothing
		if( nCurr == nDem || 0==nDem){
			rotVelCurr = 0;
			rotPosCurr = (nCurr-1)*anglePerPos;
			nReal.setValue(nCurr);
			torque.setValue(0);
			rotspeed.setValue(0);
			return;
		}
		
		/* Determine rotational direction */
		rotPosRel  = ((nDem-1)*anglePerPos-rotPosCurr);
		/* Select optimal (shortest) direction of rotation */
		if(Math.PI<Math.abs(rotPosRel)) rotPosRel = -Math.signum(rotPosRel)*(2*Math.PI-Math.abs(rotPosRel));
		
		/* Determine requested torque */
		torqueCurr = (2*rotPosRel/timestep/timestep-rotVelCurr/timestep)*theta;
		/* Limit torque */
		if( torqueMax<torqueCurr) torqueCurr= torqueMax;
		if(-torqueMax>torqueCurr) torqueCurr=-torqueMax;
		
		/* Tustin emulation */
		/*
		 * Remark: System becomes unstable with Euler fwd. emulation
		 */
		rotPosCurr += timestep/2*(rotVelCurr+timestep*torqueCurr/theta);
		rotVelCurr += timestep*torqueCurr/theta;
		
		/* Check zero crossing */
		if( rotPosCurr>=Math.PI*2) rotPosCurr-=Math.PI*2;
		else if( rotPosCurr<0)     rotPosCurr+=Math.PI*2;
		
		/* Calculate new position */
		nCurr = (int) Math.round(rotPosCurr/anglePerPos)+1;
		if(nCurr>nTotal)  nCurr-=nTotal;
		else if (1>nCurr) nCurr+=nTotal;
		nReal.setValue(nCurr);
		
		/* Set new torque/speed */
		torque.setValue(torqueCurr);
		rotspeed.setValue(rotVelCurr/Math.PI/2);
		
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
	
}