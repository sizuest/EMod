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

package ch.ethz.inspire.emod.model.math;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;


import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General Sum of inputs class
 * 
 * Assumptions:
 * 
 * 
 * Inputlist:
 *   1: Plus        : [var] : Counted positive
 *   2: Minus       : [var] : Counted negative
 * Outputlist:
 *   1: Sum         : [var] : Calculated sum
 *   
 * Config parameters:
 *   Unit           : [string]   : Unit of the sum block
 * 
 * @author simon
 *
 */

public class Sum extends APhysicalComponent{
	protected String type = "Sum";
	@XmlElement
	protected String unit;
	
	// Input Lists
	private ArrayList<IOContainer> plus;
	private ArrayList<IOContainer> minus;
	
	private IOContainer add, sub;
	
	// Output parameters:
	private IOContainer sum;
	
	// Sum
	private double tmpSum;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Sum() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Linear Motor constructor
	 * 
	 * @param unit
	 */
	public Sum(Unit unit) {
		super();
		
		this.unit=unit.toString();
		
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{	
		/* Define Input parameters */
		inputs   = new ArrayList<IOContainer>();
		plus     = new ArrayList<IOContainer>();
		minus    = new ArrayList<IOContainer>();
		
		add      = new IOContainer("Plus",  new SiUnit(unit), 0);
		sub      = new IOContainer("Minus", new SiUnit(unit), 0);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		sum     = new IOContainer("Sum", new SiUnit(unit), 0);
		outputs.add(sum);
		
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
		
	}
    
    /**
     * Returns the desired IOContainer
     * 
     * If the desired input name matches Plus or Minus, a new input
     * is created and added to the set of avaiable inputs
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
		if(name.matches(add.getName())) {
			temp = new IOContainer(add.getName()+(plus.size()+1), add);
			inputs.add(temp);
			plus.add(temp);
		}
		else if(name.matches(sub.getName())) {
			temp = new IOContainer(sub.getName()+(minus.size()+1), sub);
			inputs.add(temp);
			minus.add(temp);
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
		tmpSum = 0;
		
		for( IOContainer in : plus)
			tmpSum += in.getValue();
		
		for( IOContainer in : minus)
			tmpSum -= in.getValue();
		
		sum.setValue(tmpSum);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		//this.type = type;
	}
}
