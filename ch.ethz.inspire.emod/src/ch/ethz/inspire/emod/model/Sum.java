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
	@XmlElement
	protected String type;
	
	// Input Lists
	private ArrayList<IOContainer> plus;
	private ArrayList<IOContainer> minus;
	
	// Output parameters:
	private IOContainer sum;
	
	// Unit of the element 
	private Unit unit;
	
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
	 * @param type
	 */
	public Sum(String type) {
		super();
		
		this.type=type;
		
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
		unit = Unit.valueOf(type.toUpperCase());
		
		/* Define Input parameters */
		inputs   = new ArrayList<IOContainer>();
		plus     = new ArrayList<IOContainer>();
		minus    = new ArrayList<IOContainer>();
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		sum     = new IOContainer("Sum", unit, 0);
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
    
    @Override
    public IOContainer getInput(String name) {
		IOContainer temp=null;
		
		/* 
		 * If the initialization has not been done, create a output with same unit as input
		 */
		if(name.matches("Plus")) {
			temp = new IOContainer("+"+(plus.size()+1), unit, 0);
			inputs.add(temp);
			plus.add(temp);
		}
		else if(name.matches("Minus")) {
			temp = new IOContainer("-"+(minus.size()+1), unit, 0);
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
}
