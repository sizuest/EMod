package ch.ethz.inspire.emod.model.fluid;

import ch.ethz.inspire.emod.model.Valve;
import ch.ethz.inspire.emod.utils.IOContainer;

public class FECValve extends AFluidElementCharacteristic{
	
	Valve valve;
	IOContainer state;
	
	public FECValve(Valve valve, IOContainer state){
		this.valve = valve;
		this.state = state;
	}

	@Override
	public double getA0(double flowRate, double pressure) {
		if(0==state.getValue())
			return 0;
		else
			return valve.getPressure(flowRate)-flowRate*valve.getPressureDrivative(flowRate);
	}

	@Override
	public double getA1(double flowRate, double pressure) {
		if(0==state.getValue())
			return 1;
		else
			return valve.getPressureDrivative(flowRate);
	}

	@Override
	public double getEp(double flowRate, double pressure) {
		if(0==state.getValue())
			return 0;
		else
			return 1;
	}

}
