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
import java.io.InputStream;
import java.util.Properties;

import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Utility class to extract configuration parameters from a machine component 
 * configuration file.
 * <p>
 * The constructor opens the configuration file. Then, the user can read
 * the values of the configuration parameters by calling the corresponding methods. 
 * The user must know the type (double, double array, double matrix, ...) 
 * of the chosen parameter.
 * <p>
 * Path and filename:
 * Using the name of the machine component (this is equivalent to the
 * name of the physical model) and the type of the machine component
 * the path and filename is generated as follows:
 *   MachineComponentDB/CompenentName/CompenentName_Type.xml
 * <p>
 * File Format:<br />
 * The component configuration file must be in the following format:<br />
 * Example:<br />
 * 
 *  &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;<br />
 *  &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;<br />
 *  &lt;properties&gt;<br />
 *    &lt;comment&gt;Some comment&lt;/comment&gt;<br />
 *    &lt;entry key="DoubleParamXY"&gt;123.0&lt;/entry&gt;<br />
 *    &lt;entry key="DoubleArrayParamABC"&gt;12, 13, 14;&lt;/entry&gt;<br />
 *    &lt;entry key="DoubleMatrixParam123"&gt;<br />
 *       1.1, 2.3, 1.2;<br />
 *       7.2, 5.3, 9.8;<br />
 *    &lt;/entry&gt;<br />
 *  &lt;/properties&gt;
 * 
 * @author andreas
 *
 */
public class ComponentConfigReader {

	private Properties xmlprop;
	private String filename;
	
	/**
	 * Constructor opens the parameter definition file and reads the
	 * properties. The file must satisfy the XML format and the DTD 
	 * specification.
	 * 
	 * @param component Name of the machine component model
	 * @param type      Type of the machine component.
	 * @throws Exception if file could not be found or if an unexpected file
	 *                  format occurs.
	 */
	public ComponentConfigReader(String component, String type) throws Exception
	{	
		/* Build path and filename of file defining the model parameters.
		 * The following structure is applied:
		 *   MachineComponentDB/CompenentName/CompenentName_Type.xml
		 */
		String path_prefix = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix");
		if (path_prefix == null)
			path_prefix = "MachineComponentDB"; // set default path prefix
		filename = path_prefix + "/" + component + "/" + component + "_" + type + ".xml";
		InputStream paramstream = new FileInputStream(filename);
		
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
		xmlprop = new Properties();
		try {
			xmlprop.loadFromXML(paramstream);
		}
		catch (Exception e) {
			throw new Exception("File format error in '" + filename + "'\n    " 
					+ e.getMessage());
		}
		
		paramstream.close(); /* Close the file */
	}
	
	/**
	 * Close the properties object.
	 */
	public void Close()
	{
		// TODO Close xmlprop;
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
	public double getDoubleParam(String paramname) throws Exception
	{
		String valstr = xmlprop.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + filename + "'!");
		}
	
		try {
			return Double.parseDouble(valstr);
		}
		catch (NumberFormatException e) {
			throw new NumberFormatException("Unknown format of propertiy '" + paramname 
					+ "' in file '" + filename + "'\n   " + e.getMessage());
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
	public double[] getDoubleArrayParam(String paramname) throws Exception
	{
		String valstr = xmlprop.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No property '" + paramname + "' found in '" + filename + "'!");
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
					+ "' in file '" + filename + "'\n   " + e.getMessage());
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
	public double[][] getDoubleMatrixParam(String paramname) throws Exception
	{
		String valstr = xmlprop.getProperty(paramname);
		if (valstr == null) {
			throw new Exception("No propertiy '" + paramname + "' found in '" + filename + "'!");
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
					+ "' in file '" + filename + "'\n   " + e.getMessage());
		}
		
		return retmatrix;
	}
}
