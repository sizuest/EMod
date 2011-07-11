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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author dhampl
 *
 */
public class AlgoTest {

	/**
	 * Test method for {@link ch.ethz.inspire.emod.utils.Algo#bilinearInterpolation(double, double, double[], double[], double[][])}.
	 */
	@Test
	public void testBilinearInterpolation() {
		double x = 3.1261;
		double y = 2.1356;
		double[] xsamples = {1,2,3,4};
		double[] ysamples = {1,2,3,4};
		double[][] zvalues = {{13,42,53,64},{11,22,63,14},{21,92,23,74},{81,52,43,24}};
		assertEquals("bilinear interpol", 78.6255, Algo.bilinearInterpolation(x, y, xsamples, ysamples, zvalues),0.0001);
		// TODO: randbedingungen testen
	}

	/**
	 * Test method for {@link ch.ethz.inspire.emod.utils.Algo#linearInterpolation(double, double[], double[])}.
	 */
	@Test
	public void testLinearInterpolation() {
		double x=7.2725;
		double[] xsamples = {0,2,5,7,9,13,16,18};
		double[] yvals = {3,7,2,6,8,26,4,3};
		assertEquals("interpolation", 6.2725, Algo.linearInterpolation(x, xsamples, yvals),0.0001);
		// TODO: randbedingungen testen
	}

	/**
	 * Test method for {@link ch.ethz.inspire.emod.utils.Algo#findInterval(double, double[])}.
	 */
	@Test
	public void testFindInterval() {
		double[] vals = {-4,-2,-1.3,0,2,2.4,3,3.5,5,9};
		// "normal" cases with value inside array
		assertEquals("normal", 7, Algo.findInterval(3.6, vals));
		assertEquals("normal", 1, Algo.findInterval(-1.6, vals));
		// case with value smaller than first item of array
		assertEquals("smaller than first item", -1, Algo.findInterval(-7, vals));
		// case with value bigger than last item
		assertEquals("larger than last item", 9, Algo.findInterval(12, vals));
	}

}
