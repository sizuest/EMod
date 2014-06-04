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
 * General linear axis model class.
 * Implements the physical model of a linear axis.
 * From the input parameter translational speed and process force, 
 * the requested motor torque is calculated
 * 
 * Assumptions:
 * The inertia of mass and frictional forces are negligible
 * 
 * Inputlist:
 *   1: Speed       : [mm/min] : Actual translational speed
 *   2: ProcessForce: [N]      : Actual process force along the axis
 * Outputlist:
 *   1: Torque      : [Nm]   : Calculated torque
 *   2: RotSpeed	: [rpm]  : Requested rotational speed
 *   3: PTotal      : [W]    : Input power
 *   4: PUse        : [W]    : Output power (usable)
 *   5: PLoss       : [W]    : Output power (losses)
 *   
 * Config parameters:
 *   Transmission         : [mm/rev]   : Transmission between the motor (rpm) 
 *   								 and the translation (mm_min)
 *   Mass				  : [kg]   : Mass of the moving part
 *   Alpha				  : [deg]  : Angle between the axis and the vertical 
 *   								 of the machine
 * 
 * @author simon
 *
 */
@XmlRootElement
public class LinAxis extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer speed;
	private IOContainer force;
	// Output parameters:
	private IOContainer torque;
	private IOContainer rotspeed;
	private IOContainer puse;
	private IOContainer ploss;
	private IOContainer ptotal;
	
	// Save last input values
	private double lastspeed, 
				   lastforce;
	
	// Parameters used by the model. 
	private double transmission;	// [mm/rev] Transmission ratio
	private double mass;            // [kg]     Moved mass
	private double alpha;           // [deg]    Angle
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public LinAxis() {
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
	public LinAxis(String type) {
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
		speed  = new IOContainer("Speed",        Unit.MM_MIN, 0, ContainerType.MECHANIC);	
		force  = new IOContainer("ProcessForce", Unit.NEWTON, 0, ContainerType.MECHANIC);
		inputs.add(speed);
		inputs.add(force);
		
		/* Define output parameters */
		outputs  = new ArrayList<IOContainer>();
		torque   = new IOContainer("Torque",   Unit.NEWTONMETER, 0, ContainerType.MECHANIC);
		rotspeed = new IOContainer("RotSpeed", Unit.RPM,         0, ContainerType.MECHANIC);
		puse     = new IOContainer("PUse",     Unit.WATT,        0, ContainerType.MECHANIC);
		ploss    = new IOContainer("PLoss",    Unit.WATT,        0, ContainerType.THERMAL);
		ptotal   = new IOContainer("PTotal",   Unit.WATT,        0, ContainerType.MECHANIC);
		outputs.add(torque);
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
			mass         = params.getDoubleValue("Mass");
			alpha        = params.getDoubleValue("Alpha");
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
    	if (mass < 0) {
    		throw new Exception("LinearAxis, type:" + type +
    				": Negative value: Mass must be non negative");
    	}
    	if (alpha > 360 || alpha < -360) {
    		throw new Exception("LinearAxis, type:" + type +
    				": Value: Alpha must be between 360 and -360 degrees");
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
		
		lastspeed = speed.getValue(); // [mm/min]
		lastforce = force.getValue(); // [N]
		
		/* Rotation speed
		 * The requested rotational speed is given by the transmission
		 */
		rotspeed.setValue(lastspeed/transmission);
		
		/* Torque
		 * The absolute torque needed to overcome the friction is given as
		 * T = k*(Fp-m*g*cos alpha)
		 * 
		 * Remark: transmission is [mm/rev]
		 */
		torque.setValue(transmission/1000 * ( lastforce - mass*9.81*Math.cos(alpha*Math.PI/180) ) );
		/* Powers
		 * PUse [W] = v [mm/min] * 1000/60 [min*m/mm/s] * F [N]
		 * PTotal [W] = omega [rpm] * 2*pi/60 [rad/s/rpm] * T [N]
		 * PLoss [W] = PTotal [W] - PUse [W];
		 */
		puse.setValue(lastspeed*lastforce*1000/60);
		ptotal.setValue(rotspeed.getValue()*torque.getValue()*2*Math.PI/60);
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
		init();
	}
	
}