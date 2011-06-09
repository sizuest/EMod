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
package ch.ethz.inspire.emod.model.units;

/**
 * Unit type for torque in N*m
 * 
 * @author dhampl
 *
 */
public class Torque {

	private double torque;
	
	public Torque(double torque) {
		this.torque = torque;
	}

	/**
	 * @return the torque
	 */
	public double getTorque() {
		return torque;
	}

	/**
	 * @param torque the torque to set
	 */
	public void setTorque(double torque) {
		this.torque = torque;
	}
}
