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

package ch.ethz.inspire.emod.model.math;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General Gain on inputs class
 * 
 * Assumptions:
 * 
 * 
 * Inputlist: 1: Input : [var] : Input to be multiplied by gain Outputlist: 1:
 * Output : [var] : Output of the gain
 * 
 * Config parameters: Unit : [string] : Unit of the gain block Gain : [-] : Gain
 * 
 * @author simon
 * 
 */

public class Gain extends APhysicalComponent {
	protected String type = "Gain";
	@XmlElement
	protected String unit;
	@XmlElement
	protected double gain;

	// Input Lists
	private IOContainer in;

	// Output parameters:
	private IOContainer out;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public Gain() {
		super();
	}

	/**
	 * post xml init method (loading physics data)
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		init();
	}

	/**
	 * Gain constructor
	 * 
	 * @param unit
	 * @param gain
	 */
	public Gain(SiUnit unit, double gain) {
		super();

		this.unit = unit.toString();
		this.gain = gain;

		init();
	}

	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init() {
		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		in = new IOContainer("Input", new SiUnit(unit), 0);
		inputs.add(in);

		/* Define output parameters */
		outputs = new ArrayList<IOContainer>();
		out = new IOContainer("Output", new SiUnit(unit), 0);
		outputs.add(out);

		// Validate the parameters:
		try {
			checkConfigParams();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {
		// Not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		/*
		 * Output is simply the input multiplied by the gain Out = Gain * Input
		 */
		out.setValue(gain * in.getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return unit;
	}

	@Override
	public void setType(String type) {
		// TODO this.type = type;
	}

	@Override
	public void updateBoundaryConditions() {
		// TODO Auto-generated method stub

	}
}
