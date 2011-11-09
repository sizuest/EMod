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
 *   3: State		: [1]   : Subsystem state (on=1,off=0);
 * Outputlist:
 *   1: Ptotal      : [W]   : Calculated electrical power
 *   2: Ploss       : [W]   : Calculated power loss
 *   
 * Config parameters:
 *   StaticFriction       : [Nm]   : Static friction of the motor
 *   KappaA               : [Nm/A] : Motor torque constant
 *   KappaI               : [V/rmp]: Motor speed constant. 
 *   ArmatureResistance   : [Ohm]  : Internal resistance of the motor
 *   AMPPower             : [W]    : Power demand of the AMP if on
 *   AMPLoss			  : [W]    : Heat loss of the AMP if off
 *   Break				  : [1]    : Indicates if the servo has got a break (true)
 *   								 or not (false)
 * 
 * @author simon
 *
 */
@XmlRootElement
public class ServoMotor extends APhysicalComponent{
	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer state;
	private IOContainer rotspeed;
	private IOContainer torque;
	// Output parameters:
	private IOContainer pel;
	private IOContainer ploss;
	
	// Save last input values
	private double laststate;
	private double lastrotspeed;
	private double lasttorque;
	
	// Parameters used by the model. 
	private double T_f;
	private double kappa_a;
	private double kappa_i;
	private double R_a;
	private double P_el_amp;
	private double P_th_amp;
	private boolean Brake;
	
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
		torque   = new IOContainer("Torque", Unit.NEWTONMETER, 0);
		state    = new IOContainer("State", Unit.NONE, 0);
		inputs.add(rotspeed);
		inputs.add(torque);
		inputs.add(state);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pel     = new IOContainer("Ptotal", Unit.WATT, 0);
		ploss   = new IOContainer("Ploss", Unit.WATT, 0);
		outputs.add(pel);
		outputs.add(ploss);
		
		
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
			T_f      = params.getDoubleValue("StaticFriction");
			kappa_a  = params.getDoubleValue("KappaA");
			kappa_i  = params.getDoubleValue("KappaI");
			R_a      = params.getDoubleValue("ArmatureResistance");
			P_el_amp = params.getDoubleValue("AMPPower");
			P_th_amp = params.getDoubleValue("AMPLoss");
			Brake    = params.getBooleanValue("BrakeExist");
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
		if (T_f < 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negative Value: StaticFriction must be non negative");
		}
		if (kappa_i < 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negative Value: KappaI must be non negative");
		}
		if (R_a< 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negative Value: ResistanceI must be non negative");
		}
		if (P_el_amp < 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negative Value: AMPPower must be non negative");
		}
		if (P_th_amp < 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negative Value: AMPLoss must be non negative");
		}
		// Strictly positive values
		if (kappa_a <= 0) {
			throw new Exception("ServoMotor, type:" +type+ 
					": Negativ Value: KappaA must be non negative and non zero");
		}
		
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		if ( (lasttorque   == Math.abs(torque.getValue()) ) &&
			 (lastrotspeed == Math.abs(rotspeed.getValue()) ) &&
			 (laststate    == state.getValue() ) ) {
			// Input values did not change, nothing to do.
			return;
		}
		lasttorque   = Math.abs(torque.getValue());
		lastrotspeed = Math.abs(rotspeed.getValue());
		laststate    = Math.abs(state.getValue());
		
		/* Check if component is running. If not, set 
		 * all to 0 and exit.
		 */
		if( laststate == 0 || ( (lasttorque==0) && (lastrotspeed==0)) ) {
			pel.setValue(0);
			ploss.setValue(0);
			return;
		}
		/* Check, if a rotational speed is required, and if a brake exist */
		if( Brake && (lastrotspeed==0) ) {
			pel.setValue(P_el_amp);
			ploss.setValue(P_th_amp);
			return;
		}
		
		/* The electrical power is equal to the motor power plus 
		 * the amplifier power
		 * pel = (T_m [Nm] + T_f [Nm])/kappa_a [Nm/A] *
		 *	     (kappa_i [V/rmp] * omega [rpm] + (T_m [Nm] + T_f [Nm])/kappa_a [Nm/A] * R_a [Ohm])
		 *		 +P_el_amp [W] 
		 */
		pel.setValue( (lasttorque+T_f)/kappa_a * 
					  ( kappa_i*lastrotspeed + (lasttorque+T_f)*R_a / kappa_a  ) +
					  P_el_amp );
		
		/* The heat loss is equal to the power by the resitor power plus 
		 * the amplifier loss 
		 * pel =  ((T_m [Nm] + T_f [Nm])/kappa_a [Nm/A] )^2 * R_a [Ohm] +P_th_amp [W] 
		 */
		ploss.setValue( Math.pow(((lasttorque+T_f)/kappa_a),2) * R_a + P_th_amp );
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
