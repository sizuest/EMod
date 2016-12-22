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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * SI unit class
 * 
 * @author sizuest
 * 
 */
@XmlRootElement
public class SiUnit {
	// Exponents of the dimensions
	private double L = 0, // length
			M = 0, // mass
			T = 0, // time
			I = 0, // current
			theta = 0, // temperature
			N = 0, // quantity
			J = 0; // light intensity
	@XmlElement
	private String unitText = "";

	/**
	 * Constructor for unmarshaller
	 */
	public SiUnit() {
	}

	/**
	 * @param u
	 */
	public SiUnit(Unit u) {
		SiUnit su = SiUnitDefinition.getUpdateMap().get(u);
		this.set(su.get());
	}

	/**
	 * @param s
	 */
	public SiUnit(String s) {
		this.set(s);
	}

	/**
	 * @param e
	 */
	public SiUnit(double[] e) {
		this.set(e);
	}

	/**
	 * called after the unmarshaller
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		this.set(this.unitText);
	}

	/**
	 * @param L
	 * @param M
	 * @param T
	 * @param I
	 * @param theta
	 * @param N
	 * @param J
	 */
	public SiUnit(double L, double M, double T, double I, double theta,
			double N, double J) {
		this.set(L, M, T, I, theta, N, J);
	}

	/**
	 * Sets the unit according the the exponents of the seven basic dimensions
	 * 
	 * @param L
	 * @param M
	 * @param T
	 * @param I
	 * @param theta
	 * @param N
	 * @param J
	 */
	public void set(double L, double M, double T, double I, double theta,
			double N, double J) {
		this.L = L;
		this.M = M;
		this.T = T;
		this.I = I;
		this.theta = theta;
		this.N = N;
		this.J = J;

		this.unitText = toString();
	}

	/**
	 * Sets the unit according to the stated string REQUIRED FORMAT: x^i y^-j,
	 * where x and y are the units of the basic dimensions and i and j are the
	 * exponents. Example: kg m^-2
	 * 
	 * @param s
	 */
	public void set(String s) {
		this.set(SiUnitDefinition.convertToBaseUnit(s).get());
	}

	/**
	 * Sets the unit according to the exponents in array u
	 * 
	 * @param u
	 */
	public void set(double[] u) {
		this.L = u[0];
		this.M = u[1];
		this.T = u[2];
		this.I = u[3];
		this.theta = u[4];
		this.N = u[5];
		this.J = u[6];
		;

		this.unitText = toString();
	}

	/**
	 * Returns the exponents as an array
	 * 
	 * @return array of exponents
	 */
	public double[] get() {
		double[] u = { this.L, this.M, this.T, this.I, this.theta, this.N,
				this.J };
		return u;
	}

	/**
	 * @param a
	 *            First operand
	 * @param b
	 *            Second operand
	 * @return Multiplication of the units
	 */
	public static SiUnit multiply(SiUnit a, SiUnit b) {
		SiUnit su = new SiUnit();
		su.L = a.L + b.L;
		su.M = a.M + b.M;
		su.T = a.T + b.T;
		su.I = a.I + b.I;
		su.theta = a.theta + b.theta;
		su.N = a.N + b.N;
		su.J = a.J + b.J;
		return su;
	}

	/**
	 * @param a
	 *            Nominator
	 * @param b
	 *            Denominator
	 * @return Division of the units
	 */
	public static SiUnit divide(SiUnit a, SiUnit b) {
		SiUnit su = new SiUnit();
		su.L = a.L - b.L;
		su.M = a.M - b.M;
		su.T = a.T - b.T;
		su.I = a.I - b.I;
		su.theta = a.theta - b.theta;
		su.N = a.N - b.N;
		su.J = a.J - b.J;
		return su;
	}

	/**
	 * @param a basis
	 * @param e exponent
	 * @return
	 */
	public static SiUnit pow(SiUnit a, double e) {
		SiUnit su = new SiUnit();
		su.L = a.L * e;
		su.M = a.M * e;
		su.T = a.T * e;
		su.I = a.I * e;
		su.theta = a.theta * e;
		su.N = a.N * e;
		su.J = a.J * e;

		return su;
	}

	/**
	 * @return true if units are equal
	 */
	@Override
	public boolean equals(Object o) {
		try {
			SiUnit u = (SiUnit) o;
			if (this.L == u.L & this.M == u.M & this.T == u.T & this.I == u.I
					& this.theta == u.theta & this.N == u.N & this.J == u.J)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @return String representation of the unit
	 */
	@Override
	public String toString() {
		return SiUnitDefinition.getString(this);
	}
}
