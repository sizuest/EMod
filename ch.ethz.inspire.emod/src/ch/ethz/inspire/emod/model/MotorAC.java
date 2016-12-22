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
 * General Induction Motor model class. Implements the physical model of a
 * induction motor. From the input parameters torque and rotational speed, the
 * mechanical power, the motor efficiency and the power loss are calculated.
 * 
 * 
 * Inputlist: 1: RotSpeed : [rpm] : Actual rotational speed 2: Torque : [Nm] :
 * Actual torque Outputlist: 1: PTotal : [W] : Calculated total energy demand 2:
 * PLoss : [W] : Calculated power loss 3: PUse : [W] : Calculated mechanical
 * power 4: Efficiency : [1] : Calculated efficiency
 * 
 * Config parameters: Ls : [H] : Stator inductance Lr : [H] : Rotor inductance
 * Lm : [H] : Mutal inductance Rs : [Ohm] : Stator resistance Rr : [Ohm] : Rotor
 * resistance p : [-] : Number of pole pairs opU : [V] : Rated stator voltage
 * opFreq : [Hz] : Rated field frequency maxU : [V] : Maximum stator frequency
 * 
 * @author andreas
 * 
 */
@XmlRootElement
public class MotorAC extends AMotor {

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
	private double lastrotspeed = Double.NaN;
	private double lasttorque = Double.NaN;

	// Parameters used by the model.
	private double Lm, Lr, Ls; // mutal, rotor, stator inductance [H]
	private double Rr, Rs; // rotor, stator resistance [Ohm]
	private double p; // number of pole pairs [1]
	private double opU, opFreq; // Operational point
	private double maxU;
	private double k;

