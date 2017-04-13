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

import java.awt.Color;
import java.awt.Font;

import org.piccolo2d.extras.nodes.PComposite;
import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.PSWTText;

import ch.ethz.inspire.emod.gui.ModelGraphGUI;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * AIONode class
 * 
 * Generic representation of an {@link IOContainer} in the grphical
 * representation of the mode. This type is intended to be child of a
 * {@link AGraphElement}. The element provides an clickable rectangle to
 * interact with the node.
 * 
 * @author sizuest
 * 
 */
public abstract class AIONode extends PComposite {

	private static final long serialVersionUID = 1L;

	/* Size of the interacting rectangle in px */
	protected static int SIZE = 10;
	/* IOContainer to be represented */
	protected IOContainer ioObject;
	/* Interacting node */
	protected PSWTPath ioNode;
	/* Describtive text (name) */
	protected PSWTText ioText;

	/**
	 * Constructor
	 */
	protected AIONode() {
		super();

		this.ioNode = PSWTPath.createRectangle(0, 0, getSize(), getSize());
		this.ioText = new PSWTText("IONode");
		this.ioText.setFont(new Font(this.ioText.getFont().getFamily(),
				Font.PLAIN, (int) (this.ioText.getFont().getSize() * .75)));
		
		this.ioText.setGreekThreshold(0);

		this.addChild(ioNode);
		this.addChild(ioText);
	}

	/**
	 * setLeft
	 * 
	 * Configures the node as a left hand element:
	 * 
	 * O Name [Unit]
	 * 
	 * | |__ Text |__ IONode
	 */
	public void setLeft() {
		this.ioNode.setOffset(-getSize() - 5, ioText.getHeight() / 2
				- getSize() / 2);
		this.setBounds(-getSize() - 5, 0, getSize() + 5 + ioText.getWidth(),
				ioText.getHeight());
	}

	/**
	 * set Right
	 * 
	 * Configures the node as a right hand element:
	 * 
	 * Name [Unit] O
	 * 
	 * |__ Text | |__ IONode
	 */
	public void setRight() {
		this.ioNode.setOffset(this.ioText.getWidth() + getSize(),
				ioText.getHeight() / 2 - getSize() / 2);
		this.setBounds(-getSize() - 5, 0, getSize() + 5 + ioText.getWidth(),
				ioText.getHeight());
	}

	/**
	 * getIOObject
	 * 
	 * Returns the represented IOContainer
	 * 
	 * @return {@link IOContainer}
	 */
	public abstract IOContainer getIOObject();

	/**
	 * getIONode
	 * 
	 * Returns the node representing the IOContainer
	 * 
	 * @return Node
	 */
	public abstract PSWTPath getIONode();

	/**
	 * getSize
	 * 
	 * Returns the node size
	 * 
	 * @return node size
	 */
	public static int getSize() {
		return SIZE;
	}

	/**
	 * setHighlight
	 * 
	 * Enables/disables the highlighting of the node. The color is chose as
	 * defined in {@link ModelGraphGUI}
	 * 
	 * @param b
	 */
	public void setHighlight(boolean b) {
		if (b)
			ioNode.setPaint(ModelGraphGUI.getIOColor(ioObject));
		else
			ioNode.setPaint(Color.WHITE);
	}

	/**
	 * updateText
	 * 
	 * Triggers an update of the descriptive text.
	 */
	public abstract void updateText();

}
