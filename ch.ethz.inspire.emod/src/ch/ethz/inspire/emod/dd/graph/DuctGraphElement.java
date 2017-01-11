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
package ch.ethz.inspire.emod.dd.graph;

import java.awt.Color;

import org.piccolo2d.extras.nodes.PComposite;
import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.PSWTText;

import ch.ethz.inspire.emod.dd.model.ADuctElement;

/**
 * Representation of a ADuctElement in the graphical editor
 * @author simon
 *
 */
public class DuctGraphElement extends PComposite{

	private static final long serialVersionUID = 1L;
	
	/* Element to be represented */
	protected ADuctElement ductElement;
	
	/* Element name */
	protected PSWTText textName, textType;
	/* Surrounding box, isolation*/
	protected PSWTPath box, isolation; 
	/* Representation of the element */
	protected PComposite element;
	
	/* Input / Output */
	protected DuctGraphElementIO input, output;

	protected DuctGraph parent;
	
	
	/**
	 * Create a new graphical representation of the stated duct element
	 * @param ductElement
	 * @param parent 
	 */
	public DuctGraphElement(ADuctElement ductElement, DuctGraph parent){
		this.ductElement = ductElement;
		this.parent = parent;
		
		input  = new DuctGraphElementIO();
		output = new DuctGraphElementIO();
		
		textName   = new PSWTText(ductElement.getName());	
		
		element = new PComposite();
		
		textType = new PSWTText(ductElement.getClass().getSimpleName().replace("Duct", ""));
		element.addChild(textType);
		textType.setOffset(-textType.getBounds().getWidth()/2, 0);
		element.setBounds(-75, 0, 150, textType.getHeight());
				
		box = PSWTPath.createRectangle(	(int) element.getFullBounds().getX()-10,
										(int) element.getFullBounds().getY()-10,
										(int) element.getBounds().getWidth()+20,
										(int) element.getBounds().getHeight()+20);
		
		isolation = PSWTPath.createRectangle(	(int) element.getFullBounds().getX()-20,
												(int) element.getFullBounds().getY()-10,
												(int) element.getBounds().getWidth()+40,
												(int) element.getBounds().getHeight()+20);
		isolation.setPaint(Color.GRAY);
		
		
		
		this.setBounds(box.getFullBounds());		
		
		this.addChild(isolation);	
		this.addChild(box);
		this.addChild(input);
		this.addChild(output);
		this.addChild(textName);
		this.addChild(element);		
		update();
	}
	
	/**
	 * Updates all text and sizes
	 */
	public void update(){
		// Box
		box.setPathToRectangle(	(int) element.getBounds().getX()-10,
								(int) element.getBounds().getY()-10,
								(int) element.getBounds().getWidth()+20,
								(int) element.getBounds().getHeight()+20);
		this.setBounds(box.getBounds());
		
		// Isolation
		if(ductElement.hasIsolation())
			isolation.setPathToRectangle(	(int) element.getBounds().getX()-20,
											(int) element.getBounds().getY()-10,
											(int) element.getBounds().getWidth()+40,
											(int) element.getBounds().getHeight()+20);
		else
			isolation.setPathToRectangle(	(int) element.getBounds().getX()-10,
											(int) element.getBounds().getY()-10,
											(int) element.getBounds().getWidth()+20,
											(int) element.getBounds().getHeight()+20);
		
		// IO
		input.setOffset( box.getCenter().getX()-input.getWidth()/2,  box.getBounds().getMinY()-input.getHeight()-2);
		output.setOffset(box.getCenter().getX()-output.getWidth()/2, box.getBounds().getMaxY());
		
		// Name		
		textName.setText(ductElement.getName());
		textName.setOffset(-textName.getBounds().getWidth()-10+box.getBounds().getMinX(), box.getBounds().getCenterY()-textName.getBounds().getCenterY());		
	}
	
	/**
	 * @return
	 */
	public DuctGraphElementIO getInput(){
		return input;
	}
	
	/**
	 * @return
	 */
	public DuctGraphElementIO getOutput(){
		return output;
	}
	
	/**
	 * @return
	 */
	public ADuctElement getElement(){
		return this.ductElement;
	}
	
	/**
	 * @return
	 */
	public DuctGraph getDuctGraph(){
		return this.parent;
	}
	
	/**
	 * Remove the element from its parent
	 */
	@Override
	public void removeFromParent() {
		/*
		 * We have to do some additional taks: Remove the element from the
		 * duct and redraw */
		parent.getDuct().removeElement(this.ductElement.getName());
		parent.redraw();
		parent.updateAll();
		super.removeFromParent();
	}
	

}
