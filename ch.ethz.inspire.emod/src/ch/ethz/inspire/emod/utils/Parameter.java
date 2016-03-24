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

public class Parameter <T> {
	private T value; 
	
	public T getValue(){
		return value;
	}
	
	public void setValue(T value){
		this.value = value;
	}
}
