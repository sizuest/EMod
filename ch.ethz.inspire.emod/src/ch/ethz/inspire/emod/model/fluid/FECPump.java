package ch.ethz.inspire.emod.model.fluid;

import ch.ethz.inspire.emod.model.Pump;
import ch.ethz.inspire.emod.utils.IOContainer;

public class FECPump extends AFluidElementCharacteristic{
	
	Pump pump;
	IOContainer state;
	
	public FECPump(Pump pump, IOContainer state){
		this.pump  = pump;
		this.state = state;
	}

	@Override
	public double getA0(double flowRate, double pressureIn, double pressureOut) {
		double a0;
		if(0==state.getValue())
			a0 = 0;
		else
			a0 = -pump.getPressure(flowRate)+flowRate*pump.getPressureDrivative(flowRate);
		
		return a0;
	}

	@Override
	public double getA1(double flowRate, double pressureIn, double pressureOut) {
		if(0==state.getValue())
			return 1;
		else
			return -pump.getPressureDrivative(flowRate);
	}

	@Override
	public double getEp(double flowRate, double pressureIn, double pressureOut) {
		if(0==state.getValue())
			return 0;
		else
			return 1;
	}

}
