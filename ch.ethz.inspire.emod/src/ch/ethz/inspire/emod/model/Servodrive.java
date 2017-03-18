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

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.femexport.BoundaryCondition;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General Spindle model class. Implements the physical model of a Servodrive.
 * 
 * 
 * Inputlist: 1: State : [-] : On/Off 2: RotSpeed : [rpm] : Actual rotational
 * speed 3: Torque : [Nm] : Actual torque Outputlist: 1: PTotal : [W] :
 * Calculated total energy demand 2: PLoss : [W] : Calculated power loss 3: PUse
 * : [W] : Calculated mechanical power 4: TemperatureOut : [K] : Coolant outlet
 * temperature
 * 
 * Config parameters: TODO
 * 
 * @author sizuest
 * 
 */
@XmlRootElement
public class Servodrive extends APhysicalComponent {

	@XmlElement
	protected String type;

	// Input parameters:
	private IOContainer state;
	private IOContainer rotspeed;
	private IOContainer torque;
	// Output parameters:
	private IOContainer pmech;
	private IOContainer ploss;
	private IOContainer pel;
	private IOContainer temperature;

	// Parameters used by the model.
	private String motorType;
	private double massStructure;
	private double powerBreake;

	// Submodels
	private AMotor motor;
	private ThermalElement structure;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Servodrive() {
		super();
	}

	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		// post xml init method (loading physics data)
		init();
	}

	/**
	 * Linear Motor constructor
	 * 
	 * @param type
	 */
	public Servodrive(String type) {
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
		state = new IOContainer("State", new SiUnit(Unit.NONE), 0,
				ContainerType.CONTROL);
		rotspeed = new IOContainer("RotSpeed", new SiUnit(Unit.REVOLUTIONS_S),
				0, ContainerType.MECHANIC);
		torque = new IOContainer("Torque", new SiUnit(Unit.NEWTONMETER), 0,
				ContainerType.MECHANIC);
		inputs.add(state);
		inputs.add(rotspeed);
		inputs.add(torque);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pmech = new IOContainer("PUse", new SiUnit(Unit.WATT), 0,
				ContainerType.MECHANIC);
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0,
				ContainerType.THERMAL);
		pel = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0,
				ContainerType.ELECTRIC);
		temperature = new IOContainer("Temperature", new SiUnit(Unit.KELVIN),
				293, ContainerType.THERMAL);
		outputs.add(pel);
		outputs.add(ploss);
		outputs.add(pmech);
		outputs.add(temperature);

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

			motorType = params.getString("MotorType");
			massStructure = params.getPhysicalValue("StructureMass", new SiUnit("kg")).getValue();
			powerBreake = params.getPhysicalValue("BreakePower", new SiUnit("W")).getValue();

			String[] mdlType = motorType.split("_");

			for (int i = 2; i < mdlType.length; i++)
				mdlType[1] += "_" + mdlType[i];

			// Create Sub Elements
			try {
				// Get class and constructor objects
				Class<?> cl = Class.forName("ch.ethz.inspire.emod.model."
						+ mdlType[0]);
				Constructor<?> co = cl.getConstructor(String.class);
				// initialize new component
				motor = (AMotor) co.newInstance(mdlType[1]);
			} catch (Exception e) {
				Exception ex = new Exception("Unable to create component "
						+ mdlType[0] + "(" + mdlType[1] + ")" + " : "
						+ e.getMessage());
				ex.printStackTrace();
				motor = null;
			}

			structure = new ThermalElement(
					params.getString("StructureMaterial"), massStructure);

			// Change state names
			structure.getTemperature().setName("TemperatureStructure");

			// Add states
			dynamicStates = new ArrayList<DynamicState>();
			dynamicStates.add(0, structure.getTemperature());

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

		/* Boundary conditions */
		boundaryConditions = new ArrayList<BoundaryCondition>();
		boundaryConditions.addAll(motor.getBoundaryConditions());
	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {
		// Check model parameters:
		// Check dimensions:
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {

		if (1 == state.getValue()) {

			motor.getInput("RotSpeed").setValue(rotspeed.getValue());
			motor.getInput("Torque").setValue(torque.getValue());
			motor.update();

			pmech.setValue(rotspeed.getValue() * torque.getValue() * Math.PI
					* 2);
			pel.setValue(motor.getOutput("PTotal").getValue() + powerBreake);
			// ploss.setValue(motor.getOutput("PLoss").getValue()+frictionLosses);
			ploss.setValue(motor.getOutput("PLoss").getValue() + powerBreake);
		} else {
			motor.getOutput("PUse").setValue(0);
			motor.getOutput("PLoss").setValue(0);
			motor.getOutput("PTotal").setValue(0);

			pmech.setValue(0);
			pel.setValue(0);
			// ploss.setValue(motor.getOutput("PLoss").getValue()+frictionLosses);
			ploss.setValue(0);
		}

		// Thermal flows
		structure.setHeatInput(ploss.getValue());

		// Update submodels
		structure.integrate(timestep);

		// Write outputs
		temperature.setValue(structure.getTemperature().getValue());

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
	public void updateBoundaryConditions() {
		motor.updateBoundaryConditions();
	}

}
