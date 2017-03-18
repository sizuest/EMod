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

package ch.ethz.inspire.emod.gui.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.piccolo2d.extras.nodes.PComposite;

import ch.ethz.inspire.emod.gui.ModelGraphGUI;

/**
 * AGraphElement class
 * 
 * Definition of a generic element in the graph representing the machine
 * structure
 * 
 * @author sizuest
 * 
 */

public abstract class AGraphElement extends PComposite {

	private static final long serialVersionUID = 1L;

	/**
	 * Saves the position of all graph elements in the Machine class
	 */
	public abstract void savePosition();

	/**
	 * Rotate the element CW by the state angle 1=360°
	 */
	@Override
	public abstract void rotate(double r);

	/**
	 * Returns all graphical representations of the I/O nodes of all graph
	 * elements
	 * 
	 * @return Array of all {@link AIONode} managed by this element
	 */
	public abstract ArrayList<AIONode> getIONodes();

	/**
	 * Constructor
	 */
	public AGraphElement() {
		super();

		// Trigger an update, if the properties of the element have changed
		this.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				ModelGraphGUI.updateConnections();
			}
		});

	}

	/**
	 * Remove the element from its parent
	 */
	@Override
	public void removeFromParent() {
		/*
		 * We have to do some additional taks: Remove the element from the
		 * machine class and as well as all relevant IO links. -> Let the
		 * ModelGraphGUI Class handle the tasks.
		 */
		ModelGraphGUI.removeGraphElement(this);
		super.removeFromParent();
	}

	/**
	 * @param b
	 */
	public abstract void  setSelected(boolean b);

}
