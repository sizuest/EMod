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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.parameters.PhysicalValue;
import ch.ethz.inspire.emod.model.units.SiUnit;


/**
 * Generic configuration reader (XML)
 * 
 * @author sizuest
 *
 */
public class ConfigReader {

	protected Properties props;
	protected String filePath;
	protected String comment = "";

	/**
	 * @param fname
	 * @throws Exception
	 */
	public ConfigReader(String fname) throws Exception {
		filePath = fname;

		ConfigReaderOpen();
	}

	/**
	 * 
	 */
	public ConfigReader() {
	}

	/**
	 * @throws Exception
	 */
	public void ConfigReaderOpen() throws Exception {

		/*
		 * Load model parameters from file. The file must satisfy the XML format
		 * and DTD definition.
		 * 
		 * Example file: <?xml version="1.0" encoding="UTF-8" standalone="no"?>
		 * <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
		 * <properties> <comment>Some comment</comment> <entry
		 * key="parameter1">123.0</entry> <entry key="parameter2">12, 13,
		 * 14</entry> </properties>
		 */
		
		InputStream iostream;
		
		File file = new File(filePath);
		if(!(file.exists()))
			file.createNewFile();
		
		
		try {
			iostream = new FileInputStream(filePath);
		} catch (Exception e) {
			throw new Exception("Error in reading properties from file '"
					+ filePath + "' bad format. \n" + e.getMessage());
		}
		
		try {
			props = new Properties();
			props.loadFromXML(iostream);
			iostream.close();
		} catch (Exception e) {}

	}

	/**
	 * Close
	 */
	public void Close() {
		props.clear();
	}

	/**
	 * Returns the configuration file path
	 * 
	 * @return
	 */
	public String getPath() {
		return filePath;
	}

