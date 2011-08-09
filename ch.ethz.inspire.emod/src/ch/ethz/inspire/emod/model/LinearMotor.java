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
 * Linear motor model class.
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
 *   1: Pmech       : [W]   : Calculated mechanical power
 *   2: Ploss       : [W]   : Calculated power loss
 *   3: Efficiency  : [1]   : Calculated efficiency
 *   
 * Config parameters:
 *   TorqueNominal        : [Nm]  : Nominal torque of motor
 *   RotspeedNominal      : [rpm] : Nominal rotational speed of motor
 *   NormedTorqueSamples  : [1]   : Normed torque samples used for linear 
 *                                  interpolation of the efficiency
 *   NormedRotspeedSamples: [1]   : Normed rotational speed samples used for linear 
 *                                  interpolation of the efficiency
 *   EfficiencyMatrix     : [1]   : Efficiency matrix. 
 *                                  The matrix value at the position t,r (EfficiencyMatrix[t,r])
 *                                  corresponds to the motor efficiency when the motor is running
 *                                  at torque TorqueSamples[t] and at rotational speed
 *                                  RotspeedSamples[r].
 * 
 * @author andreas
 *
 */
@XmlRootElement
public class LinearMotor extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer rotspeed;
	private IOContainer torque;
	// Output parameters:
	private IOContainer pmech;
	private IOContainer ploss;
	private IOContainer efficiency;
	
	// Save last input values
	double lastrotspeed;
	double lasttorque;
	
	// Parameters used by the model. 
	private double torquenominal; // Nominal torque [Nm]
	private double rotspeednominal; // Nominal rotational speed [rpm]
	private double[] torqueSamples; // Samples of normed torque [1]
	private double[] rotspeedSamples; // Samples of normed rotational speed [1]
	private double[][] efficiencyMatrix; // Efficiency sample matrix [1]
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public LinearMotor() {
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
	public LinearMotor(String type) {
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
		pmech = new IOContainer("Pmech", Unit.WATT, 0);
		outputs.add(pmech);
		ploss = new IOContainer("Ploss", Unit.WATT, 0);
		outputs.add(ploss);
		efficiency = new IOContainer("Efficiency", Unit.NONE, 0);
		outputs.add(efficiency);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("LinearMotor", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			torquenominal = params.getDoubleParam("TorqueNominal");
			rotspeednominal = params.getDoubleParam("RotspeedNominal");
			torqueSamples = params.getDoubleArrayParam("NormedTorqueSamples");
			rotspeedSamples = params.getDoubleArrayParam("NormedRotspeedSamples");
			efficiencyMatrix = params.getDoubleMatrixParam("EfficiencyMatrix");
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
		if (torqueSamples.length != efficiencyMatrix.length) {
			throw new Exception("LinearMotor, type:" +type+ 
					": Dimension missmatch: Vector 'TorqueSamples' must have same dimension as " +
					"'EfficiencyMatrix' (" + torqueSamples.length + "!=" + efficiencyMatrix.length + ")!");
		}
		for (int i=0; i<torqueSamples.length; i++) {
			if (rotspeedSamples.length != efficiencyMatrix[i].length) {
				throw new Exception("LinearMotor, type:" +type+ 
						": Dimension missmatch: Vector 'RotspeedSamples' must have same dimension as " +
						"'EfficiencyMatrix["+i+"]' (" + rotspeedSamples.length + "!=" + efficiencyMatrix[i].length + ")!");
			}
		}
		// Check if sorted:
		for (int i=1; i<torqueSamples.length; i++) {
			if (torqueSamples[i] <= torqueSamples[i-1]) {
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
		for	(int i=0; i<torqueSamples.length; i++) {
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
			 (lastrotspeed == rotspeed.getValue()) ) {
			// Input values did not change, nothing to do.
			return;
		}
		
		/* The mechanical power is equal to the product of rotational speed
		 * and torque. */
		// pmech = rotspeed [rot/min] / 60 [s/min] * torque [Nm] * 2 * pi 
		pmech.setValue(rotspeed.getValue() * torque.getValue() * Math.PI/ 30.0);
		
		/*
		 * Get efficiency from the configured sample values by bilinear interpolation.
		 */
		double eff = Algo.bilinearInterpolation(torque.getValue() / torquenominal, 
				                                rotspeed.getValue() / rotspeednominal,
				                                torqueSamples,
				                                rotspeedSamples,
				                                efficiencyMatrix);
		
		/* The power loss depends on the efficiency of the motor for the actual
		 * working point (actual rotational speed and torque).
		 */
		// ptot = pmech / eff
		// ploss = ptot - pmech = pmech / eff - pmech = pmech (1/eff -1)
		ploss.setValue(pmech.getValue() * (1/eff - 1));
		efficiency.setValue(eff);
	}
	
}
