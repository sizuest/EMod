/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod;


import java.io.*;

import org.eclipse.swt.widgets.*;

import ch.ethz.inspire.emod.gui.EModGUI;

/**
 * @author dhampl
 *
 */
public class EModMain {
	
	public static void main(String[] args) {
		Display disp = new Display();
		
		MessageHandler.addLogger(new BufferedWriter(new OutputStreamWriter(System.out)));
		try {
			MessageHandler.addLogger(new BufferedWriter(new FileWriter(PropertiesHandler.getProperty("app.logfile"),true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageHandler.logMessage(LogLevel.INFO, "Program started");
		//create localization handler
		
		
		//start program
		new EModGUI(disp);
		MessageHandler.closeLog();
		disp.dispose();
	}

	public EModMain(Display display) {
		
	}
}
