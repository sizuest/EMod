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
public class SamplePeriodConverterTest {

	/**
	 * Test method for {@link ch.ethz.inspire.emod.utils.SamplePeriodConverter#convertSamples(double, double, double[])}.
	 */
	@Test
	public void testConvertSamples() {
		double[] samples = {1, 5, 3, 7}; //values
		double originalPeriod = 1.0; // sample period = 1s
		double targetPeriod = 0.2; // target sample period = 200ms
		double[] expresult = {1, 1.8, 2.6, 3.4, 4.2, 5, 4.6, 4.2, 3.8, 3.4, 3, 3.8, 4.6, 5.4, 6.2, 7, 7, 7, 7, 7};
		double[] result=null;
		try {
			result = SamplePeriodConverter.convertSamples(originalPeriod, targetPeriod, samples);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals("1s -> 200ms, number of samples", 20, result.length);
		for(int i=0;i<expresult.length;i++) {
			assertEquals("1s -> 200ms, values", expresult[i], result[i], 0.001);
		}
		
		try {
			result = SamplePeriodConverter.convertSamples(targetPeriod, originalPeriod, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals("200ms -> 1s, number of samples", 4, result.length);
		for(int i=0;i<samples.length;i++) {
			assertEquals("200ms -> 1s, values", samples[i], result[i], 0.0001);
		}
	}

}
