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

import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.PropertiesHandler;
import ch.ethz.inspire.emod.model.APhysicalComponent;

/**
 * General layer thermal storage class
 * 
 * Assumptions: Specific heat constant does not depend on temperature.
 * Convection and conduction losses trough the wall are dominat compared to
 * radiation
 * 
 * 
 * Inputlist: 1: TemperatureIn : [K] : Temperature of inflow 2: MassFlow :
 * [kg/s] : Massflow in 3: TemperatureAmb : [K] : Temperature of ambient 4:
 * HeatSource : [W] : Interal heat sources
 * 
 * Outputlist: 1: TemperatureOut : [K] : Temperature of outflow 2: PLoss: : [W]
 * : Thermal loss
 * 
 * Config parameters: HeatCapacity : [J/K/kg] : Internal heat capacity Mass :
 * [kg] : Total mass of the storage Surface : [m^2] : Surface of the storage
 * exposed to ambient ConvectionConstant : [W/K/m^2] : Heat transfer constant
 * for convection ConductionConstant : [W/K/m] : Heat transfer constant for
 * conduction WallThickness : [m] : Thickness of the storage wall
 * InitialTemperature : [K] : Initial temperature NumberOfElements : [-] :
 * Number of elements (to divide the pipe in)
 * 
 * 
 * @author simon
 * 
 */

public class LayerStorage extends APhysicalComponent {
	@XmlElement
	protected String type;
	@XmlElement
	protected String parentType;

	// Input Lists
	private IOContainer tempIn;
	private IOContainer tempAmb;
	private IOContainer mDotIn;
	private IOContainer heatSrc;
	private IOContainer pressure;

	// Output parameters:
	private IOContainer tempOut;
	private IOContainer tempAvg;
	private IOContainer ploss;

	// Unit of the element
	private double volume;
	private double surf;
	private double alpha;
	private double temperatureInit = 0;
	private int nElements;
	private String fluidType;

	private ThermalArray thermalArray;

