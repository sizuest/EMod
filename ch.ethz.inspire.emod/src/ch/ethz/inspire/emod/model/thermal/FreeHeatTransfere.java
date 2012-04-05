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

package ch.ethz.inspire.emod.model.thermal;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;


import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.PropertiesHandler;
import ch.ethz.inspire.emod.model.APhysicalComponent;

/**
 * General free heat transfere class
 * 
 * Calculates the heat flow trough a wall with different layers
 * and convection on both sides. The temperature gradient is
 * given by the inputs
 * 
 * Assumptions:
 *   Conduction and convection are dominant, where radiation
 *   is negligible. The convection/conduction constants are
 *   no dependent on temperature
 * 
 * Inputlist:
 *   1: Temperature1      : [K]    : Temperature level 1
 *   2: Temperature2      : [K]    : Temperature level 2
 *   
 * Outputlist:
 *   1: PThermal12        : [K]    : Thermal heat flow from 1 to 2
 *   2: PThermal21        : [K]    : Thermal heat flow from 2 to 1
 *   
 * Config parameters:
 *   Surface             : [m^2]     : Surface between level 1 and 2
 *   ConvectionConstants : [W/K/m^2] : Heat transfer constants for convection on side 1 and 2
 *   ConductionConstants : [W/K/m]   : Heat transfer constants for conduction
 *   WallThicknesses     : [m]       : Thicknesses of the wall layers  
 *   
 * 
 * @author simon
 *
 */

public class FreeHeatTransfere extends APhysicalComponent{
	@XmlElement
	protected String type;
	@XmlElement
	protected String parentType;
	
	// Input Lists
	private IOContainer temp1;
	private IOContainer temp2;
	// Output parameters:
	private IOContainer pth12;
	private IOContainer pth21;
	
	// Unit of the element 
	private double surf;
	private double[] alpha;
	private double[] lambda;
	private double[] dWall;
	
	// Heat transfere resistance
	private double thRessistance;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public FreeHeatTransfere() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Free heat transfere constructor
	 * 
	 * @param type
	 * @param parentType
	 */
	public FreeHeatTransfere(String type, String parentType) {
		super();
		
		this.type       = type;
		this.parentType = parentType;
		
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{		
		/* Define Input parameters */
		inputs   = new ArrayList<IOContainer>();
		temp1    = new IOContainer("Temperature1", Unit.KELVIN, 273);
		temp2    = new IOContainer("Temperature2", Unit.KELVIN, 273);
		inputs.add(temp1);
		inputs.add(temp2);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		pth12   = new IOContainer("PThermal12", Unit.WATT, 0);
		pth21   = new IOContainer("PThermal21", Unit.WATT, 0);
		outputs.add(pth12);
		outputs.add(pth21);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* If no parent model file is configured, the local configuration file
		 * will be opened. Otherwise the cfg file of the parent will be opened
		 */		
		if (parentType.isEmpty()) {
			String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix")+
							"/"+PropertiesHandler.getProperty("sim.MachineName")+"/"+Defines.MACHINECONFIGDIR+"/"+
							PropertiesHandler.getProperty("sim.MachineConfigName")+
							"/"+this.getClass().getSimpleName()+"_"+type+".xml";
			try {
				params = new ComponentConfigReader(path);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		else {
		
			/* Open file containing the parameters of the parent model type */
			try {
				params = new ComponentConfigReader(parentType, type);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		/* Read the config parameter: */
		try {
			surf            = params.getDoubleValue("thermal.Surface");
			alpha           = params.getDoubleArray("thermal.ConvectionConstants");
			lambda          = params.getDoubleArray("thermal.ConductionConstants");
			dWall           = params.getDoubleArray("thermal.WallThicknesses");
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
		
		/* Calculate thermal ressistance k, with cases:
		 * - lambda=alpha=0
		 *   k = 0
		 * - alpha=0
		 *   k=lambda/d;
		 * - lambda=0
		 *   k=alpha;
		 * - else
		 *   k = (1/alpha+d/lambda)^-1
		 */
		thRessistance = 0;
		
		for (int i=0; i<alpha.length; i++){
			if (0<alpha[i])
				thRessistance += 1/alpha[i];
		}
		
		for (int i=0; i<lambda.length; i++){
			if (0<lambda[i])
				thRessistance += dWall[i]/lambda[i];
		}
		
		if (0!=thRessistance)
			thRessistance = 1/thRessistance;
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
    	// Check model parameters:
    	
    	if (surf < 0) {
    		throw new Exception("FreeHeatTransfere, type:" + type +
    				": Negative value: Surface must be non negative");
    	}
    	
    	for (int i=0; i<alpha.length; i++)
	    	if (alpha[i] < 0) {
	    		throw new Exception("FreeHeatTransfere, type:" + type +
	    				": Negative value: ConvectionConstant must be non negative");
	    	}
    	
    	if (lambda.length != dWall.length) {
    		throw new Exception("FreeHeatTransfere, type:" + type +
						": Array size: ConductionConstant and WallThicknesses must have same size");
    	}
    	for (int i=0; i<lambda.length; i++)
	    	if (lambda[i] < 0) {
	    		throw new Exception("FreeHeatTransfere, type:" + type +
	    				": Negative value: ConductionConstant must be non negative");
	    	}
    	for (int i=0; i<dWall.length; i++)
	    	if (dWall[i] <= 0 && lambda[i] != 0) {
	    		throw new Exception("FreeHeatTransfere, type:" + type +
	    				": Negative value: WallThickness must be positive");
	    	}    		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	@Override
	 */
	public void update() {
		
		/* The heat transfere from 1 to 2 is
		 * Qdot12 [W] = k [W/m²] * A [m²] * (T_2 - T_1)
		 */
		
		pth12.setValue( thRessistance * surf * (temp1.getValue()-temp2.getValue()));
		pth21.setValue(-pth12.getValue());
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
}
