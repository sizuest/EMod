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

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.fluid.FECBypass;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General Bypass model class.
 * Implements the physical model of a bypass.
 * 
 * Assumptions:
 * 
 * 
 * 
 * Inputlist:
 *   1: FluidIn     : [-]  
 
 * Outputlist:
 *   1: FluidOut    : [-]
 *   2: Zeta        : [-] : Debug
 *   
 * Config parameters:
 *   PressureMax      : [Pa]
 * 
 * @author kraandre
 *
 */
@XmlRootElement
public class Bypass extends APhysicalComponent implements Floodable{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private FluidContainer fluidIn;
	
	// Output parameters:
	private IOContainer debug;
	private FluidContainer fluidOut;
	
	// Parameters used by the model. 
	private double pressureMax;
	private double zeta;
	
	// Fluid properties
	private FluidCircuitProperties fluidProperties;
	private FECBypass bypassCharacteristics;
	
	private DynamicState temperature;
	
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Bypass() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Valve constructor
	 * 
	 * @param type
	 */
	public Bypass(String type) {
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
		inputs    = new ArrayList<IOContainer>();
		
		bypassCharacteristics = new FECBypass(0,0);
		temperature = new DynamicState("Temperature", new SiUnit(Unit.KELVIN));
		
		/* Fluid Properties */
		fluidProperties = new FluidCircuitProperties(bypassCharacteristics, temperature);
		
		
		/* Define output parameters */
		outputs      = new ArrayList<IOContainer>();
		debug        = new IOContainer("Zeta",        new SiUnit(Unit.NONE), 0, ContainerType.FLUIDDYNAMIC);
		outputs.add(debug);
		
		/* Fluid in- and output */
		fluidIn  = new FluidContainer("FluidIn",  new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
		fluidOut = new FluidContainer("FluidOut", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
		inputs.add(fluidIn);
		outputs.add(fluidOut);

			
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
			pressureMax = params.getDoubleValue("PressureMax");
			zeta        = params.getDoubleValue("PressureLossCoefficient");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
		// Validate the parameters:
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}		
		
		bypassCharacteristics.setPressure(pressureMax);
		bypassCharacteristics.setZeta(zeta);
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{				
    	if (pressureMax<=0) {
			throw new Exception("Bypass, type:" +type+ 
					": Dimension missmatch: PressureMax must be greater than zero!");
		}
		
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {	
		debug.setValue(bypassCharacteristics.getZeta(fluidProperties.getPressureDrop()));
		
		//Adjust temperature out
		temperature.setInitialCondition(fluidProperties.getTemperatureIn());
		if(fluidProperties.getFlowRate()!=0)
			temperature.addValue(fluidProperties.getInternalLoss()/(fluidProperties.getMaterial().getHeatCapacity()*fluidProperties.getMassFlowRate()));
	
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
	}

	@Override
	public ArrayList<FluidCircuitProperties> getFluidPropertiesList() {
		ArrayList<FluidCircuitProperties> out = new ArrayList<FluidCircuitProperties>();
		out.add(fluidProperties);
		return out;
	}	
	
	@Override
	public void flood(){/* Not used */}
}
