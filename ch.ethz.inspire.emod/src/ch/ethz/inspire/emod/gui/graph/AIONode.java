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

import org.piccolo2d.extras.nodes.PComposite;
import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.PSWTText;

import ch.ethz.inspire.emod.gui.ModelGraphGUI;
import ch.ethz.inspire.emod.utils.IOContainer;

public abstract class AIONode extends PComposite {
	
	private static final long serialVersionUID = 1L;
	protected static int SIZE=10;
	
	protected IOContainer ioObject;
	protected PSWTPath ioNode;
	protected PSWTText ioText;
	
	protected AIONode(){
		super();
		
		this.ioNode = PSWTPath.createRectangle(0, 0, getSize(), getSize());
	}

	public abstract IOContainer getIOObject();
	
	public abstract PSWTPath getIONode();
	
	public static int getSize(){
		return SIZE;
	}
	
	public void setHighlight(boolean b){
		if(b)
			ioNode.setPaint(ModelGraphGUI.getIOColor(ioObject));
		else
			ioNode.setPaint(Color.WHITE);
	}

}
