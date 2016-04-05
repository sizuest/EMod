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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.PhysicalValue;

public class ConfigReader {
	
	protected Properties props;
	protected String fileName;
	protected String comment = "";
	
	public ConfigReader(String fname) throws Exception {
		fileName = fname;
		
		ConfigReaderOpen();
	}
	public ConfigReader() {
	}
	
	public void ConfigReaderOpen() throws Exception {
		
		/* Load model parameters from file.
		 * The file must satisfy the XML format and DTD definition.
		 * 
		 * Example file:
		 *  <?xml version="1.0" encoding="UTF-8" standalone="no"?>
		 *  <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
         *  <properties>
         *    <comment>Some comment</comment>
         *    <entry key="parameter1">123.0</entry>
         *    <entry key="parameter2">12, 13, 14</entry>
         *  </properties>
		 */
		InputStream iostream = new FileInputStream(fileName);
		props = new Properties();
		try {
			props.loadFromXML(iostream);
		} catch (Exception e) {
			throw new Exception("Error in reading properties from file '"+fileName+
					"' bad format. \n"
					+e.getMessage());
		}
		iostream.close();
	}
	
	/**
	 * Close
	 */
	public void Close() {
		//TODO
	}
	
	public String getPath(){
		return fileName;
	}
	
	/**
	 * Get property as string.
	 * 
	 * @param paramname Name of property
	 * @return Value of property
	 * @throws Exception 
	 */
	public String getString(String paramname) throws Exception
	{
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + fileName + "'!");
		}
		return valstr;
	}
	
	/**
	 * Get property by name. The value of the property must be
	 * a double value.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  Double value.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &lt;entry key="PARAMNAME"&gt;1230.0&lt;/entry&gt;
	 * 
	 * @param paramname Name of the property.
	 * @return the value of the property: a double value.
	 * @throws Exception if the property could not be found or if the value could
	 *         not be converted to a double value.
	 */
	public Double getDoubleValue(String paramname) throws Exception
	{
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + fileName + "'!");
		}
	
		try {
			return Double.parseDouble(valstr);
		}
		catch (NumberFormatException e) {
			throw new NumberFormatException("Unknown format of propertiy '" + paramname 
					+ "' in file '" + fileName + "'\n   " + e.getMessage());
		}
	}
	
	public PhysicalValue getPhysicalValue(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);		
		PhysicalValue out = new PhysicalValue();
		
		String unit = "";
		double value[];
		
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + fileName + "'!");
		}

		value = parseDoubleArray(valstr.replaceFirst("[a-zA-Z].*$", ""));
		unit  = valstr.replaceFirst("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?", "");
		
		out.set(value, unit);
		
		return out;
		
		
	}
	
	/**
	 * Get property by name. The value of the property must be
	 * a integer value.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  Integer value.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &lt;entry key="PARAMNAME"&gt;1230.0&lt;/entry&gt;
	 * 
	 * @param paramname Name of the property.
	 * @return the value of the property: a integer value.
	 * @throws Exception if the property could not be found or if the value could
	 *         not be converted to a integer value.
	 */
	public Integer getIntValue(String paramname) throws Exception
	{
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + fileName + "'!");
		}
	
		try {
			return Integer.parseInt(valstr);
		}
		catch (NumberFormatException e) {
			throw new NumberFormatException("Unknown format of propertiy '" + paramname 
					+ "' in file '" + fileName + "'\n   " + e.getMessage());
		}
	}
	
	/**
	 * Get property by name. The value of the property must be
	 * a bool value.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  boolean value.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &lt;entry key="PARAMNAME"&gt;true&lt;/entry&gt;
	 * 
	 * @param paramname Name of the property.
	 * @return the value of the property: a boolean value.
	 * @throws Exception if the property could not be found or if the value could
	 *         not be converted to a boolean value.
	 */
	public Boolean getBooleanValue(String paramname) throws Exception
	{
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + fileName + "'!");
		}
	
		try {
			return Boolean.parseBoolean(valstr);
		}
		catch (NumberFormatException e) {
			throw new NumberFormatException("Unknown format of propertiy '" + paramname 
					+ "' in file '" + fileName + "'\n   " + e.getMessage());
		}
	}
	
	/**
	 * Get property by name. The value of the property must be
	 * a double array.
	 * <p>
	 * Format:<br />
	 *  The double values must be separated by ',' or ' '. After the last element<br /> 
	 *  of the array, a ';' can terminate the array.<br />
	 * Example:<br />
	 *   &lt;entry key="PARAMNAME"&gt;1.1, 2.3, 1.2;&lt;/entry&gt;
	 * 
	 * @param paramname Name of the property.
	 * @return the value of the property: a double array.
	 * @throws Exception if the property could not be found or if the value could
	 *         not be converted to a double array.
	 */
	public double[] getDoubleArray(String paramname) throws Exception
	{
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No property '" + paramname + "' found in '" + fileName + "'!");
		}
		
		double[] retarray;
		try {
			retarray = parseDoubleArray(valstr);
		}
		catch (Exception e) {
			throw new Exception("Unknown format of propertiy '" + paramname 
					+ "' in file '" + fileName + "'\n   " + e.getMessage());
		}
		return retarray;
	}
	
	
	/**
	 * Get property by name. The value of the property must be
	 * a double matrix.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  The values of a row must be separated by ',' or ' '. At the end<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  of each row, a ';' occurs.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &lt;entry key="PARAMNAME"&gt;<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    1.1, 2.3, 1.2;<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    7.2, 5.3, 9.8;<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &lt;/entry&gt;
	 * 
	 * @param paramname Name of the property.
	 * @return the value of the property: a matrix of doubles.
	 *         The first index of the matrix indicates the row, the second
	 *         the column: Matrix[row][column].
	 * @throws Exception if the property could not be found or if the value could
	 *         not be converted to a double matrix.
	 */
	public double[][] getDoubleMatrix(String paramname) throws Exception
	{
		String valstr = props.getProperty(paramname);
		double[][] retmatrix = null;
		
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + fileName + "'!");
		}
		try{
			retmatrix = stringToDoubleMatrix(valstr);
		}
		catch(Exception e){
			throw new Exception("Unknown format of propertiy '" + paramname 
					+ "' in file '" + fileName + "'\n   " + e.getMessage());
		}
		
		return retmatrix;
	}
	
	private double[][] stringToDoubleMatrix(String valstr) {
		double[][] retmatrix = null;

		// Remove all CRs and LFs, if exists:
		String valstr1 = valstr.replace("\n", "").replace("\r", "");
		// Split to rows:
		String rows[] = valstr1.trim().split(";");

		// Allocate row of retmatrix:
		retmatrix = new double[rows.length][];
		// Proceed row for row:
		for (int row=0; row<rows.length; row++) {
			// Change colons to spaces:
			String rowstr = rows[row].replace(",", " ");
			// Split at white spaces:
			String[] rowarray = rowstr.trim().split("\\s+");
			
			// Allocate columns of retmatrix:
			retmatrix[row] = new double[rowarray.length];
			// Convert string array to double array:
			for (int col=0; col<rowarray.length; col++) {
				retmatrix[row][col] = Double.parseDouble(rowarray[col]);
			}
		}
		return retmatrix;
	}
	
	/**
	 * Get property by name. The value of the property must be
	 * a material name.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  fluid name.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  &lt;entry key="PARAMNAME"&gt;Water&lt;/entry&gt;
	 * 
	 * @param paramname Name of the property.
	 * @return the material object
	 * @throws Exception if the property could not be found or if the value could
	 *         not be converted to a double value.
	 */
	public Material getMaterial(String paramname) throws Exception
	{
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + fileName + "'!");
		}
	
		try {
			return (new Material(valstr));
		}
		catch (Exception e) {
			throw new Exception("Unknown format of propertiy '" + paramname 
					+ "' in file '" + fileName + "'\n   " + e.getMessage());
		}
	}
	
	/**
	 * Sets the property "name" to "value"
	 * @param name
	 * @param value
	 * @throws IOException 
	 */
	public void setValue(String name, Object value) {
		props.setProperty(name, value.toString());
	}
	
	/**
	 * Sets the property "name" to "value"
	 * @param name
	 * @param value
	 * @throws IOException 
	 */
	public void setValue(String name, String value) {
		props.setProperty(name, value);
	}
	
	/**
	 * Sets the property "name" to "value"
	 * @param name
	 * @param value
	 * @throws IOException 
	 */
	public void setValue(String name, double value) {
		props.setProperty(name, Double.toString(value));
	}
	
	/**
	 * Sets the property "name" to "value"
	 * @param name
	 * @param value
	 * @throws IOException 
	 */
	public void setValue(String name, boolean value) {
		props.setProperty(name, Boolean.toString(value));
	}
	
	/**
	 * Sets the property "name" to "value"
	 * @param name
	 * @param value
	 * @throws IOException 
	 */
	public void setValue(String name, double[] value) {
		String valueAsString = "";
		for(double v : value)
			valueAsString += Double.toString(v)+",";
		props.setProperty(name,  valueAsString);
	}
	
	public void setValue(String name, PhysicalValue value) {
		props.setProperty(name, value.toString());
	}
	
	public void deleteValue(String name) {
		props.remove(name);
	}
	
	/**
	 * Saves the defined properties in an xml file
	 * @throws IOException
	 */
	public void saveValues() throws IOException{
		OutputStream ioStream = new FileOutputStream(fileName);
		props.storeToXML(ioStream, comment);	
	}
	
	/**
	 * @param paramname
	 * @return String array
	 * @throws Exception
	 */
	public String[] getStringArray(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + fileName + "'!");
		}
	
		String[] retarray = null;
		try {
			// Remove semicolon at the end, if exists:
			String valstr1 = valstr.replace(";", "");
			// Change colons to spaces:
			String valstr2 = valstr1.replace(",", " ");
			// Split at white spaces:
			String[] strarray = valstr2.trim().split("\\s+");
		
			// Convert string array to double array:
			retarray = new String[strarray.length];
			for (int i=0; i<strarray.length; i++) {
				retarray[i] = strarray[i];
			}
		}
		catch (NumberFormatException e) {
			throw new NumberFormatException("Unknown format of propertiy '" + paramname 
					+ "' in file '" + fileName + "'\n   " + e.getMessage());
		}
		
		return retarray;
	}
	
	private double[] parseDoubleArray(String valstr){
		double[] retarray = null;
		
		// Remove semicolon at the end, if exists:
		String valstr1 = valstr.replace(";", "");
		// Change colons to spaces:
		String valstr2 = valstr1.replace(",", " ");
		// Split at white spaces:
		String[] strarray = valstr2.trim().split("\\s+");
	
		// Convert string array to double array:
		retarray = new double[strarray.length];
		for (int i=0; i<strarray.length; i++) {
			retarray[i] = Double.parseDouble(strarray[i]);
		}
		
		return retarray;
	}
	
	
	/**
	 * @return Array of available keys
	 */
	public ArrayList<String> getKeys() {
		ArrayList<String> keys = new ArrayList<String>();
		
		if(null!=props){		
			Enumeration<Object> enuKeys = props.keys();
			while(enuKeys.hasMoreElements())
				keys.add((String) enuKeys.nextElement());
		}
		
		return keys;
	}
	
	/**
	 * Reading values with error handling
	 * @param paramname 
	 * @param defVal 
	 * @return 
	 * @return 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(String paramname, T defVal) throws Exception{
		
		T value = defVal;
		
		try{
			if(value instanceof String)
				value = (T) getString(paramname);
			else if(value instanceof Double)
				value = (T) getDoubleValue(paramname);
			else if(value instanceof PhysicalValue)
				value = (T) getPhysicalValue(paramname);
			else if(value instanceof Integer)
				value = (T) getIntValue(paramname);
			else if(value instanceof Boolean)
				value = (T) getBooleanValue(paramname);
			else if(value instanceof Double[])
				value = (T) getDoubleArray(paramname);
			else if(value instanceof Double[][])
				value = (T) getDoubleMatrix(paramname);
			else if(value instanceof Material)
				value = (T) getMaterial(paramname);
			else if(value instanceof String[])
				value = (T) getStringArray(paramname);
			else
				throw new Exception("ConfigReader: No action defined for type "+defVal.getClass().getSimpleName());
		}
		catch(Exception e){
			System.err.println(e.getMessage());
			System.out.println("ConfigReader: "+fileName+": Creating new parameter '"+paramname+"' with value '"+defVal+"'");
			
			setValue(paramname, defVal);
			try {
				saveValues();
			} catch (IOException e1) {
				throw e1;
			}
		}
		
		return value;
	}
	
}
