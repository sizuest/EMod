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

import java.util.ArrayList;

import org.piccolo2d.extras.swt.PSWTPath;

import ch.ethz.inspire.emod.simulation.ASimulationControl;

public class SimulationControlGraphElement extends AGraphElement{
	
	private static final long serialVersionUID = 1L;
	protected ASimulationControl simulationControl;
	protected OutputNode node;
	
	
	public SimulationControlGraphElement(ASimulationControl sc){
		super();
		
		this.simulationControl = sc;
		
        node = new OutputNode(sc.getOutput());        	
        
        final PSWTPath box  = PSWTPath.createRoundRectangle(-5, -10, (float) node.getWidth()+5-AIONode.getSize(), (float) node.getHeight()+20, (float) (node.getHeight()+20), (float) (node.getHeight()+20));
                
        this.setBounds(-5, -10, box.getWidth(), box.getHeight());
        
        this.addChild(box);
        this.addChild(node);
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

}
