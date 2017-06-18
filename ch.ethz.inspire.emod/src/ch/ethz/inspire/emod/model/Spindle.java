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

import java.lang.Math;
import java.lang.reflect.Constructor;

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.femexport.BoundaryCondition;
import ch.ethz.inspire.emod.femexport.BoundaryConditionType;
import ch.ethz.inspire.emod.model.fluid.FECDuct;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitProperties;
import ch.ethz.inspire.emod.model.parameters.PhysicalValue;
import ch.ethz.inspire.emod.model.thermal.ThermalArray;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Spindle model class. Implements the physical model Spindels.
 * 
 * 
 * Inputlist: 1: State : [-] : On/Off 2: RotSpeed : [rpm] : Actual rotational
 * speed 3: Torque : [Nm] : Actual torque Outputlist: 1: PTotal : [W] :
 * Calculated total energy demand 2: PLoss : [W] : Calculated power loss 3: PUse
 * : [W] : Calculated mechanical power 4: TemperatureOut : [K] : Coolant outlet
 * temperature 5: CAirFlow : [l/min]: Compressed air flow rate
 * 
 * Config parameters: TODO
 * 
 * @author sizuest
 * 
 */
@XmlRootElement
public class Spindle extends APhysicalComponent implements Floodable {

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
	private IOContainer cairFlow;
	private IOContainer temperature;
	// Boundary Conditions
	private BoundaryCondition bcHeatSrcAirgap;
	private BoundaryCondition bcHeatSrcCair;
	private BoundaryCondition bcCoolantHTC;
	private BoundaryCondition bcCoolantTemperature;
	private BoundaryCondition bcCoolantHeatFlux;
	// Fluid
	private FluidContainer fluidIn, fluidOut;
	// Fluid Properties
	FluidCircuitProperties fluidProperties;

	// Parameters used by the model.
	private String[] bearingType;
	private String motorType;
	private double[] preloadForce;
	private double massStructure;
	private double alphaCoolant, volumeCoolant;
	private double lastTemperatureIn = Double.NaN;
	
	private double agDiameter;
	private double agGapWidth;
	private double agGapLength;

	// Submodels
	private AMotor motor;
	private Bearing[] bearings;
	private ThermalElement structure;
	private ThermalArray coolant;
	private Duct coolingDuct;

