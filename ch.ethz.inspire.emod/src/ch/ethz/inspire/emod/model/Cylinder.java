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
import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitProperties;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Cylinder model class. Implements the physical model of a hydraulic
 * cylinder
 * 
 * Assumptions: -3 States (idle, moving, extended&hold) -Leakage only occurs
 * internally and is treated as annular passage flow. -Fitting between cylinder
 * and piston is assumed as H7/h6. -Friction is taken into account by
 * hydraulic-mechanic efficiency
 * 
 * 
 * Inputlist: 1: Force : [N] : Required force 2: Velocity : [mm/min] : Required
 * displacement velocity 3: FluidIn : [-] : Fluid input Outputlist: 1: PUse :
 * [W] : Mechanical power 2: PLoss : [W] : Power loss 3: PHydraulic : [W] :
 * Hydraulic power 4: FluidOut : [-] : Fluid output
 * 
 * Config parameters: PistonDiameter : [m] PistonThickness : [m] Stroke : [m]
 * Efficiency : [] : Hydraulic-mechanic PistonRodDiameter : [m]
 * ConnectionDiameter : [m] : Dyameter of the connection PMax : [bar] : Maximum
 * allowed pressure in the cylinder CylinderType : [] : According to this
 * parameter, the cylinder type is chosen. 1 = single-action cylinder, 2 =
 * double action cylinder
 * 
 * @author kraandre
 * 
 */
@XmlRootElement
public class Cylinder extends APhysicalComponent implements Floodable {

	@XmlElement
	protected String type;

	// Input parameters:
	private IOContainer force;
	private IOContainer velocity;
	private FluidContainer fluidIn;

	// Output parameters:
	private IOContainer pmech;
	private IOContainer ploss;
	private IOContainer phydr;
	private IOContainer flowRate;
	private FluidContainer fluidOut;

	// Parameters used by the model.
	private double pistonDiameter;
	private double pistonThickness;
	private double stroke;
	private double H_7; // Tolerance Cylinder
	private double h_6; // Tolerance Piston
	private double efficiency;
	private double pistonRodDiameter;
	private double k = 0.5; // Geometric loss koefficient
	private double area;
	private double flowRateLeak = 0;
	private double structureMass = 1;
	private String structureMaterial = "Steel";

	/* Fluid Properties */
	private FluidCircuitProperties fluidProperties;

	/* Position */
	private DynamicState position;

	/* Thermal elements */
	private ThermalElement structure, fluid;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Cylinder() {
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
	 * Cylinder constructor
	 * 
	 * @param type
	 */
	public Cylinder(String type) {
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
		force = new IOContainer("Force", new SiUnit(Unit.NEWTON), 0,
				ContainerType.MECHANIC);
		velocity = new IOContainer("Velocity", new SiUnit(Unit.M_S), 0,
				ContainerType.MECHANIC);
		inputs.add(force);
		inputs.add(velocity);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pmech = new IOContainer("PUse", new SiUnit(Unit.WATT), 0,
				ContainerType.MECHANIC);
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0,
				ContainerType.THERMAL);
		phydr = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0,
				ContainerType.FLUIDDYNAMIC);
		outputs.add(pmech);
		outputs.add(ploss);
		outputs.add(phydr);

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
			pistonDiameter = params.getDoubleValue("PistonDiameter");
			pistonThickness = params.getDoubleValue("PistonThickness");
			stroke = params.getDoubleValue("CylinderStroke");
			efficiency = params.getDoubleValue("Efficiency");
			pistonRodDiameter = params.getDoubleValue("PistonRodDiameter");
			params.getDoubleValue("ConnectionDiameter");
			structureMass = params.getDoubleValue("StructuralMass");
			structureMaterial = params.getString("StructureMaterial");
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

		/* Area */
		area = Math.PI
				/ 4
				* (Math.pow(pistonDiameter, 2) - Math.pow(pistonRodDiameter, 2));

		/* Choosing of the fitting according to the piston diameter */
		if (pistonDiameter > 0.03 && pistonDiameter <= 0.05) {
			H_7 = 12.5 * Math.pow(10, -6);
			h_6 = 8 * Math.pow(10, -6);
		}

		else if (pistonDiameter > 0.05 && pistonDiameter <= 0.065) {
			H_7 = 15 * Math.pow(10, -6);
			h_6 = 19 / 2 * Math.pow(10, -6);
		}

		else if (pistonDiameter > 0.065 && pistonDiameter <= 0.08) {
			H_7 = 15 * Math.pow(10, -6);
			h_6 = 19 / 2 * Math.pow(10, -6);
		}

		else if (pistonDiameter > 0.08 && pistonDiameter <= 0.1) {
			H_7 = 17.5 * Math.pow(10, -6);
			h_6 = 11 * Math.pow(10, -6);
		}

		/* Thermal elements */
		structure = new ThermalElement(structureMaterial, structureMass);
		fluid = new ThermalElement("Example", 1);

		structure.getTemperature().setName("TemperatureStructure");
		fluid.getTemperature().setName("TemperatureFluid");
		fluid.getTemperature().setInitialCondition(293.15);
		fluid.setVolume(area * stroke);

