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
 * Physical model for constant components. 
 * <p>
 * configuration file:<br />
 * xml properties file according to http://java.sun.com/dtd/properties.dtd
 * 1 entry named "level" with the power levels per state. 
 * </p>
 * input: <br />
 * the current state as a double value without a unit.<br /> 
 * <p>
 * example:<br /> 
 * 3 use levels with power values 0.0, 50.0, 500.0<br />
 * input is 0.0, 1.0, 2.0</p>
 * 
 * @author dhampl
 *
 */
@XmlRootElement
public class ConstantComponent extends APhysicalComponent {

	@XmlElement
	protected String type;
	protected double[] levels;
	
	//input
	private IOContainer level;
	
	//output
	private IOContainer ptotal;
	
	public ConstantComponent(String type) {
		this.type=type;
		init();
	}
	/**
	 * empty JAXB constructor
	 */
	public ConstantComponent() {
		
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	private void init() {
		//input
		inputs = new ArrayList<IOContainer>();
		level = new IOContainer("level", Unit.NONE, 0);
		inputs.add(level);
		//output
		outputs = new ArrayList<IOContainer>();
		ptotal = new IOContainer("ptotal", Unit.WATT, 0);
		outputs.add(ptotal);
		
		ComponentConfigReader configReader = null;
		try {
			configReader = new ComponentConfigReader("ConstantComponent", type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			levels = configReader.getDoubleArrayParam("levels");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		if(level.getValue()<0 || level.getValue()>levels.length-1)
			ptotal.setValue(Double.NaN);
		else
			ptotal.setValue(levels[(int)level.getValue()]);
	}

}
