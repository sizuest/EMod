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

import ch.ethz.inspire.emod.dd.model.DuctDrilling;

/**
 * @author sizuest
 *
 */
public class DuctGraphDummy extends DuctGraphElement {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @param parent 
	 */
	public DuctGraphDummy(DuctGraph parent) {
		super(new DuctDrilling("Dummy"), parent);
		
		
		textName.removeFromParent();
		textType.setText("...");
		textType.setOffset(-textType.getBounds().getWidth()/2, 0);
		textType.setPaint(Color.YELLOW);
		box.setPaint(Color.YELLOW);
		
		input.setVisible(false);
		output.setVisible(false);
		
		
		update();
		
	}


}