		/* Define state */
		position = new DynamicState("Position", new SiUnit(Unit.M));
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(position);
		dynamicStates.add(structure.getTemperature());
		dynamicStates.add(fluid.getTemperature());

		/* Fluid Properties */
		flowRate = new IOContainer("FlowRate", new SiUnit("m^3/s"), 0);
		fluidProperties = new FluidCircuitProperties(
				new FECForcedFlow(flowRate), fluid.getTemperature());

		fluid.setMaterial(fluidProperties.getMaterial());

		fluidIn = new FluidContainer("FluidIn", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidProperties);
		fluidOut = new FluidContainer("FluidOut", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidProperties);

		inputs.add(fluidIn);
		outputs.add(fluidOut);
	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {
		if (0 > pistonDiameter) {
			throw new Exception(
					"Cylinder, type:"
							+ type
							+ ": Non physical value: Variable 'PistonDiameter' must be bigger than zero!");
		}

		if (0 > pistonThickness) {
			throw new Exception(
					"Cylinder, type:"
							+ type
							+ ": Non physical value: Variable 'PistonThickness' must be bigger than zero!");
		}

		if (0 > efficiency || efficiency > 1) {
			throw new Exception(
					"Cylinder, type:"
							+ type
							+ ": Non physical value: Variable 'Efficiency' must reach from bigger than zero to 1!");
		}

		if (0 > pistonRodDiameter) {
			throw new Exception(
					"Cylinder, type:"
							+ type
							+ ": Non physical value: Variable 'PistonRodDiameter' must be bigger than zero!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {

		fluid.setMaterial(fluidProperties.getMaterial());

		double pressureDrop = 0, viscosity, density, velocity, deltaPosition, heat2Fluid, fluidRth;

		/* Material properties */
		viscosity = fluid.getMaterial().getViscosityDynamic(
				fluidIn.getTemperature());
		density = fluid.getMaterial().getDensity(fluidIn.getTemperature(),
				fluidIn.getPressure());

		/* Position */
		velocity = this.velocity.getValue();
		deltaPosition = velocity * timestep;

		if (position.getValue() + deltaPosition < 0) {
			deltaPosition = -position.getValue();
		} else if (position.getValue() + deltaPosition > stroke) {
			deltaPosition = stroke - position.getValue();
		}

		position.addValue(deltaPosition);
		position.setTimestep(timestep);
		velocity = position.getTimeDerivate();

		/* Pressure drop */
		if (0 == velocity)
			pressureDrop = Math.abs(force.getValue())
					/ (Math.PI / 4 * (Math.pow(pistonDiameter, 2) - Math.pow(
							pistonRodDiameter, 2)));
		else
			pressureDrop = Math.abs(force.getValue())
					/ (efficiency * area)
					+ 875
					* Math.pow(velocity, 2)
					* Math.pow(
							Math.PI
									/ 4
									* (Math.pow(pistonDiameter, 2) - Math.pow(
											pistonRodDiameter, 2)), 2)
					/ (2 * Math.pow(Math.PI / 4 * k * Math.pow(0.01, 2), 2));

		/* Leak flow */
		flowRateLeak = Math.PI
				* pressureDrop
				* Math.pow(pistonDiameter / 2 + H_7
						- (pistonDiameter / 2 - h_6), 3)
				* (pistonDiameter / 2 + H_7 + pistonDiameter / 2 - h_6)
				/ (12 * viscosity * Math.pow(10, -6) * pistonThickness)
				/ density;

		/* Flow Rate */
		flowRate.setValue(flowRateLeak + Math.abs(velocity * area));

		/* Powers */
		pmech.setValue(Math.abs(force.getValue()) * Math.abs(velocity));
		phydr.setValue((fluidProperties.getPressureIn() - fluidProperties
				.getPressureOut()) * flowRate.getValue());
		ploss.setValue(phydr.getValue() - pmech.getValue());

		/* Heat Flux */
		double surface = Math.PI * pistonDiameter * stroke;
		if (velocity == 0)
			fluidRth = surface
					* Fluid.convectionFreeCylinderHorz(fluid.getMaterial(),
							structure.getTemperature().getValue(), fluid
									.getTemperature().getValue(),
							pistonDiameter);
		else
			fluidRth = surface
					* Fluid.convectionForcedPipe(fluid.getMaterial(), fluid
							.getTemperature().getValue(), stroke,
							pistonDiameter, flowRate.getValue());

		heat2Fluid = fluidRth
				* (structure.getTemperature().getValue() - fluid
						.getTemperature().getValue());
		structure.setHeatInput(-heat2Fluid);

		fluid.setHeatInput(heat2Fluid + 0 * ploss.getValue());
		fluid.setTemperatureIn(fluidProperties.getTemperatureIn());

		fluid.integrate(timestep, flowRate.getValue(), flowRate.getValue(),
				fluidProperties.getPressure());
		structure.integrate(timestep);

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
		// TODO Auto-generated method stub

	}

}
