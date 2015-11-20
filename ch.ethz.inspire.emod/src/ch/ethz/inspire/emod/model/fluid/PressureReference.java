package ch.ethz.inspire.emod.model.fluid;

import java.util.ArrayList;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * Pressure Reference
 * 
 * Privides a component with a constant pressure drop
 * @author sizuest
 *
 */
public class PressureReference extends APhysicalComponent implements Floodable{
	
	private FluidContainer fluidIn, fluidOut;
	
	FluidCircuitProperties fluidProperties;
	
	/**
	 * @param pressureDrop [Pa]
	 */
	public PressureReference(double pressureDrop){
		init();
		this.getFluidPropertiesList().get(0).setPressureDrop(pressureDrop);
	}
	
	private void init()
	{
		inputs         = new ArrayList<IOContainer>();
		outputs        = new ArrayList<IOContainer>();
		
		/* Define FlowRate */
		fluidProperties = new FluidCircuitProperties();
		fluidProperties.setMaterial(new Material("Example"));
		
		/* Define FluidIn parameter */
		fluidIn        = new FluidContainer("FluidIn", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
		inputs.add(fluidIn);

		/* Define FluidOut parameter */
		fluidOut        = new FluidContainer("FluidOut", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
		outputs.add(fluidOut);
		

	}
	
	/**
	 * @param pressureDrop [Pa]
	 */
	public void setPressureDrop(double pressureDrop){
		this.getFluidPropertiesList().get(0).setPressureDrop(pressureDrop);
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setType(String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<FluidCircuitProperties> getFluidPropertiesList() {
		ArrayList<FluidCircuitProperties> out = new ArrayList<FluidCircuitProperties>();
		out.add(fluidProperties);
		return out;
	}

}
