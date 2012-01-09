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
 * General homogenous thermal storage class
 * 
 * Assumptions:
 * 
 * 
 * Inputlist:
 *   1: In          : [var] : Thermal flows in
 *   2: Out         : [var] : Thermal flow out
 * Outputlist:
 *   1: Temperature : [var] : Calculated temperature
 *   
 * Config parameters:
 *   HeatCapacity       : [J/K/kg] : Internal heat capacity
 *   Mass               : [kg]     : Total mass of the storage    
 *   InitialTemperature : [K]      : Initial temperature  
 * 
 * @author simon
 *
 */

public class HomogStorage extends APhysicalComponent{
	@XmlElement
	protected String type;
	
	// Input Lists
	private ArrayList<IOContainer> thIn;
	private ArrayList<IOContainer> thOut;
	
	// Output parameters:
	private IOContainer temperature;
	
	// Unit of the element 
	private double cp;
	private double m;
	private double temperatureInit;
	
	// Sum
	private double tmpSum, curTemperature;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public HomogStorage() {
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
	public HomogStorage(String type) {
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
		thIn     = new ArrayList<IOContainer>();
		thOut    = new ArrayList<IOContainer>();
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		temperature     = new IOContainer("Temperature", Unit.KELVIN, 0);
		outputs.add(temperature);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("HomogStorage", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			m               = params.getDoubleValue("Mass");
			cp              = params.getDoubleValue("HeatCapacity");
			temperatureInit = params.getDoubleValue("InitialTemperature");
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
		
		// Initial Temperature:
		curTemperature = temperatureInit;
		temperature.setValue(curTemperature);
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
    	// Check model parameters:
		// Parameter must be non negative and non zero
    	if (m <= 0) {
    		throw new Exception("HomogStorage, type:" + type +
    				": Non positive value: Mass must be non negative and non zero");
    	}
    	if (cp <= 0) {
    		throw new Exception("HomogStorage, type:" + type +
    				": Non positive value: HeatCapacity must be non negative and non zero");
    	}
    	if (temperatureInit <= 0) {
    		throw new Exception("HomogStorage, type:" + type +
    				": Non positive value: InitialTemperature must be non negative and non zero");
    	}
	}
    
    @Override
    public IOContainer getInput(String name) {
		IOContainer temp=null;
		
		/* 
		 * If the initialization has not been done, create a output with same unit as input
		 */
		if(name.matches("In")) {
			temp = new IOContainer("+"+(thIn.size()+1), Unit.WATT, 0);
			inputs.add(temp);
			thIn.add(temp);
		}
		else if(name.matches("Out")) {
			temp = new IOContainer("-"+(thOut.size()+1), Unit.WATT, 0);
			inputs.add(temp);
			thOut.add(temp);
		}
		else {
			for(IOContainer ioc:inputs){
				if(ioc.getName().equals(name)) {
					temp=ioc;
					break;
				}
			}
		}
			
		return temp;
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		tmpSum = 0;
		
		// Sum up inputs
		for( IOContainer in : thIn)
			tmpSum += in.getValue();
		
		for( IOContainer in : thOut)
			tmpSum -= in.getValue();
		
		/* Integration step:
		 * T(k+1) [K] = T(k) [K]+ SampleTime[s]*(P_in [W] - P_out [W]) / cp [J/kg/K] / m [kg] 
		 */	
		curTemperature += sampleperiod * tmpSum / cp / m; 
		
		// Set output
		temperature.setValue(curTemperature);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
}
