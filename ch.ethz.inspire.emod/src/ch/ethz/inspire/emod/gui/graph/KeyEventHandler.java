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

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

/**
 * KeyEventHandler class
 * 
 * Handles the following key event for the model graph: - SPACE: Zoom to shown
 * the whole model
 * 
 * @author sizuest
 * 
 */
public class KeyEventHandler implements KeyListener {

	/* Graph to perform actions on */
	private IGraphEditable parent;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Model graph to apply listener
	 */
	public KeyEventHandler(IGraphEditable parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.character) {
		/* SPACE: Show all */
		case ' ':
			parent.showAll();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) { /* Not used */
	}

}
