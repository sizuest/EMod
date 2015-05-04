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
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
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
 *   1: In          : [W]  : Thermal flows in
 *   2: Out         : [W]  : Thermal flow out
 *   3: Pressure    : [Pa] : Pressure
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
@XmlRootElement
public class HomogStorage extends APhysicalComponent{
	@XmlElement
	protected String name;
	@XmlElement
	protected String type;
	@XmlElement
	protected String parentType;
	
	// Input Lists
	private ArrayList<IOContainer> thIn;
	private ArrayList<IOContainer> thOut;
	private IOContainer pressure;
	
	// Output parameters:
	private IOContainer temperatureOut;
	
	// Unit of the element 
	// private double cp;
	private double m;
	private String materialType;
	//private Material material;
	private ThermalElement thermalElement;
	
	// Initial Value
	double temperatureInit = 0;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public HomogStorage() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		loadParameters();
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
		
		loadParameters();
		init();
	}
	
	/**
	 * Homog. Storage constructor
	 * 
	 * @param type
	 * @param parentType
	 * @param temperatureInit
	 */
	public HomogStorage(String type, String parentType, double temperatureInit) {
		super();
		
		this.type            = type;
		this.parentType      = parentType;
		this.temperatureInit = temperatureInit;
		
		loadParameters();
		init();
	}
	
	/**
	 * Homog. Storage constructor
	 * 
	 * @param material string indicating the {@link: Material} to be used;
	 * @param mass [kg]
	 * @param temperatureInit [K]
	 */
	public HomogStorage(String material, double mass, double temperatureInit)
	{
		this.materialType = material;
		this.m = mass;
		this.temperatureInit = temperatureInit;
		
		init();
	}
	
	/**
	 * Homog. Storage constructor
	 * 
	 * @param material string indicating the {@link: Material} to be used;
	 * @param mass [kg]
	 */
	public HomogStorage(String material, double mass)
	{
		this.materialType = material;
		this.m = mass;
		
		init();
	}
	
	/**
	 * loadParameters
	 * 
	 * Loads the system parameters from the database. 
	 */	
 	private void loadParameters()
	{
		ComponentConfigReader params = null;
		String path;
		/* If no parent model file is configured, the local configuration file
		 * will be opened. Otherwise the cfg file of the parent will be opened
		 */		
		if (parentType.isEmpty()) {
			path = PropertiesHandler.getProperty("app.MachineDataPathPrefix")+
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
				//System.exit(-1);
			}
		}		
			
		/* Read the config parameter: */
		try {
			m            = params.getDoubleValue("thermal.mass");
			materialType = params.getString("Material");
			//temperatureInit = params.getDoubleValue("thermal.InitialTemperature");
			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
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
		pressure = new IOContainer("Pressure", Unit.PA, 1E5, ContainerType.FLUIDDYNAMIC);
		inputs.add(pressure);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		temperatureOut     = new IOContainer("Temperature", Unit.KELVIN, 0, ContainerType.THERMAL);
		outputs.add(temperatureOut);
		
		

		// Validate the parameters:
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
		
		/* Thermal Element */
		thermalElement = new ThermalElement(materialType, m);
		thermalElement.getTemperature().setInitialCondition(temperatureInit);
		
		/* State */
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(thermalElement.getTemperature());
		
		//temperature   = newDynamicState("Temperature", Unit.KELVIN);
			
		// Fluid object
		// material = new Material(materialType);
		
		// Initialize State
		// temperature.setInitialCondition(temperatureInit);
		
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
			temp = new IOContainer("In"+(thIn.size()+1), Unit.WATT, 0, ContainerType.THERMAL);
			inputs.add(temp);
			thIn.add(temp);
		}
		else if(name.matches("Out")) {
			temp = new IOContainer("Out"+(thOut.size()+1), Unit.WATT, 0, ContainerType.THERMAL);
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
		
			
		//cp = material.getHeatCapacity();
		//tmpSum = 0;
		
		// Sum up inputs
		for( IOContainer in : thIn)
			if (!Double.isNaN(in.getValue()))
				thermalElement.addHeatInput(in.getValue());
		
		for( IOContainer out : thOut)
			if (!Double.isNaN(out.getValue()))
				thermalElement.addHeatInput(-out.getValue());
		
		/* Integration step:
		 * T(k+1) [K] = T(k) [K]+ SampleTime[s]*(P_in [W] - P_out [W]) / cp [J/kg/K] / m [kg] 
		 */	
		//temperature.setValue( temperature.getValue() + timestep * tmpSum / cp / m );
		thermalElement.integrate(timestep);
		
		//if (0>curTemperature)
		//	curTemperature = 0;
		
		// Set output
		temperatureOut.setValue(thermalElement.getTemperature().getValue());
	}
	
	/*@Override
	public ArrayList<DynamicState> getDynamicStateList(){
		ArrayList<DynamicState> dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.set(0, thermalElement.getTemperature());
		return dynamicStates;
	}*/

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		//TODO this.type = type;
	}
}
