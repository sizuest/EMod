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
 * General Servo Motor model class.
 * Implements the physical model of a servo motor with amplifier.
 * 
 * Assumptions:
 * 
 * 
 * Inputlist:
 *   1: RotSpeed    : [rpm] : Actual rotational speed
 *   2: Torque      : [Nm]  : Actual torque
 * Outputlist:
 *   1: PTotal      : [W]   : Calculated electrical power
 *   2: PLoss       : [W]   : Calculated power loss
 *   3: PUse        : [W]   : Calculated mech. power
 *   4: PServo      : [W]   : Calculated electrical servo power
 *   5: PBreak      : [W]   : Calculated electrical break power
 *   
 * Config parameters:
 *   StaticFriction       : [Nm]   : Static friction of the motor
 *   KappaA               : [Nm/A] : Motor torque constant
 *   KappaI               : [V/rmp]: Motor speed constant. 
 *   ArmatureResistance   : [Ohm]  : Internal resistance of the motor
 *   PolePairs            : [-]    : Number of pole pairs
 *   BrakePower			  : [W]    : Electrical power needed to release the brake, 0 if no break is avaiable
 * 
 * @author simon
 *
 */
@XmlRootElement
public class ServoMotor extends APhysicalComponent{
	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer rotspeed;
	private IOContainer torque;
	// Output parameters:
	private IOContainer pel;
	private IOContainer ploss;
	private IOContainer pmech;
	private IOContainer pservo;
	private IOContainer pbrake;
	
	// Save last input values
	private double lastrotspeed;
	private double lasttorque;
	
	// Parameters used by the model. 
	private double frictionTorque;
	private double kappa_a;
	private double kappa_i;
	private double armatureResistance;
	private int    p;
	private double brakePower;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public ServoMotor() {
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
	public ServoMotor(String type) {
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
		rotspeed = new IOContainer("RotSpeed", Unit.RPM, 0);
		torque   = new IOContainer("Torque",   Unit.NEWTONMETER, 0);
		inputs.add(rotspeed);
		inputs.add(torque);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pel     = new IOContainer("PTotal", Unit.WATT, 0);
		ploss   = new IOContainer("PLoss",  Unit.WATT, 0);
		pmech   = new IOContainer("PUse",   Unit.WATT, 0);
		pservo  = new IOContainer("PServo", Unit.WATT, 0);
		pbrake  = new IOContainer("PBrake", Unit.WATT, 0);
		outputs.add(pel);
		outputs.add(ploss);
		outputs.add(pmech);
		outputs.add(pservo);
		outputs.add(pbrake);
		
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("ServoMotor", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			frictionTorque     = params.getDoubleValue("StaticFriction");
			kappa_a            = params.getDoubleValue("KappaA");
			kappa_i            = params.getDoubleValue("KappaI");
			armatureResistance = params.getDoubleValue("ArmatureResistance");
			p                  = params.getIntValue("PolePairs");
			brakePower         = params.getDoubleValue("BrakePower");
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
		// Check NonNegativ:
		if (frictionTorque < 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negative Value: StaticFriction must be non negative");
		}
		if (kappa_i < 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negative Value: KappaI must be non negative");
		}
		if (armatureResistance< 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negative Value: ResistanceI must be non negative");
		}
		if (brakePower< 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negative Value: BreakPower must be non negative");
		}
		// Strictly positive values
		if (kappa_a <= 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Non-positive Value: KappaA must be non negative and non zero");
		}
		if (p <= 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Non-positive Value: PolePairs must be non negative and non zero");
		}
		
		
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		if ( (lasttorque   == Math.abs(torque.getValue()) ) &&
			 (lastrotspeed == Math.abs(rotspeed.getValue()) ) ) {
			// Input values did not change, nothing to do.
			return;
		}
		lasttorque   = Math.abs(torque.getValue());
		lastrotspeed = Math.abs(rotspeed.getValue());
		
		/* Check if component is running. If not, set 
		 * all to 0 and exit.
		 */
		if( ( (lasttorque==0) && (lastrotspeed==0)) ) {
			pel.setValue(0);
			ploss.setValue(0);
			pmech.setValue(0);
			pservo.setValue(0);
			pbrake.setValue(0);
			return;
		}
		/* Check, if a rotational speed is required, and if a brake exist */
		if( (0!=brakePower) && (0==lastrotspeed) ) {
			pel.setValue(brakePower);
			ploss.setValue(0);
			pmech.setValue(0);
			pservo.setValue(0);
			pbrake.setValue(0);
			return;
		}
		
		/* The electrical power is equal to the motor power plus 
		 * the amplifier power
		 * pel = (T_m [Nm] + T_f [Nm])/kappa_a [Nm/A] *
		 *	     (kappa_i [V/rmp] * omega [rpm] + (T_m [Nm] + T_f [Nm])/kappa_a [Nm/A] * R_a [Ohm])
		 *		 +P_el_amp [W] + P_brake [W]
		 */
		pservo.setValue( p*(lasttorque+frictionTorque)/kappa_a * 
					  ( kappa_i*lastrotspeed + (lasttorque+frictionTorque)*armatureResistance / kappa_a  ) );
		pel.setValue( pservo.getValue() + brakePower);
		pbrake.setValue(brakePower);
		
		/* The heat loss is equal to the power by the resistor power plus 
		 * the amplifier loss 
		 * pel =  ((T_m [Nm] + T_f [Nm])/kappa_a [Nm/A] )^2 * R_a [Ohm] +P_th_amp [W] 
		 */
		ploss.setValue( p*Math.pow(((lasttorque+frictionTorque)/kappa_a),2) * armatureResistance );
		
		/* The mechanical power is given by the rotational speed and the torque:
		 * pmech = T_m [Nm] * omega [rpm] * pi/30 [rad/rpm]
		 */
		pmech.setValue( lasttorque * lastrotspeed * Math.PI/30 );
		
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
