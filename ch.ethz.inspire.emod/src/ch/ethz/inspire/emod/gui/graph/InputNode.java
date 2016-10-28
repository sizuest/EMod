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

/**
 * InputNode class
 * 
 * Graphical representation of a {@link IOContainer} used as an input in the
 * graphical model representation.
 * 
 * @author sizuest
 *
 */
public class InputNode extends AIONode{

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor 
	 * 
	 * The graphical representation will look as follows:
	 *                
	 *  Name [Unit]  O
	 *  
	 *    |__ Text   |
	 *               |__ IONode
	 * 
	 * @param ioObject {@link IOContainer} to be represented
	 */
	public InputNode(IOContainer ioObject){
		super();
		this.ioObject = ioObject;
		this.ioText = new PSWTText(ioObject.getName()+" ["+ioObject.getUnit().toString()+"]");
		this.ioText.setFont(new Font(this.ioText.getFont().getFamily(), Font.PLAIN, (int)(this.ioText.getFont().getSize()*.75)));
		
		this.ioNode.setOffset(-getSize()-5,ioText.getHeight()/2-getSize()/2);
		this.ioText.setOffset(0, 0);
		
		this.addChild(ioNode);
		this.addChild(ioText);
		this.setBounds(-getSize()-5, 0, getSize()+5+ioText.getWidth(), ioText.getHeight());
	}
	
	public IOContainer getIOObject(){
		return this.ioObject;
	}
	
	public PSWTPath getIONode(){
		return this.ioNode;
	}
	
	public void updateText(){
		// Set new name and unit
		ioText.setText(ioObject.getName()+" ["+ioObject.getUnit().toString()+"]");
		
		// Update offset to fit the new text length
		this.ioNode.setOffset(this.ioText.getWidth()+getSize(), ioText.getHeight()/2-getSize()/2);
		
		// Update bounds to fit the new text length
		this.setBounds(-getSize()-5, 0, getSize()+5+ioText.getWidth(), ioText.getHeight());
	
	}

}
