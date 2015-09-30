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


import ch.ethz.inspire.emod.model.units.SiUnit;

/**
 * @author sizuest
 *
 * @param <T> Object type of the value
 */
public class PhysicalValue<T> {
	T value;
	SiUnit unit = new SiUnit();
	
	
	public String toString(){
		return value+" "+unit.toString();
	}
	
	/**
	 * @param value
	 * @param unit
	 */
	public void set(T value, String unit){
		this.value = value;
		this.unit.set(unit);
	}
	
	public static PhysicalValue<Double> multiply(PhysicalValue<Double> a, double b){
		PhysicalValue<Double> pv = new PhysicalValue<Double>();
		
		// New value
		pv.value = a.value*b;
		// New unit
		pv.unit  = a.unit;
		
		return pv;
	}
	
	
	public static PhysicalValue<Double> multiply(PhysicalValue<Double> a, PhysicalValue<Double> b){
		PhysicalValue<Double> pv = new PhysicalValue<Double>();
		
		// New value
		pv.value = a.value*b.value;
		// New unit
		pv.unit  = SiUnit.multiply(a.unit, b.unit);
		
		return pv;
	}
	
	public static PhysicalValue<Double> divide(PhysicalValue<Double> a, double b){
		PhysicalValue<Double> pv = new PhysicalValue<Double>();
		
		// New value
		pv.value = a.value/b;
		// New unit
		pv.unit  = a.unit;
		
		return pv;
	}
	
	public static PhysicalValue<Double> divide(PhysicalValue<Double> a, PhysicalValue<Double> b){
		PhysicalValue<Double> pv = new PhysicalValue<Double>();
		
		// New value
		pv.value = a.value/b.value;
		// New unit
		pv.unit  = SiUnit.divide(a.unit, b.unit);
		
		return pv;
	}
	
	public static PhysicalValue<Double> add(PhysicalValue<Double> a, PhysicalValue<Double> b) throws Exception {
		PhysicalValue<Double> pv = new PhysicalValue<Double>();
		
		// Check unit;
		if(!a.unit.equals(b.unit))
			throw new Exception("Physical value: add: Units do not match");
		
		// New value
		pv.value = a.value+b.value;
		// New unit
		pv.unit  = a.unit;
		
		return pv;
	}
	
	public static PhysicalValue<Double> subtract(PhysicalValue<Double> a, PhysicalValue<Double> b) throws Exception {
		PhysicalValue<Double> pv = new PhysicalValue<Double>();
		
		// Check unit;
		if(!a.unit.equals(b.unit))
			throw new Exception("Physical value: subtract: Units do not match");
		
		// New value
		pv.value = a.value-b.value;
		// New unit
		pv.unit  = a.unit;
		
		return pv;
	}
	
	public static PhysicalValue<Double> pow(PhysicalValue<Double> v, double exp){
		PhysicalValue<Double> pv = new PhysicalValue<Double>();
		
		// New value
		pv.value = Math.pow(v.value, exp);
		// New unit
		pv.unit  = SiUnit.pow(v.unit, exp);
		
		return pv;
	}
	

}
