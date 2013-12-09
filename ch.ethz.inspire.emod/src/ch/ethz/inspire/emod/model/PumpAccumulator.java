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
 *   PressureSamples      : [Pa]    : Pressure samples for liner interpolation
 *   VolFlowSamples       : [l/min] : Vol flow samples for liner interpolation
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
	private IOContainer level;
	private IOContainer density;
	// Output parameters:
	private IOContainer pel;
	private IOContainer pth;
	private IOContainer pmech;
	private IOContainer massFlowIn;
	private IOContainer volFlowIn;
	private IOContainer pFluid;
	
	
	// Parameters used by the model. 
	private double[] pressureSamples;  	// Samples of pressure [Pa]
	private double[] volFlowSamples;  	// Samples of vol flow [l/min]
	//private double[] pressureSamplesR, volFlowSamplesR;
	private double[] powerSamples;
	private double pGasInit;			// Initial gas pressure [Pa]
	private double volGasInit;			// Initial gas volume [m3]
	private double volFluidInit;		// Initial fluid volume [m3]
	private double hystPMax, hystPMin;  // Contoller switch off/on values

	
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
		inputs      = new ArrayList<IOContainer>();
		massFlowOut = new IOContainer("MassFlowOut", Unit.KG_S, 0);
		level       = new IOContainer("Level", Unit.NONE, 0);
		density		= new IOContainer("Density", Unit.KG_MCUBIC, 0);
		inputs.add(massFlowOut);
		inputs.add(level);
		inputs.add(density);
		
		/* Define output parameters */
		outputs    = new ArrayList<IOContainer>();
		pel        = new IOContainer("PTotal", Unit.WATT, 0);
		pth        = new IOContainer("PLoss",  Unit.WATT, 0);
		pmech      = new IOContainer("PUse",   Unit.WATT, 0);
		massFlowIn = new IOContainer("MassFlowIn", Unit.KG_S, 0);
		volFlowIn  = new IOContainer("VolFlowIn", Unit.L_MIN, 0);
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
			volFlowSamples = params.getDoubleArray("VolFlowSamples");
			powerSamples	= params.getDoubleArray("PowerSamples");
			pGasInit		= params.getDoubleValue("GasPressureInitial");
			volGasInit        = params.getDoubleValue("GasVolumeInitial");
			volFluidInit      = params.getDoubleValue("FluidVolumeInitial");
			hystPMax        = params.getDoubleValue("PressureMax");
			hystPMin		= params.getDoubleValue("PressureMin");

			
			/*
			 * Revert arrays;
			 */
			/*pressureSamplesR = new double[pressureSamples.length];
			for (int i=0; i<pressureSamples.length; i++) {
				pressureSamplesR[i] = pressureSamples[pressureSamples.length-1-i];
			}
			volFlowSamplesR = new double[volFlowSamples.length];
			for (int i=0; i<volFlowSamples.length; i++) {
				volFlowSamplesR[i] = volFlowSamples[volFlowSamples.length-1-i];
			}*/
				
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
		if (pressureSamples.length != volFlowSamples.length) {
			throw new Exception("Pump, type:" +type+ 
					": Dimension missmatch: Vector 'pressureSamples' must have same dimension as " +
					"'massFlowSamples' (" + pressureSamples.length + "!=" + volFlowSamples.length + ")!");
		}
		// Check if sorted:
		for (int i=1; i<powerSamples.length; i++) {
			if (powerSamples[i-1] >= powerSamples[i]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'PowerSamples' must be sorted!");
			}
		}
		
		for (int i=1; i<volFlowSamples.length; i++) {
			if (volFlowSamples[i] <= volFlowSamples[i-1]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'massFlowSamples' must be sorted!");
			}
		}
		
		for (int i=1; i<pressureSamples.length; i++) {
			if (pressureSamples[i] >= pressureSamples[i-1]) {
				throw new Exception("PumpAccumulator, type:" +type+ 
						": Sample vector 'pressureSamples' must be sorted!");
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
		volFluid += (massFlowIn.getValue()-massFlowOut.getValue())/ 880 * sampleperiod; //TODO Density muss hier konstant sein, andernfalls ist der Druck NaN
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
		
		if (level.getValue() < 1) pumpOn = false;
		else {
			if      ( pumpOn && pGas>hystPMax) pumpOn = false;
			else if (!pumpOn && pGas<hystPMin) pumpOn = true;
		}
		
		//TODO Pumpe schaltet nicht ein, obwohl Level immer 1 und pGas<hystPMin ist
		if (pumpOn) {
			/*
			 * Lookup mass flow in pump map and dive by density
			 */
			volFlowIn.setValue(Algo.linearInterpolation(pFluid.getValue(), pressureSamples, volFlowSamples));
			pel.setValue(Algo.linearInterpolation(volFlowIn.getValue(), volFlowSamples, powerSamples));
			massFlowIn.setValue(volFlowIn.getValue()/60000*density.getValue());
		}
		else {
			pel.setValue(0);
			massFlowIn.setValue(0);
		}
			
		
		/* The mechanical power is given by the pressure and the voluminal flow:
		 * Pmech = pFluid [Pa] * Vdot [m3/s]
		 */
		pmech.setValue( Math.abs(massFlowIn.getValue()*pFluid.getValue()/density.getValue()) );
		
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
