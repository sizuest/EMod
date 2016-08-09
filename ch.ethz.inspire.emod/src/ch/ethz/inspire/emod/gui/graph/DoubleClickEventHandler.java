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

import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;

public class DoubleClickEventHandler extends PBasicInputEventHandler{
	@Override
    public void mouseClicked(PInputEvent event) {
        if (event.getClickCount() == 2) {
            System.out.println(event.getPickedNode().toString());
        }
        event.setHandled(true);
	}	
}
