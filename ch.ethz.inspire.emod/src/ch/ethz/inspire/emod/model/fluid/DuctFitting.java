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

package ch.ethz.inspire.emod.model.fluid;

import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.utils.ParameterSet;


/**
 * Implements the hydrodynamic properties of a fitting
 * @author sizuest
 *
 */
public class DuctFitting extends ADuctElement{
	
	private AHydraulicProfile p1 = new HPCircular(1), p2 = new HPCircular(1);
	
	/**
	 * Constructor for XmlUnmarshaler 
	 */
	public DuctFitting(){}
	
	/**
	 * constructor by name and connected elements
	 * 
	 * @param name 
	 * @param p1
	 * @param p2
	 */
	public DuctFitting(String name, AHydraulicProfile p1, AHydraulicProfile p2){
		this.name = name;
		this.p1 = p1;
		this.p2 = p2;
		init();
	}
	
	/**
	 * Initializes the element
	 */
	private void init(){
		this.profile = new HPCircular((p1.getDiameter()+p2.getDiameter())/2);
		this.length  = 0; // Dummy
	}
	
	/**
	 * Sets the profiles
	 * 
	 * @param p1 
	 * @param p2 
	 */
	public void setProfiles(AHydraulicProfile p1, AHydraulicProfile p2){
		this.p1 = p1;
		this.p2 = p2;
		init();
	}
	
	/**
	 * @return true if profiles are equal in (hydr. diameter)
	 */
	public boolean hasEqualProfiles(){
		if(p1.getDiameter() == p2.getDiameter())
			return true;
		else
			return false;
	}

	@Override
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {
		return 0;
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure,
			double temperatureFluid) {
		return Fluid.pressureLossFitting(getMaterial(), temperatureFluid, getLength(), p1, p2, flowRate);
	}
	
	@Override
	public ParameterSet getParameterSet() {
		return null;
	}

	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		// Not used
	}

}
