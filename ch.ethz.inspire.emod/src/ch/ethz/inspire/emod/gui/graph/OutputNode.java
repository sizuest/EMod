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

import org.piccolo2d.extras.swt.PSWTPath;

import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * OutputNode class
 * 
 * Graphical representation of a {@link IOContainer} used as an output in the
 * graphical model representation.
 * 
 * @author sizuest
 * 
 */
public class OutputNode extends AIONode {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param ioObject
	 *            {@link IOContainer} to be represented
	 */
	public OutputNode(IOContainer ioObject) {
		super();
		this.ioObject = ioObject;
		this.ioText.setText(ioObject.getName() + " ["
				+ ioObject.getUnit().toString() + "]");

		setRight();
	}

	@Override
	public IOContainer getIOObject() {
		return this.ioObject;
	}

	@Override
	public PSWTPath getIONode() {
		return this.ioNode;
	}

	@Override
	public void updateText() {
		// Set new name and unit
		ioText.setText(ioObject.getName() + " ["
				+ ioObject.getUnit().toString() + "]");

		// Update offset to fit the new text length
		this.ioNode.setOffset(this.ioText.getWidth() + getSize(),
				ioText.getHeight() / 2 - getSize() / 2);

		// Update bounds to fit the new text length
		this.setBounds(0, 0, getSize() + 5 + ioText.getWidth(),
				ioText.getHeight());

	}

}
