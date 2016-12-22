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

package ch.ethz.inspire.emod.model.thermal;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.PropertiesHandler;
import ch.ethz.inspire.emod.model.APhysicalComponent;

/**
 * General forced heat transfere class
 * 
 * Assumptions: Total heat transfere takes place with internal energy of a moved
 * fluid. The internal heat capacity is assumed as constant
 * 
 * 
 * Inputlist: 1: Temperature1 : [K] : Temperature level 1 2: Temperature2 : [K]
 * : Temperature level 2 3: MassFlow : [kg/s] : Mass flow of the fluid
 * 
 * Outputlist: 1: PThermal12 : [K] : Thermal heat flow from 1 to 2 2: PThermal21
 * : [K] : Thermal heat flow from 2 to 1
 * 
 * Config parameters: HeatCapacity : [J/kg/K] : Heat capacity of the fluid
 * 
 * 
 * @author simon
 * 
 */

public class ForcedHeatTransfere extends APhysicalComponent {
	@XmlElement
	protected String type;
	@XmlElement
	protected String parentType;

	// Input Lists
	private IOContainer temp1;
	private IOContainer temp2;
	private IOContainer massFlow;
	// Output parameters:
	private IOContainer pth12;
	private IOContainer pth21;

	// Fluid properties
	private double cp;
	private String fluidType;
	private Material fluid;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public ForcedHeatTransfere() {
		super();
	}

	/**
	 * post xml init method (loading physics data)
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		init();
	}

	/**
	 * Homog. Storage constructor
	 * 
	 * @param type
	 * @param parentType
	 */
	public ForcedHeatTransfere(String type, String parentType) {
		super();

		this.type = type;
		this.parentType = parentType;

		init();
	}

	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init() {
		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		temp1 = new IOContainer("Temperature1", new SiUnit(Unit.KELVIN), 273,
				ContainerType.THERMAL);
		temp2 = new IOContainer("Temperature2", new SiUnit(Unit.KELVIN), 273,
				ContainerType.THERMAL);
		massFlow = new IOContainer("MassFlow", new SiUnit(Unit.KG_S), 0,
				ContainerType.FLUIDDYNAMIC);
		inputs.add(temp1);
		inputs.add(temp2);
		inputs.add(massFlow);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pth12 = new IOContainer("PThermal12", new SiUnit(Unit.WATT), 0,
				ContainerType.THERMAL);
		pth21 = new IOContainer("PThermal21", new SiUnit(Unit.WATT), 0,
				ContainerType.THERMAL);
		outputs.add(pth12);
		outputs.add(pth21);

		/* *********************************************************************** */
		/* Read configuration parameters: */
		/* *********************************************************************** */
		ComponentConfigReader params = null;
		/*
		 * If no parent model file is configured, the local configuration file
		 * will be opened. Otherwise the cfg file of the parent will be opened
		 */
		if (parentType.isEmpty()) {

			String path = PropertiesHandler
					.getProperty("app.MachineDataPathPrefix")
					+ "/"
					+ PropertiesHandler.getProperty("sim.MachineName")
					+ "/"
					+ Defines.MACHINECONFIGDIR
					+ "/"
					+ PropertiesHandler.getProperty("sim.MachineConfigName")
					+ "/"
					+ this.getClass().getSimpleName()
					+ "_"
					+ type
					+ ".xml";
			try {
				params = new ComponentConfigReader(path);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		} else {

			/* Open file containing the parameters of the parent model type */
			try {
				params = new ComponentConfigReader(parentType, type);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		/* Read the config parameter: */
		try {
			fluidType = params.getString("Material");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		params.Close(); /* Model configuration file not needed anymore. */

		// Validate the parameters:
		try {
			checkConfigParams();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Create Fluid object
		fluid = new Material(fluidType);

	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {
		// Not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 * 
	 * @Override
	 */
	@Override
	public void update() {

		cp = fluid.getHeatCapacity();

		/*
		 * The heat transfere from 1 to 2 is Qdot12 [W] = cp [J/K/kg] * mDot
		 * [kg/s] * (T_1 - T_2) [K]
		 */

		pth12.setValue(cp * massFlow.getValue()
				* (temp1.getValue() - temp2.getValue()));
		pth21.setValue(-pth12.getValue());
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
		// TODO this.type = type;
	}

	@Override
	public void updateBoundaryConditions() {
		// TODO Auto-generated method stub

	}
}
