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
 * General Amplifier model class.
 * Implements the physical model of a Amplifier
 * 
 * Assumptions:
 * * Amplifier can be modeled by the Approach
 *    P_tot = P_dmd/eta + P_ctrl
 *  
 *   Where P_dmd is the demanded power, and P_ctrl the power for the
 *   controller. Eta is the transmission efficency, and 
 *   P_amp = P_dmd/eta
 *   
 * * The efficency can be modeled as a function of P_dmd
 *  
 * 
 * 
 * 
 * Inputlist:
 *   1: State       : [-] : On/Off
 *   2: PDmd        : [W] : Actual electrical power demand
 * Outputlist:
 *   1: PTotal      : [W]   : Calculated total energy demand
 *   2: PLoss       : [W]   : Calculated power loss
 *   3: PAmp        : [W]   : Calculated amplifier power
 *   4: PCtrl       : [W]   : Calculated controller power
 *   
 * Config parameters:
 *   PowerSamples   : [W]   : Power samples used for linear 
 *                            interpolation of the efficiency
 *   Efficiency     : [1]   : Efficiency vector. 
 *   PowerCtrl      : [W]   : Static control power
 * 
 * @author andreas
 *
 */
@XmlRootElement
public class Amplifier extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer pdmd;
	private IOContainer level;
	// Output parameters:
	private IOContainer ploss;
	private IOContainer pel;
	private IOContainer pamp;
	private IOContainer pctrl;
	private IOContainer efficiency;
	
	// Parameters used by the model. 
	private double[] powerSamples;     // Samples of power [W]
	private double   powerCtrl;        // Samples of normed rotational speed [1]
	private double[] efficiencyVector; // Efficiency sample matrix [1]
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Amplifier() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Amplifier constructor
	 * 
	 * @param type
	 */
	public Amplifier(String type) {
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
		inputs = new ArrayList<IOContainer>();
		level = new IOContainer("State", Unit.NONE, 0);
		pdmd  = new IOContainer("PDmd", Unit.WATT, 0);
		inputs.add(level);
		inputs.add(pdmd);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pel        = new IOContainer("PTotal",     Unit.WATT, 0);
		ploss      = new IOContainer("PLoss",      Unit.WATT, 0);
		pamp       = new IOContainer("PAmp",       Unit.WATT, 0);
		pctrl      = new IOContainer("PCtrl",      Unit.WATT, 0);
		efficiency = new IOContainer("Efficiency", Unit.NONE, 0);
		outputs.add(pel);
		outputs.add(ploss);
		outputs.add(pamp);
		outputs.add(pctrl);
		outputs.add(efficiency);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Amplifier", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			powerSamples     = params.getDoubleArray("PowerSamples");
			efficiencyVector = params.getDoubleArray("Efficiency");
			powerCtrl        = params.getDoubleValue("PowerCtrl");
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
		if (powerSamples.length != efficiencyVector.length) {
			throw new Exception("Amplifier, type:" +type+ 
					": Dimension missmatch: Vector 'PowerSamples' must have same dimension as " +
					"'Efficiency' (" + powerSamples.length + "!=" + efficiencyVector.length + ")!");
		}
		// Check if sorted:
		for (int i=1; i<powerSamples.length; i++) {
			if (powerSamples[i] <= powerSamples[i-1]) {
				throw new Exception("Amplifier, type:" +type+ 
						": Sample vector 'PowerSamples' must be sorted!");
			}
		}
		// Check efficiency values:
		for	(int i=0; i<efficiencyVector.length; i++) {
			if ( (efficiencyVector[i] <= 0) || 
				 (efficiencyVector[i] > 1.0) ) {
					throw new Exception("Amplifier, type:" +type+ 
							": 'Efficiency' must be >0 and <=1!");
				}
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		
		if (0!=level.getValue()){
			pctrl.setValue(powerCtrl);
			/*
			 * Get efficiency from the configured sample values by linear interpolation.
			 */
			double eff = Algo.linearInterpolation(pdmd.getValue(), powerSamples, efficiencyVector);
			
			/* The power loss depends on the efficiency of the amp for the actual
			 * working point (actual rotational speed and torque).
			 * 	P_tot  = P_dmd / eff + P_Ctrl
			 *  P_loss = P_tot - P_dmd = P_dmd / eff - P_dmd  + P_ctrl = P_dmd (1/eff -1)  + P_ctrl
			 */
			pamp.setValue(pdmd.getValue() / eff);
			pel.setValue(pamp.getValue()+pctrl.getValue());
			ploss.setValue(pamp.getValue() * (1/eff - 1) + pctrl.getValue());
			efficiency.setValue(eff);
		}
		else {
			pctrl.setValue(0);
			pamp.setValue(0);
			pel.setValue(0);
			efficiency.setValue(0);
			ploss.setValue(0);
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
