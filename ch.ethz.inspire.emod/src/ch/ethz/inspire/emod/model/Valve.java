/***********************************
 * $Id: Valve.java 103 2013-10-31 13:39:36Z kraandre $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/src/ch/ethz/inspire/emod/model/Cylinder.java $
 * $Author: kraandre $
 * $Date: 2013-10-31 14:39:36 +0100 (Do, 31 Okt 2013) $
 * $Rev: 103 $
 *
 * Copyright (c) 2013 by Inspire AG, ETHZ
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
 * General Valve model class.
 * Implements the physical model of a valve
 * 
 * Assumptions:
 * -2 States (idle, open)
 * -No leakage
 * 
 * 
 * Inputlist:
 *   1: Pressure    : [Pa]  
 *   2: Massflow    : [kg/s] : Oil flow through the valve
 *   3: Viscosity   : [m2/s] : Hydraulic oil`s kinematic viscosity
 *   4: Density     : [kg/m3] : Hydraulic oil`s density
 * Outputlist:
 *   1: Pressure    : [Pa]   : Pressure in the cylinder chamber
 *   2: MassFlow    : [kg/s] : Massflow into the cylinder chamber
 *   3: Ploss		: [W]	 : Power loss
 *   4: Pressureloss: [Pa]	 : Pressuredifference over the valve
 *   
 * Config parameters:
 *   ElectricPower  : [W] 
 *   PressureSamples: [Pa]
 *   VolflowSamples : [l/min]: Unit [l/min] is chosen because of easier handling for the user. Manufacturer data always given in [l/min]. From [kg/s] to [l/min], the factor 60*1000/density must be taken.
 * 
 * @author kraandre
 *
 */
@XmlRootElement
public class Valve extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer pressureOut;
	private IOContainer massflowOut;
	
	//Saving last input values:
	
	private double lastdensity   = 880;
	private double lastpressure  = 0;
	private double lastmassflow  = 0;
	
	//TODO Viskosität und Dichte je nach Öl und Temperatur variabel
	
	// Output parameters:
	private IOContainer pressureIn;
	private IOContainer massflowIn;
	private IOContainer ploss;
	private IOContainer pressureloss;
	
	// Parameters used by the model. 
	private double electricPower;
	private double[] pressureSamples;
	private double[] volflowSamples;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Valve() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Valve constructor
	 * 
	 * @param type
	 */
	public Valve(String type) {
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
		inputs       = new ArrayList<IOContainer>();
		pressureOut  = new IOContainer("PressureOut", Unit.PA, 0);
		massflowOut  = new IOContainer("MassFlowOut", Unit.KG_S, 0);
		inputs.add(pressureOut);
		inputs.add(massflowOut);
		
		/* Define output parameters */
		outputs   = new ArrayList<IOContainer>();
		pressureIn  = new IOContainer("PressureIn", Unit.PA, 0);
		massflowIn  = new IOContainer("MassFlowIn", Unit.KG_S, 0);
		ploss       = new IOContainer("PLoss"   , Unit.WATT, 0);
		pressureloss= new IOContainer("PressureLoss"   , Unit.PA, 0);
		outputs.add(pressureIn);
		outputs.add(massflowIn);
		outputs.add(ploss);
		outputs.add(pressureloss);

			
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Valve", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			electricPower   = params.getDoubleValue("ElectricPower");
			pressureSamples = params.getDoubleArray("PressureSamples");
			volflowSamples	= params.getDoubleArray("VolFlowSamples");
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
		//Check dimensions
		if(pressureSamples.length != volflowSamples.length)
			throw new Exception("Valve, type:" +type+
					": PressureSamples and MassFlowSamples must have the same dimensions!");
		
		//Check if sorted
		
		for (int i=1; i<pressureSamples.length; i++) {
			if (pressureSamples[i] <= pressureSamples[i-1]) {
				throw new Exception("Valve, type:" +type+ 
						": Sample vector 'PressureSamples' must be sorted!");
			}
		}
		for (int i=1; i<volflowSamples.length; i++) {
			if (volflowSamples[i] <= volflowSamples[i-1]) {
				throw new Exception("Valve, type:" +type+ 
						": Sample vector 'VolumetricflowSamples' must be sorted!");
			}
		}
		
		//Check physical value
		for(int i=0; i<pressureSamples.length; i++) {
			if(pressureSamples[i]<0) {
				throw new Exception("Valve, type:" +type+": Pressure must be bigger than zero!");
				}
		}
		if(0>electricPower){
			throw new Exception("Valve, type:" +type+ 
					": Non physical value: Variable 'electricPower' must be bigger than zero!");
		}
		
		for(int i=0; i<volflowSamples.length; i++) {
			if(volflowSamples[i]<0) {
				throw new Exception("Valve, type:" +type+": Volumetric flow must be bigger than zero!");
			}
		}
		
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		if ( (lastpressure == pressureOut.getValue() ) &&
				 (lastmassflow == massflowOut.getValue() ) ) 
			{
				// Input values did not change, nothing to do.
				return;
			}
		
		lastpressure  = pressureOut.getValue();
		lastmassflow  = massflowOut.getValue();
		//lastdensity   = density.getValue();
		//lastviscosity = viscosity.getValue();
		
		//Interpolation to get the pressureloss
		pressureloss.setValue(Algo.linearInterpolation(lastmassflow/lastdensity*60*1000, volflowSamples, pressureSamples));
		ploss.setValue(lastmassflow/lastdensity*pressureloss.getValue()+electricPower);
		massflowIn.setValue(lastmassflow);
		pressureIn.setValue(lastpressure+pressureloss.getValue());	
	
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
