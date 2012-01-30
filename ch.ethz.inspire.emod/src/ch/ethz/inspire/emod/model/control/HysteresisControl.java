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

package ch.ethz.inspire.emod.model.control;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.PropertiesHandler;


/**
 * General hysteresis controller class.
 * 
 * Output value takes values Low or High if any of the inputs
 * is crossing the Threshold value Low from above or the
 * threshold value high from below.
 * 
 * Assumptions:
 * 
 * 
 * Inputlist:
 *   1: Input       : [var] : Signals in
 * Outputlist:
 *   1: Output      : [var] : Output signal
 *   
 * Config parameters:
 *   OutputLow     : [var]    : Output during low state
 *   OutputHigh    : [var]    : Output during high state
 *   ThresholdLow  : [var]    : Lower threshold
 *   ThresholdHigh : [var]    : Upper threshold
 *   UnitInput     : [String] : Input unit
 *   UnitOutput    : [String] : Output unit
 *   
 * 
 * @author andreas
 *
 */
@XmlRootElement
public class HysteresisControl extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private ArrayList<IOContainer> inpList;
	// Output parameters:
	private IOContainer outSignal;
	
	// Parameters used by the model. 
	private double outLow, outHigh;
	private double thLow, thHigh;
	private String unitIn, unitOut;
	
	private boolean wasLow = true;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public HysteresisControl() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Hysteresis constructor
	 * 
	 * @param type
	 */
	public HysteresisControl(String type) {
		super();
		
		this.type=type;
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
		
		outputs  = new ArrayList<IOContainer>();
		inputs   = new ArrayList<IOContainer>();
		inpList  = new ArrayList<IOContainer>();

		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix")+
								"/"+PropertiesHandler.getProperty("sim.MachineName")+"/"+Defines.MACHINECONFIGDIR+"/"+
								PropertiesHandler.getProperty("sim.MachineConfigName");
		path = path+"/Control_"+type+".xml";
		try {
			params = new ComponentConfigReader(path);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			outLow     = params.getDoubleValue("OutputIfLow");
			outHigh    = params.getDoubleValue("OutputIfHigh");
			thLow      = params.getDoubleValue("LowerThreshold");
			thHigh     = params.getDoubleValue("UpperThreshold");
			unitIn     = params.getString("InputUnit");
			unitOut    = params.getString("OutputUnit");
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
		
		/* Define output parameters */
		outSignal = new IOContainer("Output",   Unit.valueOf(unitOut), outLow);
		outputs.add(outSignal);
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
		if ( thLow >= thHigh) {
			throw new Exception("Hysteresis, type:" +type+ 
					": Threshold value: Lower limit must be smaller than upper limit");
		}
		
		/* Try to conver units to enum */
		try {
			Unit.valueOf(unitIn);
		}
		catch (Exception e){
			throw new Exception( "Hysteresis, type:" +type+ 
					": Nonexistent Unit: Unit "+unitIn+" does not exist");
		}
		try {
			Unit.valueOf(unitOut);
		}
		catch (Exception e){
			throw new Exception( "Hysteresis, type:" +type+ 
					": Nonexistent Unit: Unit "+unitOut+" does not exist");
		}
	}
    
    /**
     * Returns the desired IOContainer
     * 
     * If the desired input name matches Input, a new input
     * is created and added to the set of available inputs
     * 
     * @param  name	Name of the desired input
     * @return temp IOContainer matched the desired name
     * 
     * @author simon
     */
    @Override
    public IOContainer getInput(String name) {
		IOContainer temp=null;
		
		/* 
		 * If the initialization has not been done, create a output with same unit as input
		 */
		if(name.matches("Input")) {
			temp = new IOContainer("In"+(inpList.size()+1), Unit.valueOf(unitIn), 0);
			inputs.add(temp);
			inpList.add(temp);
		}
		else {
			for(IOContainer ioc:inputs){
				if(ioc.getName().equals(name)) {
					temp=ioc;
					break;
				}
			}
		}
			
		return temp;
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		for( IOContainer in : inpList) {
			
			if (wasLow && in.getValue()>= thHigh) {
				outSignal.setValue(outHigh);
				wasLow = false;
				break;
			}
			else if (!wasLow && in.getValue()<= thLow) {
				outSignal.setValue(outLow);
				wasLow = true;
				break;
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