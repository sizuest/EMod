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

import ch.ethz.inspire.emod.model.units.Torque;

/**
 * motor simulation input values. final torque and rounds per
 * minute values.
 * 
 * @author dhampl
 *
 */
public class MotorSimulationInput implements ISimulationInput {

	private final Torque torque;
	private final double rpm;
	
	/**
	 * @param torque
	 * @param rpm
	 */
	public MotorSimulationInput(Torque torque, double rpm) {
		super();
		this.torque = torque;
		this.rpm = rpm;
	}
	
	/**
	 * @return the torque
	 */
	public Torque getTorque() {
		return torque;
	}
	
	/**
	 * @return the rpm
	 */
	public double getRpm() {
		return rpm;
	}
	
	
}
