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

import java.util.Arrays;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.simple.SimpleMatrix;

import ch.ethz.inspire.emod.utils.ArrayIndexComparator;

/**
 * Class implementing mathematical utility functions for algorithms. Most of this
 * functions are defined static.
 * 
 * @author andreas
 *
 */
public class Algo {
	
	/**
	 * Double linear interpolation.
	 * Given two vectors xsamples={xi|i=1,2...n} and ysamples={yj|j=1,2...m}, as well as a 
	 * matrix zvalues={zij|i=1,2,...n , j=1,2,...m}, the value y is estimated based 
	 * on x and z by double linear interpolation:
	 * - zvec = {zvecj|j=1,2,...,m} is estimated by a linear interpolation for each
	 *   column of z at the value x
	 * - y is estimated by a linear interpolation on zvec at z
	 * 
	 * @param x x-value
	 * @param z z-value
	 * @param xsamples  Samples 'x_i' of the 'zvalues'. 
	 * @param ysamples  Samples 'y_j' of the 'zvalues'. 
	 * @param zvalues   Set of sample values ( z_ij=f(x_i, y_j) )
	 * @return x, estimated by double linear interpolation
	 */
	public static double doubleLinearInterpolation(double x, double z, double[] xsamples, double[] ysamples, double[][] zvalues)
	{
		// Conditions:
		//   xsamples.length == zvalues.length
		//   ysamples.length == zvalues[i].length
		//   xsamples[i] < xsamples[i+1]
		//   ysamples[i] < ysamples[i+1] 
		
		double[] zvector = new double[ysamples.length];
		double[] yvector = new double[ysamples.length];
		double[] ztmp    = new double[xsamples.length];
		
		int xind = findInterval(x, xsamples);
		if (xind < 0)                  for(int i=0; i<xsamples.length; i++) zvector[i] = zvalues[0][i];
		if (xind == xsamples.length-1) for(int i=0; i<xsamples.length; i++) zvector[i] = zvalues[xind][i];
		else
			for(int i=0; i<ysamples.length; i++) {
				for(int j=0; j<xsamples.length; j++) ztmp[j] = zvalues[j][i];
				zvector[i] = linearInterpolationWithIndex(x, xsamples, ztmp, xind);
			}
		
		ArrayIndexComparator comparator = new ArrayIndexComparator(zvector);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		Arrays.sort(zvector);
		
		for(int i=0; i<ysamples.length; i++) yvector[indexes[i].intValue()] = ysamples[i];
		
		return linearInterpolation(z, zvector, yvector);
	}
	
	/**
	 * Logarithmic interpolation
	 * A function is given by a set of (x,y) points. The y-value belonging to
	 * a given x value is determined by logarithmic interpolation.
	 * 
	 * @param x     Value on the x axis to find the corresponding y value.
	 * @param xsamples Samples on the x axis. Must be sorted (lowest value at first position).
	 * @param yvals Samples on the y axis.
	 * @return y-value belonging to 'x'.
	 */
	public static double logInterpolation(double x, double[] xsamples, double[] yvals)
	{
		// Conditions:
		//   xsamples.length == yvals.length
		//   xsamples[i] < xsamples[i+1] 
		
		int index = findInterval(x, xsamples);
		double y = logInterpolationWithIndex(x, xsamples, yvals, index);
		return y;
	}
	
	/**
	 * Bilinear interpolation.
	 * A two-dimensional function z=f(x,y) is given by a set of sample values ( z_ij=f(x_i, y_j) ).
	 * The z-value of the point (x,y) is calculated by bilinear interpolation.
	 * 
	 * @param x x-value
	 * @param y y-value
	 * @param xsamples  Samples 'x_i' of the 'zvalues'. 
	 * @param ysamples  Samples 'y_j' of the 'zvalues'. 
	 * @param zvalues   Set of sample values ( z_ij=f(x_i, y_j) )
	 * @return The bilinear interpolation at the point (x,y) for the function specified by 'xsamples',
	 *         'ysamples' and 'zvalues'.
	 */
	public static double bilinearInterpolation(double x, double y, double[] xsamples, double[] ysamples, double[][] zvalues)
	{
		// Conditions:
		//   xsamples.length == zvalues.length
		//   ysamples.length == zvalues[i].length
		//   xsamples[i] < xsamples[i+1]
		//   ysamples[i] < ysamples[i+1] 
		
		int xind = findInterval(x, xsamples);
		if (xind < 0) {
			return linearInterpolation(y, ysamples, zvalues[0]);
		}
		if (xind == xsamples.length-1) {
			return linearInterpolation(y, ysamples, zvalues[xind]);
		}
		
		int yind = findInterval(y, ysamples);
		double z_xl = linearInterpolationWithIndex(y, ysamples, zvalues[xind], yind);
		double z_xh = linearInterpolationWithIndex(y, ysamples, zvalues[xind+1], yind);
		
		double z = z_xl + 
		           (x-xsamples[xind]) / (xsamples[xind+1]-xsamples[xind]) * (z_xh - z_xl);
		
		return z;
	}
	
