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

import ch.ethz.inspire.emod.femexport.BoundaryCondition;
import ch.ethz.inspire.emod.femexport.BoundaryConditionType;
import ch.ethz.inspire.emod.model.fluid.FECIdeal;
import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitProperties;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Pump model class. Implements the physical model of a pump with
 * reservoir. From the input parameter mass flow, the electrical power and the
 * supply mass flow are calculated.
 * 
 * Assumptions: Perfect gas
 * 
 * 
 * Inputlist: 1: FluidIn : [-] : - 2: TemperatureAmb : [K] : Ambient Temeprature
 * Outputlist: 1: FluidOut : [-] : 2: Content : [m3] : Current content 5:
 * pressure : [Pa] : Pressure in the tank
 * 
 * Config parameters: GasVolumeInitial : [m3] : Initial volume of the gas in the
 * reservoir FluidValumeInitial : [m3] : Initial volume of the fluid in the
 * reservoir PressureMax : [Pa] : Hysteresis controller max. reservoir pressure
 * PressureMin : [Pa] : Hysteresis controller min. reservoir pressure
 * 
 * @author simon
 * 
 */
@XmlRootElement
public class HydraulicAccumulator extends APhysicalComponent implements
		Floodable {

	@XmlElement
	protected String type;

	// Input parameters:
	private FluidContainer fluidIn;
	private IOContainer temperatureAmb, pressureAmb;
	// Output parameters:
	private IOContainer pfluid;
	private IOContainer content;
	private FluidContainer fluidOut;
	private IOContainer pGas;

	// Boundary Conditions
	private BoundaryCondition bcTemperature;
	private BoundaryCondition bcHTC;

	// Global values
	private double thermalResistance;;

	// Parameters used by the model.
	private double pGasInit; // Initial gas pressure [Pa]
	private double volGasInit; // Initial gas volume [m3]
	private double volFluidInit; // Initial fluid volume [m3]
	private double hystPMax, hystPMin; // Contoller switch off/on values

	private double volFluid; // Fluid mass in the reservoir
	private double volGas; // Gas volume
	private boolean pumpOn; // Pump state
	private double radius; // Radius of the Tank

	// Sub-models used by the model
	private ThermalElement fluid;

	// Fluid properties
	private FluidCircuitProperties fluidCircuitPropertiesIn,
			fluidCircuitPropertiesOut;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public HydraulicAccumulator() {
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
	 * Pump constructor
	 * 
	 * @param type
	 */
	public HydraulicAccumulator(String type) {
		super();

		this.type = type;
		init();
	}

	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init() {
		fluid = new ThermalElement("Example", 1);

		fluidCircuitPropertiesIn = new FluidCircuitProperties(new FECIdeal(),
				fluid.getTemperature());
		fluidCircuitPropertiesOut = new FluidCircuitProperties(new FECIdeal(),
				fluid.getTemperature());

		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		fluidIn = new FluidContainer("FluidIn", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidCircuitPropertiesIn);
		temperatureAmb = new IOContainer("TemperatureAmb", new SiUnit(
				Unit.KELVIN), 293.15, ContainerType.THERMAL);
		pressureAmb = new IOContainer("PressureAmb", new SiUnit(Unit.PA), 1E5,
				ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);
		inputs.add(temperatureAmb);
		inputs.add(pressureAmb);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		fluidOut = new FluidContainer("FluidOut", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidCircuitPropertiesOut);
		content = new IOContainer("Content", new SiUnit(Unit.METERCUBIC), 0,
				ContainerType.FLUIDDYNAMIC);
		pfluid = new IOContainer("State", new SiUnit(Unit.NONE), 0,
				ContainerType.CONTROL);
		pGas = new IOContainer("PressureGas", new SiUnit(Unit.PA), 0,
				ContainerType.INFORMATION);
		outputs.add(fluidOut);
		outputs.add(content);
		outputs.add(pfluid);
		outputs.add(pGas);

		/* Boundary conditions */
		boundaryConditions = new ArrayList<BoundaryCondition>();
		bcTemperature = new BoundaryCondition("Temeprature", new SiUnit("K"),
				293.15, BoundaryConditionType.ROBIN);
		bcHTC = new BoundaryCondition("HTC", new SiUnit("W/K"), 0,
				BoundaryConditionType.ROBIN);
		boundaryConditions.add(bcTemperature);
		boundaryConditions.add(bcHTC);

		fluidCircuitPropertiesOut.setPressureReferenceOut(pGas);
		fluidCircuitPropertiesIn.setPressureReferenceIn(pGas);

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
			this.pGasInit = params.getDoubleValue("GasPressureInitial");
			this.volGasInit = params.getDoubleValue("GasVolumeInitial");
			this.volFluidInit = params.getDoubleValue("FluidVolumeInitial");
			this.hystPMax = params.getDoubleValue("PressureMax");
			this.hystPMin = params.getDoubleValue("PressureMin");

			this.volFluid = volFluidInit;

			/* Sub Models */
			this.fluid.setMass(volFluidInit
					* fluid.getMaterial().getDensity(293, pGasInit));

			/*
			 * Define fluid circuit properties In this case, the element leads
			 * to a non-direct connected in- and outlet!
			 */

			fluid.setMaterial(fluidCircuitPropertiesIn.getMaterial());

			/* States */
			this.dynamicStates = new ArrayList<DynamicState>();
			this.dynamicStates.add(0, this.fluid.getTemperature());

			/*
			 * initial pump state
			 */
			pGas.setValue(pGasInit + pressureAmb.getValue());
			if (pGasInit < hystPMin)
				pumpOn = true;
			else
				pumpOn = false;

			/* Estimate radius */
			radius = Math.pow(0.75 / Math.PI * (volFluidInit + volGasInit),
					0.3333);
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

		// Strictly positive
		if (pGasInit <= 0) {
			throw new Exception("Pump, type:" + type
					+ ": Negative value: Initial pressure must be positive!");
		}
		if (volGasInit <= 0) {
			throw new Exception("Pump, type:" + type
					+ ": Negative value: Initial volume must be positive!");
		}
		if (volFluidInit <= 0) {
			throw new Exception("Pump, type:" + type
					+ ": Negative value: Initial volume must be positive!");
		}

		// Check controller
		if ((hystPMin >= hystPMax) && (volGasInit != 0 && volFluidInit != 0)) {
			throw new Exception(
					"Pump, type:"
							+ type
							+ ": Controller settings: Maximum pressure must be larger than minimum pressure!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {

		fluid.setMaterial(fluidCircuitPropertiesIn.getMaterial());

		/* Convection */
		thermalResistance = 4.0
				* Math.PI
				* Math.pow(radius, 2)
				* Fluid.convectionFreeSphere(new Material("Air"), fluid
						.getTemperature().getValue(),
						temperatureAmb.getValue(), radius);

		/* Thermal Energy flows by fluid */
		fluid.setTemperatureIn(fluidIn.getTemperature());
		fluid.setTemperatureAmb(temperatureAmb.getValue());
		fluid.setThermalResistance(thermalResistance);
		fluid.integrate(timestep, fluidCircuitPropertiesIn.getFlowRate(),
				fluidCircuitPropertiesOut.getFlowRate(), pGas.getValue());

		volFluid = fluid.getVolume();
		/*
		 * New gas volume V_gas(t) [m3] = V_gas,0 [m3] + V_fluid,0 [m3] - V(t)
		 * [m3]
		 */
		volGas = volGasInit + volFluidInit - volFluid;
		if (volGas <= 0)
			volGas = .0001 * volFluidInit;
		/*
		 * New fluid pressure = gas pressure p_gas [Pa] = p_gas,0[Pa] * V_gas,0
		 * [m3] / V_gas [m3]
		 */
		pGas.setValue(pGasInit * volGasInit / volGas + pressureAmb.getValue());

		/* Hysteresis controller for the pump */
		if (pumpOn && pGas.getValue() - pressureAmb.getValue() >= hystPMax)
			pumpOn = false;
		else if (!pumpOn
				&& pGas.getValue() - pressureAmb.getValue() <= hystPMin)
			pumpOn = true;

		if (pumpOn)
			pfluid.setValue(1);
		else
			pfluid.setValue(0);

		content.setValue(fluid.getMass().getValue());

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
		out.add(fluidCircuitPropertiesIn);
		out.add(fluidCircuitPropertiesOut);
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
