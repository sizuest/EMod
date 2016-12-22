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

package ch.ethz.inspire.emod.simulation;

import java.util.EnumMap;
import java.util.logging.Logger;

import javax.xml.bind.Unmarshaller;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.SamplePeriodConverter;
import ch.ethz.inspire.emod.utils.SimulationConfigReader;

/**
 * Process simulation control. The process definition is read from the process
 * definition file in the simulation config directory. In this file several
 * input parameters (given as time vectors) are defined. Each of these input
 * parameter vectors must correspond to a process simulation vector. A
 * difference to other simulation control classes is, that the process
 * simulation control object are created before reading the process parameters.
 * 
 * @author andreas
 * 
 */
public class ProcessSimulationControl extends ASimulationControl {

	private static Logger logger = Logger
			.getLogger(ProcessSimulationControl.class.getName());

	protected double[] processsamples;
	protected int simulationStep;

	/**
	 * Constructor by hand: name and unit are specified.
	 * 
	 * @param name
	 * @param unit
	 */
	public ProcessSimulationControl(String name, SiUnit unit) {
		this.name = name;
		this.unit = unit;

		init();
	}

	/**
	 * Empty constructor for JABX
	 */
	public ProcessSimulationControl() {
		/* name and unit are set by JABX */
	}

	@Override
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		init();
	}

	/**
	 * Called from the different constructors.
	 */
	private void init() {
		simulationOutput = new IOContainer(name, unit, 0);
		if (SimulationConfigReader.SimulationConfigReaderExist(this.getClass()
				.getSimpleName(), name)) {
			/*
			 * Simulation control config file exists: read state mapping from
			 * file:
			 */
			readConfig();
		}

		/*
		 * Make default mapping: Component state OFF for all Machine states. The
		 * samples and state for during the process is set after reading the
		 * process definition.
		 */
		stateMap = new EnumMap<MachineState, ComponentState>(MachineState.class);
		for (MachineState ms : MachineState.values()) {
			if (ms == MachineState.PROCESS) {
				stateMap.put(ms, ComponentState.PERIODIC);
			} else {
				stateMap.put(ms, ComponentState.OFF);
			}
		}

		/* Init state: OFF */
		state = ComponentState.OFF;
		simulationStep = 0;
		processsamples = null;
	}

	/**
	 * sets and maps the {@link MachineState} to the appropriate
	 * {@link SimulationState}
	 * 
	 * @param state
	 */
	@Override
	public void setState(MachineState state) {
		if (this.state != stateMap.get(state)) {
			super.setState(state);
			simulationStep = 0;
		}
	}

	/**
	 * Method to provide the next sample.
	 */
	@Override
	public void update() {
		if (state == ComponentState.PERIODIC) {
			/* During process the samples are repeated periodically. */
			double sample = processsamples[simulationStep];
			simulationOutput.setValue(sample);
			simulationStep = (simulationStep + 1) % processsamples.length;
		} else {
			/* The value is zero if we are not in the process state. */
			simulationOutput.setValue(0);
		}
	}

	/**
	 * Set the process samples
	 * 
	 * @param samps
	 *            Process samples
	 * @param time
	 */
	public void setProcessSamples(double[] samps, double[] time) {
		simulationPeriod = Math.max(
				Algo.greatestCommonDivisor(Algo.getIncrements(time)), 1);
		processsamples = SamplePeriodConverter.convertSamples(simulationPeriod,
				time, samps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.inspire.emod.simulation.ASimulationControl#setSimulationPeriod
	 * (double)
	 */
	@Override
	public void setSimulationPeriod(double periodLength) {

		/* Resample the samples if the sampleperiod changed. */
		if (simulationPeriod != periodLength) {
			logger.log(LogLevel.DEBUG, "Resamling from" + simulationPeriod
					+ " to " + periodLength);
			try {
				processsamples = SamplePeriodConverter.convertSamples(
						simulationPeriod, periodLength, processsamples, false);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			simulationPeriod = periodLength;
		}
	}
}