	// Heat transfere resistance
	private double thRessistance;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public LayerStorage() {
		super();
	}

	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		// post xml init method (loading physics data)
		loadParameters();
		init();
	}

	/**
	 * Layer Storage constructor
	 * 
	 * @param type
	 * @param parentType
	 */
	public LayerStorage(String type, String parentType) {
		super();

		this.type = type;
		this.parentType = parentType;

		loadParameters();
		init();
	}

	/**
	 * Layer Storage constructor
	 * 
	 * @param fluidType
	 * @param volume
	 * @param surf
	 * @param alpha
	 * @param nElements
	 * @param temperatureInit
	 */
	public LayerStorage(String fluidType, double volume, double surf,
			double alpha, int nElements, double temperatureInit) {
		this.fluidType = fluidType;
		this.volume = volume;
		this.surf = surf;
		this.alpha = alpha;
		this.nElements = nElements;
		this.temperatureInit = temperatureInit;

		init();
	}

	/**
	 * Layer Storage constructor
	 * 
	 * @param type
	 * @param parentType
	 * @param temperatureInit
	 */
	public LayerStorage(String type, String parentType, double temperatureInit) {
		super();

		this.type = type;
		this.parentType = parentType;
		this.temperatureInit = temperatureInit;

		loadParameters();
		init();
	}

	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init() {
		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		tempIn = new IOContainer("TemperatureIn", new SiUnit(Unit.KELVIN), 293,
				ContainerType.THERMAL);
		tempAmb = new IOContainer("TemperatureAmb", new SiUnit(Unit.KELVIN),
				293, ContainerType.THERMAL);
		mDotIn = new IOContainer("MassFlow", new SiUnit(Unit.KG_S), 0,
				ContainerType.FLUIDDYNAMIC);
		heatSrc = new IOContainer("HeatSource", new SiUnit(Unit.WATT), 0,
				ContainerType.THERMAL);
		pressure = new IOContainer("Pressure", new SiUnit(Unit.PA), 1E5,
				ContainerType.FLUIDDYNAMIC);
		inputs.add(tempIn);
		inputs.add(tempAmb);
		inputs.add(mDotIn);
		inputs.add(heatSrc);
		inputs.add(pressure);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		tempOut = new IOContainer("TemperatureOut", new SiUnit(Unit.KELVIN), 0,
				ContainerType.THERMAL);
		tempAvg = new IOContainer("TemperatureAvg", new SiUnit(Unit.KELVIN), 0,
				ContainerType.THERMAL);
		ploss = new IOContainer("PLoss", new SiUnit(Unit.WATT), 0,
				ContainerType.THERMAL);
		outputs.add(tempOut);
		outputs.add(tempAvg);
		outputs.add(ploss);

		// Validate the parameters:
		try {
			checkConfigParams();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Array object:
		thermalArray = new ThermalArray(fluidType, volume, nElements);
		thermalArray.getTemperature().setInitialCondition(temperatureInit);

		// Temperature state:
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(thermalArray.getTemperature());

		/*
		 * Calculate thermal ressistance k, with cases: - alpha=0 k = 0 - else k
		 * = alpha
		 */
		if (0 == alpha)
			thRessistance = 0.0;
		else
			thRessistance = alpha * surf;
	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {
		// Check model parameters:
		// Parameter must be non negative and non zero
		if (surf < 0) {
			throw new Exception("LayerStorage, type:" + type
					+ ": Negative value: Surface must be non negative");
		}
		if (alpha < 0) {
			throw new Exception(
					"LayerStorage, type:"
							+ type
							+ ": Negative value: ConvectionConstant must be non negative");
		}
		if (nElements < 1) {
			throw new Exception("LayerStorage, type:" + type
					+ ": Negative value: NumberOfElements must be at least one");
		}

	}

	private void loadParameters() {
		/* *********************************************************************** */
		/* Read configuration parameters: */
		/* *********************************************************************** */
		ComponentConfigReader params = null;
		String path;
		/*
		 * If no parent model file is configured, the local configuration file
		 * will be opened. Otherwise the cfg file of the parent will be opened
		 */
		if (parentType.isEmpty()) {
			path = PropertiesHandler.getProperty("app.MachineDataPathPrefix")
					+ "/" + PropertiesHandler.getProperty("sim.MachineName")
					+ "/" + Defines.MACHINECONFIGDIR + "/"
					+ PropertiesHandler.getProperty("sim.MachineConfigName")
					+ "/" + this.getClass().getSimpleName() + "_" + type
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
			/*
			 * Since there are multiple sources for the surface characterization
			 * we have to test all of them
			 */
			if (parentType.contentEquals("Pipe")) {
				surf = params.getDoubleValue("PipeDiameter")
						* params.getDoubleValue("PipeLength") * Math.PI;
				volume = Math.pow(params.getDoubleValue("PipeDiameter") / 2, 2)
						* params.getDoubleValue("PipeLength") * Math.PI;
			} else {
				surf = params.getDoubleValue("thermal.Surface");
				volume = params.getDoubleValue("thermal.Volume");
			}

			// Load the other parameters
			alpha = params.getDoubleValue("thermal.ConvectionConstant");
			nElements = params.getIntValue("thermal.NumberOfElements");
			fluidType = params.getString("Material");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		params.Close(); /* Model configuration file not needed anymore. */
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

		// Set boundary conditions
		thermalArray.setFlowRate(mDotIn.getValue()
				/ thermalArray.getMaterial().getDensity(tempIn.getValue(),
						pressure.getValue()));
		thermalArray.setHeatSource(0);
		thermalArray.setTemperatureAmb(tempAmb.getValue());
		thermalArray.setTemperatureIn(tempIn.getValue());
		thermalArray.setThermalResistance(thRessistance);

		// Update thermal array
		thermalArray.integrate(timestep, 0, 0, pressure.getValue());

		// Set outputs
		tempOut.setValue(thermalArray.getTemperatureOut().getValue());
		ploss.setValue(thermalArray.getHeatLoss());
		tempAvg.setValue(thermalArray.getTemperature().getValue());

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
