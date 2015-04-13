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

package ch.ethz.inspire.emod.utils;

import java.util.logging.Logger;

import ch.ethz.inspire.emod.EModMain;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * contains information on simulation input sources and targets
 * through references to IOContainers of MachineComponents and
 * SimulationControls.
 * 
 * @author dhampl
 * @param <T>
 *
 */
public class IOConnection {
	protected IOContainer source;
	protected IOContainer target;
	protected double gain;
	
	private static Logger logger = Logger.getLogger(EModMain.class.getName());
	
	public IOConnection(){
		
	}
	
	
	/**
	 * 
	 * @param <T>
	 * @param source
	 * @param target 
	 * @throws Exception thrown if units don't match
	 */
	public IOConnection(IOContainer source, IOContainer target) throws Exception {
		this.source = source;
		this.target = target;
		this.gain   = 1;
		
		if(source.getUnit()!=target.getUnit()) {
			unitConversion();
		}
	}
	
	/**
	 * gets the Source IOContainer of the Connection
	 * @return the Source
	 */
	public IOContainer getSource() {
		return source;
	}
	
	/**
	 * gets the Target IOContainer of the Connection
	 * @return the Target
	 */
	public IOContainer getTarget() {
		return target;
	}
	
	/**
	 * @return gain
	 */
	public double getGain() {
		return gain;
	}

	public void unitConversion() throws Exception{
		/*
		 * We have a unit missmatch: lets check if it can be solved by 
		 * a unit conversion. If not: drop an exception.
		 */
		
		switch (source.getUnit()) {
			// Meter
			case M:
				switch (target.getUnit()) {
					// Milimeter
					case MM:
						this.gain = 1000;
						break;
					default:
						this.gain = -1;						
				}
				break;
			// Milimeter
			case MM:
				switch (target.getUnit()) {
					// Meter
					case M:
						this.gain = 0.001;
						break;
					default:
						this.gain = -1;						
				}
				break;
			case L_S:
				switch (target.getUnit()) {
					case METERCUBIC_S:
						this.gain = 0.001;
						break;
					case L_MIN:
						this.gain = 60;
						break;
					default:
						this.gain=-1;
				}
				break;
			case METERCUBIC_S:
				switch (target.getUnit()) {
					case L_S:
						this.gain = 1000;
						break;
					case L_MIN:
						this.gain = 60000;
						break;
					default:
						this.gain=-1;
				}
				break;
			case L_MIN:
				switch (target.getUnit()) {
					case L_S:
						this.gain = 1/60;
						break;
					case METERCUBIC_S:
						this.gain = 1/60000;
						break;
					default:
						this.gain=-1;
				}
				break;				
			default:
				this.gain = -1;
		}

		//TODO: don't abort if there is a mismatch but inform the user!
		if (this.gain<=0)
			throw new Exception("units do not match "+source.getName()+
					": "+source.getUnit()+" <-> "+target.getName()+": "+
					target.getUnit());
		else if (this.gain!=1)
			logger.info("explicit unit conversion " +source.getName()+
					": "+source.getUnit()+" > * " +this.gain+ " > "+target.getName()+": "+
					target.getUnit());
	}

	/**
	 * update the connection, i.e. get the value of the source and write it to the target
	 */
	public void update() {
		this.getTarget().setValue(this.getSource().getValue() * this.getGain());
	}
}
