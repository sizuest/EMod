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
import ch.ethz.inspire.emod.utils.IOContainer;


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
 *   1: Signal      : [var] : Input in
 *   2: Control     : [var] : Control input
 * Outputlist:
 *   1: Output      : [var] : Output signal
 *   
 * Config parameters:
 *   [none]
 *   
 * 
 * @author andreas
 *
 */
@XmlRootElement
public class SwitchControl extends APhysicalComponent{

	@XmlElement
	protected String signalUnit;
	@XmlElement
	protected String controlUnit;
	@XmlElement
	protected double threshold;
	@XmlElement
	protected boolean passHigh;
	
	// Input parameters:
	private IOContainer inSignal;
	private IOContainer control;
	// Output parameters:
	private IOContainer outSignal;
	
	// Parameters used by the model. 
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public SwitchControl() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Switch constructor
	 * 
	 * @param type
	 */
	public SwitchControl(String type) {
		super();
		
		// Default values
		signalUnit  = Unit.WATT.toString();
		controlUnit = Unit.NONE.toString();
		threshold   = 1;
		passHigh    = false;
		
		//this.type=type;
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
		
		outputs  = new ArrayList<IOContainer>();
		inputs   = new ArrayList<IOContainer>();
		
		/* Define output parameters */
		inSignal  = new IOContainer("Input",   Unit.valueOf(signalUnit),  0);
		control   = new IOContainer("Control", Unit.valueOf(controlUnit), 0);
		inputs.add(inSignal);
		inputs.add(control);
		
		outSignal = new IOContainer("Output", Unit.valueOf(signalUnit), 0);
		outputs.add(outSignal);
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
		
		for(IOContainer ioc:inputs){
			if(ioc.getName().equals(name)) {
				temp=ioc;
				break;
			}
		}
			
		return temp;
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		if ( (control.getValue()>threshold && passHigh) ||
		     (control.getValue()<threshold && !passHigh) )
			outSignal.setValue(inSignal.getValue());
		else
			outSignal.setValue(0);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return "switch@"+threshold+controlUnit.toString();
	}
	public void setType(String type) {
		//TODO this.type = type;
	}
	
}
