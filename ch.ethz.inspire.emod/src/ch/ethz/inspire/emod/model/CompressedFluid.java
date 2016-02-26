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
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General compressed fluid class.
 * Implements the physical of a compressed fluid consumer
 * 
 * Assumptions:
 * Ideal gas equation, isentropic expansion
 * 
 * Inputlist:
 *   1: Flow           : [m3/s]   : Demanded flow under ambient conditions
 *   2: TemperatureAmb : [K]      : Ambient temperature
 *   3: PressureAmb    : [Pa]     : Ambient Pressure
 * Outputlist:
 *   1: PTotal         : [W]      : Power demand to generate the flow
 *   2: PUse           : [W]      : Power available in the fluid
 *   3: PLoss          : [W]      : Power loss during the generation
 *   
 * Config parameters:
 *   Density               : [N/mm]   : density of the fluid under ambient conditions
 *   HeatCapacity          : [J/kg/K] : internal heat capacity of the fluid
 *   IsentropicCoefficient : [-]      : isentropic coefficent of the fluid
 *   SupplyPressure        : [Pa]     : pressure of the fluid in the supply
 * 
 * @author sizuest
 *
 */
@XmlRootElement
public class CompressedFluid extends APhysicalComponent{
	
	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer vFlowDot;
	private IOContainer tempAmb;
	private IOContainer pAmb;
	// Output parameters:
	private IOContainer ptotal;
	private IOContainer puse;
	private IOContainer ploss;
	
	// Parameters used by the model. 
	private double rho, 	// [kg/m3]  Fluid density under normal conditions
	               cp,      // [J/K/kg] Heat capacity
	               gamma,   // [-]      Isentropic exponent
	               psupply; // [Pa]     Supply pressure
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public CompressedFluid() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Compressed fluid constructor
	 * 
	 * @param type
	 */
	public CompressedFluid(String type) {
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
		vFlowDot  = new IOContainer("Flow",           new SiUnit(Unit.METERCUBIC_S),    0,      ContainerType.FLUIDDYNAMIC);
		tempAmb   = new IOContainer("TemperatureAmb", new SiUnit(Unit.KELVIN), 0,      ContainerType.THERMAL);
		pAmb      = new IOContainer("PressureAmb",    new SiUnit(Unit.PA),     100000, ContainerType.FLUIDDYNAMIC);
		inputs.add(vFlowDot);
		inputs.add(tempAmb);
		inputs.add(pAmb);
		
		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		ptotal  = new IOContainer("PTotal", new SiUnit(Unit.WATT), 0, ContainerType.ELECTRIC);
		puse    = new IOContainer("PUse",   new SiUnit(Unit.WATT), 0, ContainerType.FLUIDDYNAMIC);
		ploss   = new IOContainer("PLoss",  new SiUnit(Unit.WATT), 0, ContainerType.THERMAL);
		outputs.add(ptotal);
		outputs.add(puse);
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
			rho     = params.getDoubleValue("Density");
			cp      = params.getDoubleValue("HeatCapacity");
			gamma   = params.getDoubleValue("IsentropicCoefficient");
			psupply = params.getDoubleValue("SupplyPressure");
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
		// Parameter must be non negative and non zero
    	if (rho <= 0) {
    		throw new Exception("CompressedFluid, type:" + type +
    				": Non positive value: Density must be non negative and non zero");
    	}
    	if (cp <= 0) {
    		throw new Exception("CompressedFluid, type:" + type +
    				": Non positive value: Heat capacity must be non negative and non zero");
    	}
    	if (gamma <= 0) {
    		throw new Exception("CompressedFluid, type:" + type +
    				": Non positive value: Isentropic coefficient must be non negative and non zero");
    	}
    	if (psupply <= 0) {
    		throw new Exception("CompressedFluid, type:" + type +
    				": Non positive value: Supply pressure must be non negative and non zero");
    	}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		if (pAmb.getValue() == 0) {
			ptotal.setValue(0);
			puse.setValue(0);
			ploss.setValue(0);
			return;
		}
		/* Calculate power required to generate the flow
		 * PTotal [W] = cp [J/Kg/K] * Tamb [K] * [ (psupply [Pa] /pamb [Pa] )^{(kappa-1)/kappa} ] * rho [kg/m3] * Vdot [m³/s] / 1000 [l/m3]
		 */
		ptotal.setValue(cp*tempAmb.getValue()*(Math.pow(psupply/pAmb.getValue(), gamma-1)-1)*rho*vFlowDot.getValue());
		/* Calculate power flow trough fluid
		 * PUse [W] = psupply [Pa]  * Vdot [m³/s]
		 */
		puse.setValue(psupply*vFlowDot.getValue());
		/* Calculate power loss during the generation
		 * PLoss [W] = PTotal-PUse
		 */
		ploss.setValue(ptotal.getValue()-puse.getValue());
	
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
	}
}