	// Global values
	private double slip;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public MotorAC() {
		super();
	}

	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		// post xml init method (loading physics data)
		init();
	}

	/**
	 * Linear Motor constructor
	 * 
	 * @param type
	 */
	public MotorAC(String type) {
		super();

		this.type = type;
		init();
	}

	/**
	 * Called from constructor or after unmarshaller.
	 */
	@Override
	protected void init() {
		super.init();

		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		rotspeed = new IOContainer("RotSpeed", new SiUnit(Unit.REVOLUTIONS_S),
				0, ContainerType.MECHANIC);
		inputs.add(rotspeed);
		torque = new IOContainer("Torque", new SiUnit(Unit.NEWTONMETER), 0);
		inputs.add(torque);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pmech = new IOContainer("PUse", new SiUnit(Unit.WATT), 0,
				ContainerType.MECHANIC);
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0,
				ContainerType.THERMAL);
		pel = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0,
				ContainerType.ELECTRIC);
		efficiency = new IOContainer("Efficiency", new SiUnit(Unit.NONE), 0,
				ContainerType.INFORMATION);
		outputs.add(pel);
		outputs.add(ploss);
		outputs.add(pmech);
		outputs.add(efficiency);

		/* *********************************************************************** */
		/* Read configuration parameters: */
		/* *********************************************************************** */
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader(getModelType(), type);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Read the config parameter: */
		try {
			Ls = params.getDoubleValue("InductanceStator");
			Lr = params.getDoubleValue("InductanceRotor");
			Lm = params.getDoubleValue("InductanceMutal");
			Rs = params.getDoubleValue("ResistanceStator");
			Rr = params.getDoubleValue("ResistanceRotor");
			p = params.getIntValue("PolePairs");
			opU = params.getDoubleValue("RatedVoltage");
			opFreq = params.getDoubleValue("RatedFrequency");
			maxU = params.getDoubleValue("MaxVoltage");

			// Us/fs at OP
			k = opU / opFreq;
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */

		// Validate the parameters:
		try {
			checkConfigParams();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {
		// Check model parameters:
		// Check dimensions:
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {

		double fs, U, eff, pwmloss, RE, Lsigma;

		if ((lasttorque == torque.getValue())
				&& (lastrotspeed == rotspeed.getValue())) {
			// Input values did not change, nothing to do.
			return;
		}

		lasttorque = torque.getValue();
		lastrotspeed = rotspeed.getValue() * 60;

		pmech.setValue(lastrotspeed * lasttorque * Math.PI / 30.0);

		/* Calculate required stator voltage */
		double[] p1 = new double[4];
		p1[0] = -6 * Math.pow(k, 2) * Math.pow(Lm, 2) * Math.PI;
		p1[1] = 4 * Rr * Math.pow((Lm + Ls) * Math.PI, 2) * lasttorque + 3
				* Math.pow(k * Lm, 2) * p * lastrotspeed * Math.PI / 30.0;
		p1[2] = 0;
		p1[3] = Rr * Math.pow(Rs, 2) * lasttorque;

		// Find the roots of the polynom p1
		Complex64F[] roots1 = Algo.findRoots(p1);
		int idxMin = 0;
		for (int i = 2; i < roots1.length; i++)
			if (Math.abs(roots1[idxMin].imaginary) > Math
					.abs(roots1[i].imaginary))
				idxMin = i;

		fs = roots1[idxMin].real;

		// Test for field-weakening
		if (fs * k > maxU) {
			double[] p2 = new double[3];
			p2[0] = 4 * Rr * Math.pow((Lm + Ls) * Math.PI, 2) * lasttorque;
			p2[1] = -6 * Math.pow(Lm * maxU, 2) * Math.PI;
			p2[2] = Rr * Math.pow(Rs, 2) * lasttorque + 3
					* Math.pow(Lm * maxU, 2) * p * lastrotspeed * Math.PI / 30;

			// Find the roots of the polynom p2
			Complex64F[] roots2 = Algo.findRoots(p2);
			fs = Double.POSITIVE_INFINITY;
			for (int i = 1; i < roots2.length; i++)
				fs = Math.min(fs, roots2[i].real);

			U = maxU;
		} else
			U = fs * k;

		/* PWM Losses */
		Lsigma = Ls + Lr * Lm / (Lr + Lm);
		RE = Rs + Rr * Lm / (Lr + Lm);
		pwmloss = Math.pow(maxU / 8000 / Lsigma, 2)
				* RE
				/ 72
				* (1 - 3 / 4 * Math.pow(U / maxU, 2) - 2 / 3 / Math.PI
						* Math.pow(U / maxU, 3) + 9 / 16 * Math
						.pow(U / maxU, 4));

		/* Efficiency */
		if (lastrotspeed == 0 || fs <= 1) {
			eff = 0;
			slip = 0;
		} else {
			eff = 1 / (fs
					* 2
					* Math.PI
					/ p
					/ (lastrotspeed * Math.PI / 30.0)
					+ Rr
					* Rs
					/ (Math.pow(Lm, 2)
							* (fs * 2 * Math.PI - p * lastrotspeed * Math.PI
									/ 30.0) * p * lastrotspeed * Math.PI / 30.0) + Rs
					/ Rr
					* Math.pow(Lr, 2)
					/ Math.pow(Lm, 2)
					* (fs * 2 * Math.PI - p * lastrotspeed * Math.PI / 30.0)
					/ p / (lastrotspeed * Math.PI / 30.0));
			slip = (fs * 2 * Math.PI - p * lastrotspeed * Math.PI / 30.0)
					/ (fs * 2 * Math.PI);
		}

		/*
		 * The power loss depends on the efficiency of the motor for the actual
		 * working point (actual rotational speed and torque).
		 */
		// ptot = pmech / eff
		// ploss = ptot - pmech = pmech / eff - pmech = pmech (1/eff -1)
		if (eff == 0)
			ploss.setValue(pmech.getValue() + pwmloss);
		else
			ploss.setValue(pmech.getValue() * (1 / eff - 1) + pwmloss);

		pel.setValue(pmech.getValue() + ploss.getValue());
		efficiency.setValue(eff);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void updateBoundaryConditions() {
		bcHeatSrcRotor.setValue(pmech.getValue() * slip);
		bcHeatSrcStator.setValue(ploss.getValue() - bcHeatSrcRotor.getValue());
	}

}
