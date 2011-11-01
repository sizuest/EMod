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

import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.Algo;
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
 *   1: massFlowOut : [kg/s] : Demanded mass flow out
 * Outputlist:
 *   1: Pel         : [W]    : Demanded electrical power
 *   2: massFlowIn  : [kg/s] : Current mass flow in
 *   3: pressure    : [Pa]   : Pressure in the tank
 *   
 * Config parameters:
 *   PressureSamples      : [Pa]    : Pressure samples for liner interpolation
 *   MassFlowSamples      : [kg/s]  : Mass flow samples for liner interpolation
 *   DensityFluid         : [kg/m3] : Working fluid density
 *   GasPressureInitial   : [Pa]    : Initial gas pressure in the fluid
 *   GasVolumeInitial     : [m3]    : Initial volume of the gas in the reservoir
 *   FluidValumeInitial   : [m3]    : Initial volume of the fluid in the reservoir
 *   PressureMax          : [Pa]    : Hysteresis controller max. reservoir pressure
 *   PressureMin          : [Pa]    : Hysteresis controller min. reservoir pressure
 * 
 * @author simon
 *
 */
@XmlRootElement
public class Pump extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer mdotout;
	// Output parameters:
	private IOContainer pel;
	private IOContainer mdotin;
	private IOContainer pfluid;
	
	
	// Parameters used by the model. 
	private double[] pressureSamples;  	// Samples of pressure [Pa]
	private double[] massFlowSamples;  	// Samples of mass flow [kg/s]
	private double[] pressureSamplesR, massFlowSamplesR;
	private double rhoFluid;			// Fluid density [kg/m3]
	private double pGasInit;			// Initial gas pressure [Pa]
	private double vGasInit;			// Initial gas volume [m3]
	private double vFluidInit;			// Initial fluid volume [m3]
	private double hystPMax, hystPMin;  // Contoller switch off/on values
	private double pelPump;				// Power demand of the pump if on [W]
	
	private double mFluid;				// Fluid mass in the reservoir
	private double vGas;                // Gas volume
	private double pGas;				// Gas pressure
	private boolean pumpOn=false;		// Pump state
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Pump() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Linear Motor constructor
	 * 
	 * @param type
	 */
	public Pump(String type) {
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
		inputs  = new ArrayList<IOContainer>();
		mdotout = new IOContainer("massFlowOut", Unit.KG_S, 0);
		inputs.add(mdotout);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pel     = new IOContainer("Pel", Unit.WATT, 0);
		mdotin  = new IOContainer("massFlowIn", Unit.KG_S, 0);
		pfluid  = new IOContainer("Pressure", Unit.PA, 0);
		outputs.add(pel);
		outputs.add(mdotin);
		outputs.add(pfluid);
		
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Pump", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			pressureSamples = params.getDoubleArray("PressureSamples");
			massFlowSamples = params.getDoubleArray("MassFlowSamples");
			rhoFluid        = params.getDoubleValue("DensityFluid");
			pGasInit		= params.getDoubleValue("GasPressureInitial");
			vGasInit        = params.getDoubleValue("GasVolumeInitial");
			vFluidInit      = params.getDoubleValue("FluidVolumeInitial");
			hystPMax        = params.getDoubleValue("PressureMax");
			hystPMin		= params.getDoubleValue("PressureMin");
			pelPump         = params.getDoubleValue("ElectricalPower");
			
			/*
			 * Revert arrays;
			 */
			pressureSamplesR = new double[pressureSamples.length];
			for (int i=0; i<pressureSamples.length; i++) {
				pressureSamplesR[i] = pressureSamples[pressureSamples.length-1-i];
			}
			massFlowSamplesR = new double[massFlowSamples.length];
			for (int i=0; i<massFlowSamples.length; i++) {
				massFlowSamplesR[i] = massFlowSamples[massFlowSamples.length-1-i];
			}
			
			/*
			 *  calculate initial mass:
			 *  m(0) [kg] = V(0) [m3] * rho [kg/m3]
			 */
			mFluid = vFluidInit * rhoFluid;
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
		// Check model parameters:
		// Check dimensions:
		if (pressureSamples.length != massFlowSamples.length) {
			throw new Exception("Pump, type:" +type+ 
					": Dimension missmatch: Vector 'pressureSamples' must have same dimension as " +
					"'massFlowSamples' (" + pressureSamples.length + "!=" + massFlowSamples.length + ")!");
		}
		// Check if sorted:
		for (int i=1; i<pressureSamplesR.length; i++) {
			if (pressureSamplesR[i] <= pressureSamplesR[i-1]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'pressureSamples' must be sorted!");
			}
		}
		
		// Check if sorted:
		for (int i=1; i<massFlowSamples.length; i++) {
			if (massFlowSamples[i] <= massFlowSamples[i-1]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'massFlowSamples' must be sorted!");
			}
		}
		
		// Non negative
		if (pGasInit<0){
			throw new Exception("Pump, type:" +type+ 
					": Negative value: Initial pressure must be positive!");
		}
		if (vGasInit<0){
			throw new Exception("Pump, type:" +type+ 
					": Negative value: Initial volume must be positive!");
		}
		if (vFluidInit<0){
			throw new Exception("Pump, type:" +type+ 
					": Negative value: Initial volume must be positive!");
		}
		
		// Strictly positive
		if (rhoFluid<=0){
			throw new Exception("Pump, type:" +type+ 
					": Negative or zero value: Density must be strictly positive!");
		}
		if (pelPump<=0){
			throw new Exception("Pump, type:" +type+ 
					": Negative or zero value: Pump power must be strictly positive!");
		}
		
		// Check controller
		if ( (hystPMin >= hystPMax) && 
			 (vGasInit != 0 && vFluidInit != 0)){
			throw new Exception("Pump, type:" +type+ 
					": Controller settings: Maximum pressure must be larger than minimum pressure!");
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		/*
		 * CHECK IF A RESERVOIR EXISTS
		 */
		
		if ( (vFluidInit != 0) &&
			 (vGasInit   != 0) ) {
			/*
			 * Update mass in the reservoir:
			 * m(t) += T[s] * (mdot_in(t-T) [kg/s] - mdot_out(t-T) [kg/s])
			 */
			mFluid += (mdotin.getValue()-mdotout.getValue());
			/*
			 * New gas volume
			 * V_gas(t) [m3] = V_gas,0 [m3] + V_fluid,0 [m3] - m(t) [kg] / rho [kg/m3]
			 */
			vGas   = vGasInit + vFluidInit - mFluid/rhoFluid;
			/*
			 * New fluid pressure = gas pressure
			 * p_gas [Pa] = p_gas,0[Pa] * V_gas,0 [m3] / V_gas [m3]
			 */
			pGas   = pGasInit * vGasInit / vGas;
			pfluid.setValue(pGas);
			/*
			 * Calculate new inflow:
			 * - zero if pump off
			 * - from map if pump on
			 */
			/*
			 * Hysteresis controller for the pump
			 */
			if      ( pumpOn && pGas>hystPMax) pumpOn = false;
			else if (!pumpOn && pGas<hystPMin) pumpOn = true;
			
			
			if (pumpOn) {
				pel.setValue(pelPump);
				/*
				 * Lookup mass flow in pump map
				 */
				mdotin.setValue(Algo.linearInterpolation(pGas, pressureSamplesR, massFlowSamplesR));
			}
			else {
				pel.setValue(0);
				mdotin.setValue(0);
			}
			
		}
		else {
			if (mdotout.getValue() != 0){
				pel.setValue(pelPump);
				mdotin.setValue(mdotout.getValue());
				pfluid.setValue(Algo.linearInterpolation(mdotout.getValue(), massFlowSamples, pressureSamples));
			}
			else {
				pel.setValue(0);
				mdotin.setValue(0);
				pfluid.setValue(0);
			}
			
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
