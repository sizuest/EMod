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

package ch.ethz.inspire.emod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import ch.ethz.inspire.emod.simulation.MachineState;
import ch.ethz.inspire.emod.simulation.SimulationState;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * @author sizuest
 * 
 */
public class States {

	private static Logger logger = Logger.getLogger(SimulationState.class
			.getName());

	private static States statesMap = null;

	/* Array for Time states */
	private ArrayList<TimeStateMapper<MachineState>> timeStateMap = new ArrayList<TimeStateMapper<MachineState>>();

	/**
	 * Private constructor for singleton implementation.
	 */
	private States() {
	}

	/**
	 * singleton implementation of the machine model
	 * 
	 * @return instance of the machine model
	 */
	public static States getInstance() {
		if (statesMap == null) {
			System.out.print("No state map existing: Creating empty state map");
			statesMap = new States();
		}
		return statesMap;
	}

	/**
	 * Returns the stateMap
	 * 
	 * @return StateMap
	 */
	public static ArrayList<TimeStateMapper<MachineState>> getStateMap() {
		return getInstance().timeStateMap;
	}

	/**
	 * @param machineName
	 * @param simConfigName
	 */
	public static void readStates(String machineName, String simConfigName) {
		statesMap = new States();

		/*
		 * Generate file name with path: e.g.
		 * Machines/NDM200/MachineConfig/TestConfig1/MachineStateSequence.txt
		 */
		String file = EModSession.getStateSequenceConfigPath();

		getInstance().readStatesFromFile(file);
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
	private void readStatesFromFile(String file) {

		logger.info("Read machine state sequence from file: " + file);

		double rtime = 0.0;
		double rduration;
		int linenr = 0;
		timeStateMap = new ArrayList<TimeStateMapper<MachineState>>();

		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;

			while ((line = input.readLine()) != null) {
				// tokenize & append
				linenr++;

				// A comment is identified by a leading '#'.
				// Remove comments (regex can be use only by replaceAll())
				String l = line.replaceAll("#.*", "").replace("\t", " ").trim();

				/* (time,state)-pairs are separated by ';'. */
				StringTokenizer st = new StringTokenizer(l, ";");

				while (st.hasMoreTokens()) {
					// time and state are separated by a ',':
					StringTokenizer str = new StringTokenizer(st.nextToken()
							.trim(), ",");
					rduration = Double.parseDouble(str.nextToken().trim());
					rtime += rduration;
					String state = str.nextToken().trim();
					MachineState ms = MachineState.valueOf(state);
					timeStateMap.add(new TimeStateMapper<MachineState>(rtime,
							rduration, ms));
				}
			}
			input.close();
		} catch (Exception e) {
			System.err.println("Format error in file '" + file + "' line "
					+ linenr);
			e.printStackTrace();
			removeAllStates();
		}
	}

	/**
	 * @param machineName
	 * @param simConfigName
	 */
	public static void saveStates(String machineName, String simConfigName) {

		/*
		 * Generate file name with path: e.g.
		 * Machines/NDM200/MachineConfig/TestConfig1/MachineStateSequence.txt
		 */
		String prefix = PropertiesHandler
				.getProperty("app.MachineDataPathPrefix");
		String file = prefix + "/" + machineName + "/"
				+ Defines.SIMULATIONCONFIGDIR + "/" + simConfigName + "/"
				+ Defines.MACHINESTATEFNAME;

		getInstance().saveStatesToFile(file);
	}

	private void saveStatesToFile(String file) {

		logger.info("Saving machine state sequence to file: " + file);

		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(file));

			for (TimeStateMapper<MachineState> ts : getInstance().timeStateMap) {
				// Write Line
				output.append(ts.Duration + ", " + ts.State.toString() + "\n");
			}
			output.close();
		} catch (Exception e) {
			System.err.println("Writing error in file '" + file + "'");
			e.printStackTrace();
		}
	}

	/**
	 * @param index
	 * @return Time
	 */
	public static Double getTime(int index) {
		if(index>=getInstance().timeStateMap.size() | index<0)
			return Double.NaN;
		
		return getInstance().timeStateMap.get(index).Time;
	}

	/**
	 * @param index
	 * @return Time
	 */
	public static Double getDuration(int index) {
		if(index>=getInstance().timeStateMap.size() | index<0)
			return Double.NaN;
		
		return getInstance().timeStateMap.get(index).Duration;
	}

	/**
	 * @param index
	 * @return Time
	 */
	public static MachineState getState(int index) {
		if(index>=getInstance().timeStateMap.size() | index<0)
			return MachineState.OFF;
		
		return getInstance().timeStateMap.get(index).State;
	}

	/**
	 * @param index
	 * @param duration
	 * @param state
	 */
	public static void insertState(int index, double duration,
			MachineState state) {

		getInstance().timeStateMap.add(index,
				new TimeStateMapper<MachineState>(Double.NaN, duration, state));
		getInstance().updateTime();
	}

	/**
	 * 
	 */
	public static void removeAllStates() {
		getInstance().timeStateMap.clear();
	}

	/**
	 * @param duration
	 * @param state
	 */
	public static void appendState(double duration, MachineState state) {
		getInstance().timeStateMap.add(new TimeStateMapper<MachineState>(
				Double.NaN, duration, state));
		getInstance().updateTime();
	}

	/**
	 * @return Number of states
	 */
	public static int getStateCount() {
		int n = getInstance().timeStateMap.size();
		return n;
	}

	/**
	 * @param index
	 * @param duration
	 * @param state
	 */
	public static void setState(int index, double duration, MachineState state) {
		getInstance().timeStateMap.get(index).Duration = duration;
		getInstance().timeStateMap.get(index).State = state;
		getInstance().updateTime();
	}

	private void updateTime() {
		double time = 0;

		for (TimeStateMapper<MachineState> tsm : getInstance().timeStateMap) {
			time += tsm.Duration;
			tsm.Time = time;
		}
	}

}

/**
 * @author andreas
 * 
 * @param <S>
 */
class TimeStateMapper<S> {
	public double Time, Duration;
	public S State;

	public TimeStateMapper(double t, S s) {
		Time = t;
		State = s;
		Duration = Double.NaN;
	}

	public TimeStateMapper(double t, double d, S s) {
		Time = t;
		State = s;
		Duration = d;
	}
}
