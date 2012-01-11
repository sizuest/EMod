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
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.PropertiesHandler;
import ch.ethz.inspire.emod.model.APhysicalComponent;

/**
 * General homogenous thermal storage class
 * 
 * Assumptions:
 *  Homegenous temperature distribuntion. Internal heat capacity
 *  constant c_p does not depend on temperature
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
	@XmlElement
	protected String parentType;
	
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
	 * @param parentType
	 */
	public HomogStorage(String type, String parentType) {
		super();
		
		this.type       = type;
		this.parentType = parentType;
		
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
		/* If no parent model file is configured, the local configuration file
		 * will be opened. Otherwise the cfg file of the parent will be opened
		 */		
		if (parentType.isEmpty()) {
			String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix")+
							"/"+PropertiesHandler.getProperty("sim.MachineName")+"/"+Defines.MACHINECONFIGDIR+"/"+
							PropertiesHandler.getProperty("sim.MachineConfigName")+
							"/"+this.getClass().getSimpleName()+"_"+type+".xml";
			try {
				params = new ComponentConfigReader(path);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		else {
		
			/* Open file containing the parameters of the parent model type */
			try {
				params = new ComponentConfigReader(parentType, type);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		/* Read the config parameter: */
		try {
			m               = params.getDoubleValue("thermal.Mass");
			cp              = params.getDoubleValue("thermal.HeatCapacity");
			temperatureInit = params.getDoubleValue("thermal.InitialTemperature");
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
    
    /**
     * Returns the desired IOContainer
     * 
     * If the desired input name matches In or Out, a new input
     * is created and added to the set of avaiable inputs
     * 
     * @param  name	Name of the desired input
     * @return temp IOContainer matched the desired name
     * 
     * @author simon
     */
    @Override
    public IOContainer getInput(String name) {
		IOContainer temp=null;
		
		/* 
		 * If the initialization has not been done, create a output with same unit as input
		 */
		if(name.matches("In")) {
			temp = new IOContainer("In"+(thIn.size()+1), Unit.WATT, 0);
			inputs.add(temp);
			thIn.add(temp);
		}
		else if(name.matches("Out")) {
			temp = new IOContainer("Out"+(thOut.size()+1), Unit.WATT, 0);
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
		
		if (0>curTemperature)
			curTemperature = 0;
		
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
