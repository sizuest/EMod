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
import org.eclipse.swt.graphics.Rectangle;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.util.PBounds;

/**
 * GraphElementPosition class
 * 
 * Implements the position of a graph element. This includes the x and y
 * coordinate, as well as the coordinate transformation between the SWT and
 * Graph coordinate systems
 * 
 * @author sizuest
 * 
 */
@XmlRootElement
public class GraphElementPosition {
	/* position in the graph element */
	@XmlElement
	private double x = 0, y = 0;
	/*
	 * Rotation of the graph 0 : 0° .5: 180° CW
	 */
	@XmlElement
	private double r = 0;

	/**
	 * Constructor for unmarshaller
	 */
	public GraphElementPosition() {}

	/**
	 * Constructor
	 * 
	 * This constructor must not be used with SWT coordinates!
	 * 
	 * @param x
	 * @param y
	 */
	public GraphElementPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Constructor
	 * 
	 * This constructor must not be used with SWT coordinates!
	 * 
	 * @param p
	 */
	public GraphElementPosition(Point p) {
		set(p);
	}

	/**
	 * Constructor
	 * 
	 * This constructor must not be used with SWT coordinates!
	 * 
	 * @param p
	 */
	public GraphElementPosition(Point2D p) {
		set(p);
	}

	/**
	 * Constructor
	 * 
	 * Sets the coordinates and applies the coordinate transformation
	 * 
	 * @param p
	 *            SWT coordinates
	 * @param parent
	 *            Graph parent
	 */
	public GraphElementPosition(Point p, PSWTCanvas parent) {
		set(p);
		// Perform tranformation
		set(get(parent));
	}

	/**
	 * Constructor
	 * 
	 * Sets the coordinates and applies the coordinate transformation
	 * 
	 * @param p
	 *            SWT coordinates
	 * @param parent
	 *            Graph parent
	 */
	public GraphElementPosition(Point2D p, PSWTCanvas parent) {
		set(p);
		set(get(parent));
	}

	/**
	 * getX
	 * 
	 * Returns the x-Coordinate (Graph coordinate system)
	 * 
	 * @return
	 */
	public double getX() {
		return x;
	}

	/**
	 * getY
	 * 
	 * Returns the y-Coordinate (Graph coordinate system)
	 * 
	 * @return
	 */
	public double getY() {
		return y;
	}

	/**
	 * get
	 * 
	 * Returns the point object (Graph coordinate system)
	 * 
	 * @return
	 */
	public Point2D get() {
		return new Point2D.Double(x, y);
	}

	/**
	 * set
	 * 
	 * Sets the point (Graph coordinate system). If not called by the apropriate
	 * constructor, this method must not be used with SWT coordinates!
	 * 
	 * @param p
	 */
	public void set(Point p) {
		this.x = p.x;
		this.y = p.y;
	}

	/**
	 * set
	 * 
	 * Sets the point (Graph coordinate system). If not called by the apropriate
	 * constructor, this method must not be used with SWT coordinates!
	 * 
	 * @param p
	 */
	public void set(Point2D p) {
		this.x = p.getX();
		this.y = p.getY();
	}

	/**
	 * Returns the position p in the canvas coordinate system
	 * 
	 * 
	 * @param parent
	 * @return
	 */
	public Point2D get(PSWTCanvas parent) {
		// Bounds of the composite
		Rectangle bounds = parent.getBounds();
		// Bounds of the field of view;
		PBounds viewBounds = parent.getCamera().getViewBounds();

		// Relative position in the composite
		double rx = this.x / bounds.width, ry = this.y
				/ bounds.height;

		// Absolute position in the canvas
		Point2D point = new Point2D.Double(
				viewBounds.x + viewBounds.width * rx, viewBounds.y
						+ viewBounds.height * ry);

		return point;

	}

	/**
	 * getRotate
	 * 
	 * Returns the angular orientation
	 * 
	 * @return
	 */
	public double getRotate() {
		return r;
	}

	/**
	 * set Rotate
	 * 
	 * Sets the angular orientation
	 * 
	 * @param r
	 */
	public void setRotate(double r) {
		this.r = r % 1.0;
	}

}
