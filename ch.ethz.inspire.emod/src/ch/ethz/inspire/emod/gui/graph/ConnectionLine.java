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
import java.awt.Paint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.piccolo2d.extras.swt.PSWTPath;

import ch.ethz.inspire.emod.gui.ModelGraphGUI;
import ch.ethz.inspire.emod.utils.IOConnection;

/**
 * ConnectionLine class
 * 
 * Representation of a {@link IOConnection} in the graphical model
 * representation
 * 
 * @author sizuest
 * 
 */
public class ConnectionLine extends PSWTPath {

	private static final long serialVersionUID = 1L;

	/* Size of the arrow in px */
	protected static int SIZE = 10;
	/* Line path */
	private PSWTPath sourceNode = null, targetNode = null;
	/* Represented IOConnection */
	private IOConnection ioc;
	/* Arrow */
	private PSWTPath arrow;
	/* Indicator for selection */
	private boolean isSelected = false;

	/**
	 * Constructor
	 * 
	 * @param ioc {@link IOConnection}
	 * @throws Exception 
	 */
	public ConnectionLine(IOConnection ioc) throws Exception{
		super();

		this.ioc = ioc;
		
		try{

			/* List of all nodes available in the model */
			ArrayList<AIONode> nodes = ModelGraphGUI.getIONodes();
	
			/* Find the source and target node */
			for (AIONode ion : nodes) {
				if (ion.getIOObject().equals(ioc.getSource().getReference()))
					sourceNode = ion.getIONode();
				else if (ion.getIOObject().equals(ioc.getTarget().getReference()))
					targetNode = ion.getIONode();
	
				// Nothing to do here
				if (sourceNode != null & targetNode != null)
					break;
			}
	
			arrow = PSWTPath.createPolyline(new float[] { -SIZE / 2, 0, SIZE / 2 },
					new float[] { SIZE, 0, SIZE });
	
			this.addChild(arrow);
	
			update();
		}
		catch (Exception e){
			throw e;
		}

	}
	
	
	private Point2D[] getPointCoordinates(){
		ArrayList<GraphElementPosition> intermPoints = ioc.getPoints();
		
		Point2D[] pointsLine = new Point2D[2+intermPoints.size()];

		// Read out center points of the source and target nodes
		pointsLine[0] = sourceNode.getGlobalFullBounds().getCenter2D();
		pointsLine[1+intermPoints.size()] = targetNode.getGlobalFullBounds().getCenter2D();
		
		// All all intermediate points (if available)
		for(int i=1; i<=intermPoints.size(); i++)
			pointsLine[i] = intermPoints.get(i-1).get();
		
		return pointsLine;
		
	}

	/**
	 * update
	 * 
	 * Triggers an update of the line:
	 * 1. Obtain the positions of the nodes in the coordinate system of the graph
	 * 2. Adapt the stroke color according to {@link ModelGraphGUI}
	 * 3. Redraw the line
	 */
	public void update() {

		if (sourceNode == null | targetNode == null)
			return;

		// Set stroke color
		if(isSelected)
			this.setStrokeColor(Color.MAGENTA);
		else
			this.setStrokeColor(getColor());
		
		
		// Draw a smooth Bezier curve through the points:		
		Point2D[] pointsLine = getPointCoordinates(); 
		
		double rxLast = 0;
		double ryLast = 0;
		double rx, ry;
		
		
		GeneralPath path = new GeneralPath();
		path.moveTo(pointsLine[0].getX(), pointsLine[0].getY());
		for(int i=1; i<pointsLine.length-1; i++){
			double r  = pointsLine[i+1].distance(pointsLine[i-1])/10;
			
			rx = (pointsLine[i+1].getX()-pointsLine[i-1].getX())/r;
			ry = (pointsLine[i+1].getY()-pointsLine[i-1].getY())/r;
			
			path.curveTo( pointsLine[i-1].getX()+rxLast, pointsLine[i-1].getY()+ryLast, 
					      pointsLine[i].getX()-rx, pointsLine[i].getY()-ry, 
					      pointsLine[i].getX(), pointsLine[i].getY());
			
			rxLast = rx;
			ryLast = ry;
		}
		
		rx = 0;
		ry = 0;
		
		path.curveTo( pointsLine[pointsLine.length-2].getX()+rxLast, pointsLine[pointsLine.length-2].getY()+ryLast, 
			          pointsLine[pointsLine.length-1].getX()+rx, pointsLine[pointsLine.length-1].getY()+ry, 
			          pointsLine[pointsLine.length-1].getX(), pointsLine[pointsLine.length-1].getY());
		
		//PFixedWidthStroke p = new PFixedWidthStroke(2);

		//this.setPathToPolyline(pointsLine);
		this.setShape(path);

		this.setOffset(0, 0);
		

		// Lets start with the initial orientation
		arrow.setRotation(0);

		// Move arrow to the end of the line
		arrow.setGlobalTranslation(targetNode.getGlobalFullBounds()
				.getCenter2D());

		// Determine the angle of the line end
		double theta, x, y;
		x = pointsLine[pointsLine.length-1].getX() - pointsLine[pointsLine.length-2].getX()-rxLast;
		y = pointsLine[pointsLine.length-1].getY() - pointsLine[pointsLine.length-2].getY()-ryLast;
		theta = -Math.atan2(x, y) + Math.PI;

		arrow.rotateAboutPoint(theta, 0, 0);
		arrow.setStrokeColor(this.getStrokePaint());
		arrow.setPaint(this.getStrokePaint());

		// Force a recalculation of the offsets
		if (null != this.getOffset())
			this.getOffset().toString();

		this.repaint();
	}

