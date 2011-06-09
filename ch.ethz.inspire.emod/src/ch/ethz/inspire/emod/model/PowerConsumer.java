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

import ch.ethz.inspire.emod.model.units.*;

/**
 * Interface for all Components which consume 
 * 
 * @author dhampl
 *
 */
public interface PowerConsumer {

	/**
	 * returns the real power
	 * 
	 * @return real power as float
	 */
	public Power getPReal();
	
	/** 
	 * returns the power loss
	 * 
	 * @return power loss as float
	 */
	public Power getPLoss();
	
	/**
	 * sets the torque in N*m
	 * 
	 * @param torque
	 */
	public void setTorque(float torque);
	
	/**
	 * sets the revolutions per minute 
	 * 
	 * @param revs
	 */
	public void setRevolutions(float revs);
}
