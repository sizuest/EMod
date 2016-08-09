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

import java.awt.Font;

import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.PSWTText;

import ch.ethz.inspire.emod.utils.IOContainer;

public class OutputNode extends AIONode{

	private static final long serialVersionUID = 1L;
	
	
	public OutputNode(IOContainer ioObject){
		super();
		this.ioObject = ioObject;
		this.ioText = new PSWTText(ioObject.getName()+" ["+ioObject.getUnit().toString()+"]");
		this.ioText.setFont(new Font(this.ioText.getFont().getFamily(), Font.PLAIN, (int)(this.ioText.getFont().getSize()*.75)));

		this.ioNode.setOffset(this.ioText.getWidth()+getSize(), ioText.getHeight()/2-getSize()/2);
		
		this.addChild(ioNode);
		this.addChild(ioText);
		this.setBounds(0, 0, getSize()+5+ioText.getWidth(), ioText.getHeight());
	}
	
	
	public IOContainer getIOObject(){
		return this.ioObject;
	}
	
	public PSWTPath getIONode(){
		return this.ioNode;
	}
	
	

}
