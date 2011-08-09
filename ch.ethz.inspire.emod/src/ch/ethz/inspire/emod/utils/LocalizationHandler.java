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

import java.util.Locale;
import java.util.ResourceBundle;

import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * @author dhampl
 *
 */
public class LocalizationHandler {

	private static ResourceBundle handler=null;
	
	private LocalizationHandler() {
		
	}
	
	public static String getItem(String item) {
		if(handler==null) {
			Locale currentLocale = new Locale(PropertiesHandler.getProperty("app.language"), PropertiesHandler.getProperty("app.country"));
			handler = ResourceBundle.getBundle("lang/MessagesBundle", currentLocale);
		}
		return handler.getString(item);
	}
}
