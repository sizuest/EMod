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

/**
 * ConnectionLine class
 * 
 * Representation of a {@link IOConnection.java} in the graphical model representation
 * 
 * @author sizuest
 *
 */
public class ConnectionLine extends PSWTPath{

	private static final long serialVersionUID = 1L;
	
	/* Line path */
	private PSWTPath sourceNode=null, targetNode=null;
	/* Represented IOConnection */
	private IOConnection ioc;
	
	/**
	 * Constructor
	 * 
	 * @param ioc {@link IOConnection.java}
	 */
	public ConnectionLine(IOConnection ioc){
		super();
		
		this.ioc = ioc;
		
		/* List of all nodes available in the model */
		ArrayList<AIONode> nodes = ModelGraphGUI.getIONodes();
		
		/* Find the source and target node */
		for(AIONode ion: nodes) {
			if(ion.getIOObject().equals(ioc.getSource().getReference()))
				sourceNode = ion.getIONode();
			else if (ion.getIOObject().equals(ioc.getTarget().getReference()))
				targetNode = ion.getIONode();
			
			// Nothing to do here
			if(sourceNode!=null & targetNode!=null)
				break;
		}
		
		update();
		
		
	}
	
	/**
	 * update
	 * 
	 * Triggers an update of the line:
	 * 1. Obtain the positions of the nodes in the coordinate system of the graph
	 * 2. Adapt the stroke color according to {@link ModelGraphGUI.getIOColor}
	 * 3. Redraw the line
	 */
	public void update(){
		
		if(sourceNode==null | targetNode==null)
			return;
		
		Point2D[] points = new Point2D[2];
		
		// Read out center points of the source and target nodes
		points[0] = sourceNode.getGlobalFullBounds().getCenter2D();
		points[1] = targetNode.getGlobalFullBounds().getCenter2D();
		
		// Set stroke color
		this.setStrokeColor(ModelGraphGUI.getIOColor(ioc.getSource()));
		
		this.setPathToPolyline(points);
		
		this.setOffset(0, 0);
	
		// Force a recalculation of the offsets
		if(null!=this.getOffset())
			this.getOffset().toString();
		
		
		this.repaint();
	}
	
	@Override
	public void removeFromParent(){
		/* We have to do some additional taks: Remove the connection from 
		 * the machine class.
		 * -> Let the ModelGraphGUI Class handle the tasks.
		 */
		ModelGraphGUI.removeConnection(this);
	}
	
	/**
	 * getIOConnection
	 * 
	 * Returns the {@link IOConnection.java} represented by this object
	 * 
	 * @return IOConnection
	 */
	public IOConnection getIOConnection(){
		return ioc;
	}
	
	/**
	 * getSourceNode
	 * 
	 * Returns the source node object
	 * @return
	 */
	public PSWTPath getSourceNode(){
		return sourceNode;
	}
	
	/**
	 * getTargetNode
	 * 
	 * Returns the target node object
	 * @return
	 */
	public PSWTPath getTargetNode(){
		return targetNode;
	}
}
