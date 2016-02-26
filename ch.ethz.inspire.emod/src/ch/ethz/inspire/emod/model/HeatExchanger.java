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


package ch.ethz.inspire.emod.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.fluid.FECZeta;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General Heat exchanger model class.
 * Implements the physical model of a heat exchanger with external
 * coolant supply.
 * 
 * 
 * Inputlist:
 *   1: State        : [-]    : State of the HE
 *   2: Fluid1In     : [-]    : Fluid 1 flowing into the HE
 *   3: Fluid2In     : [-]    : Fluid 2 flowing into the HE
 * Outputlist:
 *   1: PTotal      : [W]    : Demanded electrical power
 *   2: PLoss       : [W]    : Losses
 *   3: Fluid1Out   : [-]    : Fluid 1 flowing out of the HE
 *   4: Fluid2Out   : [-]    : Fluid 2 flowing out of the HE
 *   
 * Config parameters:
 *   PressureSamples      : [Pa]    : Pressure samples for liner interpolation
 *   FlowRateSamples      : [m^3/s] : Volumetric flow samples for liner interpolation
 *   ElectricalPower	  : [W]     : Power samples for liner interpolation
 * 
 * @author sizuest
 *
 */

@XmlRootElement
public class HeatExchanger extends APhysicalComponent implements Floodable{
	@XmlElement
	protected String type;
	
	// Input parameters
	private IOContainer level;
	private FluidContainer fluid1In;
	private FluidContainer fluid2In;
	
	// Output parameters
	private IOContainer ptotal, ploss;
	private FluidContainer fluid1Out;
	private FluidContainer fluid2Out;
	
	// Fluid properties
	private FluidCircuitProperties fluidProperties1;
	private FluidCircuitProperties fluidProperties2;
	
	private FECZeta zeta1, zeta2;
	
	private ThermalElement fluid1, fluid2;
	
	// Parameters
	private double zetaValue1, zetaValue2;
	private double htc;
	private double tempOn, tempOff;
	private double power;
	private double volume1, volume2;
	
	private boolean wasCooling = false;
	
	private boolean isInitialized = false;
	
	
	public HeatExchanger(){
		super();
	}
	
	/**
	 * HeatExchanger constructor
	 * 
	 * @param type
	 */
	public HeatExchanger(String type){
		super();
		
		this.type=type;
		init();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	private void init(){
		zeta1 = new FECZeta(1);
		zeta2 = new FECZeta(1);
		
		fluidProperties1 = new FluidCircuitProperties(zeta1);
		fluidProperties2 = new FluidCircuitProperties(zeta2);
		
		// Inputs
		inputs = new ArrayList<IOContainer>();
		level = new IOContainer("State", new SiUnit(Unit.NONE), 0, ContainerType.CONTROL);
		fluid1In = new FluidContainer("Fluid1In", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties1);
		fluid2In = new FluidContainer("Fluid2In", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties2);
		inputs.add(level);
		inputs.add(fluid1In);
		inputs.add(fluid2In);
		
		// Outputs
		outputs = new ArrayList<IOContainer>();
		ptotal    = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		ploss     = new IOContainer("PLoss",  new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		fluid1Out = new FluidContainer("Fluid1Out", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties1);
		fluid2Out = new FluidContainer("Fluid2Out", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties2);
		outputs.add(ptotal);
		outputs.add(ploss);
		outputs.add(fluid1Out);
		outputs.add(fluid2Out);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader(getModelType(), type);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Read the config parameter: */
		try {
			zetaValue1 = params.getDoubleValue("PressureLossCoefficient1");
			zetaValue2 = params.getDoubleValue("PressureLossCoefficient2");
			
			htc = params.getDoubleValue("HeatTransferCoefficient");
			
			tempOn  = params.getDoubleValue("TemperatureHigh");
			tempOff = params.getDoubleValue("TemperatureLow");
			
			power = params.getDoubleValue("Power");
			
			volume1 = params.getDoubleValue("Volume1");
			volume2 = params.getDoubleValue("Volume2");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Set pressure loss coeff. */
		zeta1.setZeta(zetaValue1);
		zeta2.setZeta(zetaValue2);
		
		/* Create thermal elements */
		fluid1 = new ThermalElement("Example", 1.0);
		fluid2 = new ThermalElement("Example", 1.0);
		
		fluid1.setVolume(volume1);
		fluid2.setVolume(volume2);
		
		fluid1.getTemperature().setName("Temperature1");
		fluid2.getTemperature().setName("Temperature2");
		
		fluid1.getMass().setName("Mass1");
		fluid2.getMass().setName("Mass2");
		
		fluid1.setMaterial(fluidProperties1.getMaterial());
		fluid2.setMaterial(fluidProperties2.getMaterial());
		
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid1.getTemperature());
		dynamicStates.add(fluid2.getTemperature());
	
		
		fluidProperties1.setTemperature(fluid1.getTemperature());
		fluidProperties2.setTemperature(fluid2.getTemperature());
		
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void update() {
		fluid1.setMaterial(fluidProperties1.getMaterial());
		fluid2.setMaterial(fluidProperties2.getMaterial());
		
		double htc = 0;
		
		if(!isInitialized){
			fluid1.getMass().setInitialCondition(volume1*fluid1.getMaterial().getDensity(fluid1.getTemperature().getValue()));
			fluid2.getMass().setInitialCondition(volume2*fluid2.getMaterial().getDensity(fluid2.getTemperature().getValue()));
			isInitialized = false;
		}
		
		
		if(level.getValue() == 1){
			ptotal.setValue(power);
			ploss.setValue(power);
			
			if( (wasCooling & fluidProperties1.getTemperatureIn()<tempOff) |
				(!wasCooling & fluidProperties1.getTemperatureIn()>=tempOn) ){
				wasCooling = true;
				htc = this.htc;
				
			}
			else
				wasCooling = false;
		}
		else{
			wasCooling = false;
			ptotal.setValue(0);
			ploss.setValue(0);
		}
		
		fluid1.setThermalResistance(htc);
		fluid2.setThermalResistance(htc);
		
		fluid1.setTemperatureAmb(fluid2.getTemperature().getValue());
		fluid2.setTemperatureAmb(fluid1.getTemperature().getValue());
		
		fluid1.integrate(timestep, fluidProperties1.getFlowRate(), fluidProperties1.getFlowRate(), fluidProperties1.getPressure());
		fluid2.integrate(timestep, fluidProperties2.getFlowRate(), fluidProperties2.getFlowRate(), fluidProperties2.getPressure());
		
		fluid1.setTemperatureIn(fluid1In.getTemperature());
		fluid2.setTemperatureIn(fluid2In.getTemperature());
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public ArrayList<FluidCircuitProperties> getFluidPropertiesList() {
		ArrayList<FluidCircuitProperties> out = new ArrayList<FluidCircuitProperties>();
		out.add(fluidProperties1);
		out.add(fluidProperties2);
		return out;
	}
	
	@Override
	public void flood(){/* Not used */}

}
