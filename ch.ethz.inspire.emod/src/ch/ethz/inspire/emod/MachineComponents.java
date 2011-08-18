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
package ch.ethz.inspire.emod;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.LinearMotor;
import ch.ethz.inspire.emod.model.ConstantComponent;
import ch.ethz.inspire.emod.model.MachineComponent;

/**
 * Data class for automatically XML<->object generation.
 * 
 * @author dhampl
 *
 */
@XmlRootElement(namespace = "ch.ethz.inspire.emod.model")
@XmlSeeAlso({APhysicalComponent.class, LinearMotor.class, ConstantComponent.class})
public class MachineComponents {
	
	@XmlElementWrapper(name = "machine")
	@XmlElement(name = "machineComponent")
	private ArrayList<MachineComponent> componentList;
	
	/**
	 * Dummy constructor for JABX
	 */
	public MachineComponents()
	{
	}
	
	/**
	 * 
	 * @return list with all machine components.
	 */
	public ArrayList<MachineComponent> getMachineComponentList() {
		return componentList;
	}
	
}
