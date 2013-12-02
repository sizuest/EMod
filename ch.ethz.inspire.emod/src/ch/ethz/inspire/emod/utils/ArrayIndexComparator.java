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

import java.util.Comparator;

/**
 * @author sizuest
 * 
 * Used for sorting arrays
 *
 */

public class ArrayIndexComparator implements Comparator<Integer>
{
    private final double[] array;

    public ArrayIndexComparator(double[] array)
    {
        this.array = array;
    }

    public Integer[] createIndexArray()
    {
        Integer[] idx = new Integer[array.length];
        for (int i = 0; i < array.length; i++) idx[i] = i;
        return idx;
    }

    @Override
    public int compare(Integer index1, Integer index2)
    {
    	return Double.valueOf(array[index1]).compareTo(array[index2]);
    }
}