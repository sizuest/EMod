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

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.units.Unit;

/**
 * Linear motor model class. Physical and thermal simulation class.
 * 
 * @author dhampl
 *
 */
@XmlRootElement
public class LinearMotor extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	public LinearMotor() {
		super();
	}
	
	public LinearMotor(String type) {
		super();
		this.type=type;
		inputs = new ArrayList<IOContainer>();
		outputs = new ArrayList<IOContainer>();
		inputs.add(new IOContainer("rpm", Unit.NONE, 0));
		inputs.add(new IOContainer("torque", Unit.NEWTONMETER, 0));
		outputs.add(new IOContainer("mech", Unit.WATT, 0));
		outputs.add(new IOContainer("loss", Unit.WATT, 0));
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		// TODO Dummy Method
		outputs.get(0).setValue(inputs.get(0).getValue()*inputs.get(1).getValue()*0.3);
		outputs.get(1).setValue(inputs.get(0).getValue()*inputs.get(1).getValue()*0.7);
	}

}
