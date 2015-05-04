package ch.ethz.inspire.emod.utils;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.Material;

/**
 * class to extend IOConnection to perform with fluids
 * 
 * @author manick
 *
 */
public class FluidConnection extends IOConnection {
	
	protected Material material;
	
	/**
	 * create FluidConnection with two components, source has to have output "FluidOut", target has to have input "FluidIn"
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	public FluidConnection(APhysicalComponent source, APhysicalComponent target) throws Exception{
		// connect if source has "FluidOut" and target has "FluidIn"
		super(source.getOutput("FluidOut"), target.getInput("FluidIn"));

		// flood the target component with the same fluid as the source component
		FluidCircuit.floodCircuit(source,target);
	}
	
	/**
	 * create a connection of two Containers
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	public FluidConnection(FluidContainer source, FluidContainer target) throws Exception{
		super((IOContainer)source, (IOContainer)target);
	}
	
	
	/**
	 * @param io 
	 * @param IOConnection
	 * @throws Exception 
	 */
	public FluidConnection(IOConnection io) throws Exception{
		super(io.getSource(), io.getTarget());
	}
	
	/**
	 * @param material to set
	 * @throws Exception
	 */
	public void setMaterial(Material material) throws Exception{
		this.material = material;
	}
	
	/**
	 * @return get material
	 */
	public Material getMaterial(){
		return material;
	}

	/**
	 * init a fluidconnection with values for temperature/pressure/flowRate
	 * @param temperature
	 * @param pressure
	 */
	public void init(double temperature, double pressure){
		((FluidContainer)source).setValues(temperature, pressure);
		((FluidContainer)target).setValues(temperature, pressure);
	}
	
	/**
	 * update from source to target or vice versa according to the direction of calculation
	 * obsolete? updating value happens with getValue/setValue method of FluidContainer!
	 */
	@Override
	public void update(){
		/* direction of calculation
		 * temperature [K]    : source --> target
		 * pressure    [Pa]   : source --> target
		 */
		((FluidContainer)this.target).setTemperature(((FluidContainer)this.source).getTemperature());
		((FluidContainer)this.target).setPressure(((FluidContainer)this.source).getPressure());
	}
}


