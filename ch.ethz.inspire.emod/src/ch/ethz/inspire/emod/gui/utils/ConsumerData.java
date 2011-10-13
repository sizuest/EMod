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
package ch.ethz.inspire.emod.gui.utils;


import java.util.ArrayList;
import java.util.List;

import ch.ethz.inspire.emod.model.units.Unit;

/**
 * holds data read from simulation output file. 
 * 
 * @author dhampl
 *
 */
public class ConsumerData {

	private String consumer;
	private List<String> names; // i/o names
	private List<Unit> units; 
	private List<double[]> values; //the values
	private List<Boolean> active; //which values are plotted
	private List<Double> energy; //integrated energy. use only for io components with watt as unit
	
	/**
	 * 
	 * @param consumerName
	 */
	public ConsumerData(String consumerName) {
		consumer = consumerName;
		names = new ArrayList<String>();
		units = new ArrayList<Unit>();
		values = new ArrayList<double[]>();
		active = new ArrayList<Boolean>();
		energy = new ArrayList<Double>();
	}
	
	/**
	 * integrates the power values for every value sample set
	 */
	public void calculateEnergy() {
		for(double[] vals : values) {
			double res=0;
			double lastval=0;
			for(double sample:vals) {
				double valdiff=sample-lastval;
				res+=0.2*(lastval+valdiff*0.5);
				lastval=sample;
			}
			energy.add(res);
		}
	}
	
	public void calculate() {
		int pmech = 0, ploss = 0;
		double peak = 0, avg = 0;
		for(int i=0;i<values.size();i++) {
			if(names.get(i).equals("Pmech"))
				pmech=i;
			if(names.get(i).equals("Ploss"))
				ploss=i;
		}
		if(pmech == ploss && ploss == 0)
			return;
		double[] pTotal=new double[values.get(ploss).length];
		for(int j=0;j<values.get(ploss).length;j++) {
			// calc the total power consumption
			pTotal[j]=values.get(ploss)[j]+values.get(pmech)[j];
			if(pTotal[j]>peak)
				peak=pTotal[j];
			avg+=pTotal[j];
		}
		// calc the average power consumption
		avg = avg/pTotal.length;
		
		// calc the varicance (s^2=sum(x_i - avg)^2 / n-1)
		double variance=0;
		for(int k=0;k<pTotal.length;k++) {
			variance += (pTotal[k]-avg)*(pTotal[k]-avg);
		}
		variance = variance / (pTotal.length-1);
	}
	
	/**
	 * @return the consumer
	 */
	public String getConsumer() {
		return consumer;
	}
	/**
	 * @return the inputs
	 */
	public List<String> getNames() {
		return names;
	}
	
	/**
	 * appends a name to the names list and sets the new entry to inactive
	 * 
	 * @param name
	 */
	public void addName(String name) {
		names.add(name);
		active.add(false);
	}
	
	/**
	 * @return the inputUnits
	 */
	public List<Unit> getUnits() {
		return units;
	}
	
	/**
	 * append a unit to the units list
	 * 
	 * @param unit
	 */
	public void addUnit(Unit unit) {
		units.add(unit);
	}
	
	/**
	 * @return the inputValues
	 */
	public List<double[]> getValues() {
		return values;
	}
	
	/**
	 * append a value array to the values list
	 * 
	 * @param values
	 */
	public void addInputValues(double[] values) {
		this.values.add(values);
	}
	
	public List<Boolean> getActive() {
		return active;
	}

	/**
	 * toggles the active flag for a input or output
	 * @param name the in- or output's name
	 */
	public void toggleActive(String name) {
		for(int i=0;i<names.size();i++){
			if(names.get(i).equals(name)) {
				toggleActiveElement(i);
			}
		}
	}
	
	/**
	 * toggles the active flag for the element at index
	 * 
	 * @param index
	 */
	public void toggleActiveElement(int index) {
		active.set(index, !active.get(index));
	}
	
	/**
	 * sets the active flag for element at index to provided value
	 * @param index
	 * @param value
	 */
	public void setActive(int index, boolean value) {
		active.set(index, value);
	}
	
	/**
	 * returns the energy values list
	 * 
	 * @return
	 */
	public List<Double> getEnergy() {
		return energy;
	}
}
