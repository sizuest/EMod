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
package ch.ethz.inspire.emod.utils;

import java.util.ArrayList;

import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.DynamicState;

/**
 * Implements an integration step to numerically solve a ODE of the form
 * 
 *   dm/dt  = mDotIn + mDotOut
 *   dTi/dt = Ai*Ti+Bi
 *   
 *   where
 *   - T = [T1 T2 ... TN]^T
 *   - u = [u1 u2 ... uM]^T
 *   - Ai = Ai(T1, T2, .., xi-1, m, u1, u2, ... uM)
 *   - Bi = Bi(T1, T2, .., xi-1, m, u1, u2, ... uM)
 *   
 *  By calling the function 'integrate', a single step integration using a
 *  Runge-Kutta of 2nd order is performed:
 *  
 *   m[k+1]  = m[k] + ts/2 * (mDotIn[k] + mDotIn[k+1] - mDotOut[k] - mDotOut[k+1])
 *  
 *   Ti[k+1] = (1-ts/2*Ai[k+1])^-1 * ( Ti[k]*ts/2*Ai[k] + ts/2*(Bi[k]+Bi[k+1]) )
 *   
 * 
 * @author sizuest
 *
 */
public abstract class AThermalIntegrator {
	private ArrayList<ShiftProperty<Double>> A, B;
	protected ArrayList<ShiftProperty<Double>> temperature;
	protected ShiftProperty<Double> mDotIn, mDotOut; 
	protected ShiftProperty<Double> pressure;
	protected DynamicState massState, temperatureState, temperatureOutState;
	protected Material material;
	protected int numElements = 1;
	
	/**
	 * Constructor
	 * @param N number of elements
	 */
	public AThermalIntegrator(int N){		
		this.numElements = N;
		init();
	}
	
	
	/**
	 * Constructor
	 */
	public AThermalIntegrator(){
		init();
	}
	
	protected void init(){
		A  = new ArrayList<ShiftProperty<Double>>();
		B  = new ArrayList<ShiftProperty<Double>>();
		
		temperature = new ArrayList<ShiftProperty<Double>>();
		
		mDotIn  = new ShiftProperty<Double>(0.0);
		mDotOut = new ShiftProperty<Double>(0.0);
		
		pressure  = new ShiftProperty<Double>(0.0);
		
		massState           = new DynamicState("Mass", new SiUnit(Unit.KG));
		temperatureState    = new DynamicState("Temperature", new SiUnit(Unit.KELVIN), 293.15);
		temperatureOutState = new DynamicState("TemperatureOut", new SiUnit(Unit.KELVIN), 293.15);
		try {
			temperatureState.setInitialConditionFunction(this.getClass().getMethod("setInitialTemperature", double.class), this);
		} catch (Exception e) {
			System.err.println("Dynamic State: couldn't assosiate init function!");
			e.printStackTrace();
		}
		
		setN(numElements);
	}
	
	/**
	 * Set the number of elements
	 * @param N
	 */
	private void setN(int N){
		this.numElements = N;
		for(int i=0; i<N; i++){		
			A.add(new ShiftProperty<>(0.0));
			B.add(new ShiftProperty<>(0.0));
			temperature.add(new ShiftProperty<>(0.0));
		}
	}
	
	/**
	 * Sets the temperature vector to the initial value
	 * @param temperatureInit 
	 */
	public void setInitialTemperature(double temperatureInit){
		for(int i=0;i<this.numElements;i++) {
			// Update twice, so that current and last are set to init. value
			this.temperature.get(i).update(temperatureInit);
			this.temperature.get(i).update(temperatureInit);
		}
		
		temperatureOutState.setInitialCondition(temperatureInit);
	}
	
	/**
	 * Set the material
	 * @param material {@link Material.java}
	 */
	public void setMaterial(Material material){
		this.material = material;
	}
	
	/**
	 * Set the material by name
	 * @param materialName
	 */
	public void setMaterial(String materialName){
		if(null==material)
			this.material = new Material(materialName);
		else
			this.material.setMaterial(materialName);
	}
	
