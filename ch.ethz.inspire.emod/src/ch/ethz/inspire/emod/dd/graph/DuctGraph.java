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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

import org.piccolo2d.extras.nodes.PComposite;
import org.piccolo2d.util.PBounds;

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.dd.model.ADuctElement;
import ch.ethz.inspire.emod.dd.model.DuctBypass;

/**
 * Representation of a Duct in the graphical editor
 * @author simon
 *
 */
public class DuctGraph extends PComposite {

	private static final long serialVersionUID = 1L;
	
	/* Duct to be represented */
	protected Duct duct;
	/* Main graph */
	protected DuctGraph mainGraph;
	
	protected ArrayList<DuctGraphElement> elements = new ArrayList<DuctGraphElement>();	
	
	/**
	 * @param duct
	 * @param mainGraph 
	 */
	public DuctGraph(Duct duct, DuctGraph mainGraph){
		this.duct = duct;
		this.mainGraph = mainGraph;
		
		redraw();
	}
	
	/**
	 * Get the main graph
	 * @return 
	 */
	public DuctGraph getMainGraph(){
		if(null!=mainGraph)
			return mainGraph;
		else
			return this;
	}
	
	/**
	 * Updates graph starting from the source graph
	 */
	public void updateAll(){
		if(null!=mainGraph)
			mainGraph.update();
		else
			update();
	}
	
	/**
	 * Updates all elements
	 */
	public void update(){
		
		// Reposition elements
		int lastPos = 0;
		
		for (DuctGraphElement e: elements) {
			e.update();
			e.setOffset(-e.getBounds().getCenterX(), -(e.getBounds().getMinY()-lastPos));
			lastPos += e.getBounds().getHeight();
			
			
		}
		
		// Update bounds
		PBounds ductBounds = new PBounds(0, 0, 0, 0);
		
		for(int i=0; i<this.getChildrenCount(); i++){
			ductBounds.add(this.getChild(i).getFullBounds());
		}		
		this.setBounds(ductBounds);
		
	}
	
	/**
	 * Redraws all elements
	 */
	public void redraw(){
		
		this.removeAllChildren();
		
		// Add elements
		for(ADuctElement e: duct.getElementsExceptFittings()){
			DuctGraphElement ge;
			
			if(e instanceof DuctBypass)
				ge = new DuctGraphBypass(e, this);
			else
				ge = new DuctGraphElement(e, this);
			elements.add(ge);
			this.addChild(ge);
		}
		
		if(elements.size()==0){
			this.addChild(new DuctGraphDummy(this));
		}
		
		// Update the positions
		update();
		
	}

	/**
	 * @param duct
	 */
	public void redraw(Duct duct) {
		this.duct = duct;
		redraw();
	}
	
	@Override
	public void removeAllChildren(){
		super.removeAllChildren();
		elements = new ArrayList<DuctGraphElement>();
	}
	
	/**
	 * Returns the input object
	 * @return 
	 */
	public DuctGraphElementIO getInput(){
		return elements.get(0).getInput();
	}
	
	/**
	 * Returns the output object
	 * @return 
	 */
	public DuctGraphElementIO getOutput(){
		return elements.get(elements.size()-1).getOutput();
	}
	
	/**
	 * Moves an element to the hovered position
	 * @param element
	 * @param position
	 */
	public void moveElement(DuctGraphElement element, Point2D position){
		
		int idx = elements.indexOf(element);
		
		
		if(element.getGlobalBounds().getMinY()>position.getY() & idx>0) {
			duct.moveElementUp(element.getElement());
			Collections.swap(elements, idx, idx-1);
		}
		else if(element.getGlobalBounds().getMaxY()<position.getY() & idx<elements.size()-1) {
			duct.moveElementDown(element.getElement());
			Collections.swap(elements, idx, idx+1);
		}
		
		this.update();
		
	} 
	
	/**
	 * Returns the index of the hovered element in the current duct
	 * @param position
	 * @return
	 */
	public int getElementIndex(Point2D position) {
		int idx = 0;
		
		for(DuctGraphElement e: elements){		
			if(e.getGlobalBounds().contains(position)){
				idx = duct.getElementIndex(e.getElement().getName());
				
				if(e.getGlobalBounds().getMinY()+e.getGlobalBounds().getHeight()/2<position.getY())
					idx+=1;
				break;
			}
		}
		
		return idx;
	}

	/**
	 * @param position
	 * @return
	 */
	public DuctGraphElement getSelection(Point2D position) {
		DuctGraphElement ret = null;
		
		for(DuctGraphElement e: elements){				
			if(e.getGlobalBounds().contains(position))
				if(e instanceof DuctGraphBypass)
					ret = ((DuctGraphBypass) e).getSelection(position);
				else
					ret = e;
			
			if(null!=ret)
				break;
		}
			
		return ret;
	}

	/**
	 * @return 
	 * 
	 */
	public Duct getDuct() {
		return duct;
	}

	/**
	 * @param p
	 * @return
	 */
	public DuctGraph getDuctGraph(Point2D p) {
		
		for(DuctGraphElement e: elements)
			if(e instanceof DuctGraphBypass)
				if(e.getGlobalBounds().contains(p)){
					DuctGraph dg = ((DuctGraphBypass) e).getDuctGraph(p);
					if(this.equals(dg))
						return this;
					else
						return dg.getDuctGraph(p);
				}

		return this;
	}

}
