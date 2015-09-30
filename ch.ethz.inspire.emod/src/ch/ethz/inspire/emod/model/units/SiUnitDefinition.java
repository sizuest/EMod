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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the SI definitions
 * @author sizuest
 *
 */
public class SiUnitDefinition {
	
	// SI definition object
	private static SiUnitDefinition siDefinition = null;
	// Conversion Map
	private static Map<String,SiUnit> convMap = new HashMap<String,SiUnit>();
	// Units of the dimensions
	private String[] unitNames = {"m", 
			                      "kg", 
			                      "s", 
			                      "A", 
			                      "K", 
			                      "mol", 
			                      "cd"};
	
	/**
	 * Private constructor for singelton implementation
	 */
	private SiUnitDefinition(){}
	
	/**
	 * singleton implementation of the SI unit definition
	 * 
	 * @return instance of the SI defintions
	 */
	public static SiUnitDefinition getInstance(){
		if(siDefinition == null){
			siDefinition = new SiUnitDefinition();
			initConversionMap();
		}
		return siDefinition;
		
	}
	
	/**
	 * @return instance of the conversion map
	 */
	public static Map<String,SiUnit> getConversionMap(){
		return convMap;
	}
	
	
	private static void initConversionMap(){
		// Base units
		// Have to be stated at very first in exponential representation!
		convMap.put("m",   (new SiUnit(1,0,0,0,0,0,0)));
		convMap.put("kg",  (new SiUnit(0,1,0,0,0,0,0)));
		convMap.put("s",   (new SiUnit(0,0,1,0,0,0,0)));
		convMap.put("A",   (new SiUnit(0,0,0,1,0,0,0)));
		convMap.put("K",   (new SiUnit(0,0,0,0,1,0,0)));
		convMap.put("mol", (new SiUnit(0,0,0,0,0,1,0)));
		convMap.put("cd",  (new SiUnit(0,0,0,0,0,0,1)));
		// Additional SI units
		convMap.put("rad", (new SiUnit()));
		convMap.put("sr",  (new SiUnit()));
		convMap.put("Hz",  (new SiUnit("s^-1")));
		convMap.put("N",   (new SiUnit("m kg s^-2")));
		convMap.put("Pa",  (new SiUnit("kg m^-1 s^-1")));
		convMap.put("J",   (new SiUnit("m^2 kg s^-2")));
		convMap.put("W",   (new SiUnit("m^2 kg s^-3")));
		convMap.put("C",   (new SiUnit("A s")));
		convMap.put("V",   (new SiUnit("m^2 kg s^-3 A^-1")));
		convMap.put("Ohm", (new SiUnit("m^2 kg s^-3 A^-2")));
		convMap.put("S",   (new SiUnit("m^-2 kg ^-1 s^3 A^2")));
		convMap.put("Wb",  (new SiUnit("m^2 kg s^-2 A^-1")));
		convMap.put("T",   (new SiUnit("kg s^-2 A^-1")));
		convMap.put("H",   (new SiUnit("m^2 kg s^-2 A^-2")));
		convMap.put("°C",  (new SiUnit("K")));
		convMap.put("lm",  (new SiUnit("cd")));
		convMap.put("lx",  (new SiUnit("m^-2 cd")));
		convMap.put("Bq",  (new SiUnit("s^-1")));
		convMap.put("Gy",  (new SiUnit("m^2 s^-2")));
		convMap.put("Sv",  (new SiUnit("m^2 s^-2")));
		convMap.put("kat", (new SiUnit("mol s^-1")));
	}
	
	private String getRegexPattern(){
		String out;
		
		String[] u = getConversionMap().keySet().toArray(new String[0]);
		
		out = "(("+u[0]+"\\s)|("+u[0]+"$)|("+u[0]+".?-?\\d+))";
		for(int i=1; i<u.length; i++){
			out = out+"|(("+u[i]+"\\s)|("+u[i]+"$)|("+u[i]+".?[-]?\\d+))";
		}

		return out;
	}
	
	/**
	 * Returns the base units
	 * @return Array with base units
	 */
	public static String[] getBaseUnits(){
		return getInstance().unitNames;
	}
	
	public static String getString(SiUnit unit){
		String out = "";
		
		/* Test for simple representation */
		if(SiUnitDefinition.convMap.containsValue(unit))
			for(String s:SiUnitDefinition.convMap.keySet()){
				if(SiUnitDefinition.convMap.get(s).equals(unit)){
					out = s;
					break;
				}
			}
		else{
			double[] exp = unit.get();
			
			for(int i=0; i<exp.length; i++)
				if(0!=exp[i]) {
					out+=SiUnitDefinition.getBaseUnits()[i];  // Add unit name
					if(1!=exp[i]){           // Add exponent  if required
						if(0==exp[i]%1)
							out+="^"+(int)exp[i];
						else
							out+="^"+exp[i];  
					}
					out+=" ";
					
				}
				
			if (out.length()>0)
				out = out.substring(0, out.length()-1);
		}
		
		
		return out;
	}
	
	/**
	 * Converts a SI unit to the SI base units
	 * E.g.: N -> kg m s^-2
	 * @param s
	 * @return {@link SIUnit} objects
	 */
	public static SiUnit convertToBaseUnit(String s){
		double[] exp = {0,0,0,0,0,0,0};
		
		double[] expInner = exp;
		double expOut = 1;
		
		String subUnit;
		
		// Search for numbers
		Pattern p = Pattern.compile(getInstance().getRegexPattern());
		Matcher m = p.matcher(s);
		
		// Prepare other search units
		Pattern pUnit = Pattern.compile("[a-zA-Z]+|\\d");
		Matcher mUnit;
		
		while(m.find()){
			subUnit = m.group();
			
			mUnit = pUnit.matcher(subUnit);
			mUnit.find();
						
			// Get the corresponding base unit
			expInner = convMap.get(mUnit.group()).get();
			
			// Check if an exponent exists
			if(mUnit.find())
				expOut = Integer.valueOf(mUnit.group());
			else
				expOut = 1;
			
			// Add sub unit properties to global unit properties
			for(int i=0; i<exp.length; i++)
				exp[i] += expOut*expInner[i];
			
		}
		
 		return (new SiUnit(exp));
	}
	
	

}