	/**
	 * Get the material object
	 * @return {@link Material.java}
	 */
	public Material getMaterial(){
		return this.material;
	}
	
	/**
	 * Temperature of the element
	 * @return {@link DynamicState.java}
	 */
	public DynamicState getTemperature(){
		return temperatureState;
	}
	
	/**
	 * Temperature of the last element
	 * @return {@link DynamicState.java}
	 */
	public DynamicState getTemperatureOut(){
		return temperatureOutState;
	}
	
	/**
	 * Mass in the element
	 * @return {@link DynamicState.java}
	 */
	public DynamicState getMass(){
		return massState;
	}
	
	/**
	 * Used for the update of Ai. Is called before each iteration step.
	 * Must return the new Ai at the current time step
	 * @param i 
	 * @return A
	 */
	public abstract double getA(int i);
	
	/**
	 * Used for the update of Bi. Is called before each iteration step.
	 * Must return the new Bi at the current time step
	 * @param i 
	 * @return B
	 */
	public abstract double getB(int i);
	
	/**
	 * Performs a single integration step of length 'timestep'
	 * @param timestep [s]
	 * @param flowRateIn [m3/s]
	 * @param flowRateOut [m3/s]
	 * @param p [Pa]
	 */
	public final void integrate(double timestep, double flowRateIn, double flowRateOut, double p ){
		
		/* Fluid properties */ 
		double rho  = material.getDensity(temperatureState.getValue(), p);
		
		mDotIn.update(flowRateIn*rho);
		mDotOut.update(flowRateOut*rho);
		pressure.update(p);
	
		/*
		 * Perform one integration step for mass:
		 * 
		 *  m[k+1] = m[k] + Ts/2*(mDotIn[k]+mDotIn[k+1]-mDotOut[k]-mDotOut[k+1])
		 */
		ShiftProperty<Double> mDotIn, mDotOut;
		mDotIn  = getMassFlowIn();
		mDotOut = getMassFlowOut();
		
		massState.setValue(massState.getValue() + timestep/2*(mDotIn.getCurrent()+mDotIn.getLast()-mDotOut.getCurrent()-mDotOut.getLast()));
		
		/*
		 * Perform one integration step for temperature:
		 * 
		 *  T[k+1] = (1-A[k+1])^-1 * (T[k]*(1+A[k]) + Ts/2 * (B[k]+B[k+1]))
		 */
		for(int i=0; i<numElements; i++){
			// update Ai and Bi */
			A.get(i).update(getA(i));
			B.get(i).update(getB(i));
			// Integration
			this.temperature.get(i).update(( ( this.temperature.get(i).getCurrent()*(1+timestep/2*A.get(i).getLast()) + timestep/2*(B.get(i).getCurrent()+B.get(i).getLast()) ) / (1-timestep/2*A.get(i).getCurrent()) ));
		}

		/* Bulk temperature as system state */
		temperatureState.setValue(getTemperatureBulk());
		temperatureOutState.setValue(temperature.get(temperature.size()-1).getCurrent());
	}
	
	protected ShiftProperty<Double> getMassFlowIn(){
		return mDotIn;
	}
	
	protected ShiftProperty<Double> getMassFlowOut(){
		return mDotOut;
	}
	
	/**
	 * Performs a single integration step of length 'timestep' without mass flows
	 * @param timestep
	 */
	public final void integrate(double timestep){
		integrate(timestep, 0, 0, 100000);
	}
	
	/**
	 * Bulk temperature
	 * @return bulk temperature
	 */
	protected double getTemperatureBulk(){
		double m = 0; // Mass
		double H = 0; // Enthalpy
		double cp = material.getHeatCapacity();
		
		for(int i=0;i<numElements;i++){			
			m += massState.getValue()/numElements;
			H += massState.getValue()/numElements*cp*temperature.get(i).getCurrent();
		}

		return H/m/cp;
	}
	
	
}
