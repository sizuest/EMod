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
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.eclipse.swt.widgets.*;

import ch.ethz.inspire.emod.gui.EModGUI;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * energy model main class
 * 
 * @author dhampl
 * 
 */
public class EModMain {
	private static Logger rootlogger = Logger.getLogger("");
	private static Logger logger = Logger.getLogger(EModMain.class.getName());

	/**
	 * Main EMod class
	 * This is the entry point to start EMod
	 * @param args
	 */
	public static void main(String[] args) {
		Display disp = new Display();

		Locale.setDefault(new Locale("de", "CH"));

		// init logging
		LogManager logManager = LogManager.getLogManager();
		logManager.reset();
		try {
			// Let the loggers write to file:
			FileHandler fh = new FileHandler(
					PropertiesHandler.getProperty("app.logfile"), 100000, 1,
					true);
			fh.setFormatter(new SimpleFormatter());
			rootlogger.addHandler(fh);
			// Set log level:
			// The following loglevel are available (from lowest to highest):
			// OFF, FINEST, FINER, FINE=DEBUG, CONFIG, INFO, WARNING, SEVERE,
			// ALL
			rootlogger.setLevel(LogLevel.DEBUG);

			// Add stdout to logger: All logging output is written to stdout
			// too.
			SimpleFormatter fmt = new SimpleFormatter();
			StreamHandler sh = new StreamHandler(System.out, fmt);
			rootlogger.addHandler(sh);

			OutputStream losOut = new LoggingOutputStream(logger, Level.INFO);
			System.setOut(new PrintStream(losOut, true));

			// Redirect stderr to logger (and then to stdout as stdout is a
			// handler of logger)
			OutputStream losErr;
			losErr = new LoggingOutputStream(Logger.getLogger("stderr"),
					Level.SEVERE);
			System.setErr(new PrintStream(losErr, true));

		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// start program
		new EModMain();

		logger.info("EModMain called EModGUI");
		new EModGUI(disp);

		// shut down
		disp.dispose();
	}

	/**
	 * Energy Model main method.
	 */
	public EModMain() {

		logger.info("Start EModMain");

	}
}
