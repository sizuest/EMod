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

/**
 * @author dhampl
 *
 */
public interface PowerConsumer {

	public float getPReal();
	
	public float getPLoss();
	
	public void setTorque(float torque);
	
	public void setRevolutions(float revs);
}
