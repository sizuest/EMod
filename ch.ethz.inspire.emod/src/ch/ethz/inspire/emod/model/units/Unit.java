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
	 * RPM [rpm]
	 * rotational speed
	 */
	RPM,          // rotations per minute
	/**
	 * MM_MIN [mm/min]
	 * translational speed
	 */
	MM_MIN,       // mm/min
	/**
	 * MM [mm]
	 * distance
	 */
	MM,
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
	 * MMSQUARE_S [mm²/s]
	 * viscosity
	 */
	MMSQUARE_S,
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
	 * L_MIN [l/min]
	 * voluminal flow
	 */
	L_MIN,
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
	 * L_S [l/s]
	 * voluminal flow
	 */
	L_S,
	/**
	 * NONE [-]
	 * control signals
	 * information
	 * misc
	 */
	NONE;
}