	// Global values
	private double frictionAirGap;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Spindle() {
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
	 * Spindle constructor
	 * 
	 * @param type
	 */
	public Spindle(String type) {
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
		rotspeed = new IOContainer("RotSpeed", new SiUnit(Unit.REVOLUTIONS_S), 0, ContainerType.MECHANIC);
		torque = new IOContainer("Torque", new SiUnit(Unit.NEWTONMETER), 0, ContainerType.MECHANIC);
		inputs.add(state);
		inputs.add(rotspeed);
		inputs.add(torque);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pmech = new IOContainer("PUse", new SiUnit(Unit.WATT), 0, ContainerType.MECHANIC);
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		pel = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		cairFlow = new IOContainer("CAirFlow", new SiUnit(Unit.METERCUBIC_S), 0, ContainerType.FLUIDDYNAMIC);
		temperature = new IOContainer("Temperature", new SiUnit(Unit.KELVIN), 293, ContainerType.THERMAL);
		outputs.add(pel);
		outputs.add(ploss);
		outputs.add(pmech);
		outputs.add(cairFlow);
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
			preloadForce  = params.getPhysicalValue("PreloadForce", new SiUnit("N")).getValues();
			bearingType   = params.getStringArray("BearingType");
			massStructure = params.getPhysicalValue("StructureMass", new SiUnit("kg")).getValue();
			agDiameter  = params.getValue("AirGapDiameter", new PhysicalValue(0.0, new SiUnit("m"))).getValue();
			agGapWidth  = params.getValue("AirGapWidth", new PhysicalValue(0.0, new SiUnit("m"))).getValue();
			agGapLength = params.getValue("AirGapLength", new PhysicalValue(0.0, new SiUnit("m"))).getValue();
			params.deleteValue("AirGapFrictionCoeff");
			params.saveValues();

			String[] mdlType = motorType.split("_", 2);

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

			bearings = new Bearing[bearingType.length];
			for (int i = 0; i < bearingType.length; i++)
				bearings[i] = new Bearing(bearingType[i]);

			coolingDuct = Duct.buildFromFile(getModelType(), getType(), params.getString("StructureDuct"));
			volumeCoolant = coolingDuct.getVolume();
			structure = new ThermalElement(params.getString("StructureMaterial"), massStructure);
			coolant = new ThermalArray("Example", volumeCoolant, 20);

			/* Fluid properties */
			fluidProperties = new FluidCircuitProperties(new FECDuct(
					coolingDuct, coolant.getTemperature()),
					coolant.getTemperature());

			/* Define fluid in-/outputs */
			fluidIn = new FluidContainer("CoolantIn", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
			inputs.add(fluidIn);
			fluidOut = new FluidContainer("CoolantOut", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
			outputs.add(fluidOut);

			// Change state names
			structure.getTemperature().setName("TemperatureStructure");
			coolant.getTemperature().setName("TemperatureCoolant");

			// Add states
			dynamicStates = new ArrayList<DynamicState>();
			dynamicStates.add(0, structure.getTemperature());
			dynamicStates.add(1, coolant.getTemperature());

			/* Fluid circuit parameters */
			coolant.setMaterial(fluidProperties.getMaterial());
			coolingDuct.setMaterial(fluidProperties.getMaterial());

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
		bcHeatSrcAirgap = new BoundaryCondition("HeatSrcAirgap", new SiUnit("W"), 0, BoundaryConditionType.NEUMANN);
		bcHeatSrcCair = new BoundaryCondition("HeatSrcCair", new SiUnit("W"), 0, BoundaryConditionType.NEUMANN);
		bcCoolantHTC = new BoundaryCondition("CoolantHTC", new SiUnit("W/K"), 0, BoundaryConditionType.ROBIN);
		bcCoolantTemperature = new BoundaryCondition("CoolantTemperature", new SiUnit("K"), 293.15, BoundaryConditionType.ROBIN);
		bcCoolantHeatFlux = new BoundaryCondition("CoolantHeatFlux", new SiUnit("W"), 0, BoundaryConditionType.NEUMANN);
		// Motor
		for (BoundaryCondition bc : motor.getBoundaryConditions())
			bc.setName("Motor" + bc.getName());
		// Bearing
		boundaryConditions.addAll(motor.getBoundaryConditions());
		for (int i = 0; i < bearings.length; i++) {
			for (BoundaryCondition bc : bearings[i].getBoundaryConditions())
				bc.setName("Bearing" + (i + 1) + bc.getName());
			boundaryConditions.addAll(bearings[i].getBoundaryConditions());
		}
		// Others
		boundaryConditions.add(bcHeatSrcAirgap);
		boundaryConditions.add(bcHeatSrcCair);
		boundaryConditions.add(bcCoolantHTC);
		boundaryConditions.add(bcCoolantTemperature);
		boundaryConditions.add(bcCoolantHeatFlux);
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

		if (fluidIn.getTemperature() <= 0) {
			if (Double.isNaN(lastTemperatureIn))
				lastTemperatureIn = structure.getTemperature().getValue();
		} else
			lastTemperatureIn = fluidIn.getTemperature();

		double frictionTorque = 0;
		double frictionLosses = 0;

		if (1 == state.getValue()) {

			// Bearings
			if (rotspeed.getValue() != 0)
				for (int i = 0; i < bearings.length; i++) {
					bearings[i].getInput("RotSpeed").setValue( rotspeed.getValue());
					bearings[i].getInput("ForceRadial").setValue(0);// TODO
					bearings[i].getInput("ForceAxial").setValue(preloadForce[i]);
					bearings[i].getInput("Temperature1").setValue( structure.getTemperature().getValue());
					bearings[i].getInput("Temperature2").setValue(structure.getTemperature().getValue());
					bearings[i].update();

					frictionTorque += bearings[i].getOutput("Torque").getValue();
					frictionLosses += bearings[i].getOutput("PLoss").getValue();
				}

			// Air Gap
			frictionAirGap = AirGap.getTorque(rotspeed.getValue(), structure.getTemperature().getValue(), 
					agDiameter, agGapWidth, agGapLength);

			frictionTorque += frictionAirGap;
			frictionLosses += frictionAirGap * rotspeed.getValue() * 2 * Math.PI;

			motor.getInput("RotSpeed").setValue(rotspeed.getValue());
			motor.getInput("Torque").setValue(
					frictionTorque + torque.getValue());
			motor.update();

			pmech.setValue(rotspeed.getValue() * torque.getValue() * Math.PI
					* 2);
			pel.setValue(motor.getOutput("PTotal").getValue());
			// ploss.setValue(motor.getOutput("PLoss").getValue()+frictionLosses);
			ploss.setValue(motor.getOutput("PLoss").getValue() + frictionLosses);
		} else {
			motor.getOutput("PUse").setValue(0);
			motor.getOutput("PLoss").setValue(0);
			motor.getOutput("PTotal").setValue(0);

			pmech.setValue(0);
			pel.setValue(0);
			// ploss.setValue(motor.getOutput("PLoss").getValue()+frictionLosses);
			ploss.setValue(0);
		}

		// Thermal resistance
		alphaCoolant = coolingDuct.getThermalResistance(fluidProperties
				.getFlowRate(), fluidProperties.getPressureIn(),
				fluidProperties.getTemperatureIn(), structure.getTemperature()
						.getValue());

		// Coolant
		coolant.setThermalResistance(alphaCoolant);
		coolant.setFlowRate(fluidProperties.getFlowRate());
		coolant.setHeatSource(0.0);
		coolant.setTemperatureAmb(structure.getTemperature().getValue());
		coolant.setTemperatureIn(fluidIn.getTemperature());

		// Thermal flows
		structure.setHeatInput(motor.getOutput("PLoss").getValue()
				+ frictionLosses);
		structure.addHeatInput(coolant.getHeatLoss());

		// Update submodels
		structure.integrate(timestep);
		// TODO set Pressure!
		coolant.integrate(timestep, 0, 0, 100000);

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
	public ArrayList<FluidCircuitProperties> getFluidPropertiesList() {
		ArrayList<FluidCircuitProperties> out = new ArrayList<FluidCircuitProperties>();
		out.add(fluidProperties);
		return out;
	}

	@Override
	public void flood() {/* Not used */
	}

	@Override
	public void updateBoundaryConditions() {
		motor.updateBoundaryConditions();
		for (Bearing b : bearings)
			b.updateBoundaryConditions();

		bcHeatSrcAirgap.setValue(frictionAirGap * rotspeed.getValue() * 2
				* Math.PI);
		bcHeatSrcCair.setValue(0); // TODO
		bcCoolantTemperature.setValue(coolant.getTemperature().getValue());
		bcCoolantHTC.setValue(alphaCoolant);
		bcCoolantHeatFlux.setValue(coolant.getHeatLoss());
	}
}