	/**
	 * A function is given by a set of (x,y) points. The y-value belonging to
	 * a given x value is determined by linear interpolation.
	 * 
	 * @param x     Value on the x axis to find the corresponding y value.
	 * @param xsamples Samples on the x axis. Must be sorted (lowest value at first position).
	 * @param yvals Samples on the y axis.
	 * @return y-value belonging to 'x'.
	 */
	public static double linearInterpolation(double x, double[] xsamples, double[] yvals)
	{
		// Conditions:
		//   xsamples.length == yvals.length
		//   xsamples[i] < xsamples[i+1] 
		
		int index = findInterval(x, xsamples);
		double y = linearInterpolationWithIndex(x, xsamples, yvals, index);
		return y;
	}
	
	/**
	 * Numerically derivates the given series y=f(x) at aa specified point x0:
	 * 
	 * f'(x0) = ( f(x[i+1])-f(x[i]) ) / ( x[i+1]-x[i] )    where
	 * x[i]<=x0<x[i+1]
	 * 
	 * @param x        Value at which the derivate shall be calculated
	 * @param xsamples Samples on the x axis. Must be sorted (lowest value at first position).
	 * @param yvals    Samples on the y axis.
	 * @return 		   Estimated derivative unit: [y]/[x]
	 */
	public static double numericalDerivative(double x, double[] xsamples, double[] yvals){
		
		int index = findInterval(x, xsamples);
		int low, high;
		
		
		if(index == xsamples.length-1){
			high = index;
			low  = index-1;
		}
		else{
			high = index+1;
			low  = index;
		}
		
					
		return (yvals[high]-yvals[low])/(xsamples[high]-xsamples[low]);
	}
	
	/**
	 * A function is given by a set of (x,y) points. The y-value belonging to
	 * a given x value is determined by linear interpolation.
	 * 
	 * @param x     Value on the x axis to find the corresponding y value.
	 * @param xsamples Samples on the x axis. Must be sorted (lowest value at first position).
	 * @param yvals Samples on the y axis.
	 * @param index From 'x' and 'xsamples', the index is calculated such that xsamples[index] <= x < xsamples[index+1]
	 * @return y-value belonging to 'x'.
	 */
	private static double linearInterpolationWithIndex(double x, double[] xsamples, double[] yvals, int index)
	{
		// Conditions:
		//   xsamples.length == yvals.length
		//   xsamples[i] < xsamples[i+1] 
		
		if (index < 0) {
			// 'x' is outside of the specified 'xsamples' periods. Return the first y value.
			//System.out.println(x + " <= xmin(" + xsamples[0] + ") => y=" + yvals[0]);
			return 	yvals[0];
		}
		if (index >= yvals.length-1) {
			// 'x' is outside of the specified 'xsamples' periods. Return the last y value.
			//System.out.println(x + " >= xmax(" + xsamples[index] + ") => y=" + yvals[index]);
			return	yvals[index];
		}
		double y = yvals[index] + 
		          (x-xsamples[index])/(xsamples[index+1]-xsamples[index])*(yvals[index+1]-yvals[index]);
		//System.out.println(x + " " + "x/y_s[" + index + "]=" + xsamples[index] + "/" + yvals[index]
		//                          + " x/y_l[" + (index+1) + "]=" + xsamples[index+1] + "/" + yvals[index+1]
		//                          + " y=" + y);
		return y;
	}
	
