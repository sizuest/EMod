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
 * @author dhampl
 * See the unit describtions for the units
 */
public enum Unit {
	/**
	 * KG [kg] 
	 * Mass
	 */
	KG,
	/**
	 * WATT [W] 
	 * Power
	 */
	WATT, 
	/**
	 * KELVIN [K]
	 * Temperature
	 */
	KELVIN, 
	/**
	 * NEWTONMETER [Nm]
	 * Torque
	 */
	NEWTONMETER, 
	/**
	 * Rev. per second [rpm]
	 * rotational speed
	 */
	REVOLUTIONS_S,          // rotations per minute
	RPM,
	/**
	 * M [m]
	 * distance
	 */
	M,
	/**
	 * M_S [m/s]
	 * translational speed
	 */
	M_S,		  // m/s
	/**
	 * METERCUBIC [m³]
	 * volume
	 */
	METERCUBIC,
	/**
	 * S [s]
	 * time
	 */
	S,
	/**
	 * NEWTON [N]
	 * force
	 */
	NEWTON,
	/**
	 * METERCUBIC_S [m³/s]
	 * voluminal flow
	 */
	METERCUBIC_S,
	/**
	 * KG_MCUBIC [kg/m³]
	 * density
	 */
	KG_MCUBIC,
	/**
	 * KG_S [kg/s]
	 * mass flow
	 */
	KG_S,
	/**
	 * PA [Pa]
	 * pressure
	 */
	PA,
	/**
	 * NONE [-]
	 * control signals
	 * information
	 * misc
	 */
	NONE;
}