	/**
	 * @return
	 */
	public Paint getColor() {
		return ModelGraphGUI.getIOColor(ioc.getSource());
	}

	@Override
	public void removeFromParent() {
		/*
		 * We have to do some additional taks: Remove the connection from the
		 * machine class. -> Let the ModelGraphGUI Class handle the tasks.
		 */
		ModelGraphGUI.removeConnection(this);
		super.removeFromParent();
	}

	/**
	 * getIOConnection
	 * 
	 * Returns the {@link IOConnection} represented by this object
	 * 
	 * @return IOConnection
	 */
	public IOConnection getIOConnection() {
		return ioc;
	}

	/**
	 * getSourceNode
	 * 
	 * Returns the source node object
	 * 
	 * @return
	 */
	public PSWTPath getSourceNode() {
		return sourceNode;
	}

	/**
	 * getTargetNode
	 * 
	 * Returns the target node object
	 * 
	 * @return
	 */
	public PSWTPath getTargetNode() {
		return targetNode;
	}
	
	/**
	 * get the list of all point elements
	 * @return 
	 */
	
	public ArrayList<ConnectionLinePoint> getPoints(){
		ArrayList<ConnectionLinePoint> points = new ArrayList<ConnectionLinePoint>();
		
		for(GraphElementPosition p: ioc.getPoints())
			points.add(new ConnectionLinePoint(this, p));
		
		return points;
	}

	/**
	 * @param position
	 * @return 
	 */
	public int addPoint(Point2D position) {
		/*
		 * We have to go through all positions, and determine where to add the new point
		 */
		Point2D[] pointsLine = getPointCoordinates();
		
		if(pointsLine.length == 2){
			this.ioc.getPoints().add(new GraphElementPosition(position));
			return 0;
		}
		
		double rx, ry;
		for(int i=0; i<pointsLine.length-1; i++){
			
			rx = (pointsLine[i].getX()-position.getX()) / (pointsLine[i].getX()-pointsLine[i+1].getX()); 
			ry = (pointsLine[i].getY()-position.getY()) / (pointsLine[i].getY()-pointsLine[i+1].getY());
			
			if(Math.abs(rx)<1 & Math.abs(rx/ry-1)<.1) {
				this.ioc.getPoints().add(i, new GraphElementPosition(position));
				return i;
			}
			
		}
		
		return -1;
	}

	/**
	 * @param position
	 */
	public void removePoint(GraphElementPosition position) {
		for(int i=ioc.getPoints().size()-1; i>=0; i--){
			if(position.equals(ioc.getPoints().get(i)))
				ioc.getPoints().remove(i);
		}
	}


	/**
	 * @param b
	 */
	public void setSelected(boolean b) {
		
		isSelected = b;				
		update();
	}
}
