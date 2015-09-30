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

package ch.ethz.inspire.emod.model.units;



/**
 * @author sizuest
 *
 * @param <T> Object type of the value
 */
public class PhysicalValue {
	private double value;
	private SiUnit unit = new SiUnit();
	
	/**
	 * Returns the current value
	 * @return value <T>
	 */
	public double getValue(){
		return value;
	}
	
	/**
	 * returns the current unit
	 * @return unit {@link SiUnit.java}
	 */
	public SiUnit getUnit(){
		return unit;
	}
	
	/**
	 * Sets a new value
	 * @param value
	 */
	public void setValue(double value){
		this.value = value;
	}
	
	/**
	 * Returns the value and unit as String
	 * @return String
	 */
	public String toString(){
		return value+" "+unit.toString();
	}
	
	/**
	 * @param value
	 * @param unit
	 */
	public void set(double value, String unit){
		this.value = value;
		this.unit.set(unit);
	}
	
	public static PhysicalValue multiply(PhysicalValue a, double b){
		PhysicalValue pv = new PhysicalValue();
		
		// New value
		pv.value = a.value*b;
		// New unit
		pv.unit  = a.unit;
		
		return pv;
	}
	
	
	public static PhysicalValue multiply(PhysicalValue a, PhysicalValue b){
		PhysicalValue pv = new PhysicalValue();
		
		// New value
		pv.value = a.value*b.value;
		// New unit
		pv.unit  = SiUnit.multiply(a.unit, b.unit);
		
		return pv;
	}
	
	public static PhysicalValue divide(PhysicalValue a, double b){
		PhysicalValue pv = new PhysicalValue();
		
		// New value
		pv.value = a.value/b;
		// New unit
		pv.unit  = a.unit;
		
		return pv;
	}
	
	public static PhysicalValue divide(PhysicalValue a, PhysicalValue b){
		PhysicalValue pv = new PhysicalValue();
		
		// New value
		pv.value = a.value/b.value;
		// New unit
		pv.unit  = SiUnit.divide(a.unit, b.unit);
		
		return pv;
	}
	
	public static PhysicalValue add(PhysicalValue a, PhysicalValue b) throws Exception {
		PhysicalValue pv = new PhysicalValue();
		
		// Check unit;
		if(!a.unit.equals(b.unit))
			throw new Exception("Physical value: add: Units do not match");
		
		// New value
		pv.value = a.value+b.value;
		// New unit
		pv.unit  = a.unit;
		
		return pv;
	}
	
	public static PhysicalValue subtract(PhysicalValue a, PhysicalValue b) throws Exception {
		PhysicalValue pv = new PhysicalValue();
		
		// Check unit;
		if(!a.unit.equals(b.unit))
			throw new Exception("Physical value: subtract: Units do not match");
		
		// New value
		pv.value = a.value-b.value;
		// New unit
		pv.unit  = a.unit;
		
		return pv;
	}
	
	public static PhysicalValue pow(PhysicalValue v, double exp){
		PhysicalValue pv = new PhysicalValue();
		
		// New value
		pv.value = Math.pow(v.value, exp);
		// New unit
		pv.unit  = SiUnit.pow(v.unit, exp);
		
		return pv;
	}
	

}
