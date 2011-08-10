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
	
	public ConsumerData(String consumerName) {
		consumer = consumerName;
		names = new ArrayList<String>();
		units = new ArrayList<Unit>();
		values = new ArrayList<double[]>();
		active = new ArrayList<Boolean>();
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
	public void addUnit(Unit unit) {
		units.add(unit);
	}
	
	/**
	 * @return the inputValues
	 */
	public List<double[]> getValues() {
		return values;
	}
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
	public void toggleActiveElement(int index) {
		active.set(index, !active.get(index));
	}
	
	public void setActive(int index, boolean value) {
		active.set(index, value);
	}
}
