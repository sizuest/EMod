package ch.ethz.inspire.emod.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.femexport.BoundaryCondition;
import ch.ethz.inspire.emod.femexport.BoundaryConditionType;
import ch.ethz.inspire.emod.model.fluid.FECDuct;
import ch.ethz.inspire.emod.model.fluid.FluidCircuitProperties;
import ch.ethz.inspire.emod.model.parameters.PhysicalValue;
import ch.ethz.inspire.emod.model.thermal.ThermalArray;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * Implements the physical model of a single mass element with a heat sorce
 * and a thermal interface to a fluid (coolant)
 * @author Simon Züst
 *
 */
@XmlRootElement
public class CooledHeatSource extends APhysicalComponent implements Floodable{
	
	@XmlElement
	protected String type;
	
	// Inputs
	protected IOContainer heatSource;
	protected FluidContainer fluidIn;
	// Outputs
	protected FluidContainer fluidOut;
	// Fluid Properties
	protected FluidCircuitProperties fluidProperties;
	
	// Model parameters
	protected double massStructure;
	protected double volumeCoolant;
	
	// Intermediate results
	private double currentHTC = 0, currentHeatTransfer = 0;
	private double lastTemperatureIn = Double.NaN;
	
	// Sub models
	protected Duct duct;
	protected ThermalElement structure;
	protected ThermalArray fluid;
	
	// Boundary conditions
	BoundaryCondition bcHeatSource;
	BoundaryCondition bcHeatFlux;
	BoundaryCondition bcFluidTemperature;
	BoundaryCondition bcHTC;
	BoundaryCondition bcStructureTemperature;
	
	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public CooledHeatSource() {
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
	 * CooledHeatSource constructor
	 * 
	 * @param type
	 */
	public CooledHeatSource(String type) {
		super();

		this.type = type;
		init();
	}
	
	
	/**
	 * Initialize the model
	 */
	private void init(){
		// Inputs
		inputs = new ArrayList<IOContainer>();
		heatSource = new IOContainer("HeatInput", new SiUnit("W"), 0, ContainerType.THERMAL);
		inputs.add(heatSource);
		
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
		
		try {
			massStructure = params.getValue("StructureMass", new PhysicalValue(1.0, new SiUnit("kg"))).getValue();
			params.saveValues();

			duct = Duct.buildFromFile(getModelType(), getType(), params.getValue("StructureDuct", "DuctCoolant"));
			volumeCoolant = duct.getVolume();
			structure = new ThermalElement(params.getValue("StructureMaterial", "Steel"), massStructure);
			fluid = new ThermalArray("Example", volumeCoolant, 20);

			/* Fluid properties */
			fluidProperties = new FluidCircuitProperties(new FECDuct(duct, fluid.getTemperature()), fluid.getTemperature());

			/* Define fluid in-/outputs */
			fluidIn = new FluidContainer("CoolantIn", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
			inputs.add(fluidIn);
			fluidOut = new FluidContainer("CoolantOut", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
			outputs.add(fluidOut);

			// Change state names
			structure.getTemperature().setName("TemperatureStructure");
			fluid.getTemperature().setName("TemperatureCoolant");

			// Add states
			dynamicStates = new ArrayList<DynamicState>();
			dynamicStates.add(0, structure.getTemperature());
			dynamicStates.add(1, fluid.getTemperature());

			/* Fluid circuit parameters */
			fluid.setMaterial(fluidProperties.getMaterial());
			duct.setMaterial(fluidProperties.getMaterial());

		} catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
		/* Boundary conditions */
		boundaryConditions = new ArrayList<BoundaryCondition>();
		bcHTC = new BoundaryCondition("CoolantHTC", new SiUnit("W/K"), 0, BoundaryConditionType.ROBIN);
		bcFluidTemperature = new BoundaryCondition("CoolantTemperature", new SiUnit("K"), 293.15, BoundaryConditionType.ROBIN);
		bcHeatFlux = new BoundaryCondition("CoolantHeatFlux", new SiUnit("W"), 0, BoundaryConditionType.NEUMANN);
		bcHeatSource =new BoundaryCondition("HeatSource", new SiUnit("W"), 0, BoundaryConditionType.NEUMANN);
		bcStructureTemperature = new BoundaryCondition("StructureTemperature", new SiUnit("K"), 293.15, BoundaryConditionType.DIRICHLET);
		
		boundaryConditions.add(bcHTC);
		boundaryConditions.add(bcFluidTemperature);
		boundaryConditions.add(bcHeatFlux);
		boundaryConditions.add(bcHeatSource);
		boundaryConditions.add(bcStructureTemperature);
	}
	

	@Override
	public ArrayList<FluidCircuitProperties> getFluidPropertiesList() {
		ArrayList<FluidCircuitProperties> out = new ArrayList<FluidCircuitProperties>();
		out.add(fluidProperties);
		return out;
	}

	@Override
	public void flood() {}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void update() {
		if (fluidIn.getTemperature() <= 0) {
			if (Double.isNaN(lastTemperatureIn))
				lastTemperatureIn = structure.getTemperature().getValue();
		} else
			lastTemperatureIn = fluidIn.getTemperature();


		// Thermal resistance
		currentHTC = duct.getThermalResistance(fluidProperties.getFlowRate(), fluidProperties.getPressureIn(),
				fluidProperties.getTemperatureIn(), structure.getTemperature().getValue());

		// Coolant
		fluid.setThermalResistance(currentHTC);
		fluid.setFlowRate(fluidProperties.getFlowRate());
		fluid.setHeatSource(0.0);
		fluid.setTemperatureAmb(structure.getTemperature().getValue());
		fluid.setTemperatureIn(fluidIn.getTemperature());

		// Thermal flows
		structure.setHeatInput(heatSource.getValue());
		structure.addHeatInput(fluid.getHeatLoss());

		// Update submodels
		structure.integrate(timestep);
		// TODO set Pressure!
		fluid.integrate(timestep, 0, 0, 100000);
	}

	@Override
	public void updateBoundaryConditions() {
		bcStructureTemperature.setValue(structure.getTemperature().getValue());
		bcHTC.setValue(currentHTC);
		bcFluidTemperature.setValue(fluid.getTemperature().getValue());
		bcHeatFlux.setValue(currentHeatTransfer);
		bcHeatSource.setValue(heatSource.getValue());
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

}
