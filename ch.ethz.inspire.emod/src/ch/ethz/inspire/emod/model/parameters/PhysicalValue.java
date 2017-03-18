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

package ch.ethz.inspire.emod.model.parameters;

import ch.ethz.inspire.emod.model.units.SiUnit;

/**
 * @author sizuest
 */
public class PhysicalValue {
	private double[] value;
	private SiUnit unit = new SiUnit();

	/**
	 * 
	 */
	public PhysicalValue() {
	}

	/**
	 * @param value
	 * @param unit
	 */
	public PhysicalValue(double value, SiUnit unit) {
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
	 * 
	 * @return value <T>
	 */
	public double getValue() {
		return getValue(0);
	}

	/**
	 * @param idx
	 * @return value
	 */
	public double getValue(int idx) {
		if (null == this.value)
			return Double.NaN;
		return this.value[idx];
	}

	/**
	 * @return values
	 */
	public double[] getValues() {
		return this.value;
	}
	
	/**
	 * Return the values as formated string
	 * @return string
	 */
	public String valuesToString(){
		if(value.length<1)
			return "";
		
		String ret = value[0]+"";
		
		for(int i=1; i<value.length; i++)
			ret +=", "+value[i];
		
		return ret;
	}

	/**
	 * returns the current unit
	 * 
	 * @return unit {@link SiUnit}
	 */
	public SiUnit getUnit() {
		return unit;
	}

	/**
	 * Sets a new value
	 * 
	 * @param value
	 */
	public void setValue(double value) {
		setValue(0, value);
	}

	/**
	 * Set the value by index
	 * @param idx
	 * @param value
	 */
	public void setValue(int idx, double value) {
		if (null == this.value)
			this.value = new double[idx + 1];
		this.value[idx] = value;
	}

	/**
	 * Set all values
	 * @param value
	 */
	public void setValue(double[] value) {
		this.value = value;
	}

	/**
	 * Returns the value and unit as String
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		if (1 == getValues().length)
			return getValue() + " " + unit.toString();

		String out = "" + getValue(0);
		for (int i = 1; i < getValues().length; i++)
			out += ", " + getValue(i);

		return out + " " + unit.toString();
	}

	/**
	 * @param value
	 * @param unit
	 */
	public void set(double value, String unit) {
		setValue(value);
		this.unit.set(unit);
	}

	/**
	 * Set values and unit
	 * @param value
	 * @param unit
	 */
	public void set(double[] value, String unit) {
		setValue(value);
		this.unit.set(unit);
	}

	/**
	 * Set values and unit
	 * @param value
	 * @param unit
	 */
	public void set(double[] value, SiUnit unit) {
		setValue(value);
		this.unit = unit;
	}

	/**
	 * Multiply a physical value with a scalar
	 * @param a
	 * @param b
	 * @return
	 */
	public static PhysicalValue multiply(PhysicalValue a, double b) {
		PhysicalValue pv = new PhysicalValue();
		double[] value = a.getValues().clone();

		for (int i = 0; i < value.length; i++)
			value[i] = a.getValue(i) * b;

		pv.set(value, a.unit.toString());

		return pv;
	}

	/**
	 * Multiply a physical value with a physical value
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static PhysicalValue multiply(PhysicalValue a, PhysicalValue b)
			throws Exception {
		PhysicalValue pv = new PhysicalValue();

		if (a.getValues().length != b.getValues().length)
			throw new Exception(
					"Physical value: multiply: Arrays do not match!");

		double[] value = a.getValues().clone();

		for (int i = 0; i < value.length; i++)
			value[i] = a.getValue(i) * b.getValue(i);

		pv.set(value, SiUnit.multiply(a.getUnit(), b.getUnit()));

		return pv;
	}

	/**
	 * Divide a physical value by a scalar
	 * @param a
	 * @param b
	 * @return
	 */
	public static PhysicalValue divide(PhysicalValue a, double b) {
		PhysicalValue pv = new PhysicalValue();
		double[] value = a.getValues();

		for (int i = 0; i < value.length; i++)
			value[i] = a.getValue(i) / b;

		pv.set(value, a.unit.toString());

		return pv;
	}

	/**
	 * Divide a physical vlue by a physical value
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static PhysicalValue divide(PhysicalValue a, PhysicalValue b)
			throws Exception {
		PhysicalValue pv = new PhysicalValue();

		if (a.getValues().length != b.getValues().length)
			throw new Exception("Physical value: divide: Arrays do not match!");

		double[] value = a.getValues().clone();

		for (int i = 0; i < value.length; i++)
			value[i] = a.getValue(i) / b.getValue(i);

		pv.set(value, SiUnit.divide(a.getUnit(), b.getUnit()));

		return pv;
	}

	/**
	 * Add two physical values
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static PhysicalValue add(PhysicalValue a, PhysicalValue b)
			throws Exception {
		PhysicalValue pv = new PhysicalValue();

		// Check unit;
		if (!a.unit.equals(b.unit))
			throw new Exception("Physical value: add: Units do not match");

		if (a.getValues().length != b.getValues().length)
			throw new Exception("Physical value: add: Arrays do not match!");

		double[] value = a.getValues().clone();

		for (int i = 0; i < value.length; i++)
			value[i] = a.getValue(i) + b.getValue(i);

		pv.set(value, a.unit.toString());

		return pv;
	}

	/**
	 * Subtract two physical values
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static PhysicalValue subtract(PhysicalValue a, PhysicalValue b)
			throws Exception {
		PhysicalValue pv = new PhysicalValue();

		// Check unit;
		if (!a.unit.equals(b.unit))
			throw new Exception("Physical value: subtract: Units do not match");

		if (a.getValues().length != b.getValues().length)
			throw new Exception(
					"Physical value: subtract: Arrays do not match!");

		double[] value = a.getValues().clone();

		for (int i = 0; i < value.length; i++)
			value[i] = a.getValue(i) - b.getValue(i);

		pv.set(value, a.unit.toString());

		return pv;
	}

	/**
	 * Put a physical value to the power of a scalar
	 * @param v
	 * @param exp
	 * @return
	 */
	public static PhysicalValue pow(PhysicalValue v, double exp) {
		PhysicalValue pv = new PhysicalValue();
		double[] value = v.getValues().clone();

		for (int i = 0; i < value.length; i++)
			value[i] = Math.pow(v.getValue(i), exp);

		pv.set(value, SiUnit.pow(v.getUnit(), exp).toString());

		return pv;
	}

}