	/**
	 * Get property as string.
	 * 
	 * @param paramname
	 *            Name of property
	 * @return Value of property
	 * @throws Exception
	 */
	public String getString(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '"
					+ filePath + "'!");
		}
		return valstr;
	}

	/**
	 * Get property by name. The value of the property must be a double value.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Double value.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;entry
	 * key="PARAMNAME"&gt;1230.0&lt;/entry&gt;
	 * 
	 * @param paramname
	 *            Name of the property.
	 * @return the value of the property: a double value.
	 * @throws Exception
	 *             if the property could not be found or if the value could not
	 *             be converted to a double value.
	 */
	public Double getDoubleValue(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '"
					+ filePath + "'!");
		}

		try {
			return Double.parseDouble(valstr);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Unknown format of propertiy '"
					+ paramname + "' in file '" + filePath + "'\n   "
					+ e.getMessage());
		}
	}

	/**
	 * Returns the physical value with the stated name
	 * 
	 * @param paramname
	 * @return
	 * @throws Exception
	 */
	public PhysicalValue getPhysicalValue(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);
		PhysicalValue out = new PhysicalValue();

		String unit = "";
		double value[];

		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '"
					+ filePath + "'!");
		}
		
		if(valstr.contains(",") & valstr.contains(";")){
			throw new Exception("Cant parse matrix '" + paramname + "' in '"
					+ filePath + "' to physical value!");
		}

		value = parseDoubleArray(valstr.replaceFirst("[a-df-zA-DF-Z].*$", ""));
		//unit = valstr.replaceFirst("(([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)[,;]?)+","");
		unit = valstr.replace(valstr.replaceFirst("[a-df-zA-DF-Z].*$", ""), "");

		out.set(value, unit);

		return out;

	}
	
	/**
	 * Same as getPhysicalValue(String), but in case of failing to read a 
	 * physical value, a double[] value is tried to be read. On success
	 * this value is converted to a new physical value with the desired
	 * si unit
	 * @param paramname
	 * @param desUnit
	 * @return
	 * @throws Exception
	 */
	public PhysicalValue getPhysicalValue(String paramname, SiUnit desUnit) throws Exception {
		PhysicalValue pvalue;
		try{
			pvalue = getPhysicalValue(paramname);
		}
		catch (Exception e){
			/* Ok, this did not work: try to read a double[] value: */
			try{
				double[] value = getDoubleArray(paramname);
				pvalue = new PhysicalValue(value, desUnit);
				// Save it, so its all fine for next time
				setValue(paramname, pvalue);
				saveValues();
				System.out.println("ConfigReader: " + filePath
						+ ": Creating new phisical parameter '" + paramname
						+ "' with value '" + value + " " + desUnit.toString() + "'");
			}
			catch(Exception e2){
				throw new Exception("No propertiy '" + paramname + "' found in '" + filePath + "'!");
			}
		}
		
		/* Test if the unit is correct  */
		if(!(pvalue.getUnit().equals(desUnit))){
			System.out.println("ConfigReader: " + filePath
					+ ": Changing wrong unit ofparameter '" + paramname
					+ "' from '" + pvalue.getUnit().toString() + "' to '" + desUnit.toString() + "'");
			pvalue.set(pvalue.getValues(), desUnit);
			try {
				setValue(paramname, pvalue);
				saveValues();
			} catch (IOException e1) {
				throw e1;
			}
		}
		
		return pvalue;
		
		
	}

	/**
	 * Get property by name. The value of the property must be a integer value.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Integer value.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;entry
	 * key="PARAMNAME"&gt;1230.0&lt;/entry&gt;
	 * 
	 * @param paramname
	 *            Name of the property.
	 * @return the value of the property: a integer value.
	 * @throws Exception
	 *             if the property could not be found or if the value could not
	 *             be converted to a integer value.
	 */
	public Integer getIntValue(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '"
					+ filePath + "'!");
		}

		try {
			return Integer.parseInt(valstr);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Unknown format of propertiy '"
					+ paramname + "' in file '" + filePath + "'\n   "
					+ e.getMessage());
		}
	}

	/**
	 * Get property by name. The value of the property must be a bool value.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; boolean value.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;entry
	 * key="PARAMNAME"&gt;true&lt;/entry&gt;
	 * 
	 * @param paramname
	 *            Name of the property.
	 * @return the value of the property: a boolean value.
	 * @throws Exception
	 *             if the property could not be found or if the value could not
	 *             be converted to a boolean value.
	 */
	public Boolean getBooleanValue(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '"
					+ filePath + "'!");
		}

		try {
			return Boolean.parseBoolean(valstr);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Unknown format of propertiy '"
					+ paramname + "' in file '" + filePath + "'\n   "
					+ e.getMessage());
		}
	}

	/**
	 * Get property by name. The value of the property must be a double array.
	 * <p>
	 * Format:<br />
	 * The double values must be separated by ',' or ' '. After the last element
	 * <br />
	 * of the array, a ';' can terminate the array.<br />
	 * Example:<br />
	 * &lt;entry key="PARAMNAME"&gt;1.1, 2.3, 1.2;&lt;/entry&gt;
	 * 
	 * @param paramname
	 *            Name of the property.
	 * @return the value of the property: a double array.
	 * @throws Exception
	 *             if the property could not be found or if the value could not
	 *             be converted to a double array.
	 */
	public double[] getDoubleArray(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No property '" + paramname + "' found in '"
					+ filePath + "'!");
		}

		double[] retarray;
		try {
			retarray = parseDoubleArray(valstr);
		} catch (Exception e) {
			throw new Exception("Unknown format of propertiy '" + paramname
					+ "' in file '" + filePath + "'\n   " + e.getMessage());
		}
		return retarray;
	}

	/**
	 * Get property by name. The value of the property must be a double matrix.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; The values of a row must be separated by
	 * ',' or ' '. At the end<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; of each row, a ';' occurs.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;entry key="PARAMNAME"&gt;<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 1.1, 2.3,
	 * 1.2;<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 7.2, 5.3,
	 * 9.8;<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/entry&gt;
	 * 
	 * @param paramname
	 *            Name of the property.
	 * @return the value of the property: a matrix of doubles. The first index
	 *         of the matrix indicates the row, the second the column:
	 *         Matrix[row][column].
	 * @throws Exception
	 *             if the property could not be found or if the value could not
	 *             be converted to a double matrix.
	 */
	public double[][] getDoubleMatrix(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);
		double[][] retmatrix = null;

		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '"
					+ filePath + "'!");
		}
		try {
			retmatrix = stringToDoubleMatrix(valstr);
		} catch (Exception e) {
			throw new Exception("Unknown format of propertiy '" + paramname
					+ "' in file '" + filePath + "'\n   " + e.getMessage());
		}

		return retmatrix;
	}

	/**
	 * Parse a double matrix
	 * @param valstr
	 * @return
	 */
	public static double[][] stringToDoubleMatrix(String valstr) {
		double[][] retmatrix = null;

		// Remove all CRs and LFs, if exists:
		String valstr1 = valstr.replace("\n", "").replace("\r", "");
		// Split to rows:
		String rows[] = valstr1.trim().split(";");

		// Allocate row of retmatrix:
		retmatrix = new double[rows.length][];
		// Proceed row for row:
		for (int row = 0; row < rows.length; row++) {
			// Change colons to spaces:
			String rowstr = rows[row].replace(",", " ");
			// Split at white spaces:
			String[] rowarray = rowstr.trim().split("\\s+");

			// Allocate columns of retmatrix:
			retmatrix[row] = new double[rowarray.length];
			// Convert string array to double array:
			for (int col = 0; col < rowarray.length; col++) {
				retmatrix[row][col] = Double.parseDouble(rowarray[col]);
			}
		}
		return retmatrix;
	}

	/**
	 * Get property by name. The value of the property must be a material name.
	 * <p>
	 * Format:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; fluid name.<br />
	 * Example:<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;entry
	 * key="PARAMNAME"&gt;Water&lt;/entry&gt;
	 * 
	 * @param paramname
	 *            Name of the property.
	 * @return the material object
	 * @throws Exception
	 *             if the property could not be found or if the value could not
	 *             be converted to a double value.
	 */
	public Material getMaterial(String paramname) throws Exception {
		String valstr = props.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '"
					+ filePath + "'!");
		}

		try {
			return (new Material(valstr));
		} catch (Exception e) {
			throw new Exception("Unknown format of propertiy '" + paramname
					+ "' in file '" + filePath + "'\n   " + e.getMessage());
		}
	}

	/**
	 * Sets the property "name" to "value"
	 * 
	 * @param name
	 * @param value
	 */
	public void setValue(String name, Object value) {
		props.setProperty(name, value.toString());
	}

	/**
	 * Sets the property "name" to "value"
	 * 
	 * @param name
	 * @param value
	 */
	public void setValue(String name, String value) {
		props.setProperty(name, value);
	}

	/**
	 * Sets the property "name" to "value"
	 * 
	 * @param name
	 * @param value
	 */
	public void setValue(String name, double value) {
		if(null!=props)
			props.setProperty(name, Double.toString(value));
	}

	/**
	 * Sets the property "name" to "value"
	 * 
	 * @param name
	 * @param value
	 */
	public void setValue(String name, boolean value) {
		props.setProperty(name, Boolean.toString(value));
	}

	/**
	 * Sets the property "name" to "value"
	 * 
	 * @param name
	 * @param value
	 */
	public void setValue(String name, double[] value) {
		String valueAsString = "";
		for (double v : value)
			valueAsString += Double.toString(v) + ",";
		
		if(null!=props)
			props.setProperty(name, valueAsString);
	}

	/**
	 * Set the stated value
	 * 
	 * @param name
	 * @param value
	 */
	public void setValue(String name, PhysicalValue value) {
		props.setProperty(name, value.toString());
	}

	/**
	 * Delete the stated value
	 * @param name
	 */
	public void deleteValue(String name) {
		props.remove(name);
	}

	/**
	 * Saves the defined properties in an xml file
	 * 
	 * @throws IOException
	 */
	public void saveValues() throws IOException {
		OutputStream ioStream = new FileOutputStream(filePath);
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
			throw new Exception("No propertiy '" + paramname + "' found in '"
					+ filePath + "'!");
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
			for (int i = 0; i < strarray.length; i++) {
				retarray[i] = strarray[i];
			}
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Unknown format of propertiy '"
					+ paramname + "' in file '" + filePath + "'\n   "
					+ e.getMessage());
		}

		return retarray;
	}

	/**
	 * @param valstr
	 * @return
	 */
	public static double[] parseDoubleArray(String valstr) {
		double[] retarray = null;

		// Remove semicolon at the end, if exists:
		String valstr1 = valstr.replace(";", "");
		// Change colons to spaces:
		String valstr2 = valstr1.replace(",", " ");
		// Split at white spaces:
		String[] strarray = valstr2.trim().split("\\s+");

		// Convert string array to double array:
		retarray = new double[strarray.length];
		for (int i = 0; i < strarray.length; i++) {
			retarray[i] = Double.parseDouble(strarray[i]);
		}

		return retarray;
	}

	/**
	 * @return Array of available keys
	 */
	public ArrayList<String> getKeys() {
		ArrayList<String> keys = new ArrayList<String>();

		if (null != props) {
			Enumeration<Object> enuKeys = props.keys();
			while (enuKeys.hasMoreElements())
				keys.add((String) enuKeys.nextElement());
		}

		return keys;
	}

	/**
	 * Reading values with error handling
	 * 
	 * @param paramname
	 * @param defVal
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(String paramname, T defVal) throws Exception {

		T value = defVal;

		try {
			if (value instanceof String)
				value = (T) getString(paramname);
			else if (value instanceof Double)
				value = (T) getDoubleValue(paramname);
			else if (value instanceof PhysicalValue)
				value = (T) getPhysicalValue(paramname);
			else if (value instanceof Integer)
				value = (T) getIntValue(paramname);
			else if (value instanceof Boolean)
				value = (T) getBooleanValue(paramname);
			else if (value instanceof Double[])
				value = (T) getDoubleArray(paramname);
			else if (value instanceof Double[][])
				value = (T) getDoubleMatrix(paramname);
			else if (value instanceof Material)
				value = (T) getMaterial(paramname);
			else if (value instanceof String[])
				value = (T) getStringArray(paramname);
			else
				throw new Exception("ConfigReader: No action defined for type "
						+ defVal.getClass().getSimpleName());
		} catch (Exception e) {
			System.err.println(e.getMessage());
			
			if((value instanceof String[]))
				if(((String[]) value).length==0)
					return value;
			
			System.out.println("ConfigReader: " + filePath
					+ ": Creating new parameter '" + paramname
					+ "' with value '" + defVal + "'");
					
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
