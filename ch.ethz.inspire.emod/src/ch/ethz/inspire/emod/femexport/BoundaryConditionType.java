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
package ch.ethz.inspire.emod.femexport;

/**
 * Types of boundary conditions
 * given is a ODE y'
 * 
 * @author simon
 *
 */
public enum BoundaryConditionType {
	/**
	 * y = f
	 */
	DIRICHLET, 
	/**
	 * y' = f
	 */
	NEUMANN, 
	/**
	 * c0*y+c1*y'=f
	 */
	ROBIN, 
	/**
	 * y=f and c0*y+c1*y'=f
	 */
	MIXED, 
	/**
	 * y=f and c0*y'=f
	 */
	CAUCHY, 
	/**
	 * every thing else
	 */
	OTHER
}
