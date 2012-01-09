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

package ch.ethz.inspire.emod.model.thermal;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;


import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.model.APhysicalComponent;

/**
 * General forced heat transfere class
 * 
 * Assumptions:
 * 
 * 
 * Inputlist:
 *   1: Temperature1      : [K]    : Temperature level 1
 *   2: Temperature2      : [K]    : Temperature level 2
 *   3: MassFlow          : [kg/s] : Mass flow of the fluid
 *   
 * Outputlist:
 *   1: PThermal12        : [K]    : Thermal heat flow from 1 to 2
 *   2: PThermal32        : [K]    : Thermal heat flow from 2 to 1
 *   
 * Config parameters:
 *   HeatCapacity         : [J/kg/K] : Heat capacity of the fluid
 *   
 * 
 * @author simon
 *
 */

public class ForcedHeatTransfere extends APhysicalComponent{
	@XmlElement
	protected String type;
	
	// Input Lists
	private IOContainer temp1;
	private IOContainer temp2;
	private IOContainer massFlow;
	// Output parameters:
	private IOContainer pth12;
	private IOContainer pth21;
	
	// Unit of the element 
	private double cp;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public ForcedHeatTransfere() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Homog. Storage constructor
	 * 
	 * @param type
	 */
	public ForcedHeatTransfere(String type) {
		super();
		
		this.type=type;
		
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{		
		/* Define Input parameters */
		inputs   = new ArrayList<IOContainer>();
		temp1    = new IOContainer("Temperature1", Unit.KELVIN, 0);
		temp2    = new IOContainer("Temperature2", Unit.KELVIN, 0);
		massFlow = new IOContainer("MassFlow",     Unit.KG_S, 0);
		inputs.add(temp1);
		inputs.add(temp2);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pth12   = new IOContainer("PThermal12", Unit.WATT, 0);
		pth21   = new IOContainer("PThermal12", Unit.WATT,   0);
		outputs.add(pth12);
		outputs.add(pth21);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("ForcedHeatTransfere", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			cp            = params.getDoubleValue("HeatCapacity");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
		// Validate the parameters:
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
		
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
    	// Check model parameters:
    	
    	if (cp < 0) {
    		throw new Exception("ForcedHeatTransfere, type:" + type +
    				": Negative value: HeatCapacity must be non negative");
    	}
    	
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	@Override
	 */
	public void update() {
		
		/* The heat transfere from 1 to 2 is
		 * Qdot12 [W] = cp [J/K/kg] * mDot [kg/s] * (T_2 - T_1) [K]
		 */
		
		pth12.setValue( cp * massFlow.getValue() * (temp2.getValue()-temp1.getValue()));
		pth21.setValue(-pth12.getValue());
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
}
