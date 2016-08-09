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

import java.awt.geom.Point2D;

import org.piccolo2d.PNode;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.extras.event.PSelectionEventHandler;
import org.piccolo2d.extras.swt.PSWTPath;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.ModelGraphGUI;

public class DragEventHandler extends PSelectionEventHandler{
	
	private AIONode sourceNode, targetNode;
	private PSWTPath line = new PSWTPath();
	private Point2D lineSource;
	
	public DragEventHandler(final PNode marqueeParent, final PNode selectableParent){
		super(marqueeParent, selectableParent);
	}
	
	@Override
	protected void dragActivityFirstStep(final PInputEvent event) {
		
		if(event.getButton()==1){
			
			sourceNode = ModelGraphGUI.getAIONode(event.getPosition());
			if(null!=sourceNode){
				
				unselectAll();
				
				lineSource = event.getCanvasPosition();
				
				if(sourceNode instanceof OutputNode)
					ModelGraphGUI.setInputHighlight(true, sourceNode.ioObject);
				else
					ModelGraphGUI.setOutputHighlight(true, sourceNode.ioObject);
				event.getCamera().addChild(line);
			}
		}
		else{
			unselectAll();
		}
			
		
    }
	
	@Override
	protected void dragActivityStep(final PInputEvent event) {
		if(null!=sourceNode)
			updateLine(event);
		
    }
	
	@Override
	protected void dragActivityFinalStep(final PInputEvent event) {
		ModelGraphGUI.setHighlight(false, null);
		line.removeFromParent();
		
		targetNode = ModelGraphGUI.getAIONode(event.getPosition());
		if(null!=sourceNode & null!=targetNode){
			if(targetNode instanceof InputNode & sourceNode instanceof OutputNode)
				Machine.addIOLink(sourceNode.getIOObject(), targetNode.getIOObject());
			else if(sourceNode instanceof InputNode & targetNode instanceof OutputNode)
				Machine.addIOLink(targetNode.getIOObject(), sourceNode.getIOObject());
			
			ModelGraphGUI.redrawConnections();
		}
		
		ModelGraphGUI.saveElementPositions();
		
		sourceNode = null;
		targetNode = null;
    }
	
	private void updateLine(PInputEvent event){
		Point2D[] points = {lineSource, event.getCanvasPosition()};
		
		line.setPathToPolyline(points);
		line.setScale(event.getCamera().getScale());
		
		line.setStrokeColor(ModelGraphGUI.getIOColor(sourceNode.getIOObject()));
		
	}
}
