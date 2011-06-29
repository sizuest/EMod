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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.units.Power;

/**
 * Spindle model class. Physical and thermal simulation class.
 * 
 * @author dhampl
 *
 */
@XmlRootElement
public class Spindle extends APhysicalComponent implements IThermalSource {

	@XmlElement
	protected String type;
	
	public Spindle() {
		super();
	}
	
	public Spindle(String type) {
		super();
		this.type=type;
	}
	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.Component#getConsumption()
	 */
	@Override
	public IComponentReturn getSimulationValue(ISimulationInput componentInput) {
		// TODO Dummy Method
		MotorSimulationInput msi = (MotorSimulationInput)componentInput;
		return new MotorReturnInfo(new Power(0.3*msi.getRpm()*msi.getTorque().getTorque()), new Power(0.7*msi.getRpm()*msi.getTorque().getTorque()));
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.IThermalSource#thermalFlow()
	 */
	@Override
	public float thermalFlow() {
		// TODO Dummy Method
		return 0;
	}
}
