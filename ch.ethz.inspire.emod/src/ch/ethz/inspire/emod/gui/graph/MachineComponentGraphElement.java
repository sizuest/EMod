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

/**
 * MachineComponentGraphElement
 * 
 * Graphical representation of a {@link MachineComponent} in the graphical
 * model representation:
 * 
 *           Name 
 * -------------------------- 
 * |Input 1         Output 1|
 * |Input 2         Output 2|
 * |Input 3         Output 3|
 * |Input 4         Output 4| 
 * --------------------------
 *           Type
 * 
 * @author sizuest
 * 
 */
public class MachineComponentGraphElement extends AGraphElement {

	private static final long serialVersionUID = 1L;

	/* Machine component to be represented */
	protected MachineComponent machineComponent;
	/* IONodes of the component */
	protected ArrayList<AIONode> ioNodes;
	/* Component name */
	protected PSWTText name;
	/* Component type */
	protected PSWTText type;
	/* Surrounding box */
	protected PSWTPath box;
	/* Input and output list */
	PComposite inputs, outputs;

	/**
	 * Constructor
	 * 
	 * @param mc {@link MachineComponent} to be represented
	 */
	public MachineComponentGraphElement(MachineComponent mc) {
		super();

		this.machineComponent = mc;

		ioNodes = new ArrayList<AIONode>();

		/* Set name and type */
		name = new PSWTText(this.machineComponent.getName());
		type = new PSWTText(this.machineComponent.getComponent().getModelType()
				+ ": " + this.machineComponent.getComponent().getType());

		name.setFont(new Font(name.getFont().getFamily(), Font.BOLD, name
				.getFont().getSize()));
		type.setFont(new Font(type.getFont().getFamily(), Font.ITALIC, type
				.getFont().getSize()));

		/* Combine all inputs/outputs in a PComposite to faciliate the handling */
		inputs = getInputList();
		outputs = getOutputList();
		align();

		/*
		 * If the component has one ore more states, indicate this by drawing a
		 * shadow under the box
		 */
		if (null != mc.getComponent().getDynamicStateList()) {
			final PSWTPath shadow = PSWTPath
					.createRectangle(
							0,
							-5,
							(int) (inputs.getWidth() + outputs.getWidth()) + 20,
							(int) Math.max(inputs.getHeight(),
									outputs.getHeight()) + 20);
			shadow.setPaint(Color.BLACK);
			this.addChild(shadow);
		}

		/* Draw the box */
		box = PSWTPath.createRectangle(-5, -10,
				(int) (inputs.getWidth() + outputs.getWidth()) + 20,
				(int) Math.max(inputs.getHeight(), outputs.getHeight()) + 20);

		/* Move the outputs to the right */
		outputs.setOffset(inputs.getWidth() + 20, 0);

		/* Update the title and adapt the bounds */
		updateText();
		this.setBounds(-5, -10, box.getWidth(), box.getHeight());

		/* Put everything together */
		this.addChild(box);
		this.addChild(inputs);
		this.addChild(outputs);
		this.addChild(type);
		this.addChild(name);

		/* Rotate everything */
		rotate(0);
	}

	/**
	 * Align the in- and outputs as follows: If the rotation is between -90° to
	 * 90°: just turn the initial element (left: inputs, right: outputs)
	 * otherwise: flip the element (left: outputs, right: inputs)
	 */
	private void align() {
		PComposite left = inputs, right = outputs;
		int width = 0;

		/* Check if a switch is required */
		if (machineComponent.getPosition().getRotate() >= .25
				& machineComponent.getPosition().getRotate() < .75) {
			left = outputs;
			right = inputs;
		}

		/* Set IONode on the right side */
		for (int i = 0; i < left.getChildrenCount(); i++)
			if (left.getChild(i) instanceof AIONode)
				((AIONode) (left.getChild(i))).setLeft();

		for (int i = 0; i < right.getChildrenCount(); i++)
			if (right.getChild(i) instanceof AIONode)
				((AIONode) (right.getChild(i))).setRight();

		/* Adapt offsets to get them aligned to the left */
		for (int i = 0; i < left.getChildrenCount(); i++)
			left.getChild(i).setOffset(0, left.getChild(i).getYOffset());

		/* Adapt offsets to get them aligned to the right */
		for (int i = 0; i < right.getChildrenCount(); i++)
			width = (int) Math.max(width, right.getWidth());
		for (int i = 0; i < right.getChildrenCount(); i++)
			right.getChild(i).setOffset(width - right.getChild(i).getWidth(),
					right.getChild(i).getYOffset());

		/* Set offsets */
		left.setOffset(0, 0);
		right.setOffset(left.getWidth() + 20, 0);
	}

