/***********************************
 * $Id: Pump.java 88 2012-01-11 15:43:10Z sizuest $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/src/ch/ethz/inspire/emod/model/Pump.java $
 * $Author: sizuest $
 * $Date: 2012-01-11 16:43:10 +0100 (Mit, 11. Jan 2012) $
 * $Rev: 88 $
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
 *   1: MassFlowOut : [kg/s] : Demanded mass flow out
 * Outputlist:
 *   1: PTotal      : [W]    : Demanded electrical power
 *   2: PLoss       : [W]    : Thermal pump losses
 *   3: PUse        : [W]    : Power in the pluid
 *   4: MassFlowIn  : [m3/s] : Current mass flow in
 *   5: pressure    : [Pa]   : Pressure in the tank
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
 *   ElectricalPower	  : [W]     : Nominal power if operating
 * 
 * @author simon
 *
 */
@XmlRootElement
public class PumpAccumulator extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer massFlowOut;
	// Output parameters:
	private IOContainer pel;
	private IOContainer pth;
	private IOContainer pmech;
	private IOContainer massFlowIn;
	private IOContainer pFluid;
	
	
	// Parameters used by the model. 
	private double[] pressureSamples;  	// Samples of pressure [Pa]
	private double[] massFlowSamples;  	// Samples of mass flow [kg/s]
	private double[] pressureSamplesR, massFlowSamplesR;
	private double rhoFluid;			// Fluid density [kg/m3]
	private double pGasInit;			// Initial gas pressure [Pa]
	private double volGasInit;			// Initial gas volume [m3]
	private double volFluidInit;		// Initial fluid volume [m3]
	private double hystPMax, hystPMin;  // Contoller switch off/on values
	private double pelPump;				// Power demand of the pump if on [W]
	
	private double volFluid;		    // Fluid mass in the reservoir
	private double volGas;              // Gas volume
	private double pGas;				// Gas pressure
	private boolean pumpOn=false;		// Pump state
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public PumpAccumulator() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Pump constructor
	 * 
	 * @param type
	 */
	public PumpAccumulator(String type) {
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
		inputs     = new ArrayList<IOContainer>();
		massFlowOut = new IOContainer("MassFlowOut", Unit.KG_S, 0);
		inputs.add(massFlowOut);
		
		/* Define output parameters */
		outputs    = new ArrayList<IOContainer>();
		pel        = new IOContainer("PTotal", Unit.WATT, 0);
		pth        = new IOContainer("PLoss",  Unit.WATT, 0);
		pmech      = new IOContainer("PUse",   Unit.WATT, 0);
		massFlowIn = new IOContainer("MassFlowIn", Unit.KG_S, 0);
		pFluid     = new IOContainer("Pressure", Unit.PA, 0);
		outputs.add(pel);
		outputs.add(pth);
		outputs.add(pmech);
		outputs.add(massFlowIn);
		outputs.add(pFluid);
		
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("PumpAccumulator", type);
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
			volGasInit        = params.getDoubleValue("GasVolumeInitial");
			volFluidInit      = params.getDoubleValue("FluidVolumeInitial");
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
			volFluid = volFluidInit;
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
		
		/*
		 * Update mass in the reservoir:
		 * m(t) += T[s] * (mdot_in(t-T) [kg/s] - mdot_out(t-T) [kg/s]) | / rho [kg/m3]
		 */
		volFluid += (massFlowIn.getValue()-massFlowOut.getValue()) / rhoFluid * sampleperiod;
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
		pFluid.setValue(pGas);
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
			 * Lookup mass flow in pump map and dive by density
			 */
			massFlowIn.setValue(Algo.linearInterpolation(pGas, pressureSamplesR, massFlowSamplesR) );
		}
		else {
			pel.setValue(0);
			massFlowIn.setValue(0);
		}
			
		
		/* The mechanical power is given by the pressure and the voluminal flow:
		 * Pmech = pFluid [Pa] * Vdot [m3/s]
		 */
		pmech.setValue( Math.abs(massFlowIn.getValue()*pFluid.getValue()/rhoFluid) );
		
		/* The Losses are the difference between electrical and mechanical power
		 */
		pth.setValue(pel.getValue()-pmech.getValue());
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
