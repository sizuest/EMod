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

package ch.ethz.inspire.emod.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Heat Exchanger model class. Implements the physical model of a heat
 * exchanger
 * 
 * Assumptions: All Component losses are thermal, heat exchanger can be
 * described by a energy efficency ratio
 * 
 * Inputlist: 1: Level : [-] : On/Off 2: Temperature : [K] : Temperature
 * measnurement Outputlist: 1: PTotal : [W] : Electric power demand 2: PThermal
 * : [W] : Heat flow out
 * 
 * Config parameters: CompressorPower : [W] : Installed compressor power (el.)
 * EERCooling : [-] : Energy efficency ratio cooling
 * 
 * 
 * 
 * @author simon
 * 
 */
@XmlRootElement
public class Cooler extends APhysicalComponent {

	@XmlElement
	protected String type;

	// Input parameters:
	private IOContainer state;
	private IOContainer temperature;
	// Output parameters:
	private IOContainer ptotal;
	private IOContainer puse;
	private IOContainer ploss;
	private IOContainer pth_out;

	// Parameters used by the model.
	private double epsilon; // EER [-]
	private double pCompressor; // Compressor power [W]
	private double tempOn, tempOff; // Temperature setpoints

	private boolean isOn = false;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Cooler() {
		super();
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
	 * Heat exchanger constructor
	 * 
	 * @param type
	 */
	public Cooler(String type) {
		super();

		this.type = type;
		init();
	}

	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init() {
		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		state = new IOContainer("State", new SiUnit(Unit.NONE), 0, ContainerType.CONTROL);
		temperature = new IOContainer("Temperature", new SiUnit(Unit.KELVIN), 293.15, ContainerType.CONTROL);
		inputs.add(state);
		inputs.add(temperature);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		ptotal = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		puse = new IOContainer("PUse", new SiUnit(Unit.WATT), 0, ContainerType.MECHANIC);
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		pth_out = new IOContainer("PThermal", new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		outputs.add(ptotal);
		outputs.add(ploss);
		outputs.add(puse);
		outputs.add(pth_out);

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
			pCompressor = params.getPhysicalValue("CompressorPower", new SiUnit("W")).getValue();
			epsilon = params.getPhysicalValue("EERCooling", new SiUnit("")).getValue();
			tempOn = params.getPhysicalValue("TemperatureHigh", new SiUnit("K")).getValue();
			tempOff = params.getPhysicalValue("TemperatureLow", new SiUnit("K")).getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */

		// Validate the parameters:
		try {
			checkConfigParams();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {
		// Check model parameters:

		// Strictly positive
		if (epsilon <= 0) {
			throw new Exception("HeatExchanger, type:" + type
					+ ": Negative or zero: EER must be strictly positive");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {

		// Element is off
		if (0 == state.getValue()) {
			ptotal.setValue(0);
			pth_out.setValue(0);
			return;
		}

		/*
		 * Power consumption is equal to nominal power, if component is on.
		 * P_tot = P_compressor
		 * 
		 * The transfered heat can be calculated over EER P_thermal [W] =
		 * Qdot*epsilon [W]
		 */

		if (isOn & temperature.getValue() <= tempOff | !isOn
				& temperature.getValue() >= tempOn)
			isOn = !isOn;

		if (isOn) {
			ptotal.setValue(pCompressor);
			pth_out.setValue(pCompressor * epsilon);
		} else {
			ptotal.setValue(0);
			pth_out.setValue(0);
		}

		ploss.setValue(ptotal.getValue());
		puse.setValue(0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void updateBoundaryConditions() {/* Not used */
	}
}
