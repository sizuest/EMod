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


import org.eclipse.swt.widgets.*;

import ch.ethz.inspire.emod.gui.EModGUI;

/**
 * @author dhampl
 *
 */
public class EModMain {
	
	public static void main(String[] args) {
		Display disp = new Display();
		
		MessageHandler.logMessage(LogLevel.ERROR, "omg");
		//create localization handler
		
		
		//start program
		new EModGUI(disp);
		MessageHandler.closeLog();
		disp.dispose();
	}

	public EModMain(Display display) {
		
	}
}
