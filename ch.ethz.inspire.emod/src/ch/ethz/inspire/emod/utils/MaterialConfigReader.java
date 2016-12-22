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

import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Utility class to extract configuration parameters from a material
 * configuration file.
 * <p>
 * The constructor opens the configuration file. Then, the user can read the
 * values of the configuration parameters by calling the corresponding methods.
 * The user must know the type (double, double array, double matrix, ...) of the
 * chosen parameter.
 * <p>
 * Path and filename: Using the name of the machine component (this is
 * equivalent to the name of the physical model) and the type of the machine
 * component the path and filename is generated as follows:
 * MachineComponentDB/CompenentName/CompenentName_Type.xml
 * <p>
 * File Format:<br />
 * The component configuration file must be in the following format:<br />
 * Example:<br />
 * 
 * &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;<br />
 * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;<br />
 * &lt;properties&gt;<br />
 * &lt;comment&gt;Some comment&lt;/comment&gt;<br />
 * &lt;entry key="DoubleParamXY"&gt;123.0&lt;/entry&gt;<br />
 * &lt;entry key="DoubleArrayParamABC"&gt;12, 13, 14;&lt;/entry&gt;<br />
 * &lt;entry key="DoubleMatrixParam123"&gt;<br />
 * 1.1, 2.3, 1.2;<br />
 * 7.2, 5.3, 9.8;<br />
 * &lt;/entry&gt;<br />
 * &lt;/properties&gt;
 * 
 * @author andreas
 * 
 */
public class MaterialConfigReader extends ConfigReader {

	/**
	 * Constructor opens the parameter definition file and reads the properties.
	 * The file must satisfy the XML format and the DTD specification.
	 * 
	 * @param component
	 *            Name of the machine component model
	 * @param type
	 *            Type of the machine component.
	 * @throws Exception
	 *             if file could not be found or if an unexpected file format
	 *             occurs.
	 */
	public MaterialConfigReader(String component, String type) throws Exception {
		/*
		 * Build path and filename of file defining the model parameters. The
		 * following structure is applied:
		 * MachineComponentDB/CompenentName/CompenentName_Type.xml
		 */
		String path_prefix = PropertiesHandler
				.getProperty("app.MaterialDBPathPrefix");
		if (path_prefix == null)
			path_prefix = "MaterialDB"; // set default path prefix
		filePath = path_prefix + "/" + component + "_" + type + ".xml";

		ConfigReaderOpen();
	}

	/**
	 * Constructor opens the parameter definition file and reads the properties.
	 * The file must satisfy the XML format and the DTD specification.
	 * 
	 * @param path
	 *            Full path of the config file
	 * @throws Exception
	 *             if file could not be found or if an unexpected file format
	 *             occurs.
	 */
	public MaterialConfigReader(String path) throws Exception {
		/*
		 * Build path and filename of file defining the model parameters.
		 */
		filePath = path;

		ConfigReaderOpen();
	}

}
