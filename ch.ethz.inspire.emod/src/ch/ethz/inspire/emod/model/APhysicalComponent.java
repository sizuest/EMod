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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Abstract machine component. 
 * 
 * @author dhampl
 *
 */
@XmlRootElement
@XmlSeeAlso({IOContainer.class})
public abstract class APhysicalComponent {

	@XmlElementWrapper(name="inputList")
	@XmlElement(name="inputs")
	protected List<IOContainer> inputs;
	@XmlElementWrapper(name="outputList")
	@XmlElement(name="outputs")
	protected List<IOContainer> outputs;
	
	public void setInput(int id, double value) {
		inputs.get(id).setValue(value);
	}
	
	public double getOutput(int id) {
		return outputs.get(id).getValue();
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
	
	public abstract void update();
}
