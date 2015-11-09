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

import ch.ethz.inspire.emod.model.material.Material;
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
 *   C0                 : [N]   : Static load factor.
 *   MeanDiameter       : [m]   : Mean diameter of the bearing 
 *   ContactAngle       : [°]   : Contact angle of the rolling bodies
 *   LubricationFactor  : [-]   : Lubricant factor according to Harris p448
 *   LubricantMaterial  : [-]   : Name of the lubricant material
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
	private IOContainer temperature1;
	private IOContainer temperature2;
	// Output parameters:
	private IOContainer torque;
	private IOContainer ploss;
	private IOContainer heatFlux1;
	private IOContainer heatFlux2;
	
	// Parameters used by the model. 
	double bearingX0, // radial factor (static)
	       bearingY0; // axial  factor (static)
	double bearingX1, // radial factor (dynamic)
	       bearingY1; // axial  factor (dynamic)
	double bearingC0; // static load rating
	double bearingRm; // Mean radius
	double bearingA;  // Contact angle
	double bearingF0; // Empirical lubrication factor
	double bearingZ,  // Empirical model parameters, will be calculated dependent on the angle
	       bearingY;
	int    bearingNumRows;
	int    bearingNumBodies;
	Material lubricant; 
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Bearing() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
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
		forceAxial   = new IOContainer("ForceAxial",   new SiUnit(Unit.NEWTON), 		0,      ContainerType.MECHANIC);
		forceRadial  = new IOContainer("ForceRadial",  new SiUnit(Unit.NEWTON), 		0,      ContainerType.MECHANIC);
		rotSpeed     = new IOContainer("RotSpeed",     new SiUnit(Unit.REVOLUTIONS_S),  0,      ContainerType.MECHANIC);
		temperature1 = new IOContainer("Temperature1", new SiUnit(Unit.KELVIN), 		293.15, ContainerType.THERMAL);
		temperature2 = new IOContainer("Temperature2", new SiUnit(Unit.KELVIN), 		293.15, ContainerType.THERMAL);
		inputs.add(forceAxial);
		inputs.add(forceRadial);
		inputs.add(rotSpeed);
		inputs.add(rotSpeed);
		inputs.add(temperature1);
		inputs.add(temperature2);
		
		/* Define output parameters */
		outputs   = new ArrayList<IOContainer>();
		torque    = new IOContainer("Torque",   new SiUnit(Unit.NEWTONMETER), 0, ContainerType.MECHANIC);
		ploss     = new IOContainer("PLoss",    new SiUnit(Unit.WATT),        0, ContainerType.THERMAL);
		heatFlux1 = new IOContainer("HeatFlux", new SiUnit(Unit.WATT),        0, ContainerType.THERMAL);
		heatFlux2 = new IOContainer("HeatFlux", new SiUnit(Unit.WATT),        0, ContainerType.THERMAL);
		outputs.add(torque);
		outputs.add(ploss);
		outputs.add(heatFlux1);
		outputs.add(heatFlux2);
		
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
			// Load specific parameters
			bearingRm = params.getDoubleValue("MeanDiameter")/2;
			bearingA  = params.getDoubleValue("ContactAngle");
			bearingF0 = params.getDoubleValue("LubricationFactor");
			bearingC0 = params.getDoubleValue("C0");
			bearingNumBodies = params.getIntValue("NumRollingBodies");
			
			lubricant = new Material(params.getString("LubricantMaterial"));

			// Load general Parameters
			setBearingParameters(bearingA);
			
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
	
	private void setBearingParameters( double bearingA){
		
		/* CONFIG */
		double[]   angleSamples = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 90};
		double[]   X0Samples    = {.5, 1};
		double[][] Y0Samples    = { {.6, .52, .5, .46, .42, .38, .33, .29, .26, .22, 0},
				                    {.5, 1.04,  1, .92, .84, .76, .66, .58, .52, .44, 0} }; 
		double[]   zSamples     = {0.0009, 0.0006, 0.0003, 0.0005, 0.0007, 0.0008, 0.001, 0.0012, 0.0013, 0.0013, 0.0012};
		double[]   ySamples     = {0.55, 0.48, 0.4, 0.38, 0.37, 0.35, 0.33, 0.33, 0.33, 0.33, 0.33};
		/* EOC */
		
			//	, X1Samples, Y1Samples, zSamples, ySamples
		
		/* 
		 * Axial and radial factors 
		 * SOURCE: ISO 76:2600
		 * */
		// Case: a=0°
		if(0==bearingA)
			bearingX0 = .6;
		else
			bearingX0 = X0Samples[bearingNumRows];
		bearingY0 = Algo.linearInterpolation(bearingA, angleSamples, Y0Samples[bearingNumRows]);
		
		bearingX1 = 1;
		bearingY1 = 0;
			
	
		/* 
		 * Friction factors
		 * SOURCE: Harris, A., Rolling Bearing Analysis, John Wiley & Sons Inc., p446ff.
		 */
		bearingZ  = Algo.linearInterpolation(bearingA, angleSamples, zSamples);
		bearingY  = Algo.linearInterpolation(bearingA, angleSamples, ySamples);		
		
	}
	
	private double getThermalResistance(double n){
		double vp;
		vp = bearingRm*n*Math.PI/30;
		
		return bearingNumBodies*1/2.4*Math.sqrt(14+2*Math.log(vp)-2*Math.log(bearingRm)*2000)*Math.pow(bearingRm*1000, 2);
		
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
		// Check model parameters:
    	if (bearingRm<=0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: MeanDiameter must be strictly positive!");
		}
    	if (bearingA<0){
			throw new Exception("Bearing, type:" +type+ 
					": Negative or zero value: ContactAngle must be strictly positive!");
		}
    	if (bearingA>90){
			throw new Exception("Bearing, type:" +type+ 
					": Out of bounds: ContactAngle must less than 90°!");
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
		double nu;
		
		/* Viscosity Lubricant */
		nu = lubricant.getViscosityKinematic((temperature2.getValue()+temperature1.getValue())/2);
		
		// Equivalent load
		P0 = bearingX0*forceRadial.getValue() + bearingY0*forceAxial.getValue();
		P1 = Math.max( forceAxial.getValue(), 
				       bearingX1*forceRadial.getValue() + bearingY1*forceAxial.getValue() );
		
		/* 
		 * SOURCE: Harris, A., Rolling Bearing Analysis, John Wiley & Sons Inc., p446ff.
		 */
		f1 = bearingZ*Math.pow( (P0/bearingC0), bearingY );
		
		// load torque
		Tl = f1*P1*bearingRm*2/.0254 * 0.113;
		
		// viscous torque
		if (rotSpeed.getValue() == 0)
			Tv = 0;
		else if ( rotSpeed.getValue() * 60 * nu * 1e6 < 2000 )
			Tv = 3.492E-3 * bearingF0 * Math.pow(bearingRm*2/.0254, 3) * 0.113;
		else
			Tv = 1.42E-5 * bearingF0 * Math.pow(bearingRm*2/.0254, 3) * 0.113 * 
			     Math.pow( Math.abs(rotSpeed.getValue()) * 60 * nu * 1E6, 2.0/3);	
		
		/* END OF Harris */
		
		// torque [Nm] = Tv [Nm] + Tl [Nm]
		torque.setValue(Tl+Tv);
		
		// PLoss [W] = | rotSpeed [rpm] * Pi/30 [rad/rpm] * torque [Nm] |
		ploss.setValue( Math.abs( rotSpeed.getValue()*Math.PI*2*torque.getValue() ) );
		
		// heatFlux1 [W] = lambda [W/K] * (temperature2-temperature1) [K] + PLoss [W] / 2
		heatFlux1.setValue(getThermalResistance(rotSpeed.getValue()) * (temperature2.getValue()-temperature1.getValue()) + ploss.getValue()/2 );
		heatFlux2.setValue(getThermalResistance(rotSpeed.getValue()) * (temperature1.getValue()-temperature2.getValue()) + ploss.getValue()/2 );
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
