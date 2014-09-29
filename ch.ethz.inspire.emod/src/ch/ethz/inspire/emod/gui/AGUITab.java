/***********************************
 * $Id: AEvaluationGUI.java 111 2013-11-20 09:56:10Z kraandre $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/src/ch/ethz/inspire/emod/gui/AEvaluationGUI.java $
 * $Author: kraandre $
 * $Date: 2013-11-20 10:56:10 +0100 (Mit, 20. Nov 2013) $
 * $Rev: 111 $
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


/**
 * abstract class for guis, containing configuration steps.
 * 
 * @author sizuest
 *
 */
public abstract class AGUITab extends Composite  {
	
	AGUITab(Composite parent, int i){
		super(parent, i);
	}
	
	abstract public void init();
	
	@Override
	abstract public void update();

}