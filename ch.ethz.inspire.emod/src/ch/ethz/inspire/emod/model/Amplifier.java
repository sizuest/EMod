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
 *   1: PSupply     : [W]   : Calculated total energy demand
 *   2: PLoss       : [W]   : Calculated power loss
 *   3: PATotal     : [W]   : Calculated amplifier power
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
	private IOContainer state;
	
	private ArrayList<IOContainer> inputsDyn;
	
	// Output parameters:
	private IOContainer ploss;
	private IOContainer psupply;
	private IOContainer ptotal;
	private IOContainer puse;
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
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
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
		inputs    = new ArrayList<IOContainer>();
		inputsDyn = new ArrayList<IOContainer>();
		state = new IOContainer("State", new SiUnit(Unit.NONE), 0, ContainerType.CONTROL);
		pdmd  = new IOContainer("PDmd",  new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		inputs.add(state);
		inputs.add(pdmd);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		psupply    = new IOContainer("PSupply",    new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		ploss      = new IOContainer("PLoss",      new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		ptotal     = new IOContainer("PTotal",     new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		puse       = new IOContainer("PUse",       new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		efficiency = new IOContainer("Efficiency", new SiUnit(Unit.NONE), 0, ContainerType.INFORMATION);
		outputs.add(psupply);
		outputs.add(ploss);
		outputs.add(ptotal);
		outputs.add(puse);
		outputs.add(efficiency);
		
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
			powerSamples     = params.getDoubleArray("PowerSamples");
			efficiencyVector = params.getDoubleArray("Efficiency");
			powerCtrl        = params.getDoubleValue("PowerCtrl");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
		// Validate the parameters:
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
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
			if ( (efficiencyVector[i] < 0) || 
				 (efficiencyVector[i] > 1.0) ) {
					throw new Exception("Amplifier, type:" +type+ 
							": 'Efficiency' must be >0 and <=1!");
				}
		}
	}
    
    @Override
    public IOContainer getInput(String name){
    	IOContainer temp = null;
    	if(name.equals(pdmd.getName())){
    		temp = new IOContainer(pdmd.getName()+(inputsDyn.size()+1), pdmd);
    		inputsDyn.add(temp);
    		inputs.add(temp);
    	}
    	else
    		for(IOContainer io:inputs)
    			if(name.equals(io.getName()))
    				temp = io;
    	
    	return temp;
    	
    }
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
    		
		double psum = 0; 
		for(IOContainer io: inputsDyn)
			psum+=io.getValue();
		pdmd.setValue(psum);
		
		if (0!=state.getValue()){
			puse.setValue(0);
			/*
			 * Get efficiency from the configured sample values by linear interpolation.
			 */
			double eff = Algo.linearInterpolation(pdmd.getValue(), powerSamples, efficiencyVector);
			
			/* The power loss depends on the efficiency of the amp for the actual
			 * working point 
			 */
			ptotal.setValue(pdmd.getValue() * (1/ eff-1) + powerCtrl);
			psupply.setValue(ptotal.getValue()+pdmd.getValue());
			ploss.setValue(psupply.getValue() - pdmd.getValue());
			efficiency.setValue(pdmd.getValue()/psupply.getValue());
		}
		else {
			puse.setValue(0);
			ptotal.setValue(0);
			psupply.setValue(0);
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
	
	public void setType(String type) {
		this.type = type;
	}
	
}
