package ch.ethz.inspire.emod.utils;

import java.util.List;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;

/**
 * Implementation of a fluid interface on a component
 * @author manick
 *
 */
public class FluidContainer extends IOContainer {

	/* Values for temperature [K], pressure [Pa] */
	protected double temperature;
	protected double pressure;
	/* FluidCircuitProperties */
	protected FluidCircuitProperties fluidCircuitProperties;

	/**
	 * constructor, set name, unit and type (used in IOContainer)
	 * @param name
	 * @param unit
	 * @param type
	 * @param fluidCircuitProperties 
	 */
	public FluidContainer(String name, SiUnit unit, ContainerType type, FluidCircuitProperties fluidCircuitProperties){
		super(name, unit, 0.00, type);
		this.temperature = 293;
		this.pressure    = 0;
		this.fluidCircuitProperties = fluidCircuitProperties;
	}
	
	/**
	 * constructor, set name, unit and type (used in IOContainer)
	 * @param name
	 * @param reference 
	 * @param unit
	 * @param type
	 * @param fluidCircuitProperties 
	 */
	public FluidContainer(String name, FluidContainer reference, FluidCircuitProperties fluidCircuitProperties){
		super(name, reference);
		this.temperature = 293;
		this.pressure    = 0;
		this.fluidCircuitProperties = fluidCircuitProperties;
	}
	
	/**
	 * FluidContainer()
	 */
	public FluidContainer(){}

	/**
	 * copy constructor
	 * @param that {@link FluidContainer} to copy
	 */
	public FluidContainer(FluidContainer that){
		this.temperature = that.getTemperature();
		this.pressure    = that.getPressure();
	}
	
	/**
	 * set values for temperature, pressure and flowRate all at once
	 */
	public void setValuesAsInput(){
		this.temperature = this.fluidCircuitProperties.getTemperatureIn();
		this.pressure    = this.fluidCircuitProperties.getPressureIn();
	}
	
	/**
	 * set values for temperature, pressure and flowRate all at once
	 */
	public void setValuesAsOutput(){
		this.temperature = this.fluidCircuitProperties.getTemperatureOut();
		this.pressure    = this.fluidCircuitProperties.getPressureOut();
	}
	
	/**
	 * get the temperature
	 * @return the temperature [K]
	 */
	public double getTemperature(){
		return temperature;
	}
	
	/**
	 * get the pressure
	 * @return the pressure [Pa]
	 */
	public double getPressure(){
		return pressure;
	}
	
	/**
	 * get the linked fluid circuit properties
	 * @return {@link FluidCircuitProperties}
	 */
	public FluidCircuitProperties getFluidCircuitProperties(){
		return fluidCircuitProperties;
	}
	
	/**
	 * Override the setValue method from IOContainer
	 * This method gets called when updating the Machine IOConnection-List
	 * --> see EmodSimulationMain.setInputs()
	 * @param value
	 * @deprecated use FluidConnection.update(). Formerly used in EModSimulationMain.setInputs(), changed to Connection.update()
	 */
	@Override
	public void setValue(double value){
		System.err.println("FluidContainer: setValue(...) called: This must not happen!");
	}
	
	/**
	 * Override the getValue method from IOContainer
	 * @return value is not needed in setValue-method
	 * @deprecated use FluidConnection.update(). Formerly used in EModSimulationMain.setInputs(), changed to Connection.update()
	 */
	@Override
	public double getValue(){
		return temperature;
	}
	
	public String toString(){
		return "FluidContainer " + temperature + " " + pressure;
	}

}
