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
package ch.ethz.inspire.emod.simulation;

/**
 * Implementation of a threaded simulation run
 * @author sizuest
 *
 */
public class EModSimulationSimulationThread extends Thread {

	@Override
	public void run() {
		EModSimulationRun.EModSimRun();
	}

}