	/**
	 * A function is given by a set of (x,y) points. The y-value belonging to
	 * a given x value is determined by logarithmic interpolation.
	 * 
	 * @param x     Value on the x axis to find the corresponding y value.
	 * @param xsamples Samples on the x axis. Must be sorted (lowest value at first position).
	 * @param yvals Samples on the y axis.
	 * @param index From 'x' and 'xsamples', the index is calculated such that xsamples[index] <= x < xsamples[index+1]
	 * @return y-value belonging to 'x'.
	 */
	private static double logInterpolationWithIndex(double x, double[] xsamples, double[] yvals, int index)
	{
		// Conditions:
		//   xsamples.length == yvals.length
		//   xsamples[i] < xsamples[i+1] 
		
		if (index < 0)               return yvals[0];
		if (index >= yvals.length-1) return	yvals[index];
		
		double y = yvals[index] * Math.exp( (x-xsamples[index])/(xsamples[index+1]-xsamples[index])*(Math.log(yvals[index+1])-Math.log(yvals[index])) ); 
		          
		return y;
	}
	
	/**
	 * Within a increasing sequence of values, find the interval where a given value lies in. 
	 * The sequence if given as an sorted array. The function returns the lower index of the
	 * interval where the value 'x' lies in.
	 * The following condition is true:
	 *    vals[index] <= x < vals[index+1]
	 *   where index is the return value.
	 *  
	 * @param x     Value 
	 * @param vals  Sorted array (The first entry is the smallest)
	 * @return Return the index of the last value in the array 'vals' that is smaller or equal
	 *         as the value x. If the value 'x' is smaller than the first entry, return -1.
	 */
	public static int findInterval(double x, double[] vals)
	{
		int low = 0;
		
		if (x < vals[0]) {
			return 	-1; // x smaller as the first entry.
		}
		
		int high = vals.length-1;
		if (x >= vals[vals.length-1]) {	
			return vals.length-1;  // x larger as the last entry.
		}
		
		int mid = vals.length / 2;
		while (high-low > 1) {
			if (x >= vals[mid]) {
				low = mid;
			}
			else {
				high = mid;
			}
			mid = (low + high) / 2;
		}
		return low;
	}
	
	/**
	 * Returns the roots of the polynome (real & complex parts)
	 * @param coefficients double array with the roots
	 * @return roots {@link Complex64F} 
	 */
	public static Complex64F[] findRoots(double... coefficients) {
        int N = coefficients.length-1;
        
        for (int i = 0; i < coefficients.length / 2; i++) {
        	double temp = coefficients[i];
        	coefficients[i] = coefficients[coefficients.length - 1 - i];
        	coefficients[coefficients.length - 1 - i] = temp;
		}

        // Construct the companion matrix
        DenseMatrix64F c = new DenseMatrix64F(N,N);

        double a = coefficients[N];
        for( int i = 0; i < N; i++ ) {
            c.set(i,N-1,-coefficients[i]/a);
        }
        for( int i = 1; i < N; i++ ) {
            c.set(i,i-1,1);
        }

        // use generalized eigenvalue decomposition to find the roots
        EigenDecomposition<DenseMatrix64F> evd =  DecompositionFactory.eig(N, false);

        evd.decompose(c);

        Complex64F[] roots = new Complex64F[N];

        for( int i = 0; i < N; i++ ) {
            roots[i] = evd.getEigenvalue(i);
        }

        return roots;
    }
	
	/**
	 * Solves the given least squares problem and returns the parameters
	 * 
	 * Given: 
	 *   y = H * p
	 * Solution:
	 *   p = (H'H)^-1 H' y
	 * 
	 * @param Hin
	 * @param yin
	 * @return p
	 */
	public static double[] findLeastSquares(double[][] Hin, double[] yin){
		SimpleMatrix H = new SimpleMatrix(Hin);
		SimpleMatrix y = new SimpleMatrix(yin.length, 1);
		SimpleMatrix p = new SimpleMatrix(H.numCols(),1);
		
		double[] out = new double[H.numCols()];
		
		for(int i=0; i<yin.length; i++)
			y.set(i, yin[i]);
		
		/*
		 * Solve LS problem
		 */
		p = H.transpose().mult(H).invert().mult(H.transpose()).mult(y);
		
		for(int i=0; i<p.numRows(); i++)
			out[i] = p.get(i);
		
		return out;
	}

}
