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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import ch.ethz.inspire.emod.model.Material;

public class ConfigReader {
	
	protected Properties props;
	protected String fileName;
	
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
		
	}
	
	/**
	 * Get property as string.
	 * 
	 * @param paramname Name of property
	 * @return Value of property
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
	public double getDoubleValue(String paramname) throws Exception
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
	public int getIntValue(String paramname) throws Exception
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
	public boolean getBooleanValue(String paramname) throws Exception
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
		
		double[] retarray = null;
		try {
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
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + fileName + "'!");
		}
		
		double[][] retmatrix = null;
		try {
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
		}
		catch (Exception e) {
			throw new Exception("Unknown format of propertiy '" + paramname 
					+ "' in file '" + fileName + "'\n   " + e.getMessage());
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
	public void setValue(String name, double value) throws IOException {
		props.setProperty(name, Double.toString(value));
		saveValues();
	}
	
	/**
	 * Sets the property "name" to "value"
	 * @param name
	 * @param value
	 * @throws IOException 
	 */
	public void setValue(String name, boolean value) throws IOException {
		props.setProperty(name, Boolean.toString(value));
		saveValues();
	}
	
	/**
	 * Sets the property "name" to "value"
	 * @param name
	 * @param value
	 * @throws IOException 
	 */
	public void setValue(String name, double[] value) throws IOException{
		String valueAsString = "";
		for(double v : value)
			valueAsString += Double.toString(v)+",";
		props.setProperty(name,  valueAsString);
		saveValues();
	}
	
	/**
	 * Saves the defined properties in an xml file
	 * @throws IOException
	 */
	public void saveValues() throws IOException{
		OutputStream ioStream = new FileOutputStream(fileName);
		props.storeToXML(ioStream, "");	
	}
	
}
