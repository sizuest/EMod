package ch.ethz.inspire.emod.utils;

import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.Unit;

public class FluidContainer extends IOContainer {

	/* Current temperature [K], pressure [Pa], flow rate [m^3/s]*/
	protected double temperature;
	protected double pressure;
	protected double flowRate;
	//protected Material material;
	//Material ist bereits im ThermalArray enthalten!

	/**
	 * constructor, set name, unit and type (used in IOContainer)
	 * @param name
	 * @param unit
	 * @param type
	 */
	public FluidContainer(String name, Unit unit, ContainerType type){
		super(name, unit, 0.00, type);
		this.temperature = 0.00;
		this.pressure    = 0.00;
		this.flowRate    = 0.00;
	}
	
	public FluidContainer(){	}

	/**
	 * copy constructor
	 * @param FluidContainer that to copy
	 */
	public FluidContainer(FluidContainer that){
		this.temperature = that.getTemperature();
		this.pressure    = that.getPressure();
		this.flowRate    = that.getFlowRate();
		//this.material    = that.material;
	}
	
	/**
	 * set values for temperature, pressure and flowRate all at once
	 * @param temperature [K]
	 * @param pressure    [Pa]
	 * @param flowRate    [m^3/s]
	 */
	public void setValues(double temperature, double pressure, double flowRate){
		this.temperature = temperature;
		this.pressure = pressure;
		this.flowRate = flowRate;
	}
	
	/**
	 * the temperature to set
	 * @param temperature [K]
	 */
	public void setTemperature(double temperature){
		this.temperature = temperature;
	}
	
	/**
	 * get the temperature
	 * @return the temperature [K]
	 */
	public double getTemperature(){
		return temperature;
	}

	/**
	 * the pressure to set
	 * @param pressure [Pa]
	 */
	public void setPressure(double pressure){
		this.pressure = pressure;
	}
	
	/**
	 * get the pressure
	 * @return the pressure [Pa]
	 */
	public double getPressure(){
		return pressure;
	}

	/**
	 * the flowRate to set
	 * @param flowRate [m^3/s]
	 */
	public void setFlowRate(double flowRate){
		this.flowRate = flowRate;
	}
	
	/**
	 * get the flowRate
	 * @return the flowRate [m^3/s]
	 */
	public double getFlowRate(){
		return flowRate;
	}
	
	public String toString(){
		return "FluidContainer " + temperature + " " + pressure + " " + flowRate;
	}
}
