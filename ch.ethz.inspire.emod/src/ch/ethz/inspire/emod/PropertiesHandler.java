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
package ch.ethz.inspire.emod;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author dhampl
 *
 */
public class PropertiesHandler {

	private static Properties prop=null;
	
	private PropertiesHandler() throws IOException {
		prop = new Properties();
		InputStream is = new FileInputStream("app.config");
		prop.load(is);
	}
	
	public static String getProperty(String property) {
		if(prop==null) {
			try {
				new PropertiesHandler();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return prop.getProperty(property);
	}
}
