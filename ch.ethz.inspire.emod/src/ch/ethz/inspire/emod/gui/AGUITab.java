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
package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.widgets.Composite;


/**
 * abstract class for guis, containing configuration steps.
 * 
 * @author sizuest
 *
 */
public abstract class AGUITab extends Composite  {
	
	protected AGUITab(Composite parent, int i){
		super(parent, i);
	}
	
	abstract public void init();
	
	@Override
	abstract public void update();

}