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
 * General Transmission model class. Implements the physical model of a
 * transmission.
 * 
 * Assumptions: constant efficiency, all losses are thermal
 * 
 * Inputlist: 1: RotSpeed : [rpm] : Demanded rotational speed 2: Torque : [Nm] :
 * Demanded torque Outputlist: 1: RotSpeed : [rpm] : Resulting rotational speed
 * 2: Torque : [Nm] : Resulting torque 3: PTotal : [W] : Input power 4: PUse :
 * [W] : Output power (usable) 5: PLoss : [W] : Heat loss
 * 
 * Config parameters: TransmissionRatio : [-] : Ratio between the demaned and
 * the resulting speed Efficiency : [-] : Transmission efficiency
 * 
 * @author simon
 * 
 */
@XmlRootElement
public class Transmission extends APhysicalComponent {

	@XmlElement
	protected String type;

	// Input parameters:
	private IOContainer rotSpeedIn;
	private IOContainer torqueIn;
	// Output parameters:
	private IOContainer rotSpeedOut;
	private IOContainer torqueOut;
	private IOContainer ploss;
	private IOContainer puse;
	private IOContainer ptotal;
	// Boundary conditions
	private BoundaryCondition bcHeatSrc;

	// Parameters used by the model.
	private double k;
	private double eta;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Transmission() {
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
	 * Fan constructor
	 * 
	 * @param type
	 */
	public Transmission(String type) {
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
		rotSpeedIn = new IOContainer("RotSpeed",
				new SiUnit(Unit.REVOLUTIONS_S), 0, ContainerType.MECHANIC);
		torqueIn = new IOContainer("Torque", new SiUnit(Unit.NEWTONMETER), 0,
				ContainerType.MECHANIC);
		inputs.add(rotSpeedIn);
		inputs.add(torqueIn);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		rotSpeedOut = new IOContainer("RotSpeed",
				new SiUnit(Unit.REVOLUTIONS_S), 0, ContainerType.MECHANIC);
		torqueOut = new IOContainer("Torque", new SiUnit(Unit.NEWTONMETER), 0,
				ContainerType.MECHANIC);
		ptotal = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0,
				ContainerType.MECHANIC);
		puse = new IOContainer("PUse", new SiUnit(Unit.WATT), 0,
				ContainerType.MECHANIC);
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0,
				ContainerType.THERMAL);
		outputs.add(rotSpeedOut);
		outputs.add(torqueOut);
		outputs.add(ptotal);
		outputs.add(puse);
		outputs.add(ploss);

		/* Boundary conditions */
		boundaryConditions = new ArrayList<BoundaryCondition>();
		bcHeatSrc = new BoundaryCondition("HeatSrc", new SiUnit("W"), 0,
				BoundaryConditionType.NEUMANN);
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
			System.exit(-1);
		}

		/* Read the config parameter: */
		try {
			k = params.getDoubleValue("TransmissionRatio");
			eta = params.getDoubleValue("Efficiency");
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
		if (k <= 0) {
			throw new Exception(
					"Transmission, type:"
							+ type
							+ ": Negative or zero: TransmissionRatio must be strictly positive");
		}
		if (eta <= 0 && eta > 1) {
			throw new Exception("Transmission, type:" + type
					+ ": Negative, zero or ge 1: Efficiency must be in (0,1]");
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
		 * Transmission k Efficiency eta
		 */
		rotSpeedOut.setValue(rotSpeedIn.getValue() / k);
		torqueOut.setValue(torqueIn.getValue() * k / eta);
		/*
		 * Powers PUse [W] = omegaOut [rpm] * 2*pi/60 [rad/s/rpm] * TOut [N]
		 * PTotal [W] = omegaIn [rpm] * 2*pi/60 [rad/s/rpm] * TIn [N] PLoss [W]
		 * = PTotal [W] - PUse [W];
		 */
		puse.setValue(rotSpeedIn.getValue() * torqueIn.getValue() * 2 * Math.PI);
		ptotal.setValue(rotSpeedOut.getValue() * torqueOut.getValue() * 2
				* Math.PI);
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
	public void updateBoundaryConditions() {
		bcHeatSrc.setValue(ploss.getValue());
	}

}
