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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.gui.graph.GraphElementPosition;


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
	@XmlElement
	private GraphElementPosition position = new GraphElementPosition(0, 0);
	
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
		setInitialConditions();
	}
	
	/**
	 * @return the component
	 */
	public APhysicalComponent getComponent() {
		return component;
	}

	/**
	 * @param component the {@link APhysicalComponent} to set
	 */
	public void setComponent(APhysicalComponent component) {
		this.component = component;
		setInitialConditions();
	}
	
	private void setInitialConditions(){
		if(!component.equals(null))
			component.setDynamicStateParent(name);
	}

	public GraphElementPosition getPosition() {
		return position;
	}
	
	@XmlTransient
	public void setPosition(GraphElementPosition position){
		this.position = position;
	}

}
