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

import ch.ethz.inspire.emod.model.fluid.FECForcedFlow;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitProperties;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * Implements a forced fluid flow
 * 
 * @author sizuest
 * 
 */
@XmlRootElement
public class ForcedFluidFlow extends APhysicalComponent implements Floodable {

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
	DynamicState temperature;
	Material material;

	boolean fluidSet = false;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public ForcedFluidFlow() {
		super();
		this.type = "Example";
		init();
		material = new Material("Monoethylenglykol_34");
		fluidProperties.setMaterial(material);
	}

	/**
	 * post xml init method (loading physics data)
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		init();
	}

	/**
	 * Pipe constructor
	 * 
	 * @param type
	 */
	public ForcedFluidFlow(String type) {
		super();

		this.type = type;
		init();
	}

	private void init() {

		// add inputs
		inputs = new ArrayList<IOContainer>();
		temperatureIn = new IOContainer("TemperatureIn",
				new SiUnit(Unit.KELVIN), 293.15, ContainerType.THERMAL);
		pressureIn = new IOContainer("PressureIn", new SiUnit(Unit.PA), 0,
				ContainerType.FLUIDDYNAMIC);
		flowRateCmd = new IOContainer("FlowRate",
				new SiUnit(Unit.METERCUBIC_S), 0, ContainerType.FLUIDDYNAMIC);
		inputs.add(temperatureIn);
		inputs.add(pressureIn);
		inputs.add(flowRateCmd);

		// add outputs
		outputs = new ArrayList<IOContainer>();
		temperatureRaise = new IOContainer("TemperatureRaise", new SiUnit(
				Unit.KELVIN), 0, ContainerType.THERMAL);
		pressureLoss = new IOContainer("PressureLoss", new SiUnit(Unit.PA), 0,
				ContainerType.FLUIDDYNAMIC);
		outputs.add(temperatureRaise);
		outputs.add(pressureLoss);

		// Flow rate Obj
		temperature = new DynamicState("Temperature", new SiUnit("K"));
		temperature.setInitialCondition(293.15);
		fluidProperties = new FluidCircuitProperties(new FECForcedFlow(
				flowRateCmd), temperature);

		// add fluid In/Output
		fluidIn = new FluidContainer("FluidIn", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidProperties);
		inputs.add(fluidIn);
		fluidOut = new FluidContainer("FluidOut", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidProperties);
		outputs.add(fluidOut);

		fluidProperties.setPressureReferenceOut(pressureIn);

		/* *********************************************************************** */
		/* Read configuration parameters: */
		/* *********************************************************************** */
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader(getModelType(), type);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Read the config parameter: */
		try {
			fluidProperties.getMaterial().setMaterial(
					params.getString("Material"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public void update() {
		if (!fluidSet) {
			fluidProperties.setMaterial(fluidProperties.getMaterial());
			fluidSet = true;
		}

		// Set forced output
		temperature.setValue(temperatureIn.getValue());
		// Calculate differences
		temperatureRaise.setValue(fluidIn.getTemperature()
				- temperatureIn.getValue());
		pressureLoss.setValue(pressureIn.getValue() - fluidIn.getPressure());
	}

	@Override
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
	public void flood() {
		fluidProperties.setMaterial(this.material);
	}

	@Override
	public void updateBoundaryConditions() {/* Not used */
	}

}
