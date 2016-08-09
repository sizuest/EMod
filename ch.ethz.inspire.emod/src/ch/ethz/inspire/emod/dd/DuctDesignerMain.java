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
package ch.ethz.inspire.emod.dd;


import java.io.*;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.eclipse.swt.widgets.*;

import ch.ethz.inspire.emod.dd.gui.DuctDesinerGUI;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * energy model main class
 * 
 * @author dhampl
 *
 */
public class DuctDesignerMain {
	private static Logger rootlogger = Logger.getLogger("");
	private static Logger logger = Logger.getLogger(DuctDesignerMain.class.getName());
	
	public static void main(String[] args) {
		Display disp = new Display();
		
		Locale.setDefault(new Locale("de", "CH"));
		
		// init logging
		LogManager logManager = LogManager.getLogManager();
		logManager.reset();
		try {
			// Let the loggers write to file:
			FileHandler fh = new FileHandler(PropertiesHandler.getProperty("app.logfile"), 100000, 1, true);
			fh.setFormatter(new SimpleFormatter());
			rootlogger.addHandler(fh);
			// Set log level:
			// The following loglevel are available (from lowest to highest):
			// OFF, FINEST, FINER, FINE=DEBUG, CONFIG, INFO, WARNING, SEVERE, ALL
			rootlogger.setLevel(Level.FINER);
			
			// Add stdout to logger: All logging output is written to stdout too.
			SimpleFormatter fmt = new SimpleFormatter();
			StreamHandler sh = new StreamHandler(System.out, fmt);
			rootlogger.addHandler(sh);

			// Redirect stderr to logger (and then to stdout as stdout is a handler of logger)
			//OutputStream los;
			//los = new LoggingOutputStream(Logger.getLogger("stderr"), LogLevel.SEVERE);
			//System.setErr(new PrintStream(los,true));
			
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//start program
		new DuctDesignerMain();
			
		System.out.println("DuctDesignerMain called DuctDesignerGUI");
		new DuctDesinerGUI(disp);
		
		
		//shut down
		disp.dispose();
	}

	/**
	 * Energy Model main method.
	 */
	public DuctDesignerMain() {
		
		logger.info("Start DuctDesigner");
		
	}
}
