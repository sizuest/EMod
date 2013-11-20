/***********************************
 * $Id: ConstantPump.java 96 2012-04-05 08:10:57Z sizuest $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/src/ch/ethz/inspire/emod/model/Pump.java $
 * $Author: sizuest $
 * $Date: 2012-04-05 10:10:57 +0200 (Do, 05 Apr 2012) $
 * $Rev: 96 $
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
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General ConstantPump model class.
 * Implements the physical model of a ConstantPump including a blow off valve which opens automatically if the demanded massflow is smaller than the created massflow.
 * 
 * Assumptions:
 * -No leakage
 * -Losses caused by thermal effects in the motor and by flow through blow off valve
 * -Flow is always constant
 * -Pressure is delivered according to the resistance given by the following components (e.g cylinder, valve)
 * 
 * 
 * Inputlist:
 *   1: MassFlowOut : [kg/s] : Demanded mass flow out
 *   2: PressureOut : [Pa]   : Pressure created by the pump
 *   3: Density		: [kg/m^3]
 *   4: State		: 		 : ON/OFF position of the pump. 1 means ON, 0 means OFF
 * Outputlist:
 *   1: PEl	        : [W]    : Demanded electrical power
 *   2: PBypass     : [W]    : Power loss created by bypass flow through blow off valve
 *   3: PHydr       : [W]    : Power in the fluid
 *   4: PTh			: [W]	 : Thermal power loss
 *   5: PLoss		: [W]	 : Total power loss => PTh + PBypass
 *   
 * Config parameters:
 *   ElectricalPower	  : [W]     : Nominal power if operating
 * 
 * @author simon
 *
 */
@XmlRootElement
public class ConstantPump extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer massFlowOut;
	private IOContainer pressureOut;
	private IOContainer density;
	private IOContainer	pumpCtrl;

	// Output parameters:
	private IOContainer pel;
	private IOContainer pbypass;
	private IOContainer phydr;
	private IOContainer pth;
	private IOContainer ploss;
	private IOContainer massFlowBypass;
	
	
	// Parameters used by the model. 
	private double pelPump;				// Power demand of the pump if on [W]
	private double constantFlow;		// ConstantFlow delivered by the pump [l/min]
	private double constantMassFlow;	// ConstantMassFlow delivered by the pump [kg/s]
	private double lastpressure;
	private double lastmassflow;
	private double lastpumpCtrl;

	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public ConstantPump() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * ConstantPump constructor
	 * 
	 * @param type
	 */
	public ConstantPump(String type) {
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
		massFlowOut = new IOContainer("MassFlowOut", Unit.KG_S, 0);
		pressureOut = new IOContainer("PressureOut", Unit.PA, 0);
		density	= new IOContainer("Density", Unit.KG_MCUBIC, 0);
		pumpCtrl= new IOContainer("PumpCtrl", Unit.NONE, 0);
		inputs.add(massFlowOut);
		inputs.add(pressureOut);
		inputs.add(density);
		inputs.add(pumpCtrl);
		
		/* Define output parameters */
		outputs    = new ArrayList<IOContainer>();
		pel        = new IOContainer("PEl", Unit.WATT, 0);
		pbypass    = new IOContainer("PBypass",  Unit.WATT, 0);
		phydr      = new IOContainer("PHydr", Unit.WATT, 0);
		pth		   = new IOContainer("PTh",   Unit.WATT, 0);
		ploss	   = new IOContainer("PLoss", Unit.WATT, 0);
		massFlowBypass = new IOContainer("MassFlowBypass", Unit.KG_S,0);
		outputs.add(pel);
		outputs.add(pbypass);
		outputs.add(phydr);
		outputs.add(pth);
		outputs.add(ploss);
		outputs.add(massFlowBypass);
		
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("ConstantPump", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			pelPump         = params.getDoubleValue("ElectricalPower");
			constantFlow    = params.getDoubleValue("ConstantFlow");
			
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
					
		// Strictly positive
		if (constantFlow<=0){
			throw new Exception("ConstantPump, type:" +type+ 
					": Negative or zero value: ConstantFlow must be strictly positive!");
		}
		if (pelPump<=0){
			throw new Exception("ConstantPump, type:" +type+ 
					": Negative or zero value: Pump power must be strictly positive!");
		}
	}
	
    

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
				
    if(lastpressure==pressureOut.getValue() && lastmassflow==massFlowOut.getValue() && lastpumpCtrl == pumpCtrl.getValue()){
    	    	// Input values did not change, nothing to do.
		return;
    }
    
    lastpressure = pressureOut.getValue();
	lastmassflow = massFlowOut.getValue();
	constantMassFlow = constantFlow*density.getValue()/(1000*60);	//Change [l/min] in [kg/s]
	lastpumpCtrl = pumpCtrl.getValue();

	
	if(lastpumpCtrl == 1){
		
		if(constantMassFlow>lastmassflow && lastmassflow>0){
			massFlowBypass.setValue(constantMassFlow-massFlowOut.getValue());
			phydr.setValue(lastpressure*constantMassFlow/density.getValue());
			pbypass.setValue(lastpressure*massFlowBypass.getValue()/density.getValue());
			pel.setValue(pelPump);
			pth.setValue(pel.getValue()-phydr.getValue());
			ploss.setValue(pth.getValue()+pbypass.getValue());
		}
				
		else if(constantMassFlow<=lastmassflow){
			massFlowBypass.setValue(0);
			pbypass.setValue(0);
			phydr.setValue(lastpressure*constantMassFlow/density.getValue());
			pel.setValue(pelPump);
			pth.setValue(pel.getValue()-phydr.getValue());
			ploss.setValue(pth.getValue()+pbypass.getValue());
	
		}
		
	}
	
	else{
		massFlowBypass.setValue(0);
		pbypass.setValue(0);
		phydr.setValue(0);
		pel.setValue(0);
		pth.setValue(0);
		ploss.setValue(0);
	}	
	/*else if(lastmassflow==0){
		pbypass.setValue(0);
		phydr.setValue(0);
		pel.setValue(0);
		pth.setValue(0);
		ploss.setValue(0);

	}*/
	
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
