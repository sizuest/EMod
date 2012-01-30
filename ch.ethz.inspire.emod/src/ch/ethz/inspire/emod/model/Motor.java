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
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Motor model class.
 * Implements the physical model of a linear motor.
 * From the input parameters torque and rotational speed, the mechanical
 * power, the motor efficiency and the power loss are calculated.
 * 
 * Assumptions:
 * The inertia of the motor, the tools and the part is neglected. Thus, there is no
 * peak of the mechanical power at a change of the rotational speed or torque. We 
 * assume that the motor turns with the specified rotational speed immediately after
 * a change of the input parameters.
 * 
 * Inputlist:
 *   1: RotSpeed    : [rpm] : Actual rotational speed
 *   2: Torque      : [Nm]  : Actual torque
 * Outputlist:
 *   1: PTotal      : [W]   : Calculated total energy demand
 *   2: PLoss       : [W]   : Calculated power loss
 *   3: PUse        : [W]   : Calculated mechanical power
 *   4: Efficiency  : [1]   : Calculated efficiency
 *   
 * Config parameters:
 *   PowerSamples         : [W]   : Power samples used for linear 
 *                                  interpolation of the efficiency
 *   RotspeedSamples      : [rpm] : Rotational speed samples used for linear 
 *                                  interpolation of the efficiency
 *   EfficiencyMatrix     : [1]   : Efficiency matrix. 
 *                                  The matrix value at the position t,r (EfficiencyMatrix[t,r])
 *                                  corresponds to the motor efficiency when the motor is running
 *                                  at torque TorqueSamples[t] and at rotational speed
 *                                  RotspeedSamples[r].
 *   FrictionTorque       : [Nm]  : Friction torque during idle (load free) operation
 * 
 * @author andreas
 *
 */
@XmlRootElement
public class Motor extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer rotspeed;
	private IOContainer torque;
	// Output parameters:
	private IOContainer pmech;
	private IOContainer ploss;
	private IOContainer pel;
	private IOContainer efficiency;
	
	// Save last input values
	private double lastrotspeed = 0;
	private double lasttorque = 0;
	
	// Parameters used by the model. 
	private double[] powerSamples; // Samples of power [W]
	private double[] rotspeedSamples; // Samples of normed rotational speed [1]
	private double[][] efficiencyMatrix; // Efficiency sample matrix [1]
	private double  frictionTorque;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Motor() {
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
	public Motor(String type) {
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
		rotspeed = new IOContainer("RotSpeed", Unit.RPM, 0);
		inputs.add(rotspeed);
		torque = new IOContainer("Torque", Unit.NEWTONMETER, 0);
		inputs.add(torque);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pmech      = new IOContainer("PUse",       Unit.WATT, 0);
		ploss      = new IOContainer("PLoss",      Unit.WATT, 0);
		pel        = new IOContainer("PTotal",     Unit.WATT, 0);
		efficiency = new IOContainer("Efficiency", Unit.NONE, 0);
		outputs.add(pel);
		outputs.add(ploss);
		outputs.add(pmech);
		outputs.add(efficiency);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Motor", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			powerSamples     = params.getDoubleArray("PowerSamples");
			rotspeedSamples  = params.getDoubleArray("RotspeedSamples");
			efficiencyMatrix = params.getDoubleMatrix("EfficiencyMatrix");
			frictionTorque   = params.getDoubleValue("FrictionTorque");
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
		// Check dimensions:
		if (powerSamples.length != efficiencyMatrix.length) {
			throw new Exception("LinearMotor, type:" +type+ 
					": Dimension missmatch: Vector 'TorqueSamples' must have same dimension as " +
					"'EfficiencyMatrix' (" + powerSamples.length + "!=" + efficiencyMatrix.length + ")!");
		}
		for (int i=0; i<powerSamples.length; i++) {
			if (rotspeedSamples.length != efficiencyMatrix[i].length) {
				throw new Exception("LinearMotor, type:" +type+ 
						": Dimension missmatch: Vector 'RotspeedSamples' must have same dimension as " +
						"'EfficiencyMatrix["+i+"]' (" + rotspeedSamples.length + "!=" + efficiencyMatrix[i].length + ")!");
			}
		}
		// Check if sorted:
		for (int i=1; i<powerSamples.length; i++) {
			if (powerSamples[i] <= powerSamples[i-1]) {
				throw new Exception("LinearMotor, type:" +type+ 
						": Sample vector 'TorqueSamples' must be sorted!");
			}
		}
		for (int i=1; i<rotspeedSamples.length; i++) {
			if (rotspeedSamples[i] <= rotspeedSamples[i-1]) {
				throw new Exception("LinearMotor, type:" +type+ 
						": Sample vector 'RotspeedSamples' must be sorted!");
			}
		}
		// Check efficiency values:
		for	(int i=0; i<powerSamples.length; i++) {
			for (int j=0; j<rotspeedSamples.length; j++) {
				if ( (efficiencyMatrix[i][j] <= 0) || 
					 (efficiencyMatrix[i][j] > 1.0) ) {
						throw new Exception("LinearMotor, type:" +type+ 
								": 'EfficiencyMatrix' must be >0 and <=1!");
					}
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
				
		if ( (lasttorque == torque.getValue() ) &&
			 (lastrotspeed == rotspeed.getValue() ) ) {
			// Input values did not change, nothing to do.
			return;
		}
		
		lasttorque   = torque.getValue();
		lastrotspeed = rotspeed.getValue();
				
		/* The mechanical power is equal to the product of rotational speed
		 * and torque. */
		// pmech = rotspeed [rot/min] / 60 [s/min] * torque [Nm] * 2 * pi 
		if ( lasttorque == 0 ){
			pmech.setValue(0);
			pel.setValue(lastrotspeed * frictionTorque * Math.PI/ 30.0);
			ploss.setValue(pel.getValue());
			efficiency.setValue(0);
		}
		else{
			pmech.setValue(lastrotspeed * lasttorque * Math.PI/ 30.0);

		/*
		 * Get efficiency from the configured sample values by bilinear interpolation.
		 */
			double eff = Algo.bilinearInterpolation(pmech.getValue(), 
												lastrotspeed,
				                                powerSamples,
				                                rotspeedSamples,
				                                efficiencyMatrix);
		
		/* The power loss depends on the efficiency of the motor for the actual
		 * working point (actual rotational speed and torque).
		 */
		// ptot = pmech / eff
		// ploss = ptot - pmech = pmech / eff - pmech = pmech (1/eff -1)
			ploss.setValue(pmech.getValue() * (1/eff - 1));
		
			pel.setValue(pmech.getValue()+ploss.getValue());
			efficiency.setValue(eff);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
