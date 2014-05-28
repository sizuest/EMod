/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2013 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.model;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Material model class.
 * Provides fluid properties
 * 
 * Assumptions:
 * -Newtonian fluid
 * -Pressure influence on viscosity neglected
 * -Density influenced by pressure and temperature
 * 
 *   
 * Config parameters:
 *   TemperaturSamples: [Kelvin] 
 *   PressureSamples:   [Pa]
 *   Initialviscosity:  [mm2/s]
 *   Initialdensity:    [kg/m3]
 * 
 * @author sizuest, based on HydraulicOil
 *
 */
public class Material {
	@XmlElement
	protected String type;
	
	private double heatCapacity;
	private double[] pressureSamples;
	private double[] temperatureSamples;
	private double[] viscositySamples;
	private double[][] densityMatrix;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Material() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Fluid constructor
	 * 
	 * @param type
	 */
	public Material(String type) {
		super();
		
		this.type=type;
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
					
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Material", type);
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
			heatCapacity        = params.getDoubleValue("HeatCapacity");
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
		/*for (int i=1; i<viscositySamples.length; i++) {
			if (viscositySamples[i] >= viscositySamples[i-1]) {
				throw new Exception("Valve, type:" +type+ 
						": Sample vector 'ViscositySamples' must be sorted!");
			}
		}*/
		
		//Check physical value
		if(heatCapacity<0) {
			throw new Exception("Valve, type:" +type+": Heat capacity must be bigger than zero!");
		}
		
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
    
    /**
     * @param temperature [K]
     * @param pressure [Pa]
     * @return viscosity [m2/s]
     */
    public double getViscosity(double temperature, double pressure) {
    	return Algo.logInterpolation(temperature, temperatureSamples, viscositySamples);
    }
    
    /**
     * @param temperature [K]
     * @param pressure [Pa]
     * @return density [kg/m3]
     */
    public double getDensity(double temperature, double pressure) {
    	return Algo.bilinearInterpolation(pressure, temperature, pressureSamples, temperatureSamples, densityMatrix);
    }
    
    /**
     * @return heat capacity [J/kg/K]
     * 
     * TODO check for temperature / pressure dependency
     */
    public double getHeatCapacity(){
    	return heatCapacity;
    }
}
