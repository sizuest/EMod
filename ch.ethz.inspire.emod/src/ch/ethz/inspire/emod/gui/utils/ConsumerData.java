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
import java.util.HashMap;
import java.util.List;

import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.simulation.MachineState;

/**
 * holds data read from simulation output file.
 * 
 * @author dhampl
 * 
 */
public class ConsumerData {

	private String consumer;
	private List<String> names; // i/o names
	private List<SiUnit> units;
	private List<double[]> values; // the values
	private List<Boolean> active;  // which values are plotted
	private List<Double> energy;   // integrated energy. use only for io
								   // components with watt as unit
	
	private List<Double> average;
	
	private double averagePower;
	private double peakPower;
	private double variancePower;
	private double[] pTotal = null;
	
	private double[] time;
	private MachineState[] states;
	
	private HashMap<MachineState, Double[]> stateSpecificValue = null;

	/**
	 * 
	 * @param consumerName
	 * @param time 
	 * @param states 
	 */
	public ConsumerData(String consumerName, double[] time, MachineState[] states) {
		consumer = consumerName;
		names = new ArrayList<String>();
		units = new ArrayList<SiUnit>();
		values = new ArrayList<double[]>();
		active = new ArrayList<Boolean>();
		energy = new ArrayList<Double>();
		average = new ArrayList<Double>();
		
		this.time = time;
		this.states = states;
	}

	/**
	 * integrates the power values for every value sample set
	 */
	public void calculateEnergy() {
		
		for (double[] vals : values) {
			double res = 0;
			double lastval = 0;
			for (int i=1; i<vals.length; i++) {
				double valdiff = vals[i] - vals[i-1];
				double timediff = time[i] - time[i-1];
				res += timediff * (lastval + valdiff * 0.5);
			}
			energy.add(res);
		}
	}

	/**
	 * calculate variance, peak and average power.
	 */
	public void calculate() {
		int ptotal = -1;
		double peak = 0, avg = 0;
		int length = 0;
		
		for (int i = 0; i < values.size(); i++) {
			if (names.get(i).equals("PTotal") || names.get(i).equals("Pel"))
				ptotal = i;
		}
		if (ptotal != -1)
			pTotal = values.get(ptotal);
		else {
			/*
			 * No power consumption available set values to zero and return
			 * 
			 * @author sizuest
			 */
			pTotal = new double[values.get(0).length];
			variancePower = Double.NaN;
			averagePower = Double.NaN;
			peakPower = Double.NaN;
			return;
		}

		for (int j = 0; j < values.get(ptotal).length; j++) {
			if (pTotal[j] > peak)
				peak = pTotal[j];
			
			if(!Double.isNaN(pTotal[j])){
				avg += pTotal[j];
				length++;
			}
		}
		// calc the average power consumption
		avg = avg / length;

		// calc the varicance (s^2=sum(x_i - avg)^2 / n-1)
		variancePower = 0;
		for (int k = 0; k < pTotal.length; k++) {
			if(!Double.isNaN(pTotal[k]))
				variancePower += (pTotal[k] - avg) * (pTotal[k] - avg);
		}
		variancePower = variancePower / (length-1);
		averagePower = avg;
		peakPower = peak;
	}
	
	/**
	 * Calculate the average values per state
	 */
	public void calculateStateAverage(){
		int[] stateCount = new int[MachineState.values().length];
		double[][] values = new double[MachineState.values().length][this.values.size()];
		
		// Init everything
		for(int i=0; i<values.length; i++){
			stateCount[i] = 0;
			for(int j=0; j<this.values.size(); j++)
				values[i][j] = 0;
		}
		
		for(int i=0; i<states.length; i++){
			if(states[i] == null)
				continue;
			
			int idx = states[i].ordinal();
			stateCount[idx] ++;
			for(int j=0; j<this.values.size(); j++)
				if(!Double.isNaN(this.values.get(j)[i]))
					values[idx][j] += this.values.get(j)[i];
		}

		// Write to hash table
		stateSpecificValue = new HashMap<MachineState, Double[]>();
		for(int i=0; i<values.length; i++){
			Double[] tmp = new Double[this.values.size()];
			
			for(int j=0; j<this.values.size(); j++)
				tmp[j] = values[i][j]/stateCount[i];
			
			for(int j=0; j<this.values.size(); j++)
			stateSpecificValue.put(MachineState.values()[i], tmp);
		}
		
		for(int j=0; j<this.values.size(); j++){
			double tmp = 0;
			int count = 0;
			for(int i=0; i<values.length; i++){
				tmp += values[i][j];
				count += stateCount[i];
			}
			average.add(tmp/count);
		}
		
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
	public List<SiUnit> getUnits() {
		return units;
	}

	/**
	 * append a unit to the units list
	 * 
	 * @param unit
	 */
	public void addUnit(SiUnit unit) {
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

	/**
	 * Returns whether the component is active (to be displayed) or not
	 * @return
	 */
	public List<Boolean> getActive() {
		return active;
	}

	/**
	 * toggles the active flag for a input or output
	 * 
	 * @param name
	 *            the in- or output's name
	 */
	public void toggleActive(String name) {
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).equals(name)) {
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
	 * 
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

	/**
	 * Returns the average power of the consumer
	 * @return
	 */
	public double getAveragePower() {
		return averagePower;
	}

	/**
	 * Returns the Peak power of the component
	 * @return
	 */
	public double getPeakPower() {
		return peakPower;
	}

	/** 
	 * Returns the variance in the consumers power 
	 * @return
	 */
	public double getVariancePower() {
		return variancePower;
	}
	
	/**
	 * Returns the hash map with the state specific values
	 * @return
	 */
	public HashMap<MachineState, Double[]> getStateMap(){
		return stateSpecificValue;
	}
	
	/**
	 * Returns a list with the average values
	 * @return
	 */
	public List<Double> getAverage(){
		return average;
	}

	/**
	 * Returns the total powers
	 * @return
	 */
	public double[] getPTotal() {
		return pTotal;
	}

	/**
	 * @return
	 */
	public double[] getTime() {
		return time;
	}
}
