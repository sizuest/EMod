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

import org.piccolo2d.extras.swt.PSWTPath;

/**
 * @author simon
 *
 */
public class ConnectionLinePoint extends PSWTPath{
	
	private static final long serialVersionUID = 1L;
	/* Point to be represented */
	protected GraphElementPosition position;
	protected ConnectionLine parent;
	
	private static int SIZE=10;
	
	/**
	 * @param parentLine
	 * @param position
	 */
	public ConnectionLinePoint(ConnectionLine parentLine, GraphElementPosition position){
		super();
		
		this.parent = parentLine;
		this.position = position;
		
		int localSize = (int)(SIZE * this.getGlobalScale());
		
		System.out.println(this.getGlobalScale());
		
		this.setPathToEllipse(-localSize/2, -localSize/2, localSize, localSize);
		this.setPaint(parentLine.getColor());
		
		this.setTransparency(0.5f);
		
		this.setGlobalTranslation(position.get());
		
	}
	
	/**
	 * 
	 */
	public void readPosition(){
		position.set(this.getGlobalBounds().getCenter2D());
		parent.update();
	}
	
	@Override
	public void removeFromParent(){
		parent.removePoint(this.position);
		parent.update();
		
		super.removeFromParent();
	}
	
	/**
	 * Same as removeFromParent() but does not delete the points ccordinate from the line
	 */
	public void removeFromParentOnly(){
		super.removeFromParent();
	}

	/**
	 * @param b
	 */
	public void setSelected(boolean b) {
		if(b)
			this.setPaint(Color.YELLOW);
		else
			this.setPaint(parent.getColor());
	}
	
	
	
	

}
