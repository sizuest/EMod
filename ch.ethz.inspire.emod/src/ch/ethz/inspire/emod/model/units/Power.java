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
 * Unit type for power in Watt
 * 
 * @author dhampl
 *
 */
public class Power {

	private double watt;
	
	public Power(double watt) {
		this.watt = watt;
	}

	/**
	 * @return the watt
	 */
	public double getWatt() {
		return watt;
	}

	/**
	 * @param watt the watt to set
	 */
	public void setWatt(double watt) {
		this.watt = watt;
	}
	
	
}
