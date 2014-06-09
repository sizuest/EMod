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
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * contains information on simulation input sources and targets
 * through references to IOContainers of MachineComponents and
 * SimulationControls.
 * 
 * @author dhampl
 *
 */
public class IOConnection {

	private IOContainer source;
	private IOContainer target;
	private double gain;
	
	private static Logger logger = Logger.getLogger(EModMain.class.getName());
	
	public IOConnection(){
		
	}
	
	
	/**
	 * 
	 * @param source
	 * @param target 
	 * @throws Exception thrown if units don't match
	 */
	public IOConnection(IOContainer source, IOContainer target) throws Exception {
		this.source = source;
		this.target = target;
		this.gain   = 1;
		if(source.getUnit()!=target.getUnit()) {
			
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
		
			if (this.gain<=0)
				throw new Exception("units do not match "+source.getName()+
						": "+source.getUnit()+" <-> "+target.getName()+": "+
						target.getUnit());
			else if (this.gain!=1)
				logger.info("explicit unit conversion " +source.getName()+
						": "+source.getUnit()+" > * " +this.gain+ " > "+target.getName()+": "+
						target.getUnit());
		}
	}
	
	public IOContainer getSoure() {
		return source;
	}
	
	public IOContainer getTarget() {
		return target;
	}
	
	/**
	 * @return gain
	 */
	public double getGain() {
		return gain;
	}
}
