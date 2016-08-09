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

public class GraphMidMouseEventHandler extends PPanEventHandler {
	
	public GraphMidMouseEventHandler(){
		super();
		setEventFilter(new PInputEventFilter(InputEvent.BUTTON2_MASK));
	}
}
