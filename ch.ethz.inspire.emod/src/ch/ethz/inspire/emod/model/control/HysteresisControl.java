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

package ch.ethz.inspire.emod.model.control;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General hysteresis controller class.
 * 
 * Output value takes values Low or High if any of the inputs is crossing the
 * Threshold value Low from above or the threshold value high from below.
 * 
 * Assumptions:
 * 
 * 
 * Inputlist: 1: Input : [var] : Signals in Outputlist: 1: Output : [var] :
 * Output signal
 * 
 * Config parameters: OutputLow : [var] : Output during low state OutputHigh :
 * [var] : Output during high state ThresholdLow : [var] : Lower threshold
 * ThresholdHigh : [var] : Upper threshold UnitInput : [String] : Input unit
 * UnitOutput : [String] : Output unit
 * 
 * 
 * @author andreas
 * 
 */
@XmlRootElement
public class HysteresisControl extends APhysicalComponent {

	@XmlElement
	protected String type;

	// Input parameters:
	private IOContainer inpSignal;
	// Output parameters:
	private IOContainer outSignal;

	// Parameters used by the model.
	private double outLow, outHigh;
	private double thLow, thHigh;
	private String unitIn, unitOut;

	private boolean wasLow = true;

	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public HysteresisControl() {
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
	 * Hysteresis constructor
	 * 
	 * @param type
	 */
	public HysteresisControl(String type) {
		super();

		this.type = type;
		init();
	}

	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init() {

		outputs = new ArrayList<IOContainer>();
		inputs = new ArrayList<IOContainer>();

		/* *********************************************************************** */
		/* Read configuration parameters: */
		/* *********************************************************************** */
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		String path = EModSession.getRootPath()
				+ File.separator
				+ Defines.MACHINECONFIGDIR
				+ File.separator
				+ EModSession.getMachineConfig()
				+ File.separator;
		path = path + "Control_" + type + ".xml";
		try {
			params = new ComponentConfigReader(path);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Read the config parameter: */
		try {
			outLow = params.getDoubleValue("OutputIfLow");
			outHigh = params.getDoubleValue("OutputIfHigh");
			thLow = params.getDoubleValue("LowerThreshold");
			thHigh = params.getDoubleValue("UpperThreshold");
			unitIn = params.getString("InputUnit");
			unitOut = params.getString("OutputUnit");
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */

		// Validate the parameters:
		try {
			checkConfigParams();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Define input parameters */
		inpSignal = new IOContainer("Input", new SiUnit(unitIn), 0);
		inputs.add(inpSignal);

		/* Define output parameters */
		outSignal = new IOContainer("Output", new SiUnit(unitOut), outLow);
		outputs.add(outSignal);
	}

	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
	private void checkConfigParams() throws Exception {
		// Check model parameters:
		// Check dimensions:
		if (thLow >= thHigh) {
			throw new Exception(
					"Hysteresis, type:"
							+ type
							+ ": Threshold value: Lower limit must be smaller than upper limit");
		}

		/* Try to conver units to enum */
		try {
			Unit.valueOf(unitIn);
		} catch (Exception e) {
			throw new Exception("Hysteresis, type:" + type
					+ ": Nonexistent Unit: Unit " + unitIn + " does not exist");
		}
		try {
			Unit.valueOf(unitOut);
		} catch (Exception e) {
			throw new Exception("Hysteresis, type:" + type
					+ ": Nonexistent Unit: Unit " + unitOut + " does not exist");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {

		if (wasLow && inpSignal.getValue() >= thHigh) {
			outSignal.setValue(outHigh);
			wasLow = false;
		} else if (!wasLow && inpSignal.getValue() <= thLow) {
			outSignal.setValue(outLow);
			wasLow = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void updateBoundaryConditions() {
		// TODO Auto-generated method stub

	}

}
