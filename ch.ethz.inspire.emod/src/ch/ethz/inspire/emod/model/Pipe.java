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

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.dd.model.DuctPipe;
import ch.ethz.inspire.emod.femexport.BoundaryCondition;
import ch.ethz.inspire.emod.femexport.BoundaryConditionType;
import ch.ethz.inspire.emod.model.fluid.FECDuct;
import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitProperties;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.thermal.ThermalArray;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Pipe model class. Implements the physical model of a hydraulic pipe
 * 
 * Assumptions: -No leakage -Separation between laminar and turbulent flow
 * -Smooth surface -Pipe wall is rigid
 * 
 * Inputlist: 1: PressureOut : [Pa] : Pressure at the end 2: MassflowOut :
 * [kg/s] : Mass flow in the pipe 3: TemperatureIn : [K] : Inlet temperature 4:
 * TemperatureAmb: [K] : Ambient temperature Outputlist: 1: PressureIn : [Pa] :
 * Pressure in the cylinder chamber 2: MassFlowIn : [kg/s] : Mass flow into the
 * cylinder chamber 3: TemperatureOut: [K] : Outlet temperature 4: PLoss : [W] :
 * Power loss 5: PressureLoss : [Pa] : Pressure difference over the pipe
 * 
 * Config parameters: PipeDiameter : [m] PipeLength : [m] PipeThickness : [m]
 * PipeMaterial : [Material];
 * 
 * 
 * @author kraandre, sizuest
 * 
 */
@XmlRootElement
public class Pipe extends APhysicalComponent implements Floodable {
	// public class Pipe<T> extends APhysicalComponent<T>{

	@XmlElement
	protected String type;

	// Input parameters:
	private IOContainer temperatureAmb;
	private FluidContainer fluidIn;

	// Output parameters:
	private IOContainer ploss;
	private IOContainer pressureloss;
	private IOContainer temperaturePipe;
	private FluidContainer fluidOut;

	// Boundary Conditions
	private BoundaryCondition bcTemperature;
	private BoundaryCondition bcHTC;

	// Fluid Properties
	FluidCircuitProperties fluidProperties;

	// Parameters used by the model.
	private double pipeDiameter;
	private double pipeLength;
	private double pipeThickness;
	private double pipeRoughness;
	private Material pipeMaterial;
	private double volume;
	double pipeArea, pipeThTransmittance;
	private ThermalArray fluid;
	private Duct duct;

	// Global values
	private double thermalResistance;

	// Initial temperature
	double temperatureInit = 293;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Pipe() {
		super();

		this.type = "Example";
		this.temperatureInit = 293;
		init();
		this.fluidProperties.setMaterial(new Material("Monoethylenglykol_34"));
	}

	/**
	 * @param u
	 * @param parent
	 * @throws Exception
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent)
			throws Exception {
		// post xml init method (loading physics data)
		init();

	}

	/**
	 * Pipe constructor
	 * 
	 * @param type
	 */
	public Pipe(String type) {
		super();

		this.type = type;
		init();
	}

	/**
	 * Pipe constructor
	 * 
	 * @param type
	 * @param temperatureInit
	 * @param fluidType
	 */
	public Pipe(String type, double temperatureInit, String fluidType) {
		super();

		this.type = type;
		this.temperatureInit = temperatureInit;
		init();
		this.fluidProperties.setMaterial(new Material(fluidType));
		this.fluid.getTemperature().setInitialCondition(temperatureInit);
		this.fluid.getTemperature().setInitialCondition();
	}

	/**
	 * Pipe constructor
	 * 
	 * @param type
	 * @param temperatureInit
	 * @param fluid
	 * @throws Exception
	 */
	/*
	 * public Pipe(String type, double temperatureInit, String materialName,
	 * double volume, int numElements) { super();
	 * 
	 * this.type = type; this.temperatureInit = temperatureInit; this.fluid =
	 * new ThermalArray(materialName, volume, numElements); init(); }
	 */

	/**
	 * Called from constructor or after unmarshaller.
	 * 
	 * @throws Exception
	 */
	private void init() {
		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		temperatureAmb = new IOContainer("TemperatureAmb", new SiUnit(
				Unit.KELVIN), temperatureInit, ContainerType.THERMAL);
		inputs.add(temperatureAmb);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0.00,
				ContainerType.THERMAL);
		pressureloss = new IOContainer("PressureLoss", new SiUnit(Unit.PA),
				0.00, ContainerType.FLUIDDYNAMIC);
		temperaturePipe = new IOContainer("Temperature",
				new SiUnit(Unit.KELVIN), temperatureInit, ContainerType.THERMAL);
		outputs.add(ploss);
		outputs.add(pressureloss);
		outputs.add(temperaturePipe);

