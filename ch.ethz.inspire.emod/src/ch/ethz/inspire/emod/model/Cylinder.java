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
 * -Leakage only occurs internally and is treated as annular passage flow.
 * -Fitting between cylinder and piston is assumed as H8/f7.
 * -Friction is taken into account by hydraulic-mechanic efficiency
 * 
 * 
 * Inputlist:
 *   1: Force   	      : [N]      : Required force
 *   2: Velocity   	      : [mm/min] : Required displacement velocity
 *   3: Viscosity 	      : [mm2/s]  : Hydraulic oil`s viscosity
 *   4: Density  	   	  : [kg/m3]  : Hydraulic oil`s density
 *   5: ConnectionDiameter: [m]		 : Connection diameter corresponds to pipe diameter
 * Outputlist:
 *   1: Pressure  	      : [Pa]     : Pressure in the cylinder chamber
 *   2: MassFlow  	      : [kg/s]   : Massflow into the cylinder chamber
 *   3: LeakFlow  	      : [kg/s]   : Internal leakage from high pressure chamber to low low pressure chamber
 *   4: Pmech		  	  : [W]	 	 : Mechanical power
 *   5: Ploss		  	  : [W]	 	 : Power loss
 *   6: Phydr			  : [W]	 	 : Hydraulic power
 *   
 * Config parameters:
 *   PistonDiameter 	: [m] 
 *   PistonThickness	: [m]
 *   Stroke				: [m]
 *   Efficiency			: [] 	 : Hydraulic-mechanic
 *   PistonRodDiameter	: [m]
 *   PMax				: [bar]	 : Maximum allowed pressure in the cylinder
 *   CylinderType	    : []	 : According to this parameter, the cylinder type is chosen.
 *   							   1 = single-action cylinder, 2 = double action cylinder 
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
	private IOContainer connectionDiameter;
	
	//Saving last input values:
	
	private double lastforce     = 0;
	private double lastvelocity  = 0;
	private double lastposition;

	
	// Output parameters:
	private IOContainer pressureIn;
	private IOContainer massFlowIn;
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
	private double efficiency;
	private double pistonRodDiameter;
	private double k = 5;				//Geometric loss koefficient
	private double pmax;
	private int cylinderType;
	
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
		velocity  = new IOContainer("Velocity", Unit.MM_MIN, 0);
		viscosity = new IOContainer("Viscosity", Unit.MMSQUARE_S, 0);
		density   = new IOContainer("Density", Unit.KG_MCUBIC, 0);
		connectionDiameter = new IOContainer("ConnectionDiameter", Unit.M, 0);
		inputs.add(force);
		inputs.add(velocity);
		inputs.add(density);
		inputs.add(viscosity);
		inputs.add(connectionDiameter);
		
		/* Define output parameters */
		outputs   = new ArrayList<IOContainer>();
		pressureIn  = new IOContainer("PressureIn", Unit.PA, 0);
		massFlowIn  = new IOContainer("MassFlowIn", Unit.KG_S, 0);
		leakFlow  = new IOContainer("LeakFlow", Unit.KG_S, 0);
		pmech     = new IOContainer("PMech"   , Unit.WATT, 0);
		ploss     = new IOContainer("PLoss"   , Unit.WATT, 0);
		phydr     = new IOContainer("PHydr"   , Unit.WATT, 0);
		outputs.add(pressureIn);
		outputs.add(massFlowIn);
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
			pistonDiameter 	    = params.getDoubleValue("PistonDiameter");
			pistonThickness		= params.getDoubleValue("PistonThickness");
			stroke				= params.getDoubleValue("CylinderStroke");
			efficiency			= params.getDoubleValue("Efficiency");
			pistonRodDiameter	= params.getDoubleValue("PistonRodDiameter");
			pmax				= params.getDoubleValue("PMax");
			cylinderType		= params.getIntValue("CylinderType");
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
		if(0>pistonDiameter){
			throw new Exception("Cylinder, type:" +type+ 
					": Non physical value: Variable 'PistonDiameter' must be bigger than zero!");
		}
		
		if(0>pistonThickness){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'PistonThickness' must be bigger than zero!");
		}
		
		if(0>efficiency || efficiency>1){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'Efficiency' must reach from bigger than zero to 1!");
		}
		
		if(0>pistonRodDiameter){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'PistonRodDiameter' must be bigger than zero!");
		}
		
		if(0>pmax){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'PMax' must be bigger than zero!");
		}
		
		if(cylinderType != 1 && cylinderType != 2){
			throw new Exception("Cylinder, type:" +type+
					": Non physical value: Variable 'PMax' must be bigger than zero!");
		}
		
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
		
		lastvelocity  = velocity.getValue() /60/1000;	// mm/min -> m/s
		lastforce     = force.getValue();

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
		
		else if(pistonDiameter > 0.065 && pistonDiameter <= 0.08)
		{
			H_8 = 23*Math.pow(10,-6);
			f_7 = 45*Math.pow(10,-6);
		}
		
		else if(pistonDiameter > 0.08 && pistonDiameter <= 0.1)
		{
			H_8 = 27*Math.pow(10,-6);
			f_7 = 53.5*Math.pow(10,-6);
		}
		
		//State 1: Cylinder idle
		if(lastvelocity == 0 && lastforce == 0)
		{
		
			pressureIn.setValue(0);
			leakFlow.setValue(0);
			massFlowIn.setValue(0);
			pmech.setValue(0);
			ploss.setValue(0);
			phydr.setValue(0);
		
		}
		
		//State 2: Cylinder extended & hold
		else if(lastvelocity == 0 && lastforce != 0)
		{
			pressureIn.setValue(lastforce/(Math.PI/4*(Math.pow(pistonDiameter,2)-Math.pow(pistonRodDiameter, 2))));
			leakFlow.setValue(Math.PI*pressureIn.getValue()*Math.pow(pistonDiameter/2+H_8-(pistonDiameter/2-f_7), 3)*(pistonDiameter/2+H_8+pistonDiameter/2-f_7)/(12*viscosity.getValue()*Math.pow(10,-6)*pistonThickness));
			massFlowIn.setValue(leakFlow.getValue());
			pmech.setValue(lastforce*Math.abs(lastvelocity));
			phydr.setValue(pressureIn.getValue()*massFlowIn.getValue()/density.getValue());
			ploss.setValue(phydr.getValue()-pmech.getValue());
		}
		
		//State 3: Piston moving
		else if(lastvelocity != 0)
		{
			// Check if movement is possible & take measures
			if (lastposition >= stroke) {
				lastposition = stroke;
				
				if (lastvelocity > 0){
					lastvelocity = -lastvelocity;		//Move backwards
				}
			}
			
			else if (lastposition <= 0) {
				lastposition = 0;
				
				if (lastvelocity < 0){ 
					lastvelocity = -lastvelocity;		//Move forward
				}
			}
			
			if(cylinderType == 1){
				if(lastvelocity>0){
					pressureIn.setValue(lastforce/(efficiency*Math.PI/4*(Math.pow(pistonDiameter,2)-Math.pow(pistonRodDiameter, 2))));
					massFlowIn.setValue(density.getValue()*lastvelocity*Math.PI/4*(Math.pow(pistonDiameter,2)-Math.pow(pistonRodDiameter, 2)));
					pmech.setValue(lastforce*lastvelocity);
					phydr.setValue(pressureIn.getValue()*massFlowIn.getValue()/density.getValue());
					ploss.setValue(phydr.getValue()-pmech.getValue());
				}
				
				else{			//Moving back to start position
					pressureIn.setValue(0);
					massFlowIn.setValue(0);
					pmech.setValue(0);
					phydr.setValue(0);
					ploss.setValue(0);
				}
				leakFlow.setValue(0);
			}
			
			else{
				pressureIn.setValue( lastforce/(efficiency*Math.PI/4*(Math.pow(pistonDiameter,2)-Math.pow(pistonRodDiameter, 2))) + (Math.pow(lastvelocity, 2)*Math.pow(Math.PI/4*(Math.pow(pistonDiameter,2)-Math.pow(pistonRodDiameter, 2)), 2)+ Math.pow(leakFlow.getValue(), 2)*density.getValue())/(2*Math.pow(Math.PI/4*Math.pow(connectionDiameter.getValue(),2)*k, 2)) );
				leakFlow.setValue(Math.PI*pressureIn.getValue()*Math.pow(pistonDiameter/2+H_8-(pistonDiameter/2-f_7), 3)*(pistonDiameter/2+H_8+pistonDiameter/2-f_7)/(12*viscosity.getValue()*Math.pow(10,-6)*pistonThickness));
				massFlowIn.setValue(density.getValue()*(Math.abs(lastvelocity)*Math.PI/4*(Math.pow(pistonDiameter,2)-Math.pow(pistonRodDiameter, 2)))+leakFlow.getValue());
				pmech.setValue(lastforce*Math.abs(lastvelocity));
				phydr.setValue(pressureIn.getValue()*massFlowIn.getValue()/density.getValue());
				ploss.setValue(phydr.getValue()-pmech.getValue());
			}
			// Get new position
			lastposition += sampleperiod * lastvelocity;
					
		}
		
		/*if(pressureIn.getValue>pmax){
			Alarmstufe Rot
			}
		 */
	
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
}
