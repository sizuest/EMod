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
 * 
 * @author david
 *
 */
public class SamplePeriodConverter {

	private static double[] newSamples;
	private static double[] oldSamples;
	private static int pos;
	public static double[] convertSamples(double originalPeriod, double targetPeriod, double[] samples) {
		oldSamples = samples;
		double samplestime = samples.length*originalPeriod;
		double newNumberOfSamples = samplestime/targetPeriod;
		newSamples = new double[(int)newNumberOfSamples];
		convert(newNumberOfSamples/oldSamples.length);
		return newSamples;
	}
	
	private static void convert(double conversionFactor) {
		pos=0;
		if(conversionFactor == 1) {
			newSamples=oldSamples;
		}
		else if(conversionFactor < 1) {
			int min = (int) Math.floor((1/conversionFactor));
			boolean fullarray=false;
			for(int i=0;i<newSamples.length;i=i+2) {
				fullarray = integrate(i, min);
				if(fullarray)
					break;
			}
			newSamples[newSamples.length-1]=oldSamples[oldSamples.length-1];
		}
		else {
			int min = (int) Math.floor(conversionFactor);
			boolean fullarray=false;
			for(int i=0;i<oldSamples.length;i=i+2) {
				fullarray = interpol(i, min);
				if(fullarray)
					break;
			}
			newSamples[newSamples.length-1]=oldSamples[oldSamples.length-1];
		}
	}
	
	private static boolean integrate(int targetPos, int steps) {
		double val=0;
		for(int i=0;i<steps;i++) {
			if(!(pos+i<oldSamples.length))
				return true;
			val+=oldSamples[pos+i];
		}
		newSamples[targetPos] = val/steps;
		pos=pos+steps;
		val=0;
		for(int i=0;i<steps+1;i++) {
			if(!(pos+i<oldSamples.length))
				return true;
			val+=oldSamples[pos+i];
		}
		newSamples[targetPos+1] = val/(steps+1);
		pos=pos+steps+1;
		return false;
	}
	
	private static boolean interpol(int sourcePos, int steps) {
		if(steps==1){
			newSamples[pos] = oldSamples[sourcePos];
			pos++;
			if(pos>newSamples.length-1)
				return true;
			newSamples[pos] = oldSamples[sourcePos+1];
			pos++;
			if(pos>newSamples.length-1)
				return true;
			newSamples[pos] = (oldSamples[sourcePos+1]+oldSamples[sourcePos+2])/2;
			pos++;
			if(pos>newSamples.length-1)
				return true;
			return false;
		}
		else {
			double increment = (oldSamples[sourcePos+1]-oldSamples[sourcePos])/steps;
			for(int i=0;i<steps;i++) {
				newSamples[pos] = oldSamples[sourcePos]+increment*i;
				pos++;
				if(pos>newSamples.length-1)
					return true;
			}
			increment = (oldSamples[sourcePos+2]-oldSamples[sourcePos+1])/steps+1;
			for(int i=0;i<steps+1;i++) {
				newSamples[pos] = oldSamples[sourcePos]+increment*i;
				pos++;
				if(pos>newSamples.length-1)
					return true;
			}
			return false;
		}
			
	}
}
