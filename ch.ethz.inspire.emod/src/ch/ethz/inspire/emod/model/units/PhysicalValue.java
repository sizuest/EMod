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
 */
public class PhysicalValue {
	private double[] value;
	private SiUnit unit = new SiUnit();
	
	/**
	 * 
	 */
	public PhysicalValue(){}
	
	/**
	 * @param value
	 * @param unit
	 */
	public PhysicalValue(double value, SiUnit unit){
		this.unit = unit;
		this.value = new double[1];
		this.value[0] = value;
	}
	
	/**
	 * @param value
	 * @param unit
	 */
	public PhysicalValue(double[] value, SiUnit unit) {
		this.unit = unit;
		this.value = value;
	}

	/**
	 * Returns the current value
	 * @return value <T>
	 */
	public double getValue(){
		return getValue(0);
	}
	
	/**
	 * @param idx
	 * @return value
	 */
	public double getValue(int idx){
		if(null==this.value)
			return Double.NaN;
		return this.value[idx];
	}
	
	/**
	 * @return values
	 */
	public double[] getValues(){
		return this.value;
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
		setValue(0, value);
	}
	
	public void setValue(int idx, double value){
		if(null==this.value)
			this.value = new double[idx+1];
		this.value[idx] = value;
	}
	
	public void setValue(double[] value){
		this.value = value;
	}
	
	/**
	 * Returns the value and unit as String
	 * @return String
	 */
	public String toString(){
		if(1==getValues().length)
			return getValue()+" "+unit.toString();
		
		String out=""+getValue(0);
		for(int i=0; i<getValues().length; i++)
			out +=", "+getValue(i);
		
		return out+" "+unit.toString();
	}
	
	/**
	 * @param value
	 * @param unit
	 */
	public void set(double value, String unit){
		setValue(value);
		this.unit.set(unit);
	}
	
	public void set(double[] value, String unit){
		setValue(value);
		this.unit.set(unit);
	}
	
	public void set(double[] value, SiUnit unit){
		setValue(value);
		this.unit= unit;
	}
	
	public static PhysicalValue multiply(PhysicalValue a, double b){
		PhysicalValue pv = new PhysicalValue();
		double[] value = a.getValues().clone();

		for(int i=0; i<value.length; i++)
			value[i] = a.getValue(i)*b;

		pv.set(value, a.unit.toString());
		
		return pv;
	}
	
	
	public static PhysicalValue multiply(PhysicalValue a, PhysicalValue b) throws Exception{
		PhysicalValue pv = new PhysicalValue();
		
		if(a.getValues().length!=b.getValues().length)
			throw new Exception("Physical value: multiply: Arrays do not match!");
		
		double[] value = a.getValues().clone();

		for(int i=0; i<value.length; i++)
			value[i] = a.getValue(i)*b.getValue(i);

		pv.set(value, SiUnit.multiply(a.getUnit(), b.getUnit()));
		
		return pv;
	}
	
	public static PhysicalValue divide(PhysicalValue a, double b){
		PhysicalValue pv = new PhysicalValue();
		double[] value = a.getValues();

		for(int i=0; i<value.length; i++)
			value[i] = a.getValue(i)/b;

		pv.set(value, a.unit.toString());
		
		return pv;
	}
	
	public static PhysicalValue divide(PhysicalValue a, PhysicalValue b) throws Exception{
		PhysicalValue pv = new PhysicalValue();

		if(a.getValues().length!=b.getValues().length)
			throw new Exception("Physical value: divide: Arrays do not match!");
		
		double[] value = a.getValues().clone();

		for(int i=0; i<value.length; i++)
			value[i] = a.getValue(i)/b.getValue(i);

		pv.set(value, SiUnit.divide(a.getUnit(), b.getUnit()));
		
		return pv;
	}
	
	public static PhysicalValue add(PhysicalValue a, PhysicalValue b) throws Exception {
		PhysicalValue pv = new PhysicalValue();
		
		// Check unit;
		if(!a.unit.equals(b.unit))
			throw new Exception("Physical value: add: Units do not match");
		
		if(a.getValues().length!=b.getValues().length)
			throw new Exception("Physical value: add: Arrays do not match!");
		
		double[] value = a.getValues().clone();

		for(int i=0; i<value.length; i++)
			value[i] = a.getValue(i)+b.getValue(i);

		pv.set(value, a.unit.toString());
		
		return pv;
	}
	
	public static PhysicalValue subtract(PhysicalValue a, PhysicalValue b) throws Exception {
		PhysicalValue pv = new PhysicalValue();
		
		// Check unit;
		if(!a.unit.equals(b.unit))
			throw new Exception("Physical value: subtract: Units do not match");
		
		if(a.getValues().length!=b.getValues().length)
			throw new Exception("Physical value: subtract: Arrays do not match!");
		
		double[] value = a.getValues().clone();

		for(int i=0; i<value.length; i++)
			value[i] = a.getValue(i)-b.getValue(i);

		pv.set(value, a.unit.toString());
		
		return pv;
	}
	
	public static PhysicalValue pow(PhysicalValue v, double exp){
		PhysicalValue pv = new PhysicalValue();
		double[] value = v.getValues().clone();
		
		for(int i=0; i<value.length; i++)
			value[i] = Math.pow(v.getValue(i),exp);

		pv.set(value, SiUnit.pow(v.getUnit(), exp).toString());
		
		return pv;
	}
	

}
