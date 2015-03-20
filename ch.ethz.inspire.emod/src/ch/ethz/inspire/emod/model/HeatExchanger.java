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
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Heat Exchanger model class.
 * Implements the physical model of a heat exchanger
 * 
 * Assumptions:
 * All Component losses are thermal, heat exchanger can be described
 * by a energy efficency ratio
 * 
 * Inputlist:
 *   1: level       : [-]    : On/off for the compressor
 * Outputlist:
 * 	 1: PTotal      : [W]	 : Electric power demand
 *   2: PThermal    : [W]    : Heat flow out
 *   
 * Config parameters:
 *   CompressorPower  : [W]     : Installed compressor power (el.)
 *   EERCooling       : [-]     : Energy efficency ratio cooling
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
	private IOContainer level;
	// Output parameters:
	private IOContainer ptotal;
	private IOContainer pth_out;
	
	// Parameters used by the model. 
	private double epsilon;     // EER [-]
	private double pCompressor; // Compressor power [W]
	
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
	 * Heat exchanger constructor
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
		level    = new IOContainer("level",    Unit.NONE, 0, ContainerType.CONTROL);
		inputs.add(level);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		ptotal  = new IOContainer("PTotal",   Unit.WATT, 0, ContainerType.ELECTRIC);
		pth_out = new IOContainer("PThermal", Unit.WATT, 0, ContainerType.THERMAL);
		outputs.add(ptotal);
		outputs.add(pth_out);
		
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
			pCompressor = params.getDoubleValue("CompressorPower");
			epsilon     = params.getDoubleValue("EERCooling");
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
    	if (epsilon <= 0 ){
    		throw new Exception("HeatExchanger, type:" +type+ 
					": Negative or zero: EER must be strictly positive");
    	}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		// Element is off
		if ( 0==level.getValue()){
			ptotal.setValue(0);
			pth_out.setValue(0);
			return;
		}
		
		/* Power consumption is equal to nominal power, if component is
		 * on.
		 *  P_tot = P_compressor
		 * 
		 * The transfered heat can be calculated over EER
		 *  P_thermal [W] = Qdot*epsilon [W] 
		 */
		ptotal.setValue(pCompressor);
		pth_out.setValue(pCompressor * epsilon );
		
		
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
