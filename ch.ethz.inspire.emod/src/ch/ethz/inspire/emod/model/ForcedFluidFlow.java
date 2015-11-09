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

import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * Implements a forced fluid flow
 * @author sizuest
 *
 */
@XmlRootElement
public class ForcedFluidFlow  extends APhysicalComponent implements Floodable{
	
	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer temperatureIn;
	private IOContainer pressureIn;
	private IOContainer flowRateCmd;
	private FluidContainer fluidIn;
	
	// Output parameters:
	private IOContainer pressureLoss;
	private IOContainer temperatureRaise;
	private FluidContainer fluidOut;
	

	// Fluid Properties
	FluidCircuitProperties fluidProperties;
	
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public ForcedFluidFlow() {
		super();
		this.type = "Example";
		init();
		fluidProperties.getMaterial().setMaterial("Monoethylenglykol_34");
	}
	
	/**
	 * @param u
	 * @param parent
	 * @throws Exception 
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) throws Exception {
		//post xml init method (loading physics data)
		init();
		
		
	}
	
	/**
	 * Pipe constructor
	 * 
	 * @param type
	 * @throws Exception 
	 */
	public ForcedFluidFlow(String type) {
		super();
		
		this.type = type;
		init();
	}
	
	private void init()	{
		//add inputs
		inputs = new ArrayList<IOContainer>();
		temperatureIn = new IOContainer("TemperatureIn",new SiUnit(Unit.KELVIN), 293.15, ContainerType.THERMAL);
		pressureIn    = new IOContainer("PressureIn",   new SiUnit(Unit.PA), 0, ContainerType.FLUIDDYNAMIC);
		flowRateCmd      = new IOContainer("FlowRate",  new SiUnit(Unit.METERCUBIC_S), 0, ContainerType.FLUIDDYNAMIC);
		inputs.add(temperatureIn);
		inputs.add(pressureIn);
		inputs.add(flowRateCmd);
		
		//add outputs
		outputs = new ArrayList<IOContainer>();
		temperatureRaise = new IOContainer("TemperatureRaise", new SiUnit(Unit.KELVIN), 0, ContainerType.THERMAL);
		pressureLoss     = new IOContainer("PressureLoss",     new SiUnit(Unit.PA), 0, ContainerType.FLUIDDYNAMIC);
		outputs.add(temperatureRaise);
		outputs.add(pressureLoss);
		
		//add fluid In/Output
		fluidIn        = new FluidContainer("FluidIn", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);
		fluidOut        = new FluidContainer("FluidOut", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC);
		outputs.add(fluidOut);
		
		// Flow rate Obj
		fluidProperties = new FluidCircuitProperties();
		fluidProperties.setPressureReference(fluidOut);
		
		
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
			fluidProperties.getMaterial().setMaterial(params.getString("Material"));
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		params.Close(); /* Model configuration file not needed anymore. */
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public void update() {
		// Set forced output
		fluidOut.setTemperature(temperatureIn.getValue());
		fluidOut.setPressure(pressureIn.getValue());
		// Set flow rate
		fluidProperties.setFlowRateOut(flowRateCmd.getValue());
		// Calculate differences
		temperatureRaise.setValue(fluidIn.getTemperature()-temperatureIn.getValue());
		pressureLoss.setValue(pressureIn.getValue()-fluidIn.getPressure());
	}

	@Override
	public void setType(String type) {
		this.type = type;
		//init();
	}


	@Override
	public FluidCircuitProperties getFluidProperties() {
		return this.fluidProperties;
	}


}
