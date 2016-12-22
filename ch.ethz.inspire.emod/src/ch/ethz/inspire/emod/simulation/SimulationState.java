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

import ch.ethz.inspire.emod.States;

/**
 * Handles the machine state of a simulation. A list of times and machine states
 * are read from a file and stored in a list. For a given time the corresponding
 * machine state can be asked for. And the end of the simulation is determined.
 * <p>
 * File format of the input list:<br />
 * Duration1 , State1 ;<br />
 * Duration2 , State2 ;<br />
 * The duration corresponds to the duration of the state in s.<br />
 * The sum of all durations is the length of the simulation.<br />
 * A comment line begins with '#'.
 * 
 * @author andreas
 * 
 */
public class SimulationState {

	/* Variables */
	private double endtime = 0.0; /* End of simulation in [s] */
	/* List mapping the times of a state change to the next state. */
	// private ArrayList<TimeStateMapper<MachineState>> timeStateMap = null;
	private int actualindex; /* Index of actual (time,state) in list */
	private double nextStateChgTime; /* Time, when next state change occurs. */
	private MachineState actualstate; /* Actual state */

	/**
	 * Constructor: Reads state list from a file and sets the variables
	 * (endtime, actualstate, nextStateChgTime).
	 * 
	 * @param machineName
	 * @param simConfigName
	 * 
	 */
	public SimulationState(String machineName, String simConfigName) {

		readSimulationStates(machineName, simConfigName);
	}

	/**
	 * Get state at actual time. Note: Calling this method multiple times, the
	 * time parameter must be increasing. The previous machine state cannot be
	 * restored.
	 * 
	 * @param time
	 *            Time for the desired sate.
	 * @return The machine state at time 'time'.
	 */
	public MachineState getState(double time) {
		/*
		 * Comparison with a number a little bit smaller than 0, due to
		 * numerical precision of doubles.
		 */
		if (time - nextStateChgTime > -0.0001) {
			while ((time - nextStateChgTime > -0.0001)
					&& (actualindex + 1 < States.getStateCount())) {
				/* Update actual state to next state. */
				actualindex++;
				nextStateChgTime = States.getTime(actualindex);
				actualstate = States.getState(actualindex);
			}
			/*
			 * Note: If time > endtime: The last state is repeated. No exception
			 * is thrown.
			 */
		}
		return actualstate;
	}

	/**
	 * Get end time of simulation.
	 * 
	 * @return end time of simulation.
	 */
	public double simEndTime() {
		return endtime;
	}

	/**
	 * reads machine states from file.
	 * <p>
	 * syntax: time[s],{@link SimulationState};time[s],{@link SimulationState}
	 * ;...;<br />
	 * Comment lines begin with '#'.<br />
	 * Spaces are allowed.
	 * 
	 * @param file
	 */
	private void readSimulationStates(String machineName, String simConfigName) {

		States.readStates(machineName, simConfigName);

		endtime = States.getTime(States.getStateCount() - 1);
		actualindex = 0;
		nextStateChgTime = States.getTime(actualindex);
		actualstate = States.getState(actualindex);

	}

}
