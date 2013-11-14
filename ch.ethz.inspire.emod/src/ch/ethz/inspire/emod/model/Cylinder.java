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
 * -3 States (idle, moving, extended&hold)
 * -Friction is not taken into account.
 * -Leakage only occurs internally and is treated as annular passage flow.
 * -Fitting between cylinder and piston is assumed as H8/f7.
 * 
 * 
 * Inputlist:
 *   1: Force       : [N]   : Required force
 *   2: Speed       : [m/s] : Required displacement velocity
 *   3: Viscosity   : [mm2/s] : Hydraulic oil`s viscosity
 *   4: Density     : [kg/m3] : Hydraulic oil`s density
 * Outputlist:
 *   1: Pressure    : [Pa]   : Pressure in the cylinder chamber
 *   2: MassFlow    : [kg/s] : Massflow into the cylinder chamber
 *   3: LeakFlow    : [kg/s] : Internal leakage from high pressure chamber to low low pressure chamber
 *   4: Pmech		: [W]	 : Mechanical power
 *   5: Ploss		: [W]	 : Power loss
 *   6: Phydr		: [W]	 : Hydraulic power
 *   
 * Config parameters:
 *   PistonDiameter : [m] 
 *   PistonThickness: [m]
 *   Stroke			: [m]
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
	
	//Saving last input values:
	
	private double lastdensity   = 880;
	private double lastviscosity = 20;
	private double lastforce     = 0;
	private double lastvelocity  = 0;
	private double lastposition;
	
	//TODO Viskosit�t und Dichte je nach �l und Temperatur variabel
	
	// Output parameters:
	private IOContainer pressure;
	private IOContainer massFlow;
	private IOContainer leakFlow;
	private IOContainer pmech;
	private IOContainer ploss;
	private IOContainer phydr;
	
	// Parameters used by the model. 
	private double pistonDiameter;
	private double pistonThickness;
	private double stroke;
	private double H_8;
	private double f_7;
	
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
	 * Cylinder constructor
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
		pmech     = new IOContainer("PMech"   , Unit.WATT, 0);
		ploss     = new IOContainer("PLoss"   , Unit.WATT, 0);
		phydr     = new IOContainer("PHydr"   , Unit.WATT, 0);
		outputs.add(pressure);
		outputs.add(massFlow);
		outputs.add(leakFlow);
		outputs.add(pmech);
		outputs.add(ploss);
		outputs.add(phydr);
		
		/* Define initial state */
		lastposition = 0;

			
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
			stroke			= params.getDoubleValue("CylinderStroke");
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
		
		if(0>pistonThickness)
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'pistonThickness' must be bigger than zero!");
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		if ( (lastvelocity == velocity.getValue() ) &&
				 (lastforce == force.getValue() ) ) {
			// Input values did not change, nothing to do.
			return;
		}
		
		lastvelocity  = velocity.getValue() * 60/1000;	// mm/min -> m/s
		lastforce     = force.getValue();
		//lastdensity   = density.getValue();
		//lastviscosity = viscosity.getValue();
		
		
		//Choosing of the fitting according to the piston diameter
		if(pistonDiameter > 0.03 && pistonDiameter <= 0.05)
		{
			H_8 = 39/2*Math.pow(10,-6);
			f_7 = 37.5*Math.pow(10,-6);
		}
		
		else if(pistonDiameter > 0.05 && pistonDiameter <= 0.065)
		{
			H_8 = 23*Math.pow(10,-6);
			f_7 = 45*Math.pow(10,-6);
		}
		
		//State 1: Cylinder idle
		if(lastvelocity == 0 && lastforce == 0)
		{
		
			pressure.setValue(0);
			leakFlow.setValue(0);
			massFlow.setValue(0);
			pmech.setValue(0);
			ploss.setValue(0);
		
		}
		
		//State 2: Cylinder extended & hold
		else if(lastvelocity == 0 && lastforce != 0)
		{

			pressure.setValue(lastforce/(Math.PI/4*Math.pow(pistonDiameter,2)));
			leakFlow.setValue(pressure.getValue()*(pistonDiameter+H_8-f_7)*Math.pow(H_8+f_7,3)/(12*lastviscosity*Math.pow(10,-6)*lastdensity*pistonThickness));
			massFlow.setValue(leakFlow.getValue());
			pmech.setValue(0);
			ploss.setValue(pressure.getValue()*leakFlow.getValue());
		
		}
		
		//State 3: Piston moving
		else if(lastvelocity != 0 && lastforce != 0)
		{
			// Check if movement is possible & take measures
			if (lastposition >= stroke) {
				lastposition = stroke;
				
				if (lastvelocity > 0) lastvelocity = 0;
			}
			else if (lastposition <= 0) {
				lastposition = 0;
				
				if (lastvelocity < 0) lastvelocity = 0;
			}
			
			
			//Mechanical power exists
			pressure.setValue(lastforce/(Math.PI/4*Math.pow(pistonDiameter,2)));
			leakFlow.setValue(pressure.getValue()*(pistonDiameter+H_8-f_7)*Math.pow(H_8+f_7,3)/(12*lastviscosity*Math.pow(10,-6)*lastdensity*pistonThickness));
			massFlow.setValue(lastdensity*(lastvelocity*Math.PI/4*Math.pow(pistonDiameter,2)+leakFlow.getValue()));
			pmech.setValue(lastforce*lastvelocity);
			ploss.setValue(pressure.getValue()*leakFlow.getValue()/lastdensity);
			phydr.setValue(pmech.getValue()+ploss.getValue());
				
			
			// Get new position
			lastposition += sampleperiod * lastvelocity;
					
		}
		
	phydr.setValue(pmech.getValue()+ploss.getValue());
	
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
