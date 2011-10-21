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
package ch.ethz.inspire.emod.gui.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.ethz.inspire.emod.model.MachineComponent;

/**
 * @author dhampl
 *
 */
public class MachineComponentComposite extends Composite {

	private Label nameLabel;
	private Label componentClassLabel;
	private Label typeLabel;
	
	private MachineComponent component;
	/**
	 * @param parent
	 * @param style
	 */
	public MachineComponentComposite(Composite parent, int style) {
		super(parent, style);
		nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText(component.getName());
		componentClassLabel = new Label(this, SWT.NONE);
		componentClassLabel.setText(component.getComponent().getClass().getSimpleName());
		typeLabel = new Label(this, SWT.NONE);
		typeLabel.setText(component.getComponent().getType());
		pack();
	}

}
