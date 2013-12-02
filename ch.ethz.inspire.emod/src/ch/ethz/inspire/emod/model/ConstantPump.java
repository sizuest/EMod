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
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General ConstantPump model class.
 * Implements the physical model of a ConstantPump including a blow off valve which opens automatically
 * if the generated pressure by the pump reaches the pressure limit set by the blow off valve.
 * Constant means that the pump`s displacement can not be adjusted, so the mass flow can only be changed
 * by variation of rotational speed and hydraulic resistance.
 * The pump is able to deliver a maximum mass flow at a certain rotational speed. If the demanded
 * massflow is bigger, a warning is given.
 * 
 * Assumptions:
 * -No leakage
 * -Losses caused by thermal effects in the motor and by flow through blow off valve
 * -Blow off valve (DBV) included. Opens at certain pressure defined by the user
 * -Pressure is delivered according to the demanded massflow and rotational speed
 * -The bypass flow through the DBV is the difference between the maximum possible flow and the demanded massflow.
 * -The demanded massflow is defined by the user and does not differ from these values
 * 
 * 
 * Inputlist:
 *   1: MassFlowOut		 : [kg/s] 	: Demanded mass flow out
 *   2: Density			 : [kg/m^3]
 *   3: State			 : 		 	: ON/OFF position of the pump. 1 means ON, 0 means OFF
 *   4: Demanded pressure: [bar]	: Pressure that must be provided by the pump
 *   5: Rotspeed		 : [RPM]	: Actual rotational speed
 * Outputlist:
 *   1: PElectric   : [W]    : Demanded electrical power
 *   2: PBypass     : [W]    : Power loss created by bypass flow through blow off valve
 *   3: PHydraulic  : [W]    : Total power in the fluid
 *   4: PThermal	: [W]	 : Thermal power loss in the motor and pump
 *   5: PLoss		: [W]	 : Total power loss => PTh + PBypass
 *   6: PressureOut : [bar]  : Created pressure by the pump
 *   
 * Config parameters:
 *   Displacement		  : [cm^3/U]    : Flow per revolution
 *   pDBV				  : [Pa]		: Pressure at which the blow off valve (DBV) opens
 *   VolFlowSamples		  :	[kg/s]		:
 *   RotSpeedSamples	  : [RPM]		:
 *   PressureMatrix		  : [Pa]		:
 * 
 * @author simon
 *
 */
@XmlRootElement
public class ConstantPump extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer massFlowOut;		//demanded mass flow
	private IOContainer density;
	private IOContainer	pumpCtrl;
	private IOContainer demandedPressure;
	private IOContainer rotSpeed;

	// Output parameters:
	private IOContainer pressureOut;
	private IOContainer pel;
	private IOContainer pbypass;
	private IOContainer phydr;
	private IOContainer pth;
	private IOContainer ploss;
	private IOContainer massFlowBypass;
	
	
	// Parameters used by the model. 			
	private double lastpressure;
	private double lastmassflow;
	private double lastpumpCtrl;
	private double lastrotspeed;
	private double pumpmassflow;			//mass flow delivered by the pump at a certain pressure and rotational speed
	private double pumppressure;			//pressure delivered by the pump at a certain mass flow and rotational speed
	private double pDBV;
	private double displacement;
	private double[] volFlowSamples;
	private double[] pumpPowerSamples;
	private double[] rotSpeedSamples;
	private double[][] pressureMatrix;


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
		inputs       	 = new ArrayList<IOContainer>();
		massFlowOut 	 = new IOContainer("MassFlowOut", Unit.KG_S, 0);
		density			 = new IOContainer("Density", Unit.KG_MCUBIC, 0);
		pumpCtrl		 = new IOContainer("PumpCtrl", Unit.NONE, 0);
		rotSpeed		 = new IOContainer("RotSpeed", Unit.RPM, 0);
		demandedPressure = new IOContainer("DemandedPressure", Unit.PA, 0);
		inputs.add(massFlowOut);
		inputs.add(density);
		inputs.add(pumpCtrl);
		inputs.add(rotSpeed);
		inputs.add(demandedPressure);
		
		/* Define output parameters */
		outputs    	   = new ArrayList<IOContainer>();
		pressureOut    = new IOContainer("PressureOut", Unit.PA, 0);
		pel        	   = new IOContainer("PElectric", Unit.WATT, 0);
		pbypass    	   = new IOContainer("PBypass",  Unit.WATT, 0);
		phydr      	   = new IOContainer("PHydraulic", Unit.WATT, 0);
		pth		   	   = new IOContainer("PThermal",   Unit.WATT, 0);
		ploss	   	   = new IOContainer("PLoss", Unit.WATT, 0);
		massFlowBypass = new IOContainer("MassFlowBypass", Unit.KG_S,0);
		outputs.add(pressureOut);
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
			displacement     = params.getDoubleValue("Displacement");
			pDBV			 = params.getDoubleValue("P_DBV");
			volFlowSamples   = params.getDoubleArray("VolFlowSamples");
			pumpPowerSamples = params.getDoubleArray("PumpPowerSamples");
			rotSpeedSamples	 = params.getDoubleArray("RotSpeedSamples");
			pressureMatrix	 = params.getDoubleMatrix("PressureMatrix");
			
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
		if (displacement<=0){
			throw new Exception("ConstantPump, type:" +type+ 
					": Negative or zero value: Displacement must be strictly positive!");
		}

		if (pDBV<=0){
			throw new Exception("ConstantPump, type:" +type+ 
					": Negative or zero value: P_DBV must be strictly positive!");
		}
		for (int i=1; i<volFlowSamples.length; i++){
			if(volFlowSamples[i]<0){
				throw new Exception("ConstantPump, type:" +type+ 
						": Negative or zero value: VolFlowSamples must be strictly positive!");
			}
		}
		for (int i=1; i<rotSpeedSamples.length; i++){
			if(rotSpeedSamples[i]<0){
				throw new Exception("ConstantPump, type:" +type+ 
						": Negative or zero value: RotSpeedSamples must be strictly positive!");
			}
		}
		for (int i=1; i<pumpPowerSamples.length; i++){
			if(pumpPowerSamples[i]<0){
				throw new Exception("ConstantPump, type:" +type+ 
						": Negative or zero value: PumpPowerSamples must be strictly positive!");
			}
		}
		for	(int i=0; i<volFlowSamples.length; i++) {
			for (int j=0; j<rotSpeedSamples.length; j++) {
				if ( (pressureMatrix[i][j] <= 0)) {
						throw new Exception("ConstantPump, type:" +type+ 
								": 'PressureMatrix' must be >0!");
				}
			}
		}
		//Check dimensions:
		
		if (pumpPowerSamples.length != volFlowSamples.length) {
			throw new Exception("ConstantPump, type:" +type+ 
					": Dimension missmatch: Vector 'PumpPowerSamples' must have same dimension as " +
					"'VolFlowSamples' (" + pumpPowerSamples.length + "!=" + volFlowSamples.length + ")!");
		}
		if (rotSpeedSamples.length != volFlowSamples.length) {
			throw new Exception("ConstantPump, type:" +type+ 
					": Dimension missmatch: Vector 'RotSpeedSamples' must have same dimension as " +
					"'VolFlowSamples' (" + rotSpeedSamples.length + "!=" + volFlowSamples.length + ")!");
		}
		if (pumpPowerSamples.length != pressureMatrix.length) {
			throw new Exception("ConstantPump, type:" +type+ 
					": Dimension missmatch: Vector 'PumpPowerSamples' must have same dimension as " +
					"'PressureMatrixSamples' (" + pumpPowerSamples.length + "!=" + pressureMatrix.length + ")!");
		}
		// Check if sorted:
		
		for (int i=1; i<volFlowSamples.length; i++) {
			if (volFlowSamples[i] <= volFlowSamples[i-1]) {
				throw new Exception("ConstantPump, type:" +type+ 
						": Sample vector 'VolFlowSamples' must be sorted!");
			}
		}
		for (int i=1; i<rotSpeedSamples.length; i++) {
			if (rotSpeedSamples[i] <= rotSpeedSamples[i-1]) {
				throw new Exception("ConstantPump, type:" +type+ 
						": Sample vector 'RotSpeedSamples' must be sorted!");
			}
		}
		for (int i=1; i<pumpPowerSamples.length; i++) {
			if (pumpPowerSamples[i] >= pumpPowerSamples[i-1]) {
				throw new Exception("ConstantPump, type:" +type+ 
						": Sample vector 'PumpPowerSamples' must be sorted!");
			}
		}

	}
	
    

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
				
	    if(lastpressure == pressureOut.getValue() && lastmassflow==massFlowOut.getValue() && lastpumpCtrl == pumpCtrl.getValue() && lastrotspeed == rotSpeed.getValue()){
	    	    	// Input values did not change, nothing to do.
			return;
	    }
	    
	    lastpressure = pressureOut.getValue();
		lastmassflow = massFlowOut.getValue();
		lastpumpCtrl = pumpCtrl.getValue();
		lastrotspeed = rotSpeed.getValue();
	
		//Pump ON
		if(lastpumpCtrl == 1){
			
		pumppressure=Algo.bilinearInterpolation(lastrotspeed, lastmassflow/density.getValue()*(60*1000), rotSpeedSamples, volFlowSamples,pressureMatrix);
			
			//if(lastmassflow<=displacement*lastrotspeed*density.getValue()/(60*1000*1000) || demandedPressure.getValue()<=pumppressure){
				
				if(pumppressure<pDBV){
					pressureOut.setValue(pumppressure);
					pumpmassflow=lastmassflow;
				}
				
				else{
					pressureOut.setValue(pDBV);
					pumpmassflow=33.8*density.getValue()/60000;
				}
					massFlowBypass.setValue(pumpmassflow-lastmassflow);
					phydr.setValue(pressureOut.getValue()*lastmassflow/density.getValue());
					pel.setValue(Algo.linearInterpolation(pumpmassflow/density.getValue()*60000, volFlowSamples, pumpPowerSamples));
					pbypass.setValue(pressureOut.getValue()*massFlowBypass.getValue()/density.getValue());
					pth.setValue(-pbypass.getValue()+pel.getValue()-phydr.getValue());
					ploss.setValue(pth.getValue()+pbypass.getValue());
				
			//}
			
			//else{
				//Fehlerausgabe: Verlangter Massenstrom oder Druck zu gross aktuelle Einstellungen der Pumpe
			//}
		}
			
			
		//Pump OFF
		else{
			pressureOut.setValue(0);
			massFlowBypass.setValue(0);
			pbypass.setValue(0);
			phydr.setValue(0);
			pel.setValue(0);
			pth.setValue(0);
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
