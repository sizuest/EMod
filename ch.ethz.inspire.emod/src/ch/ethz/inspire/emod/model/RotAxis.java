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
import ch.ethz.inspire.emod.simulation.DynamicState;

/**
 * General rotational axis model class.
 * Implements the physical model of a linear axis.
 * From the input parameter rotational speed and process torque, 
 * the requested motor torque is calculated
 * 
 * Assumptions:
 * The inertia of mass and frictional forces are negligible
 * 
 * Inputlist:
 *   1: Speed        : [Hz] : Actual rotational speed
 *   2: ProcessTorque: [Nm] : Actual process torque
 * Outputlist:
 *   1: Torque      : [Nm]   : Calculated torque
 *   2: RotSpeed	: [rpm]  : Requested rotational speed
 *   3: PTotal      : [W]    : Input power
 *   4: PUse        : [W]    : Output power (usable)
 *   5: PLoss       : [W]    : Output power (losses)
 *   
 * Config parameters:
 *   Transmission         : [-]    : Transmission between the motor (rpm) 
 *   								 and the axis (rpm)
 *   Inertia			  : [kg m] : Inertia of the moving part
 * 
 * @author simon
 *
 */
@XmlRootElement
public class RotAxis extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer speed;
	private IOContainer torqueIn;
	// Output parameters:
	private IOContainer torqueOut;
	private IOContainer rotspeed;
	private IOContainer puse;
	private IOContainer ploss;
	private IOContainer ptotal;
	
	// Save last input values
	private double lastspeed, 
				   lasttorque;
	
	// Parameters used by the model. 
	private double transmission;	// [mm/rev] Transmission ratio
	private double inertia;         // [kg]     Moved mass
	
	// Submodel
	private MovingMass mass;
	
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public RotAxis() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Linear Axis constructor
	 * 
	 * @param type
	 */
	public RotAxis(String type) {
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
		speed  = new IOContainer("Speed",        new SiUnit(Unit.REVOLUTIONS_S), 0, ContainerType.MECHANIC);	
		torqueIn  = new IOContainer("ProcessTorque", new SiUnit(Unit.NEWTONMETER), 0, ContainerType.MECHANIC);
		inputs.add(speed);
		inputs.add(torqueIn);
		
		/* Define output parameters */
		outputs  = new ArrayList<IOContainer>();
		torqueOut   = new IOContainer("Torque",   new SiUnit(Unit.NEWTONMETER),   0, ContainerType.MECHANIC);
		rotspeed = new IOContainer("RotSpeed", new SiUnit(Unit.REVOLUTIONS_S), 0, ContainerType.MECHANIC);
		puse     = new IOContainer("PUse",     new SiUnit(Unit.WATT),          0, ContainerType.MECHANIC);
		ploss    = new IOContainer("PLoss",    new SiUnit(Unit.WATT),          0, ContainerType.THERMAL);
		ptotal   = new IOContainer("PTotal",   new SiUnit(Unit.WATT),          0, ContainerType.MECHANIC);
		outputs.add(torqueOut);
		outputs.add(rotspeed);
		outputs.add(puse);
		outputs.add(ploss);
		outputs.add(ptotal);
		
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
			transmission = params.getDoubleValue("Transmission");
			inertia      = params.getDoubleValue("Inertia");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
		// Validate the parameters:
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		
		/* Initialize sub-model */
		mass = new MovingMass(1, inertia, 0);
		
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(mass.getDynamicStateList().get(1));
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
    	if (inertia < 0) {
    		throw new Exception("RotAxis, type:" + type +
    				": Negative value: Inertia must be non negative");
    	}
    	
		// Transmission must be non zero
    	if (0==transmission) {
    		throw new Exception("LinearAxis, type:" + type +
			": Zero value: Transmission must be non zero");
    	}
    	
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		/* Update sub model */
		mass.getInput("SpeedRot").setValue(speed.getValue());
		mass.update();
		
		lastspeed  = speed.getValue();                                       // [m/s]
		lasttorque = torqueIn.getValue()+mass.getOutput("Force").getValue(); // [N]
		
		/* Rotation speed
		 * The requested rotational speed is given by the transmission
		 */
		rotspeed.setValue(lastspeed/transmission);
		
		/* Torque
		 * The absolute torque  is given as
		 * T = k*Tp
		 */
		torqueOut.setValue(transmission * lasttorque );
		/* Powers
		 * PUse [W] = v [m/s] * F [N]
		 * PTotal [W] = omega [rpm] * 2*pi/60 [rad/s/rpm] * T [N]
		 * PLoss [W] = PTotal [W] - PUse [W];
		 */
		puse.setValue(lastspeed*2*Math.PI*lasttorque);
		ptotal.setValue(rotspeed.getValue()*torqueOut.getValue());
		ploss.setValue(ptotal.getValue()-puse.getValue());
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
	
}