/***********************************
 * $Id: HydraulicOil.java 103 2013-10-31 13:39:36Z kraandre $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/src/ch/ethz/inspire/emod/model/Cylinder.java $
 * $Author: kraandre $
 * $Date: 2013-10-31 14:39:36 +0100 (Do, 31 Okt 2013) $
 * $Rev: 103 $
 *
 * Copyright (c) 2013 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/

package ch.ethz.inspire.emod.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Hydraulic Oil model class.
 * Implements the physical model of the Hydraulic Oil HLP 46
 * 
 * Assumptions:
 * -Newtonian fluid
 * -Pressure influence on viscosity neglected
 * -Density influenced by pressure and temperature
 * 
 * Inputlist:
 * 	 1: Temperature	  [Kelvin]
 * 	 2: Pressure	  [Pa]
 * 
 * Outputlist:
 *   1: Viscosity   : [mm2/s] : Hydraulic oil`s kinematic viscosity
 *   2: Density     : [kg/m3] : Hydraulic oil`s density
 *   
 * Config parameters:
 *   TemperaturSamples: [Kelvin] 
 *   PressureSamples:   [Pa]
 *   Initialviscosity:  [mm2/s]
 *   Initialdensity:    [kg/m3]
 * 
 * @author kraandre
 *
 */
@XmlRootElement
public class HydraulicOil extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer temperature;
	private IOContainer pressure;
	
	//Saving last input values
	//private double lasttemperature=293;
	//private double lastpressure=100000;

	
	// Output parameters:
	private IOContainer viscosity;
	private IOContainer density;
	
	// Parameters used by the model. 
	private double[] pressureSamples;
	private double[] temperatureSamples;
	private double[] viscositySamples;
	private double[][] densityMatrix;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public HydraulicOil() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Hydraulic Oil constructor
	 * 
	 * @param type
	 */
	public HydraulicOil(String type) {
		super();
		
		this.type=type;
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
		
		/* Define input parameters */
		inputs      = new ArrayList<IOContainer>();
		temperature = new IOContainer("Temperature", Unit.KELVIN, 273.15, ContainerType.THERMAL);
		pressure    = new IOContainer("Pressure",    Unit.PA,     0,      ContainerType.FLUIDDYNAMIC);
		inputs.add(temperature);
		inputs.add(pressure);
		
		/* Define output parameters */
		outputs   = new ArrayList<IOContainer>();
		viscosity = new IOContainer("Viscosity", Unit.MMSQUARE_S,  45, ContainerType.FLUIDDYNAMIC);
		density   = new IOContainer("Density",   Unit.KG_MCUBIC,  800, ContainerType.FLUIDDYNAMIC);
		outputs.add(viscosity);
		outputs.add(density);

			
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader(getModelType(), type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			pressureSamples     = params.getDoubleArray("PressureSamples");
			temperatureSamples	= params.getDoubleArray("TemperatureSamples");
			viscositySamples	= params.getDoubleArray("ViscositySamples");
			densityMatrix		= params.getDoubleMatrix("DensityMatrix");
			
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
		
		//Check dimensions
		if(pressureSamples.length != temperatureSamples.length)
			throw new Exception("Valve, type:" +type+
					": PressureSamples and TemperatureSamples must have the same dimensions!");
		
		if(viscositySamples.length != temperatureSamples.length)
			throw new Exception("Valve, type:" +type+
					": ViscositySamples and TemperatureSamples must have the same dimensions!");
		
		//Check if sorted
		
		for (int i=1; i<pressureSamples.length; i++) {
			if (pressureSamples[i] <= pressureSamples[i-1]) {
				throw new Exception("Valve, type:" +type+ 
						": Sample vector 'PressureSamples' must be sorted!");
			}
		}
		for (int i=1; i<temperatureSamples.length; i++) {
			if (temperatureSamples[i] <= temperatureSamples[i-1]) {
				throw new Exception("Valve, type:" +type+ 
						": Sample vector 'TemperatureSamples' must be sorted!");
			}
		}
		for (int i=1; i<viscositySamples.length; i++) {
			if (viscositySamples[i] >= viscositySamples[i-1]) {
				throw new Exception("Valve, type:" +type+ 
						": Sample vector 'ViscositySamples' must be sorted!");
			}
		}
		
		//Check physical value
		for(int i=0; i<pressureSamples.length; i++) {
			if(pressureSamples[i]<0) {
				throw new Exception("Valve, type:" +type+": Pressure must be bigger than zero!");
				}
		}
		
		for(int i=0; i<temperatureSamples.length; i++) {
			if(temperatureSamples[i]<0) {
				throw new Exception("Valve, type:" +type+": Temperature must be bigger than zero!");
				}
		}
		for(int i=0; i<viscositySamples.length; i++) {
			if(viscositySamples[i]<0) {
				throw new Exception("Valve, type:" +type+": Viscosity must be bigger than zero!");
				}
		}
		
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		//lasttemperature=temperature.getValue();
		//lastpressure=pressure.getValue();
		
		viscosity.setValue(Algo.logInterpolation(temperature.getValue(), temperatureSamples, viscositySamples));
		
		density.setValue(Algo.bilinearInterpolation(pressure.getValue(), temperature.getValue(), pressureSamples, temperatureSamples, densityMatrix));
	
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
		init();
	}
}
