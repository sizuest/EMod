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

import ch.ethz.inspire.emod.model.units.Power;

/**
 * motor simulation return values. returns final real and
 * dissipation power values.
 * 
 * @author dhampl
 *
 */
public class MotorReturnInfo implements IComponentReturn {

	private final Power realPower;
	private final Power dissipation;
	
	/**
	 * @param realPower2
	 * @param dissipation2
	 */
	public MotorReturnInfo(Power realPower, Power dissipation) {
		super();
		this.realPower = realPower;
		this.dissipation = dissipation;
	}

	/**
	 * @return the realPower
	 */
	public Power getRealPower() {
		return realPower;
	}
	
	/**
	 * @return the dissipation
	 */
	public Power getDissipation() {
		return dissipation;
	}
	
	public String toString() {
		return "real: "+realPower.getWatt()+"W dissipation: "+dissipation.getWatt()+"W";
	}
}
