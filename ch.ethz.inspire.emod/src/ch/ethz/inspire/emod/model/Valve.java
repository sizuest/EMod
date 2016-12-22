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

import ch.ethz.inspire.emod.model.fluid.FECValve;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitProperties;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Valve model class. Implements the physical model of a valve. Can be
 * used for check valves, (magnetic) way valves, pressure reducing valves.
 * 
 * Assumptions: -2 States (idle, open) -No leakage
 * 
 * 
 * Inputlist: 1: ValveCtrl : [-] ON/OFF position of the valve. 1 means ON, 0
 * means OFF. Only needed for magnetic valves 2: FluidIn : [-]
 * 
 * Outputlist: 1: PLoss : [W] : Power loss 2: PTotal : [W] : Needed electrical
 * power to open and hold the valve 3: FluidOut : [-]
 * 
 * Config parameters: ElectricPower : [W] PressureLossCoefficient : [-] Area :
 * [m2]
 * 
 * @author kraandre
 * 
 */
@XmlRootElement
public class Valve extends APhysicalComponent implements Floodable {

	@XmlElement
	protected String type;

	// Input parameters:
	private IOContainer valveCtrl;
	private FluidContainer fluidIn;

	// Output parameters:
	private IOContainer ploss;
	private IOContainer ptotal;
	private IOContainer puse;
	private FluidContainer fluidOut;

	// Parameters used by the model.
	private double electricPower;
	private double zeta;
	private double area;

	// Fluid properties
	private FluidCircuitProperties fluidProperties;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Valve() {
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
	 * Valve constructor
	 * 
	 * @param type
	 */
	public Valve(String type) {
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
		valveCtrl = new IOContainer("ValveCtrl", new SiUnit(Unit.NONE), 0,
				ContainerType.CONTROL);
		inputs.add(valveCtrl);

		/* Fluid Properties */
		fluidProperties = new FluidCircuitProperties(new FECValve(this));

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0,
				ContainerType.THERMAL);
		ptotal = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0,
				ContainerType.ELECTRIC);
		puse = new IOContainer("PUse", new SiUnit(Unit.WATT), 0,
				ContainerType.FLUIDDYNAMIC);
		outputs.add(ptotal);
		outputs.add(puse);
		outputs.add(ploss);

		/* Fluid in- and output */
		fluidIn = new FluidContainer("FluidIn", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidProperties);
		fluidOut = new FluidContainer("FluidOut", new SiUnit(Unit.NONE),
				ContainerType.FLUIDDYNAMIC, fluidProperties);
		inputs.add(fluidIn);
		outputs.add(fluidOut);

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
			electricPower = params.getDoubleValue("ElectricPower");
			zeta = params.getDoubleValue("PressureLossCoefficient");
			area = params.getDoubleValue("Area");
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
		if (zeta <= 0)
			throw new Exception("Valve, type:" + type
					+ ": Pressure loss coefficient must be greater than zero");

		if (area <= 0)
			throw new Exception("Valve, type:" + type
					+ ": Area must be greater than zero");

		if (electricPower < 0)
			throw new Exception("Valve, type:" + type
					+ ": Electric power must be geq zero");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {

		if (valveCtrl.getValue() > 0) {
			puse.setValue(fluidProperties.getMaterial().getDensity(
					fluidIn.getTemperature())
					/ Math.pow(valveCtrl.getValue() * area, 2)
					/ 2
					* Math.pow(fluidProperties.getFlowRate(), 3));
		} else {
			puse.setValue(0);
		}

		ptotal.setValue(electricPower + fluidProperties.getFlowRate()
				* fluidProperties.getPressureDrop());

		ploss.setValue(ptotal.getValue() - puse.getValue());
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

	
	/**
	 * Returns the pressure loss at the given flow rate
	 * @param flowRate
	 * @return
	 */
	public double getPressure(double flowRate) {
		return Math.pow(flowRate, 2) * getPressureLossCoefficient()
				* Math.signum(flowRate);
	}

	/**
	 * Returns the derivative of the pressure mpa at the given flow rate
	 * @return
	 */
	public double getPressureLossCoefficient() {
		double rho = fluidProperties.getMaterial().getDensity(
				fluidProperties.getTemperatureIn()), u = Math.max(1E-3,
				valveCtrl.getValue());

		return zeta * rho / 2 / Math.pow(u * area, 2);
	}

	/**
	 * Returns whether the valve is closed or not
	 * @return
	 */
	public boolean isClosed() {
		if (valveCtrl.getValue() == 0)
			return true;
		else
			return false;
	}

	@Override
	public void flood() {/* Not used */
	}

	@Override
	public void updateBoundaryConditions() {/* Not used */
	}

}
