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
import java.lang.Math;

import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Transmission model class.
 * Implements the physical model of a transmission.
 * 
 * Assumptions:
 * constant fan efficiency, all losses are thermal
 * 
 * Inputlist:
 *   1: RotSpeed        : [rpm]     : Demanded rotational speed
 *   2: Torque   		: [Nm]		: Demanded torque
 * Outputlist:
 *   1: RotSpeed	    : [rpm]     : Resulting rotational speed
 *   2: Torque			: [Nm]		: Resulting torque
 *   3: PLoss			: [W]		: Heat loss
 *   
 * Config parameters:
 *   TransmissionRatio    : [-] : Ratio between the demaned and the resulting speed
 *   Efficiency           : [-] : Transmission efficiency
 * 
 * @author simon
 *
 */
@XmlRootElement
public class LinTransmission extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer rotSpeedIn;
	private IOContainer torqueIn;
	// Output parameters:
	private IOContainer rotSpeedOut;
	private IOContainer torqueOut;
	private IOContainer pLoss;
	
	// Parameters used by the model. 
	private double k; // Fluid density [kg/m3]
	private double eta;   // Electrical power reference point [W]
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public LinTransmission() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Fan constructor
	 * 
	 * @param type
	 */
	public LinTransmission(String type) {
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
		rotSpeedIn = new IOContainer("RotSpeed", Unit.RPM, 0);
		torqueIn   = new IOContainer("Torque", Unit.NEWTONMETER, 0);
		inputs.add(rotSpeedIn);
		inputs.add(torqueIn);
		
		/* Define output parameters */
		outputs     = new ArrayList<IOContainer>();
		rotSpeedOut = new IOContainer("RotSpeed", Unit.RPM, 0);
		torqueOut   = new IOContainer("Torque", Unit.NEWTONMETER, 0);
		pLoss       = new IOContainer("PLoss",  Unit.WATT, 0);
		outputs.add(rotSpeedOut);
		outputs.add(torqueOut);
		outputs.add(pLoss);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Transmission", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			k   = params.getDoubleValue("TransmissionRatio");
			eta = params.getDoubleValue("Efficiency");
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
    	if (k <= 0){
    		throw new Exception("Transmission, type:" +type+ 
					": Negative or zero: TransmissionRatio must be strictly positive");
    	}
    	if (eta <= 0 && eta>1){
    		throw new Exception("Transmission, type:" +type+ 
					": Negative, zero or ge 1: Efficiency must be in (0,1]");
    	}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		/*
		 * Transmission k
		 * Efficiency eta
		 */
		rotSpeedOut.setValue(rotSpeedIn.getValue()/k);
		torqueOut.setValue(torqueIn.getValue()*k/eta);
		
		pLoss.setValue( rotSpeedIn.getValue()*Math.PI/30*torqueIn.getValue() * (1-eta)/eta );
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}