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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.swt.graphics.Point;

@XmlRootElement
public class GraphElementPosition {
	@XmlElement
	private double x, y;
	
	public GraphElementPosition(){}
	
	public GraphElementPosition(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public GraphElementPosition(Point p){
		set(p);
	}
	
	public GraphElementPosition(Point2D p){
		set(p);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public Point2D get(){
		return new Point2D.Double(x, y);
	}
	
	public void set(Point p){
		this.x = p.x;
		this.y = p.y;
	}
	
	public void set(Point2D p){
		this.x = p.getX();
		this.y = p.getY();
	}
	
	
}
