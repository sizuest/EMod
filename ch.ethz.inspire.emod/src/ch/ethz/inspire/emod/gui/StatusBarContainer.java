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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Implements a container for the mian GUI
 * @author sizuest
 *
 */
public class StatusBarContainer extends Composite {

	
	/**
	 * New container 
	 * @param parent
	 * @param style
	 */
	public StatusBarContainer(Composite parent, int style) {
		super(parent, style);

		RowLayout layout = new RowLayout();
		layout.center = true;
		this.setLayout(layout);

		Label separator = new Label(this, SWT.SEPARATOR);
		RowData layoutData = new RowData();
		layoutData.height = 20;
		separator.setLayoutData(layoutData);

	}

}
