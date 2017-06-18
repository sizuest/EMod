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

import ch.ethz.inspire.emod.femexport.BoundaryCondition;
import ch.ethz.inspire.emod.femexport.BoundaryConditionType;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Fan model class. Implements the physical model of a fan. From the
 * input parameter fan level, the mass flow and the electrical power consumption
 * are calculated.
 * 
 * Assumptions: Validity of the fan laws, constant fan efficiency, negligible
 * dynamics
 * 
 * Inputlist: 1: level : [1] : Fan level Outputlist: 1: PTotal : [W] :
 * Calculated total power 2: PLoss : [W] : Calculated thermal loss 3: PUse : [W]
 * : Calculated mechanical power 2: MassFlow : [kg/s] : Calculated mass flow
 * 
 * Config parameters: RhoFluid : [kg/m3] : Desity of the moved fluid PelRef :
 * [W] : Electrical power, reference point VdotRef : [m3/s] : Voluminal flow,
 * reference point, corresponding point for PmechRef pRef : [Pa] : Pressure
 * drop, reference point corresponding point for PmechRef
 * 
 * @author simon
 * 
 */
@XmlRootElement
public class Fan extends APhysicalComponent {

	@XmlElement
	protected String type;

	// Input parameters:
	private IOContainer u;
	// Output parameters:
	private IOContainer pel;
	private IOContainer ploss;
	private IOContainer pmech;
	private IOContainer mdot;
	// Boundary conditions
	private BoundaryCondition bcHeatSrc;

	// Parameters used by the model.
	private double rhoFluid; // Fluid density [kg/m3]
	private double pelRef; // Electrical power reference point [W]
	private double vdotRef; // Voluminal flow reference point [m3/s]
	private double pRef; // Pressure reference point [Pa]

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Fan() {
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
	 * Fan constructor
	 * 
	 * @param type
	 */
	public Fan(String type) {
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
		u = new IOContainer("level", new SiUnit(Unit.NONE), 0, ContainerType.CONTROL);
		inputs.add(u);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pel = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		pmech = new IOContainer("PUse", new SiUnit(Unit.WATT), 0, ContainerType.FLUIDDYNAMIC);
		mdot = new IOContainer("MassFlow", new SiUnit(Unit.KG_S), 0, ContainerType.FLUIDDYNAMIC);
		outputs.add(pel);
		outputs.add(ploss);
		outputs.add(pmech);
		outputs.add(mdot);

		/* Boundary conditions */
		boundaryConditions = new ArrayList<BoundaryCondition>();
		bcHeatSrc = new BoundaryCondition("HeatSrc", new SiUnit("W"), 0, BoundaryConditionType.NEUMANN);
		boundaryConditions.add(bcHeatSrc);

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
			rhoFluid = params.getPhysicalValue("DensityFluid", new SiUnit("kg m^-3")).getValue();
			pelRef = params.getPhysicalValue("PowerRef", new SiUnit("W")).getValue();
			vdotRef = params.getPhysicalValue("VoluminalFlowRef", new SiUnit("m^3 s^-1")).getValue();
			pRef = params.getPhysicalValue("PressureRef", new SiUnit("Pa")).getValue();
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
		if (rhoFluid <= 0) {
			throw new Exception(
					"Fan, type:"
							+ type
							+ ": Negative or zero: Fluid density must be strictly positive");
		}
		if (pelRef <= 0) {
			throw new Exception(
					"Fan, type:"
							+ type
							+ ": Negative or zero: Reference power must be strictly positive");
		}
		if (vdotRef <= 0) {
			throw new Exception(
					"Fan, type:"
							+ type
							+ ": Negative or zero: Reference voluminal flow must be strictly positive");
		}
		if (pRef <= 0) {
			throw new Exception(
					"Fan, type:"
							+ type
							+ ": Negative or zero: Reference pressure must be strictly positive");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {

		/*
		 * mdot [kg/s] = Vdot_ref [m^3/s] * rho [kg/m^3] * u [1]
		 */
		mdot.setValue(u.getValue() * vdotRef * rhoFluid);

		/*
		 * Pel [W] = Pel_ref [W] * u^3 [1]
		 */
		pel.setValue(Math.abs(pelRef * Math.pow(u.getValue(), 3)));

		/*
		 * Pmech[W] = mdot [kg/s] / rho [kg/m3] * p [Pa] where p [Pa] = pRef *
		 * u^2
		 */
		pmech.setValue(Math.abs(mdot.getValue() / rhoFluid * pRef
				* Math.pow(u.getValue(), 2)));

		/*
		 * The loss is the difference between electrical and mechanical power
		 */
		ploss.setValue(pel.getValue() - pmech.getValue());

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
		bcHeatSrc.setValue(ploss.getValue());
	}
}
