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

import org.ejml.data.Complex64F;

import java.lang.Math;

import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Induction Motor model class.
 * Implements the physical model of a induction motor.
 * From the input parameters torque and rotational speed, the mechanical
 * power, the motor efficiency and the power loss are calculated.
 * 
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
 *   TODO
 * 
 * @author andreas
 *
 */
@XmlRootElement
public class MotorAC extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer rotspeed;
	private IOContainer torque;
	// Output parameters:
	private IOContainer pmech;
	private IOContainer ploss;
	private IOContainer pel;
	private IOContainer efficiency, debug;
	
	// Save last input values
	private double lastrotspeed = 0;
	private double lasttorque = 0;
	
	// Parameters used by the model. 
	private double Lm, Lr, Ls; // mutal, rotor, stator inductance [H]
	private double Rr, Rs; // rotor, stator resistance [Ohm]
	private double p; // number of pole pairs [1]
	private double opU, opRotSpeed, opFreq;	// Operational point
	private double maxU;
	private double k;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public MotorAC() {
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
	public MotorAC(String type) {
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
		rotspeed = new IOContainer("RotSpeed", Unit.RPM, 0, ContainerType.MECHANIC);
		inputs.add(rotspeed);
		torque = new IOContainer("Torque", Unit.NEWTONMETER, 0);
		inputs.add(torque);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pmech      = new IOContainer("PUse",       Unit.WATT, 0, ContainerType.MECHANIC);
		ploss      = new IOContainer("PLoss",      Unit.WATT, 0, ContainerType.THERMAL);
		pel        = new IOContainer("PTotal",     Unit.WATT, 0, ContainerType.ELECTRIC);
		efficiency = new IOContainer("Efficiency", Unit.NONE, 0, ContainerType.INFORMATION);
		debug      = new IOContainer("Debug",      Unit.NONE, 0, ContainerType.INFORMATION);
		outputs.add(pel);
		outputs.add(ploss);
		outputs.add(pmech);
		outputs.add(efficiency);
		outputs.add(debug);
		
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
			Ls = params.getDoubleValue("InductanceStator");
			Lr = params.getDoubleValue("InductanceRotor");
			Lm = params.getDoubleValue("InductanceMutal");
			Rs = params.getDoubleValue("ResistanceStator");
			Rr = params.getDoubleValue("ResistanceRotor");
			p  = params.getIntValue("PolePairs");
			opU        = params.getDoubleValue("RatedVoltage");
			opFreq     = params.getDoubleValue("RatedFrequency");
			opRotSpeed = params.getDoubleValue("RatedRotSpeed");
			maxU       = params.getDoubleValue("MaxVoltage");
			
			// Us/fs at OP
			k = opU / opFreq;
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
		// TODO
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		double s, fs, eff;
				
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
		if ( lasttorque == 0 && lastrotspeed == 0){
			pmech.setValue(0);
			pel.setValue(0);
			ploss.setValue(pel.getValue());
			efficiency.setValue(0);
		}
		else{
			pmech.setValue(lastrotspeed * lasttorque * Math.PI/ 30.0);
			
			// Calculate required stator voltage
			
			double[] p1 = new double[4];
			p1[0] = -6*Math.pow(k, 2)*Math.pow(Lm, 2)*Math.PI;
			p1[1] = 4*Rr*Math.pow((Lm+Ls)*Math.PI,2)*lasttorque + 3*Math.pow(k*Lm, 2)*p*lastrotspeed*Math.PI/30.0;
			p1[2] = 0;
			p1[3] = Rr*Math.pow(Rs, 2)*lasttorque;
			
			// Find the roots of the polynom p1
			Complex64F[] roots1 = Algo.findRoots(p1);
			int idxMin = 0;
			for(int i=2; i<roots1.length; i++)
				if (Math.abs(roots1[idxMin].imaginary)>Math.abs(roots1[i].imaginary))
					idxMin = i;
			
			fs = roots1[idxMin].real;
			
			// Resulting motor slip
			s = (fs-p*lastrotspeed/60.0)/fs;
					
			
			// Test for field-weakening
			if(fs*k>maxU){
				double[] p2 = new double[3];
				p2[0] = 4*Rr*Math.pow((Lm+Ls)*Math.PI, 2)*lasttorque;
				p2[1] = -6*Math.pow(Lm*maxU, 2)*Math.PI;
				p2[2] = Rr*Math.pow(Rs, 2)*lasttorque+3*Math.pow(Lm*maxU, 2)*p*lastrotspeed*Math.PI/30;
				
				// Find the roots of the polynom p2
				Complex64F[] roots2 = Algo.findRoots(p2);
				fs = Double.POSITIVE_INFINITY;
				for(int i=1; i<roots2.length; i++)
					fs = Math.min(fs, roots2[i].real);
			
			}

			

			/*
			 * Get efficiency from the configured sample values by bilinear interpolation.
			 */
			if (lastrotspeed==0 || fs<=1)
				eff = 0;
			else
				eff = 1/( fs*2*Math.PI/p/(lastrotspeed*Math.PI/30.0) + 
						  Rr*Rs/(Math.pow(Lm,2)*(fs*2*Math.PI-p*lastrotspeed*Math.PI/30.0)*p*lastrotspeed*Math.PI/30.0) +
						  Rs/Rr*Math.pow(Lr,2)/Math.pow(Lm, 2)*(fs*2*Math.PI-p*lastrotspeed*Math.PI/30.0)/p/(lastrotspeed*Math.PI/30.0) );
		
			/* The power loss depends on the efficiency of the motor for the actual
			 * working point (actual rotational speed and torque).
			 */
			// ptot = pmech / eff
			// ploss = ptot - pmech = pmech / eff - pmech = pmech (1/eff -1)
			if(eff==0)
				ploss.setValue(pmech.getValue());
			else
				ploss.setValue(pmech.getValue() * (1/eff - 1));
		
			pel.setValue(pmech.getValue()+ploss.getValue());
			efficiency.setValue(eff);
			
			debug.setValue(fs);
		}
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
