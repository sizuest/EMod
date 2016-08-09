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
import java.awt.Font;
import java.util.ArrayList;

import org.piccolo2d.extras.nodes.PComposite;
import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.PSWTText;

import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.utils.IOContainer;

public class MachineComponentGraphElement extends AGraphElement{

	private static final long serialVersionUID = 1L;
	
	protected MachineComponent machineComponent;
	protected ArrayList<AIONode> ioNodes;
	
	
	public MachineComponentGraphElement(MachineComponent mc){
		super();
		
		this.machineComponent = mc;
		
		ioNodes = new ArrayList<AIONode>();
		
        final PSWTText name = new PSWTText(this.machineComponent.getName());
        final PSWTText type = new PSWTText(this.machineComponent.getComponent().getModelType()+": "+this.machineComponent.getComponent().getType());

        name.setFont(new Font(name.getFont().getFamily(), Font.BOLD, name.getFont().getSize()));
        type.setFont(new Font(type.getFont().getFamily(), Font.ITALIC, type.getFont().getSize()));
        
        final PComposite inputs = getInputList();
        final PComposite outputs = getOutputList();
        
        if(null!=mc.getComponent().getDynamicStateList()){
            final PSWTPath shadow  = PSWTPath.createRectangle(0, -5, (int) (inputs.getWidth()+outputs.getWidth())+20, (int) Math.max(inputs.getHeight(), outputs.getHeight())+20);
            shadow.setPaint(Color.BLACK);
            this.addChild(shadow);
        }
        	
        
        final PSWTPath box  = PSWTPath.createRectangle(-5, -10, (int) (inputs.getWidth()+outputs.getWidth())+20, (int) Math.max(inputs.getHeight(), outputs.getHeight())+20);
        
        outputs.setOffset(inputs.getWidth()+20, 0);
        
        name.setOffset((box.getWidth()-20)/2-name.getWidth()/2, -15-name.getHeight());
        type.setOffset((box.getWidth()-20)/2-type.getWidth()/2, box.getHeight()-5);
   
       
        this.setBounds(-5, -10, box.getWidth(), box.getHeight());
        
        this.addChild(box);
        this.addChild(inputs);
        this.addChild(outputs);
        this.addChild(type);
        this.addChild(name);
	}
	
	private PComposite getInputList(){
		PComposite list = new PComposite();
		
		int offset = 0;
		int width  = 0;
		
		for(IOContainer io: this.machineComponent.getComponent().getInputs()){
			if(!io.hasReference()){
				InputNode input = new InputNode(io);
				input.setOffset(0, offset);
				ioNodes.add(input);
				list.addChild(input);
				
				offset += input.getY()+input.getHeight()+2;
				width = (int) Math.max(width, input.getWidth());
			}
		}
		
		list.setBounds(0, 0, width, offset-2);
		
		list.setPickable(false);
		
		return list;
	}
	
	private PComposite getOutputList(){
		PComposite list = new PComposite();
		
		
		int offset = 0;
		int width  = 0;
		
		for(IOContainer io: this.machineComponent.getComponent().getOutputs()){
			if(!io.hasReference()){
				OutputNode output = new OutputNode(io);
				output.setOffset(0, offset);
				ioNodes.add(output);
				list.addChild(output);
				
				offset += output.getY()+output.getHeight()+2;
				width = (int) Math.max(width, output.getWidth());
			}
		}
		
		for(int i=0; i<list.getChildrenCount(); i++)
			list.getChild(i).setOffset(width-list.getChild(i).getWidth(), list.getChild(i).getYOffset());
		
		list.setBounds(0, 0, width, offset-2);
		
		list.setPickable(false);
		
		return list;
	}
	
	@Override
	public ArrayList<AIONode> getIONodes(){
		return ioNodes;
	}

	@Override
	public void savePosition() {
		machineComponent.setPosition(new GraphElementPosition(this.getGlobalTranslation()));
		
	}
	
	public MachineComponent getMachineComponent(){
		return machineComponent;
	}

}
