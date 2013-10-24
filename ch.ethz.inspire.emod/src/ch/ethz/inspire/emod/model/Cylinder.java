/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
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
 * General Cylinder model class.
 * Implements the physical model of a hydraulic cylinder
 * 
 * Assumptions:
 * * TODO
 * 
 * 
 * Inputlist:
 *   1: Force       : [N]   : Required force
 *   2: Speed       : [m/s] : Required displacement velocity
 *   3: Viscosity   : [mm2/s] : TODO 
 *   4: Density     : [kg/m3] : TODO
 * Outputlist:
 *   1: Pressure    : [Pa]   : TODO
 *   2: MassFlow    : [kg/s] : TODO
 *   3: LeakFlow    : [kg/s] : TODO
 *   
 * Config parameters:
 *   PistonDiameter : [m] : TODO
 *   PistonThickness: [m] : TODO
 * 
 * @author kraandre
 *
 */
@XmlRootElement
public class Cylinder extends APhysicalComponent{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer force;
	private IOContainer velocity;
	private IOContainer viscosity;
	private IOContainer density;
	
	// Output parameters:
	private IOContainer pressure;
	private IOContainer massFlow;
	private IOContainer leakFlow;
	
	// Parameters used by the model. 
	private double pistonDiameter;
	private double pistonThickness; 
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Cylinder() {
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
	public Cylinder(String type) {
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
		force     = new IOContainer("Force", Unit.NEWTON, 0);
		velocity  = new IOContainer("Speed", Unit.MM_MIN, 0);
		viscosity = new IOContainer("Viscosity", Unit.MMSQUARE_S, 0);
		density   = new IOContainer("Density", Unit.KG_MCUBIC, 0);
		inputs.add(force);
		inputs.add(velocity);
		inputs.add(viscosity);
		inputs.add(density);
		
		/* Define output parameters */
		outputs   = new ArrayList<IOContainer>();
		pressure  = new IOContainer("Pressure", Unit.PA, 0);
		massFlow  = new IOContainer("MassFlow", Unit.KG_S, 0);
		leakFlow  = new IOContainer("LeakFlow", Unit.KG_S, 0);
		outputs.add(pressure);
		outputs.add(massFlow);
		outputs.add(leakFlow);
		
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader("Cylinder", type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			pistonDiameter  = params.getDoubleValue("PistonDiameter");
			pistonThickness = params.getDoubleValue("PistonThickness");
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
		if(0>pistonDiameter)
			throw new Exception("Cylinder, type:" +type+ 
					": Non physical value: Variable 'pistonDiameter' must be bigger than zero!");
		
		// TODO pistonThickness
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
			
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
