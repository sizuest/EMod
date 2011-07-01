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
	
	// Parameters used for the model:
	private double pmechnominal; // Nominal mechanical power [W]
	private double rotspeednominal; // Nominal rotational speed [rpm]
	private double[] pmechSamples; // Samples of normed mechanical power [W]
	private double[] rotspeedSamples; // Samples of normed rotational speed [rpm]
	private double[] efficiencySamples; // Sample format: efficiency [1]
	
	public LinearMotor() {
		super();
	}
	
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
		double[] pmechvals = {0.0, 0.6, 1.0, 1.2};
		// Define the samples for normed rotational speed
		double[] rsvals = {0.2, 1, 1.2};
		// Define the samples for efficiency
		double[] esamples = {0.2, 0.6, 0.8, 0.7};
		// TODO: Checks: Length >= 2, same length, all eff > 0, Sorted?
		
		// Configure the model:
		this.configure(100,          // Nominal power   
					   3000,         // Nominal rot speed
					   pmechvals,    // Normed mech power samples
					   rsvals,       // Normed rotational speed  
					   esamples);    // Efficiency sample matrix
	}
	
	/**
	 * Set type specific parameters for LinearMotor model.
	 * 
	 * @param pnom  Nominal power of this motor [W].
	 * @param rsnom Nominal rotational speed [rpm]
	 * @param pmSamples Sample vector of mechanical power
	 * @param rsSamples Sample vector of rotational speed
	 * @param effSamples List with efficiency samples
	 */
	public void configure(double pnom, double rsnom, double[] pmSamples, double[] rsSamples, double[] effSamples)
	{
		// TODO: Check dimensions
		pmechnominal = pnom;
		rotspeednominal = rsnom;
		pmechSamples = pmSamples;
		rotspeedSamples = rsSamples;
		efficiencySamples = effSamples;
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
		double eff = getEfficiency(pmech.getValue() / pmechnominal, 
				                   torque.getValue() / rotspeednominal,
				                   pmechSamples,
				                   rotspeedSamples,
				                   efficiencySamples);
		ploss.setValue(pmech.getValue() * (1/eff - 1));
		efficiency.setValue(eff);
	}
	
	/**
	 * Get efficiency of motor power as function of the normed
	 * mechanical power.
	 * 
	 * @param p Normed mechanical power of motor.
	 * @param omega Normed rotational speed of motor
	 * @param psamples Power samples for efficency calsulation
	 * @param omegasamples Rotationsl apeed samples
	 * @return Efficiency matrix
	 */
	static private double getEfficiency(double p, double omega, 
									    double[] psamples, double[] omegasamples, double[] samples)
	{
		int index = binarySearch(p, psamples);
		
		if (index < 0) {
			//System.out.println(p + " <= pmin(" + psamples[0] + ") => eff=" + samples[0]);
			return 	samples[0];
		}
		if (index >= psamples.length-1) {
			//System.out.println(p + " >= pmax(" + psamples[index] + ") => eff=" + samples[index]);
			return	samples[index];
		}
		
		// TODO: Bilinear interpolation for two dimensional arrays.
		// Calculate efficiency by linear interpolation:
		double eff = samples[index] + 
					(p-psamples[index])/(psamples[index+1]-psamples[index])*(samples[index+1]-samples[index]);
		
		//System.out.println(p + " " + "p/e_s[" + index + "]=" + psamples[index] + "/" + samples[index]
		//                          + " p/e_l[" + index+1 + "]=" + psamples[index+1] + "/" + samples[index+1]
		//                          + " eff=" + eff);
		
		return eff;	
	}
	
	/**
	 * By a given value, find the value of a sorted array such that the value is 
	 * larger than the found value. Return the index of this value.
	 *  
	 * @param x Value
	 * @param vals Sorted array (First entry is the smallest)
	 * @return Return the index of the last value in the array vals that is smaller or equal
	 *         than the value x.
	 */
	static private int binarySearch(double x, double[] vals)
	{
		int low = 0;
		int high = vals.length-1;
		
		if (x < vals[0]) {
			return 	-1;
		}
		if (x >= vals[vals.length-1]) {	
			return vals.length-1;
		}
		
		int mid = vals.length / 2;
		while (high-low > 1) {
			if (x >= vals[mid]) {
				low = mid;
			}
			else {
				high = mid;
			}
			mid = (low + high) / 2;
		}
		return low;
	}

}