		/* Boundary conditions */
		boundaryConditions = new ArrayList<BoundaryCondition>();
		bcTemperature = new BoundaryCondition("Temperature", new SiUnit("K"),
				293.15, BoundaryConditionType.ROBIN);
		bcHTC = new BoundaryCondition("HTC", new SiUnit("W/K"), 0,
				BoundaryConditionType.ROBIN);

		/* *********************************************************************** */
		/* Read configuration parameters: */
		/* *********************************************************************** */
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader(getModelType(), type);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		/* Read the config parameter: */
		try {
			pipeDiameter = params.getPhysicalValue("PipeDiameter", new SiUnit("m")).getValue();
			pipeLength = params.getPhysicalValue("PipeLength", new SiUnit("m")).getValue();
			pipeThickness = params.getPhysicalValue("PipeThickness", new SiUnit("m")).getValue();
			pipeRoughness = params.getPhysicalValue("PipeRoughness", new SiUnit("m")).getValue();
			pipeMaterial = params.getMaterial("PipeMaterial");

			// Calculate constant parameters
			pipeArea = 2 * Math.PI * pipeDiameter / 2 * pipeLength;
			pipeThTransmittance = pipeMaterial.getThermalConductivity()
					/ pipeThickness;
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

		try {
			volume = Math.pow(pipeDiameter / 2, 2) * Math.PI * pipeLength;
			fluid = new ThermalArray("Example", volume, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Duct */
		duct = new Duct();
		duct.addElement(new DuctPipe(pipeLength, pipeDiameter, pipeRoughness));

		/* Fluid circuit parameters */
		fluidProperties = new FluidCircuitProperties(new FECDuct(duct,
				fluid.getTemperature()), fluid.getTemperatureOut());
		fluidProperties.setMaterial(fluid.getMaterial());
		duct.setMaterial(fluidProperties.getMaterial());

		/* add fluid In/Output */
		fluidIn = new FluidContainer("FluidIn", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidProperties);
		inputs.add(fluidIn);
		fluidOut = new FluidContainer("FluidOut", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidProperties);
		outputs.add(fluidOut);

		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid.getTemperature());

	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {
		if (0 > pipeDiameter) {
			throw new Exception(
					"Pipe, type:"
							+ type
							+ ": Non physical value: Variable 'pipeDiameter' must be bigger than zero!");
		}
		if (0 > pipeLength) {
			throw new Exception(
					"Pipe, type:"
							+ type
							+ ": Non physical value: Variable 'pistonLength' must be bigger than zero!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		duct.setMaterial(fluidProperties.getMaterial());

		/* Local variables */
		double alphaFluid, alphaAir;
		
		/* Pressure loss */
		pressureloss.setValue(fluidProperties.getPressureDrop());

		/* Set fluid obj. boundary positions */
		fluid.setTemperatureIn(fluidIn.getTemperature());
		fluid.setFlowRate(fluidProperties.getFlowRate());

		/* Calculate alphaFluid */
		if (fluid.getFlowRate() > 0)
			alphaFluid = Fluid.convectionForcedPipe(fluid.getMaterial(), fluid
					.getTemperature().getValue(), pipeLength, pipeDiameter,
					fluid.getFlowRate());
		else
			alphaFluid = Fluid.convectionFreeCylinderHorz(fluid.getMaterial(),
					fluid.getTemperature().getValue(),
					temperatureAmb.getValue(), pipeDiameter);

		/* Calculate alphaAir */
		alphaAir = Fluid.convectionFreeCylinderHorz(new Material("Air"), fluid
				.getTemperature().getValue(), temperatureAmb.getValue(),
				pipeDiameter);

		/* Calculate overall thermal Resistance of pipe */
		// double thermalResistance = 1/(alphaFluid * pipeArea) + 0.03 / (0.25 *
		// pipeArea) + 1/(alphaAir * pipeArea);
		thermalResistance = 1 / (alphaFluid * pipeArea) + 1
				/ (pipeThTransmittance * pipeArea) + 1 / (alphaAir * pipeArea);
		if (Double.isNaN(thermalResistance)
				| Double.isInfinite(thermalResistance))
			thermalResistance = 0;
		else
			thermalResistance = 1 / thermalResistance;

		// set array boundary conditions
		fluid.setThermalResistance(thermalResistance);
		fluid.setHeatSource(pressureloss.getValue() * fluid.getFlowRate());
		fluid.setTemperatureAmb(temperatureAmb.getValue());

		/* *********************************************************************** */
		/* Integration step: */
		/* *********************************************************************** */
		fluid.integrate(timestep, 0, 0, 100000);
		// TODO: Pressure

		ploss.setValue(fluid.getHeatLoss());
		temperaturePipe.setValue(fluid.getTemperature().getValue());
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

	/**
	 * Fluid Type
	 * 
	 * @return {@link Material}
	 */
	public String getFluidType() {
		return fluid.getMaterial().getType();
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
		bcTemperature.setValue(fluid.getTemperature().getValue());
		bcHTC.setValue(thermalResistance);
	}
}
