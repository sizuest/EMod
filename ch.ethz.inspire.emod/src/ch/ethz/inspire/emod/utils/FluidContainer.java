package ch.ethz.inspire.emod.utils;

import java.util.List;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.Unit;

public class FluidContainer extends IOContainer {

	/* Values for temperature [K], pressure [Pa], flow rate [m^3/s]*/
	protected double temperature;
	protected double pressure;
	protected double flowRate;

	/**
	 * constructor, set name, unit and type (used in IOContainer)
	 * @param name
	 * @param unit
	 * @param type
	 */
	public FluidContainer(String name, Unit unit, ContainerType type){
		super(name, unit, 0.00, type);
		this.temperature = 293;
		this.pressure    = 100000;
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
	
	/**
	 * Override the setValue method from IOContainer
	 * This method gets called when updating the Machine IOConnection-List
	 * --> see EmodSimulationMain.setInputs()
	 * @param value
	 * @deprecated use FluidConnection.update(). Formerly used in EModSimulationMain.setInputs(), changed to Connection.update()
	 */
	@Override
	public void setValue(double value){
		//Load the IOConnection List (containing also FluidConnections)
		List<IOConnection> ioc = Machine.getInstance().getIOLinkList();
		for(IOConnection io:ioc){
			//if the target of a connection equals to this FluidContainer, start the update
			if(io.getTarget().equals(this)){
				/* direction of calculation
				 * temperature [K]    : source --> target
				 * pressure    [Pa]   : source --> target
				 * flowRate:   [m^3/s]: source <-- target
				 */
				//the casting to fluidContainer is necessary in each line
				((FluidContainer)io.getTarget()).setTemperature(((FluidContainer)io.getSource()).getTemperature());
				((FluidContainer)io.getTarget()).setPressure(((FluidContainer)io.getSource()).getPressure());
				((FluidContainer)io.getSource()).setFlowRate(((FluidContainer)io.getTarget()).getFlowRate());
			};
		}
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
		return "FluidContainer " + temperature + " " + pressure + " " + flowRate;
	}

}
