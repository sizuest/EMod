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
import java.awt.geom.Point2D;

import org.piccolo2d.extras.nodes.PComposite;
import org.piccolo2d.extras.swt.PSWTPath;

/**
 * @author simon
 *
 */
public class DuctGraphElementIO extends PComposite {

	private static final long serialVersionUID = 1L;
	private static float SIZE=8;
	
	private PSWTPath indicator;
	
	
	/**
	 * Constructor
	 */
	public DuctGraphElementIO(){
		super();
		
		indicator = PSWTPath.createPolyline(new float[]{-SIZE/2, SIZE/2, 0, -SIZE/2 }, new float[]{0, 0, (float) (SIZE/Math.sqrt(2)), 0 });
		indicator.setPaint(Color.BLACK);
		this.addChild(indicator);
	}
	
	/**
	 * Returns the location of the indicators center in the global coordinate system
	 * @return
	 */
	public Point2D getCenter(){
		return indicator.getGlobalFullBounds().getCenter2D();
	}
}