	/**
	 * getInputList
	 * 
	 * Returns a PCOmposite containing all the graphical representations of the
	 * machine components inputs (left aligned)
	 */
	private PComposite getInputList() {
		PComposite list = new PComposite();

		int offset = 0;
		int width = 0;

		for (IOContainer io : this.machineComponent.getComponent().getInputs()) {
			if (!io.hasReference()) {
				InputNode input = new InputNode(io);
				input.setOffset(0, offset);
				ioNodes.add(input);
				list.addChild(input);

				offset += input.getY() + input.getHeight() + 2;
				width = (int) Math.max(width, input.getWidth());
			}
		}

		list.setBounds(0, 0, width, offset - 2);

		list.setPickable(false);

		return list;
	}

	/**
	 * getOutputList
	 * 
	 * Returns a PCOmposite containing all the graphical representations of the
	 * machine components outputs (right aligned)
	 */
	private PComposite getOutputList() {
		PComposite list = new PComposite();

		int offset = 0;
		int width = 0;

		for (IOContainer io : this.machineComponent.getComponent().getOutputs()) {
			if (!io.hasReference()) {
				OutputNode output = new OutputNode(io);
				output.setOffset(0, offset);
				ioNodes.add(output);
				list.addChild(output);

				offset += output.getY() + output.getHeight() + 2;
				width = (int) Math.max(width, output.getWidth());
			}
		}

		/* Adapt offsets to get them aligned to the right */
		for (int i = 0; i < list.getChildrenCount(); i++)
			list.getChild(i).setOffset(width - list.getChild(i).getWidth(),
					list.getChild(i).getYOffset());

		list.setBounds(0, 0, width, offset - 2);

		list.setPickable(false);

		return list;
	}

	@Override
	public void rotate(double r) {
		/* Lets start from the beginning: Rate back to initial orientaton */
		this.rotateAboutPoint(-this.getRotation(), box.getCenter());

		/* Add 90° to element position */
		machineComponent.getPosition().setRotate(
				machineComponent.getPosition().getRotate() + r);

		/*
		 * Two situations must be distingished: 1. -90° to 90°: just turn the
		 * initial element (left: inputs, right: outputs) 2. others: flip the
		 * element (left: outputs, right: inputs)
		 * 
		 * This measure shall keep the element readible, with no text turned
		 * more than +- 90°
		 */
		if (machineComponent.getPosition().getRotate() >= .25
				& machineComponent.getPosition().getRotate() < .75)
			this.rotateAboutPoint(
					(machineComponent.getPosition().getRotate() + .5) * 2
							* Math.PI, box.getCenter());
		else
			this.rotateAboutPoint(machineComponent.getPosition().getRotate()
					* 2 * Math.PI, box.getCenter());

		/* Call the alignment of the inputs and outputs */
		align();
	}

	@Override
	public ArrayList<AIONode> getIONodes() {
		return ioNodes;
	}

	@Override
	public void savePosition() {
		machineComponent.getPosition().set(this.getGlobalTranslation());
	}

	/**
	 * Return the represented MC
	 * @return
	 */
	public MachineComponent getMachineComponent() {
		return machineComponent;
	}

	/**
	 * Update all the text
	 */
	public void updateText() {
		name.setText(this.machineComponent.getName());
		type.setText(this.machineComponent.getComponent().getModelType() + ": "
				+ this.machineComponent.getComponent().getType());

		name.setOffset((box.getWidth() - 20) / 2 - name.getWidth() / 2, -15
				- name.getHeight());
		type.setOffset((box.getWidth() - 20) / 2 - type.getWidth() / 2,
				box.getHeight() - 5);

		this.repaint();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.gui.graph.AGraphElement#setSelected(boolean)
	 */
	@Override
	public void setSelected(boolean b) {
		if(b)
			box.setPaint(Color.YELLOW);
		else
			box.setPaint(Color.WHITE);
	}

}
