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

/**
 * @author dhampl
 * 
 */
public class PropertiesHandler {

	private static Properties prop = null;

	private PropertiesHandler() throws IOException {
		prop = new Properties();
		InputStream is = new FileInputStream("app.config");
		prop.load(is);
		is.close();
	}

	/**
	 * Returns the stated property
	 * @param property
	 * @return
	 */
	public static String getProperty(String property) {
		if (prop == null) {
			try {
				new PropertiesHandler();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return prop.getProperty(property);
	}

	/**
	 * Sets the stated property
	 * @param property
	 * @param value
	 */
	public static void setProperty(String property, String value) {
		prop.setProperty(property, value);

		OutputStream os = null;
		try {
			os = new FileOutputStream("app.config");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			prop.store(os, "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
