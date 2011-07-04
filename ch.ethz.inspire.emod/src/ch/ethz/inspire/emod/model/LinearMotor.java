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

/**
 * Linear motor model class. Physical and thermal simulation class.
 * 
 * @author dhampl
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
	
	// Parameters used by the model:
	private double pmechnominal; // Nominal mechanical power [W]
	private double rotspeednominal; // Nominal rotational speed [rpm]
	private double[] pmechSamples; // Samples of normed mechanical power [W]
	private double[] rotspeedSamples; // Samples of normed rotational speed [rpm]
	private double[][] efficiencySamples; // Sample format: efficiency [1]
	
	public LinearMotor() {
		super();
	}
	
	/**
	 * Linear Motor constructor
	 * 
	 * @param type
	 */
	public LinearMotor(String type) {
		super();
		
		this.type=type;
		
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
		
		/* TODO: Read the configuration of the specified type from the
		 * machine component data base.
		 */
		// Define the samples for normed mechanical power
		double[] pmechvals = {0.6, 0.8, 1.2};
		// Define the samples for normed rotational speed
		double[] rsvals = {0.8, 1.2};
		// Define the samples for efficiency
		double[][] esamples = { {0.5, 0.6}, 
				                {0.7, 0.8}, 
				                {0.75, 0.7}  
				              };
		
		// Configure the model:
		try {
			this.configure(100,          // Nominal power   
					       3000,         // Nominal rot speed
					       pmechvals,    // Normed mech power samples
					       rsvals,       // Normed rotational speed  
					       esamples);    // Efficiency sample matrix
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Set type specific parameters for LinearMotor model.
	 * 
	 * @param pnom  Nominal power of this motor [W].
	 * @param rsnom Nominal rotational speed [rpm]
	 * @param pmSamples Sample vector of mechanical power
	 * @param rsSamples Sample vector of rotational speed
	 * @param effSamples List with efficiency samples
	 * @throws Exception 
	 */
	public void configure(double pnom, double rsnom, 
					      double[] pmSamples, double[] rsSamples, double[][] effSamples) throws Exception
	{
		// Set model parameters:
		pmechnominal = pnom;
		rotspeednominal = rsnom;
		pmechSamples = pmSamples;
		rotspeedSamples = rsSamples;
		efficiencySamples = effSamples;
		
		// Check model parameters:
		// Check dimensions:
		if (pmechSamples.length != efficiencySamples.length) {
			throw new Exception("Dimension missmatch: Vector 'pmechSamples' must have same dimension as " +
					"'efficiencySamples' (" + pmechSamples.length + "!=" + efficiencySamples.length + ")!");
		}
		for (int i=0; i<pmechSamples.length; i++) {
			if (rotspeedSamples.length != efficiencySamples[i].length) {
				throw new Exception("Dimension missmatch: Vector 'rotspeedSamples' must have same dimension as " +
						"'efficiencySamples["+i+"]' (" + rotspeedSamples.length + "!=" + efficiencySamples[i].length + ")!");
			}
		}
		// Check if sorted:
		for (int i=1; i<pmechSamples.length; i++) {
			if (pmechSamples[i] <= pmechSamples[i-1]) {
				throw new Exception("Sample vector 'pmechSamples' must be sorted!");
			}
		}
		for (int i=1; i<rsSamples.length; i++) {
			if (rsSamples[i] <= rsSamples[i-1]) {
				throw new Exception("Sample vector 'rsSamples' must be sorted!");
			}
		}
		//Check values:
		for	(int i=0; i<pmechSamples.length; i++) {
			for (int j=0; j<rotspeedSamples.length; j++) {
				if ( (efficiencySamples[i][j] <= 0) || 
					 (efficiencySamples[i][j] > 1.0) ) {
						throw new Exception("'efficiencySamples' must be >0 and <= 1!");
					}
			}
		}
		
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		/* The mechanical power is equal to the product of rotational speed
		 * and torque. */
		// pmech = rotspeed [rot/min] / 60 [s/min] * torque [Nm] * 2 * pi 
		pmech.setValue(rotspeed.getValue() * torque.getValue() * Math.PI/ 30.0);
		
		/* The power loss depends on the efficiency of the motor for the actual
		 * working point (actual rotational speed and torque).
		 */
		// ptot = pmech / eff
		// ploss = ptot - pmech = pmech / eff - pmech = pmech (1/eff -1)
		double eff = Algo.bilinearInterpolation(pmech.getValue() / pmechnominal, 
				                           torque.getValue() / rotspeednominal,
				                           pmechSamples,
				                           rotspeedSamples,
				                           efficiencySamples);
		ploss.setValue(pmech.getValue() * (1/eff - 1));
		efficiency.setValue(eff);
	}
	
}
