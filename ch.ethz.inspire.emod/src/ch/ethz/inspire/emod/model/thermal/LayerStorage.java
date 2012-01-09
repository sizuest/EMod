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
 * General layer thermal storage class
 * 
 * Assumptions:
 * 
 * 
 * Inputlist:
 *   1: TemperatureIn      : [K]    : Temperature of inflow
 *   2: MassFlow           : [kg/s] : Massflow in
 *   3: TemperatureAmb     : [K]    : Temperature of ambient
 *   
 * Outputlist:
 *   1: TemperatureOut     : [K]    : Temperature of outflow
 *   2: PLoss:             : [W]    : Thermal loss
 *   
 * Config parameters:
 *   HeatCapacity       : [J/K/kg]  : Internal heat capacity
 *   Mass               : [kg]      : Total mass of the storage 
 *   Surface            : [m^2]     : Surface of the storage exposed to ambient
 *   ConvectionConstant : [W/K/m^2] : Heat transfer constant for convection  
 *   ConductionConstant : [W/K/m]   : Heat transfer constant for conduction
 *   WallThickness      : [m]       : Thickness of the storage wall     
 *   InitialTemperature : [K]       : Initial temperature 
 *   NumberOfElements   : [-]       : Number of elements (to divide the pipe in)
 *   
 * 
 * @author simon
 *
 */

public class LayerStorage extends APhysicalComponent{
	@XmlElement
	protected String type;
	
	// Input Lists
	private IOContainer tempIn;
	private IOContainer tempAmb;
	private IOContainer mDotIn;
	
	// Output parameters:
	private IOContainer tempOut;
	private IOContainer ploss;
	
	// Unit of the element 
	private double cp;
	private double m;
	private double surf;
	private double alpha;
	private double lambda;
	private double dWall;
	private double temperatureInit;
	private int    nElements;
	
	// Sum
	private double[] temperaturesCur;
	
	// Heat transfere resistance
	private double thRessistance;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public LayerStorage() {
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
	public LayerStorage(String type) {
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
		tempIn   = new IOContainer("TemperatureIn",  Unit.KELVIN, 0);
		tempAmb  = new IOContainer("TemperatureAmb", Unit.KELVIN, 0);
		mDotIn   = new IOContainer("MassFlow",       Unit.KELVIN, 0);
		inputs.add(tempIn);
		inputs.add(tempAmb);
		inputs.add(mDotIn);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		tempOut = new IOContainer("TemperatureOut", Unit.KELVIN, 0);
		ploss   = new IOContainer("PLoss",          Unit.WATT,   0);
		outputs.add(tempOut);
		outputs.add(ploss);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("LayerStorage", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			m               = params.getDoubleValue("Mass");
			cp              = params.getDoubleValue("HeatCapacity");
			surf            = params.getDoubleValue("Surface");
			alpha           = params.getDoubleValue("ConvectionConstant");
			lambda          = params.getDoubleValue("ConductionConstant");
			dWall           = params.getDoubleValue("WallThickness");
			nElements       = params.getIntValue("NumberOfElements");
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
		
		/* ************************************************************************/
		/*         Initial temperature: */
		/* ************************************************************************/
		
		/* Initialize array for element temperatures and set all to initialiation value */
		temperaturesCur = new double[nElements];
		for (int i=0; i<temperaturesCur.length; i++)
			temperaturesCur[i] = temperatureInit;
		
		/* Set ouput and input to initial temperature */
		tempIn.setValue(temperatureInit);
		tempOut.setValue(temperatureInit);
		
		/* Calculate thermal ressistance k, with cases:
		 * - lambda=alpha=0
		 *   k = 0
		 * - alpha=0
		 *   k=lambda/d;
		 * - lambda=0
		 *   k=alpha;
		 * - else
		 *   k = (1/alpha+d/lambda)^-1
		 */
		if(0==alpha && 0==lambda)
			thRessistance = 0;
		else if (0==alpha)
			thRessistance=lambda/dWall;
		else if (0==lambda)
			thRessistance=alpha;
		else
			thRessistance=1/(1/alpha + dWall/lambda);
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
    		throw new Exception("LayerStorage, type:" + type +
    				": Non positive value: Mass must be non negative and non zero");
    	}
    	if (cp <= 0) {
    		throw new Exception("LayerStorage, type:" + type +
    				": Non positive value: HeatCapacity must be non negative and non zero");
    	}
    	if (temperatureInit <= 0) {
    		throw new Exception("LayerStorage, type:" + type +
    				": Non positive value: InitialTemperature must be non negative and non zero");
    	}
    	
    	if (surf < 0) {
    		throw new Exception("LayerStorage, type:" + type +
    				": Negative value: Surface must be non negative");
    	}
    	if (alpha < 0) {
    		throw new Exception("LayerStorage, type:" + type +
    				": Negative value: ConvectionConstant must be non negative");
    	}
    	if (lambda < 0) {
    		throw new Exception("LayerStorage, type:" + type +
    				": Negative value: ConductionConstant must be non negative");
    	}
    	if (dWall <= 0 && lambda != 0) {
    		throw new Exception("LayerStorage, type:" + type +
    				": Negative value: WallThickness must be positive");
    	}
    	
    	if (nElements < 1) {
    		throw new Exception("LayerStorage, type:" + type +
			": Negative value: NumberOfElements must be at least one");
    	}
    		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	@Override
	 */
	public void update() {
		
		double tempAvg = 0;
		
		/* For each element the change in temperature is
		 * Tdot_i [K/s] = N [-] /m [kg] * mDot [kg/s] *(T_i-1 - T_i) [K] - S [m2]/m [kg] /cp [J/kg/K] * k [W/m2/K] * (T_i-T_amb) [K]
		 * 
		 * where T_-1 = T_in
		 */
		
		for (int i=nElements-1; i>0; i--) {
			temperaturesCur[i] += sampleperiod * ( nElements / m * mDotIn.getValue() * (temperaturesCur[i-1]-temperaturesCur[i]) -
					                               surf / m / cp * thRessistance * (temperaturesCur[i]-tempAmb.getValue()));
		}
		temperaturesCur[0] += sampleperiod * ( nElements / m * mDotIn.getValue() * (tempIn.getValue()-temperaturesCur[0]) -
                							   surf / m / cp * thRessistance * (temperaturesCur[0]-tempAmb.getValue()));
		
		/* The outflow thermal energy is given by the massflow and temperature of the last element: */
		tempOut.setValue(temperaturesCur[nElements-1]);
		
		
		
		/* The total heat loss is equal to:
		 * S [m²] * (1/alpha + d/lambda)^-1 [W/K*m²] * (Temp_Avg - Temp_amb) [K]
		 * wher Temp_Avg is equal to the average element temperature
		 */
		
		for (int i=0; i<nElements; i++)
			tempAvg += temperaturesCur[i];
		
		tempAvg = tempAvg/nElements;
		
		ploss.setValue( surf * thRessistance * (tempAvg-tempAmb.getValue()));
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
}
