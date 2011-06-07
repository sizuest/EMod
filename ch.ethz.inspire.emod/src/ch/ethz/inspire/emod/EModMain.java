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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author dhampl
 *
 */
public class EModMain {
	
	public static void main(String[] args) {
		Display disp = new Display();
		//start program
		new EModMain(disp);
		
		disp.dispose();
	}

	public EModMain(Display display) {
		Shell shell = new Shell(display);
		shell.setText("EMod");
		shell.setSize(300, 200);
		shell.open();
		
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
