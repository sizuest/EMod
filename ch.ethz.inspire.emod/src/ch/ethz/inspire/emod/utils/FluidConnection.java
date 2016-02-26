package ch.ethz.inspire.emod.utils;

import ch.ethz.inspire.emod.model.APhysicalComponent;

/**
 * class to extend IOConnection to perform with fluids
 * 
 * @author manick
 *
 */
public class FluidConnection extends IOConnection {
	
	/**
	 * create FluidConnection with two components, source has to have output "FluidOut", target has to have input "FluidIn"
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	public FluidConnection(APhysicalComponent source, APhysicalComponent target) throws Exception{
		// connect if source has "FluidOut" and target has "FluidIn"
		super(source.getOutput("FluidOut"), target.getInput("FluidIn"));
	}
	
	/**
	 * create a connection of two Containers
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	public FluidConnection(FluidContainer source, FluidContainer target) throws Exception{
		super((IOContainer)source, (IOContainer)target);
		
		FluidCircuit.floodCircuit(source,target);
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
	 * init a fluidconnection with values for temperature/pressure/flowRate
	 * @param temperature
	 * @param pressure
	 */
	public void init(double temperature, double pressure){
		((FluidContainer)this.source).setValuesAsOutput();
		((FluidContainer)this.target).setValuesAsInput();
	}
	
	/**
	 * update from source to target or vice versa according to the direction of calculation
	 */
	@Override
	public void update(){
		((FluidContainer)this.source).setValuesAsOutput();
		((FluidContainer)this.target).setValuesAsInput();
	}
}


