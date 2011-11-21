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
 * General Heat Exchanger model class.
 * Implements the physical model of a heat exchanger
 * 
 * Assumptions:
 * All Component losses are thermal, only conducation has relevant heat
 * transferes
 * 
 * Inputlist:
 *   1: massFlow    : [kg/s] : Demanded cooling fluid mass flow
 *   2: PThermal    : [W]    : Heat flow into the element
 * Outputlist:
 * 	 1: PTotal      : [W]	 : Electric power demand
 *   2: PPump       : [W]    : Pump energy demand
 *   3: PLoss       : [W]    : Heat flow out
 *   
 * Config parameters:
 *   DensityCf        : [kg/m3] : Density of the cooling fluid
 *   EffPump          : [-]     : Pump efficency (el->mech)
 *   EERCooling       : [-]     : Energy efficency ratio cooling
 *   MassFlowSamples  : [kg/s]  : Mass flow samples of the pump
 *   PressureSamples  : [Pa]    : Pressure samples matching the mass flow samples
 *   
 *   
 * 
 * @author simon
 *
 */
@XmlRootElement
public class HeatExchanger extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer massflow;
	private IOContainer pth_in;
	// Output parameters:
	private IOContainer ptotal;
	private IOContainer ppump;
	private IOContainer pth_out;
	
	// Parameters used by the model. 
	private double rhoCf;            // Density [kg/m3]
	private double eta, epsilon;     // COP/EER [-]
	private double[] massFlowSamples,
	                 pressureSamples;            // Pump map [kg/s/Pa]
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public HeatExchanger() {
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
	public HeatExchanger(String type) {
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
		inputs   = new ArrayList<IOContainer>();
		massflow = new IOContainer("massFlow",    Unit.KG_S, 0);
		pth_in      = new IOContainer("PThermal", Unit.WATT, 0);
		inputs.add(massflow);
		inputs.add(pth_in);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		ptotal  = new IOContainer("PTotal", Unit.WATT, 0);
		ppump   = new IOContainer("PPump",  Unit.WATT, 0);
		pth_out = new IOContainer("PLoss",  Unit.WATT, 0);
		outputs.add(ptotal);
		outputs.add(ppump);
		outputs.add(pth_out);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("HeatExchanger", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			rhoCf   = params.getDoubleValue("DensityCf");
			eta     = params.getDoubleValue("EffPump");
			epsilon = params.getDoubleValue("EERCooling");
			massFlowSamples = params.getDoubleArray("MassFlowSamples");
			pressureSamples = params.getDoubleArray("PressureSamples");
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
    	
    	// Strictly positive
    	if (rhoCf <= 0){
    		throw new Exception("HeatExchanger, type:" +type+ 
					": Negative or zero: Fluid density must be strictly positive");
    	}
    	if (epsilon <= 0 ){
    		throw new Exception("HeatExchanger, type:" +type+ 
					": Negative or zero: EER must be strictly positive");
    	}
    	
    	// Within (0,1]
    	if (eta <= 0 || eta>1){
    		throw new Exception("HeatExchanger, type:" +type+ 
					": Negative or zero: Efficency must be within (0,1]");
    	}	
    	
    	// Check arrays
    	if (massFlowSamples.length != pressureSamples.length){
    		throw new Exception("HeatExchanger, type:" +type+ 
    				": Dimension missmatch: Vector 'MassFlowSamples' must have same dimension as " +
					"'PressureSamples' (" + massFlowSamples.length + "!=" + pressureSamples.length + ")!");
    	}
    	for (int i=1; i<massFlowSamples.length; i++) {
			if (massFlowSamples[i] <= massFlowSamples[i-1]) {
				throw new Exception("HeatExchanger, type:" +type+ 
						": Sample vector 'MassFlowSamples' must be sorted!");
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		// Element i off, if no mass flow is demanded
		if ( 0==massflow.getValue() ){
			ptotal.setValue(0);
			ppump.setValue(0);
			pth_out.setValue(0);
			return;
		}
		
		/* 
		 * The pump power results from the mechanical power
		 * P_mech [W] = mdot [kg/s]/rho [kg/m3] * p_pump [Pa]
		 * 
		 * and the efficency eta
		 */
		ppump.setValue( massflow.getValue()/rhoCf * 
				Algo.linearInterpolation(massflow.getValue(), massFlowSamples, pressureSamples) / eta ) ;
		
		/*
		 * Thermal flow out of the element is equal to thermal flow
		 * into the element plus pump losses:
		 * Qdot_out [W] = Qdot_in [W] + (1-eta)/eta [-] * P_mech,pump [W]
		 */
		pth_out.setValue(pth_in.getValue() + (1-eta)*ppump.getValue());
		
		/*
		 * Power demand is the sum over all components
		 * P_total [W] = P_pump [W] + Qdot_in/epsilon [W] 
		 */

		ptotal.setValue(ppump.getValue() + pth_out.getValue()/epsilon);
		
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
