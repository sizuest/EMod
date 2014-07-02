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

import ch.ethz.inspire.emod.model.units.*;
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
 *   2: Density		: [kg/m3]:
 *   3: PumpCtrl	: []	 : Pump ON/OFF
 * Outputlist:
 *   1: PTotal      : [W]    : Demanded electrical power
 *   2: PLoss       : [W]    : Thermal pump losses
 *   3: PUse        : [W]    : Power in the pluid
 *   4: MassFlowIn  : [m3/s] : Current mass flow in
 *   5: pressure    : [Pa]   : Pressure in the tank
 *   
 * Config parameters:
 *   PressureSamples          : [Pa]    : Pressure samples for liner interpolation
 *   MassFlowSamples          : [kg/s]  : Mass flow samples for liner interpolation
 *   DensityFluid         	  : [kg/m3] : Working fluid density
 *   ElectricalPowerSamples	  : [W]     : Nominal power samples if operating for linear interpolation
 * 
 * @author beni
 *
 */
@XmlRootElement
public class PumpPower extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer massFlowOut;
	private IOContainer tempIn;
	private IOContainer pumpCtrl;
	// Output parameters:
	private IOContainer pel;
	private IOContainer pth;
	private IOContainer pmech;
	private IOContainer massFlowIn;
	private IOContainer pFluid;
	
	
	// Parameters used by the model. 
	private double[] pressureSamples;  		// Samples of pressure [Pa]
	private double[] massFlowSamples;  	// Samples of mass flow [kg/s]
	private double[] pressureSamplesR, massFlowSamplesR;
	private double[] powerSamples;		// Samples of Power demand of the pump if on [W]
	
	private Material fluid;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public PumpPower() {
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
	public PumpPower(String type) {
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
		massFlowOut = new IOContainer("MassFlowOut",   Unit.KG_S,   0, ContainerType.FLUIDDYNAMIC);
		tempIn		= new IOContainer("TemperatureIn", Unit.KELVIN, 0, ContainerType.THERMAL);
		pumpCtrl	= new IOContainer("PumpCtrl",      Unit.NONE,   0, ContainerType.CONTROL);
		inputs.add(massFlowOut);
		inputs.add(tempIn);
		inputs.add(pumpCtrl);
		
		/* Define output parameters */
		outputs    = new ArrayList<IOContainer>();
		pel        = new IOContainer("PTotal",     Unit.WATT, 0, ContainerType.ELECTRIC);
		pth        = new IOContainer("PLoss",      Unit.WATT, 0, ContainerType.THERMAL);
		pmech      = new IOContainer("PUse",       Unit.WATT, 0, ContainerType.FLUIDDYNAMIC);
		massFlowIn = new IOContainer("MassFlowIn", Unit.KG_S, 0, ContainerType.FLUIDDYNAMIC);
		pFluid     = new IOContainer("Pressure",   Unit.PA,   0, ContainerType.FLUIDDYNAMIC);
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
			params = new ComponentConfigReader(getModelType(), type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			pressureSamples = params.getDoubleArray("PressureSamples");
			massFlowSamples  = params.getDoubleArray("MassFlowSamples");
			powerSamples    = params.getDoubleArray("PowerSamples");
			fluid           = params.getMaterial("Fluid");
			
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
		// Check dimensions pressureSamples vs. massFlowSamples:
		if (pressureSamples.length != massFlowSamples.length) {
			throw new Exception("Pump, type:" +type+ 
					": Dimension missmatch: Vector 'pressureSamples' must have same dimension as " +
					"'massFlowSamples' (" + pressureSamples.length + "!=" + massFlowSamples.length + ")!");
		}
		
		// Check dimensions pressureSamples vs. powerSamples:	
		if (pressureSamples.length != powerSamples.length) {
			throw new Exception("Pump, type:" +type+ 
					": Dimension missmatch: Vector 'pressureSamples' must have same dimension as " +
					"'pelPumpSamples' (" + pressureSamples.length + "!=" + powerSamples.length + ")!");
		}
		
		// Check if sorted:		
		for (int i=1; i<massFlowSamples.length; i++) {
			if (massFlowSamples[i] <= massFlowSamples[i-1]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'volFlowSamples' must be sorted!");
			}
		}
					
		// Strictly positive
		for (int i=1; i<powerSamples.length; i++) {
			if (powerSamples[i]<=0){
				throw new Exception("Pump, type:" +type+ 
					": Negative or zero value: Pump power must be strictly positive!");
			}
		}
		
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		
		if(pumpCtrl.getValue() == 1){
			if (massFlowOut.getValue() != 0){
				pel.setValue(Algo.linearInterpolation(massFlowOut.getValue(), massFlowSamples, powerSamples));
				massFlowIn.setValue(massFlowOut.getValue());
				pFluid.setValue(Algo.linearInterpolation(massFlowOut.getValue(), massFlowSamples, pressureSamples));
			}
			else {
				pel.setValue(Algo.linearInterpolation(0, massFlowSamples, powerSamples));
				pFluid.setValue(Algo.linearInterpolation(0, massFlowSamples, pressureSamples));
				massFlowIn.setValue(0);		
			}
		}
		
		else{
			pel.setValue(0);
			massFlowIn.setValue(0);
			pFluid.setValue(0);
		}
		
		/* The mechanical power is given by the pressure and the voluminal flow:
		 * Pmech = pFluid [Pa] * Vdot [m3/s]
		 */
		pmech.setValue( Math.abs(massFlowIn.getValue()*pFluid.getValue()/fluid.getDensity(tempIn.getValue(), pFluid.getValue())) );
		
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
	
	public void setType(String type) {
		this.type = type;
		init();
	}
	
}
