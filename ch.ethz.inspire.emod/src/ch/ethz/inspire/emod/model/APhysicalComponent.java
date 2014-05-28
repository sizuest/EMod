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

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * Abstract machine component. 
 * 
 * @author dhampl
 *
 */
@XmlRootElement
public abstract class APhysicalComponent {

	protected List<IOContainer> inputs;
	protected List<IOContainer> outputs;
	protected double sampleperiod;
	
	/**
	 * @param id
	 * @param value
	 */
	public void setInput(int id, double value) {
		inputs.get(id).setValue(value);
	}
	
	public double getOutput(int id) {
		return outputs.get(id).getValue();
	}
	
	public double getInput(int id) {
		return inputs.get(id).getValue();
	}
	
	public List<IOContainer> getInputs() {
		return inputs;
	}
	
	public List<IOContainer> getOutputs() {
		return outputs;
	}
	
	public IOContainer getInput(String name) {
		IOContainer temp=null;
		for(IOContainer ioc:inputs){
			if(ioc.getName().equals(name)) {
				temp=ioc;
				break;
			}
		}
		return temp;
	}
	
	public IOContainer getOutput(String name) {
		IOContainer temp=null;
		for(IOContainer ioc:outputs){
			if(ioc.getName().equals(name)) {
				temp=ioc;
				break;
			}
		}
		return temp;
	}
	
	public abstract String getType();
	
	public abstract void update();
	
	/**
	 * Sets the sample time for the model
	 * 
	 * @param sampleperiod sample time in seconds
	 */
	public void setSimulationPeriod(double sampleperiod){
		this.sampleperiod = sampleperiod;
	}
	
	/**
	 * Sets the type of the component according to the argument
	 * @param type
	 */
	public abstract void setType(String type);
}
