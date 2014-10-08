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
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General bearing model class.
 * Implements the physical model of a bearing.
 * From the inputs force and rotational speed, the resulting friction torque and heat 
 * loss are calculateds
 * 
 * Assumptions:
 * The interia and frictional losses are negligible
 * 
 * Inputlist:
 *   1: ForceAxial    : [N]   : Actual axial force
 *   2: ForceRadial   : [N]   : Actual radial force
 *   3: RotSpeed      : [rpm] : Rotational speed
 * Outputlist:
 *   1: Torque        : [Nm]  : Resulting friction moment
 *   2: PLoss         : [W]   : Resulting lossess 
 *   
 * Config parameters:
 *   X0                 : [-]   : Static radial factor.
 *   Y0                 : [-]   : Static radial factor.
 *   X1                 : [-]   : Dynamic radial factor.
 *   Y1                 : [-]   : Dynamic radial  factor.
 *   C0                 : [N]   : Static load factor.
 *   MeanDiameter       : [m]   : Mean diameter of the bearing 
 *   LubricantViscosity : [m2/s]: Viscosity of the used lubricant
 *   ContactAngle       : [°]   : Contact angle of the rolling bodies
 *   LubricationFactor  : [-]   : Lubricant factor according to Harris p448
 * 
 * @author sizuest
 *
 */
@XmlRootElement
public class Bearing extends APhysicalComponent{
	
	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer forceAxial;
	private IOContainer forceRadial;
	private IOContainer rotSpeed;
	// Output parameters:
	private IOContainer torque;
	private IOContainer ploss;
	
	// Parameters used by the model. 
	double bearingX0, // radial factor (static)
	       bearingY0; // axial  factor (static)
	double bearingX1, // radial factor (dynamic)
	       bearingY1; // axial  factor (dynamic)
	double bearingC0; // static load rating
	double bearingRm; // Mean radius
	double bearingNu; // Lubricant viscosity
	double bearingA;  // Contact angle
	double bearingF0; // Empirical lubrication factor
	double bearingZ,  // Empirical model parameters, will be calculated dependent on the angle
	       bearingY;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Bearing() {
		super();
	}
	
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Clamp constructor
	 * 
	 * @param type
	 */
	public Bearing(String type) {
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
		forceAxial   = new IOContainer("ForceAxial",  Unit.NEWTON, 0, ContainerType.MECHANIC);
		forceRadial  = new IOContainer("ForceRadial", Unit.NEWTON, 0, ContainerType.MECHANIC);
		rotSpeed     = new IOContainer("RotSpeed",    Unit.RPM,         0, ContainerType.MECHANIC);
		inputs.add(forceAxial);
		inputs.add(forceRadial);
		inputs.add(rotSpeed);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		torque  = new IOContainer("Torque", Unit.NEWTONMETER, 0, ContainerType.MECHANIC);
		ploss   = new IOContainer("PLoss",  Unit.WATT,        0, ContainerType.THERMAL);
		outputs.add(torque);
		outputs.add(ploss);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader(getModelType(), type);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			bearingX0 = params.getDoubleValue("X0");
		    bearingY0 = params.getDoubleValue("Y0");
			bearingX1 = params.getDoubleValue("X1");
		    bearingY1 = params.getDoubleValue("Y1");
			bearingC0 = params.getDoubleValue("C0"); 
			bearingRm = params.getDoubleValue("MeanDiameter")/2;
			bearingNu = params.getDoubleValue("LubricantViscosity");
			bearingA  = params.getDoubleValue("ContactAngle");
			bearingF0 = params.getDoubleValue("LubricationFactor");
			
			
			// Estimate empirical parameters
			double[] Asamples = { 0,      30,    40,     90      };
			double[] zsamples = { 0.0009,  0.001, 0.0013, 0.0012 };
			double[] ysamples = { 0.55,    0.33,  0.33,   0.33   };
			bearingZ = Algo.linearInterpolation(bearingA, Asamples, zsamples);
			bearingY = Algo.linearInterpolation(bearingA, Asamples, ysamples);
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
		// Check model parameters:
    	if (bearingX0<0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: X0 must be strictly positive!");
		}
    	if (bearingY0<0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: Y0 must be strictly positive!");
		}
    	if (bearingX1<0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: X1 must be strictly positive!");
		}
    	if (bearingY1<0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: Y1 must be strictly positive!");
		}
    	if (bearingRm<=0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: MeanDiameter must be strictly positive!");
		}
    	if (bearingNu<=0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: LubricantViscosity must be strictly positive!");
		}
    	if (bearingA<0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: ContactAngle must be strictly positive!");
		}
    	if (bearingF0<=0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: LubricationFactor must be strictly positive!");
		}
    	
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		double P0, P1;
		double Tv, Tl;
		double f1;
		
		// Equivalent load
		P0 = bearingX0*forceRadial.getValue() + bearingY0*forceAxial.getValue();
		P1 = Math.max( forceAxial.getValue(), 
				       bearingX1*forceRadial.getValue() + bearingY1*forceAxial.getValue() );
		
		/* 
		 * SOURCE: Harris, A., Rolling Bearing Analysis, John Wiley & Sons Inc., p446ff.
		 */
		f1 = bearingZ*Math.pow( (P0/bearingC0), bearingY );
		
		// load torque
		Tl = f1*P1*bearingRm*2 * 1.3558;
		
		// viscous torque
		if (rotSpeed.getValue() == 0)
			Tv = 0;
		else if ( rotSpeed.getValue() * bearingNu * 1e6 < 2000 )
			Tv = 3.492E-3 * bearingF0 * Math.pow(bearingRm*2/.0254, 3) * 1.3558;
		else
			Tv = 1.42E-5 * bearingF0 * Math.pow(bearingRm*2/.0254, 3) * 1.3558 * 
			     Math.pow( Math.abs(rotSpeed.getValue()) * bearingNu * 1E6, 2.0/3);	
		
		/* END OF Harris */
		
		// torque [Nm] = Tv [Nm] + Tl [Nm]
		torque.setValue(Tl+Tv);
		
		// PLoss [W] = | rotSpeed [rpm] * Pi/30 [rad/rpm] * torque [Nm] |
		ploss.setValue( Math.abs( rotSpeed.getValue()*Math.PI/30*torque.getValue() ) );
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
		init();
	}
}
