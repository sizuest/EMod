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
@XmlRootElement(name = "machineComponent")
public class MachineComponent {

	private int id;
	private String name;
	private ComponentType classType;
	private String type;
	private APhysicalComponent component;
	
	/**
	 * @param id
	 * @param name
	 * @param classType
	 * @param type
	 */
	public MachineComponent(int id, String name, ComponentType classType,
			String type) {
		super();
		this.id = id;
		this.name = name;
		this.classType = classType;
		this.type = type;
	}
	
	public MachineComponent() {
		super();
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
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
	 * @return the classType
	 */
	public ComponentType getClassType() {
		return classType;
	}
	/**
	 * @param classType the classType to set
	 */
	public void setClassType(ComponentType classType) {
		this.classType = classType;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
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
