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
import java.util.ArrayList;

import org.piccolo2d.extras.swt.PSWTPath;

import ch.ethz.inspire.emod.gui.ModelGraphGUI;
import ch.ethz.inspire.emod.utils.IOConnection;

public class ConnectionLine extends PSWTPath{

	private static final long serialVersionUID = 1L;
	private PSWTPath sourceNode=null, targetNode=null;
	private IOConnection ioc;
	
	public ConnectionLine(IOConnection ioc){
		super();
		
		this.ioc = ioc;
		
		ArrayList<AIONode> nodes = ModelGraphGUI.getIONodes();
		
		/* Find the source and target node */
		for(AIONode ion: nodes) {
			if(ion.getIOObject().equals(ioc.getSource().getReference()))
				sourceNode = ion.getIONode();
			else if (ion.getIOObject().equals(ioc.getTarget().getReference()))
				targetNode = ion.getIONode();
			
			if(sourceNode!=null & targetNode!=null)
				break;
		}
		
		update();
		
		
	}
	
	public void update(){
		
		if(sourceNode==null | targetNode==null)
			return;
		
		Point2D[] points = new Point2D[2];
		
		points[0] = sourceNode.getGlobalTranslation();
		points[1] = targetNode.getGlobalTranslation();
		
		points[0] = sourceNode.getGlobalFullBounds().getCenter2D();
		points[1] = targetNode.getGlobalFullBounds().getCenter2D();
		
		
		this.setStrokeColor(ModelGraphGUI.getIOColor(ioc.getSource()));
		
		this.setPathToPolyline(points);
	}
	
	@Override
	public void removeFromParent(){
		ModelGraphGUI.removeConnection(this);
	}
	
	public IOConnection getIOConnection(){
		return ioc;
	}
	
	public PSWTPath getSourceNode(){
		return sourceNode;
	}
	
	public PSWTPath getTargetNode(){
		return targetNode;
	}
}
