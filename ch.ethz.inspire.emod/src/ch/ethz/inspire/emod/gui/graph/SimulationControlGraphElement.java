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

import java.awt.Font;
import java.util.ArrayList;

import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.PSWTText;

import ch.ethz.inspire.emod.simulation.ASimulationControl;

/**
 * SimulationControlGraphElement
 * 
 * Graphical representation of a {@link ASimulationControl.java} in the graphical model
 * representation:
 * 
 * /--------\
 * | Output |
 * \--------/
 *    Type
 * 
 * @author sizuest
 *
 */
public class SimulationControlGraphElement extends AGraphElement{
	
	private static final long serialVersionUID = 1L;
	
	/* Simulation control to be represented */
	protected ASimulationControl simulationControl;
	/* IONode of the component */
	protected OutputNode node;
	/* Control type */
	protected PSWTText type;
	/* Surrounding box */
	protected PSWTPath box;
	
	/**
	 * Constructor 
	 * 
	 * @param sc {@link ASimulationControl.java} to be represented
	 */
	public SimulationControlGraphElement(ASimulationControl sc){
		super();
		
		this.simulationControl = sc;
		
		type = new PSWTText(this.simulationControl.getType());
		type.setFont(new Font(type.getFont().getFamily(), Font.ITALIC, (int) (type.getFont().getSize()*.75)));
		
        node = new OutputNode(sc.getOutput());        	
        box  = PSWTPath.createRoundRectangle(-5, -10, (float) node.getWidth()+5-AIONode.getSize(), (float) node.getHeight()+20, (float) (node.getHeight()+20), (float) (node.getHeight()+20));
        
        updateText();
        
        this.addChild(box);
        this.addChild(node);
        this.addChild(type);
	}
	
	@Override
	public ArrayList<AIONode> getIONodes(){
		ArrayList<AIONode> retr = new ArrayList<AIONode>();
		retr.add(node);
		return retr;
	}

	@Override
	public void savePosition() {
		simulationControl.setPosition(new GraphElementPosition(this.getGlobalTranslation()));
	}
	
	public ASimulationControl getSimulationControl(){
		return simulationControl;
	}

	public void updateText() {
		node.updateText();
		
		box.setPathToRoundRectangle(-5, -10, (float) node.getWidth()+5-AIONode.getSize(), (float) node.getHeight()+20, (float) (node.getHeight()+20), (float) (node.getHeight()+20));
		type.setOffset((box.getWidth()-20)/2-type.getWidth()/2, box.getHeight()-10);
		
        this.setBounds(-5, -10, box.getWidth(), box.getHeight());
        
        this.repaint();
	}

}
