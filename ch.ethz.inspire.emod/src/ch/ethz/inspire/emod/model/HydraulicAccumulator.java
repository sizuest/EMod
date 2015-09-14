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

import ch.ethz.inspire.emod.model.fluid.FluidElement;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Pump model class.
 * Implements the physical model of a pump with reservoir.
 * From the input parameter mass flow, the electrical power
 * and the supply mass flow are calculated.
 * 
 * Assumptions:
 * Perfect gas
 * 
 * 
 * Inputlist:
 *   1: Level:		: [-]    : Indicator for pump on
 *   2: MassFlowOut : [kg/s] : Demanded mass flow out
 *   3: Density		: [kg/m^3]
 * Outputlist:
 *   1: PTotal      : [W]    : Demanded electrical power
 *   2: PLoss       : [W]    : Thermal pump losses
 *   3: PUse        : [W]    : Power in the pluid
 *   4: MassFlowIn  : [m3/s] : Current mass flow in
 *   5: pressure    : [Pa]   : Pressure in the tank
 *   
 * Config parameters:
 *   GasVolumeInitial     : [m3]    : Initial volume of the gas in the reservoir
 *   FluidValumeInitial   : [m3]    : Initial volume of the fluid in the reservoir
 *   PressureMax          : [Pa]    : Hysteresis controller max. reservoir pressure
 *   PressureMin          : [Pa]    : Hysteresis controller min. reservoir pressure
 *   ElectricalPower	  : [W]     : Nominal power if operating
 * 
 * @author simon
 *
 */
@XmlRootElement
public class HydraulicAccumulator extends APhysicalComponent implements Floodable{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private FluidContainer fluidIn;
	// Output parameters:
	private IOContainer pfluid;
	private IOContainer content;
	private FluidContainer fluidOut;
	
	// Parameters used by the model. 
	private double pGasInit;			// Initial gas pressure [Pa]
	private double volGasInit;			// Initial gas volume [m3]
	private double volFluidInit;		// Initial fluid volume [m3]
	private double hystPMax, hystPMin;  // Contoller switch off/on values

	
	private double volFluid;		    // Fluid mass in the reservoir
	private double volGas;              // Gas volume
	private double pGas;				// Gas pressure
	private boolean pumpOn;				// Pump state
	
	// Sub-models used by the model
	private ThermalElement fluid;
	private FluidElement mass;
	
	// Fluid properties
	private FluidCircuitProperties fluidCircuitProperties;
	
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public HydraulicAccumulator() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Pump constructor
	 * 
	 * @param type
	 */
	public HydraulicAccumulator(String type) {
		super();
		
		this.type=type;
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
		/* Define Input parameters */
		inputs      = new ArrayList<IOContainer>();
		fluidIn     = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);
		
		/* Define output parameters */
		outputs    = new ArrayList<IOContainer>();
		fluidOut   = new FluidContainer("FluidOut", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		content    = new IOContainer("Content",     Unit.KG,    0, ContainerType.FLUIDDYNAMIC);
		pfluid     = new IOContainer("State",       Unit.NONE,  0, ContainerType.CONTROL);
		outputs.add(fluidOut);
		outputs.add(content);
		outputs.add(pfluid);
		
		
		
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
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			this.pGasInit	  = params.getDoubleValue("GasPressureInitial");
			this.volGasInit   = params.getDoubleValue("GasVolumeInitial");
			this.volFluidInit = params.getDoubleValue("FluidVolumeInitial");
			this.hystPMax     = params.getDoubleValue("PressureMax");
			this.hystPMin     = params.getDoubleValue("PressureMin");

			
			this.volFluid = volFluidInit;
			
			/* Sub Models */
			this.fluid = new ThermalElement("Example", 1);
			this.mass  = new FluidElement("Example", volFluidInit*fluid.getMaterial().getDensity(293, pGasInit));
			this.mass.setMaterial(fluid.getMaterial());
			
			/* Define fluid circuit properties
			 * In this case, the element leads to a non-direct connected in- and outlet! */
			fluidCircuitProperties = new FluidCircuitProperties();
			fluidCircuitProperties.setCuppledInAndOut(false);
			fluidCircuitProperties.setPressureReference(fluidOut);
			fluidCircuitProperties.setMaterial(fluid.getMaterial());
			
			/* States */
			this.dynamicStates = new ArrayList<DynamicState>();
			this.dynamicStates.add(0, this.fluid.getTemperature());
			
			/*
			 * initial pump state
			 */
			if (pGasInit<hystPMin) pumpOn = true;
			else                   pumpOn = false;
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
		// Validate the parameters:
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
		
		// Strictly positive
		if (pGasInit<=0){
			throw new Exception("Pump, type:" +type+ 
					": Negative value: Initial pressure must be positive!");
		}
		if (volGasInit<=0){
			throw new Exception("Pump, type:" +type+ 
					": Negative value: Initial volume must be positive!");
		}
		if (volFluidInit<=0){
			throw new Exception("Pump, type:" +type+ 
					": Negative value: Initial volume must be positive!");
		}
		
		// Check controller
		if ( (hystPMin >= hystPMax) && 
			 (volGasInit != 0 && volFluidInit != 0)){
			throw new Exception("Pump, type:" +type+ 
					": Controller settings: Maximum pressure must be larger than minimum pressure!");
		}
	}
	
    
	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		double mIn, mOut;
		
		/* Mass flows */
		mIn =  fluidCircuitProperties.getFlowRateIn()*fluid.getMaterial().getDensity(fluidIn.getTemperature(), fluidIn.getPressure());
		mOut = fluidCircuitProperties.getFlowRateOut()*fluid.getMaterial().getDensity(fluid.getTemperature().getValue(), pGas);
		/*
		 * Update mass in the reservoir:
		 * m(t) += T[s] * (mdot_in(t-T) [kg/s] - mdot_out(t-T) [kg/s]) | / rho [kg/m3]
		 */
		volFluid += (mIn-mOut) / fluid.getMaterial().getDensity(fluid.getTemperature().getValue(), pGas) * timestep;
		/*
		 * New gas volume
		 * V_gas(t) [m3] = V_gas,0 [m3] + V_fluid,0 [m3] - V(t) [m3]
		 */
		volGas   = volGasInit + volFluidInit - volFluid;
		/*
		 * New fluid pressure = gas pressure
		 * p_gas [Pa] = p_gas,0[Pa] * V_gas,0 [m3] / V_gas [m3]
		 */
		pGas   = pGasInit * volGasInit / volGas;
		
		/* Thermal Energy flows */
		fluid.setHeatInput(mIn*fluid.getMaterial().getHeatCapacity()-mOut*fluid.getMaterial().getHeatCapacity());
		fluid.integrate(timestep);
		
		
		/* Write Fluid Channels */
		fluidOut.setPressure(pGas);
		fluidOut.setTemperature(fluid.getTemperature().getValue());
		
		
		/*
		 * Pressure Limitations
		 */
		if (pGas>hystPMax)		pGas = hystPMax;
		else if (pGas<hystPMin)	pGas = hystPMin;
		
		/*Hysteresis controller for the pump */
		if      ( pumpOn && pGas==hystPMax) pumpOn = false;
		else if (!pumpOn && pGas==hystPMin) pumpOn = true;
		
		if(pumpOn)
			pfluid.setValue(1);
		else
			pfluid.setValue(0);
		
		content.setValue(mass.getMass().getValue());
					
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
		init();
	}

	@Override
	public FluidCircuitProperties getFluidProperties() {
		return fluidCircuitProperties;
	}
	
}
