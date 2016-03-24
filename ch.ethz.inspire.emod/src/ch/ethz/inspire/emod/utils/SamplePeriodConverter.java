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

/**
 * converts a double array from length a to b. if the original array
 * is shortened, samples will be integrated, if the target array is 
 * longer than the original, a linear interpolation is used.
 * 
 * @author david
 *
 */
public class SamplePeriodConverter {

	private static double[] newSamples;
	private static double[] oldSamples;
	
	/**
	 * converts samples from one sample period to another.
	 * 
	 * @param originalPeriod sample period of the samples param double array
	 * @param targetPeriod sample period of the returned array
	 * @param samples double[] sample values
	 * @param interp
	 * @return new samples with period = targetPeriod
	 * @throws Exception 
	 */
	public static double[] convertSamples(double originalPeriod, double targetPeriod, double[] samples, boolean interp) throws Exception {
		oldSamples = samples;
		if(targetPeriod <= 0) {
			throw new Exception("Invalid target period (<=0)");
		}
		
		double samplestime = samples.length*originalPeriod;
		double[] originaltimesamples = new double[samples.length];
		for(int i=0;i<samples.length;i++)
			originaltimesamples[i] = i*originalPeriod;
		double newNumberOfSamples = samplestime/targetPeriod;
		if(newNumberOfSamples<1)
			newNumberOfSamples=1; // smaller than 1 values result in empty arrays and out of bounds exceptions in sim ctrl update
		newSamples = new double[(int)newNumberOfSamples];
		
		if(interp)
			for(int i=0;i<(int)newNumberOfSamples;i++) {
				newSamples[i] = Algo.linearInterpolation(i*targetPeriod, originaltimesamples, oldSamples);
			}
		else
			for(int i=0;i<(int)newNumberOfSamples;i++) {
				newSamples[i] = oldSamples[Algo.findInterval(i*targetPeriod, originaltimesamples)];
			}
		return newSamples;
	}

	public static double[] convertSamples(double simulationPeriod, double[] time, double[] samps) {
		int numSamples = (int) (Math.ceil(time[time.length-1]/simulationPeriod));
		double[] newSamples = new double[numSamples];
		
		for(int i=0; i<numSamples; i++)
			newSamples[i] = samps[Algo.findInterval(i*simulationPeriod, time)];
		
		return newSamples;
	}
	
}
