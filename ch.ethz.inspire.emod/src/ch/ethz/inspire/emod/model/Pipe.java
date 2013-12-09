/***********************************
 * $Id: Cylinder.java 103 2013-10-31 13:39:36Z kraandre $
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
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Pipe model class.
 * Implements the physical model of a hydraulic pipe
 * 
 * Assumptions:
 * -No leakage
 * -Separation between laminar and turbulent flow
 * -Smooth surface
 * -Pipe wall is rigid
 * 
 * Inputlist:
 *   1: PressureOut : [Pa]   : Pressure at the end
 *   2: MassflowOut : [kg/s] : Massflow in the pipe
 *   3: Viscosity  	: [mm2/s] : Hydraulic oil`s kinematic viscosity
 *   4: Density     : [kg/m3] : Hydraulic oil`s density
 * Outputlist:
 *   1: PressureIn  : [Pa]   : Pressure in the cylinder chamber
 *   2: MassFlowIn  : [kg/s] : Massflow into the cylinder chamber
 *   3: Ploss		: [W]	 : Power loss
 *   4: Pressureloss: [Pa]   : Pressuredifference over the pipe
 *   
 * Config parameters:
 *   PipeDiameter   : [m] 
 *   PipeLength		: [m]
 * 
 * @author kraandre
 *
 */
@XmlRootElement
public class Pipe extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer pressureOut;
	private IOContainer massflowOut;
	private IOContainer viscosity;
	private IOContainer density;
	
	//Saving last input values:
	
	private double lastpressure  = 0;
	private double lastmassflow  = 0;
	private double Reynolds		 = 0;
	private double lambda		 = 0;
	
	//TODO Viskosität und Dichte je nach Öl und Temperatur variabel
	
	// Output parameters:
	private IOContainer pressureIn;
	private IOContainer massflowIn;
	private IOContainer ploss;
	private IOContainer pressureloss;
	private IOContainer connectionDiameter;
	
	// Parameters used by the model. 
	private double pipeDiameter;
	private double pipeLength;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Pipe() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Cylinder constructor
	 * 
	 * @param type
	 */
	public Pipe(String type) {
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
		pressureOut     = new IOContainer("PressureOut", Unit.PA, 0);
		massflowOut  = new IOContainer("MassFlowOut", Unit.KG_S, 0);
		viscosity = new IOContainer("Viscosity", Unit.MMSQUARE_S, 0);
		density   = new IOContainer("Density", Unit.KG_MCUBIC, 0);
		inputs.add(pressureOut);
		inputs.add(massflowOut);
		inputs.add(viscosity);
		inputs.add(density);
		
		/* Define output parameters */
		outputs   = new ArrayList<IOContainer>();
		pressureIn  = new IOContainer("PressureIn", Unit.PA, 0);
		massflowIn  = new IOContainer("MassFlowIn", Unit.KG_S, 0);
		ploss     = new IOContainer("PLoss", Unit.WATT, 0);
		pressureloss = new IOContainer("PressureLoss", Unit.PA, 0);
		connectionDiameter = new IOContainer("ConnectionDiameter", Unit.M, 0);
		outputs.add(pressureIn);
		outputs.add(massflowIn);
		outputs.add(ploss);
		outputs.add(pressureloss);
		outputs.add(connectionDiameter);
			
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Pipe", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			pipeDiameter  = params.getDoubleValue("PipeDiameter");
			pipeLength = params.getDoubleValue("PipeLength");
		
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
	
	connectionDiameter.setValue(pipeDiameter);
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
		if(0>pipeDiameter){
			throw new Exception("Pipe, type:" +type+ 
					": Non physical value: Variable 'pipeDiameter' must be bigger than zero!");
		}
		if(0>pipeLength){
			throw new Exception("Pipe, type:" +type+
					": Non physical value: Variable 'pistonLength' must be bigger than zero!");
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
		
		lastpressure    = pressureOut.getValue();
		lastmassflow    = massflowOut.getValue();
		
		Reynolds = pipeDiameter*lastmassflow/(density.getValue()*viscosity.getValue()*Math.pow(10,-6)*Math.PI/4*Math.pow(pipeDiameter, 2));
		
		if (Reynolds < 2300)
			{
				lambda = 64/Reynolds;
			}
		
		else
			{
				lambda = 0.3164/Math.pow(Reynolds, 0.25);			
			}
		if(lastmassflow!=0){
			pressureloss.setValue(lambda*pipeLength*Math.pow(lastmassflow, 2)/(pipeDiameter*2*density.getValue()*Math.pow(Math.PI/4*Math.pow(pipeDiameter, 2), 2)));
			massflowIn.setValue(massflowOut.getValue());
			ploss.setValue(pressureloss.getValue()*massflowOut.getValue()/density.getValue());			
			pressureIn.setValue(pressureOut.getValue()+pressureloss.getValue());
		}
		
		else{
			pressureloss.setValue(0);
			massflowIn.setValue(0);
			ploss.setValue(0);
			pressureIn.setValue(0);
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
