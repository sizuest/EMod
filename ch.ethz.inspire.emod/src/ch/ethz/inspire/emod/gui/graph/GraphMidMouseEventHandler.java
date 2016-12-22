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

import java.awt.event.InputEvent;

import org.piccolo2d.event.PInputEventFilter;
import org.piccolo2d.event.PPanEventHandler;

/**
 * GraphMidMouseEventHandler class
 * 
 * Implements the panning of the graph based on a mouse mid-click
 * 
 * @author sizuest
 * 
 */
public class GraphMidMouseEventHandler extends PPanEventHandler {

	/**
	 * Constructor
	 * 
	 * Only thing to be done here is to set an PPanEventHandler only listening
	 * to a mid-click event
	 */
	public GraphMidMouseEventHandler() {
		super();
		setEventFilter(new PInputEventFilter(InputEvent.BUTTON2_MASK));
	}
}
