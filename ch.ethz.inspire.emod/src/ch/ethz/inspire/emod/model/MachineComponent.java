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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * General machine component
 * 
 * @author dhampl
 *
 */
@XmlRootElement
public class MachineComponent {

	private String name;
	private APhysicalComponent component;
	
	/**
	 * 
	 * @param name
	 */
	public MachineComponent(String name) {
		super();
		this.name = name;
	}
	
	public MachineComponent() {
		super();
	}
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the component
	 */
	public APhysicalComponent getComponent() {
		return component;
	}

	/**
	 * @param component the component to set
	 */
	public void setComponent(APhysicalComponent component) {
		this.component = component;
	}
	
	

}
