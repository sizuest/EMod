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
 * @author sizuest
 *
 */
public enum ContainerType {
	/**
	 * For all electric power flows
	 */
	ELECTRIC,
	/**
	 * Mechanical power flows and variables
	 */
	MECHANIC,
	/**
	 * Thermal flows (power & temperature), suitable for FluidConnections
	 */
	THERMAL,
	/**
	 * Fluiddynamic power flows and variables, suitable for FluidConnections
	 */
	FLUIDDYNAMIC,
	/**
	 * Control signals
	 */
	CONTROL,
	/**
	 * Information, not intended to be used between components
	 */
	INFORMATION,
	/**
	 * Default
	 */
	NONE;
	
	// boolean for fluidConnections
	private boolean isFluidConnection;
	
	//
	private ContainerType(){
		isFluidConnection = false;
	}
	
	private ContainerType(boolean isFluidConnection){
		this.isFluidConnection = isFluidConnection;
	}
	
	public void addFluid(){
		if (this.equals(ContainerType.THERMAL) | this.equals(ContainerType.FLUIDDYNAMIC)) {
			isFluidConnection = true;
		}
		else{
			isFluidConnection = false;
			System.out.println("Only Connections of Type Thermal or Fluiddynamic can be FluidConnections.");
		}
	}
	
	public void removeFluid(){
		isFluidConnection = false;
	}
	
	public boolean isFluidConnection(){
		return isFluidConnection;
	}
}
