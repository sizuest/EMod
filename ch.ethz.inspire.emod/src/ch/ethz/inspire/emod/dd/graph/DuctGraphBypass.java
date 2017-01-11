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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.util.PBounds;

import ch.ethz.inspire.emod.dd.model.ADuctElement;
import ch.ethz.inspire.emod.dd.model.DuctBypass;

/**
 * @author sizuest
 *
 */
public class DuctGraphBypass extends DuctGraphElement {
	
	private static final long serialVersionUID = 1L;
	
	private DuctGraph ductGraph1, ductGraph2;
	
	private PSWTPath lineTop, lineBottom;

	/**
	 * @param ductElement
	 * @param parent 
	 */
	public DuctGraphBypass(ADuctElement ductElement, DuctGraph parent) {
		super(ductElement, parent);
		
		ductGraph1 = new DuctGraph(((DuctBypass) ductElement).getPrimary(),   getDuctGraph().getMainGraph()); 
		ductGraph2 = new DuctGraph(((DuctBypass) ductElement).getSecondary(), getDuctGraph().getMainGraph());	
		
		lineTop    = PSWTPath.createRectangle(0, 0, 0, 0);
		lineBottom = PSWTPath.createRectangle(0, 0, 0, 0);

		element.removeAllChildren();
		
		element.addChild(lineTop);
		element.addChild(lineBottom);
		element.addChild(ductGraph1);
		element.addChild(ductGraph2);
		
		ductGraph1.setOffset(0, 0);
		ductGraph2.setOffset(ductGraph1.getFullBounds().getMaxX()-ductGraph2.getFullBounds().getMinX()+20, 0);
		
		ductGraph1.endResizeBounds();
		ductGraph2.endResizeBounds();
				
		//textName.rotate(-Math.PI/2);
		
		update();
	}
	
	@Override
	public void update(){
		if(null!=ductGraph1){
			ductGraph1.update();
			ductGraph2.update();
			
			PBounds b = ductGraph1.getFullBounds();
			b.add(ductGraph2.getFullBounds());
			
			element.setBounds(b);
		}
		super.update();
		
		if(null!=lineTop){
			lineTop.setVisible(true);
			lineBottom.setVisible(true);
			
			AffineTransform t = getGlobalToLocalTransform(null);
			
			
			
			
			int n1 = ductGraph1.getDuct().getElementsExceptFittings().size(),
			    n2 = ductGraph2.getDuct().getElementsExceptFittings().size();
			if(n1>0 & n2>0){
				lineTop.setPathToPolyline(   new Point2D[] {ductGraph1.getInput().getCenter(),  this.getInput().getCenter(),  ductGraph2.getInput().getCenter()});
				lineBottom.setPathToPolyline(new Point2D[] {ductGraph1.getOutput().getCenter(), this.getOutput().getCenter(), ductGraph2.getOutput().getCenter()});
			}
			else if(n1>0){
				lineTop.setPathToPolyline(   new Point2D[] {ductGraph1.getInput().getCenter(),  this.getInput().getCenter()});
				lineBottom.setPathToPolyline(new Point2D[] {ductGraph1.getOutput().getCenter(), this.getOutput().getCenter()});
			}
			else if(n2>0){
				lineTop.setPathToPolyline(   new Point2D[] {this.getInput().getCenter(),  ductGraph2.getInput().getCenter()});
				lineBottom.setPathToPolyline(new Point2D[] {this.getOutput().getCenter(), ductGraph2.getOutput().getCenter()});
			}
			else {
				lineTop.setVisible(false);
				lineBottom.setVisible(false);
			}
			
			lineTop.setTransform(t);
			lineBottom.setTransform(t);
				
		}
		
		box.setStrokeColor(Color.GRAY);
		box.setPaint(Color.WHITE);
	}
	
	/**
	 * @param position 
	 * @return
	 */
	public DuctGraphElement getSelection(Point2D position){
		DuctGraphElement ret=null;
		
		// Try the primary duct
		ret = ductGraph1.getSelection(position);
		
		if(null!=ret)
			return ret;
		
		// Try the primary duct
		ret = ductGraph2.getSelection(position);
		
		if(null!=ret)
			return ret;
		
		return this;
	}

	/**
	 * @param p
	 * @return
	 */
	public DuctGraph getDuctGraph(Point2D p) {
		if(this.ductGraph1.getGlobalBounds().contains(p))
			return ductGraph1;
		else if(this.ductGraph2.getGlobalBounds().contains(p))
			return ductGraph2;
		else
			return parent;
	}


}
