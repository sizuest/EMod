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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Handles the machine state of a simulation.
 * A list of times and machine states are read from a file and stored in
 * a list. For a given time the corresponding machine state can be asked for.
 * And the end of the simulation is determined.
 * 
 * File format of the input list:
 *   Duration1 , State1 ;
 *   Duration2 , State2 ;
 * The duration corresponds to the duration of the state in s.
 * The sum of all durations is the length of the simulation.
 * A comment line begins with '#'.
 * 
 * @author andreas
 *
 */
public class MachineState {
	
	/**
	 * All energy related machine states. 
	 * 
	 * @author dhampl
	 *
	 */
	public enum MachineStateEnum {
		ON, OFF, STANDBY, READY, CYCLE;
	}
	
	/* Variables */
	private double endtime = 0.0;      /* End of simulation in [s]*/
	/* List mapping the times of a state change to the next state. */
	private ArrayList<TimeStateMapper> timeStateMap = null;
	private int actualindex;           /* Index of actual (time,state) in list */
	private double nextStateChgTime;   /* Time, when next state change occurs. */
	private MachineStateEnum actualstate; /* Actual state */
	
	/**
	 * Constructor: Reads state list from a file and sets the
	 * variables (endtime, actualstate, nextStateChgTime).
	 * 
	 */
	public MachineState() {
		// TODO: Get filename from simulation config file.
		readSimulationStatesFromFile("initSimStates.txt");
	}
	
	/**
	 * Get state at actual time.
	 * Note: Calling this method multiple times, the time parameter must be
	 * increasing. The previous machine state cannot be restored.
	 * 
	 * @param time Time for the desired sate.
	 * @return The machine state at time 'time'.
	 */
	public MachineStateEnum getState(double time)
	{
		/* Comparison with a number a little bit smaller than 0, due to
		 * numerical precision of doubles. */
		if (time-nextStateChgTime > -0.0001) {
			while ((time-nextStateChgTime > -0.0001) && (actualindex+1 < timeStateMap.size())) {
				/* Update actual state to next state. */
				actualindex++;
				nextStateChgTime = timeStateMap.get(actualindex).Time;
				actualstate = timeStateMap.get(actualindex).State;
			}
			/* Note: If time > endtime: The last state is repeated. No exception is thrown. */
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
	 * 
	 * syntax: time[s],{@link MachineState};time[s],{@link MachineState};...;
	 * Comment lines begin with '#'.
	 * Spaces are allowed.
	 * 
	 * @param file
	 */
	private void readSimulationStatesFromFile(String file) {
		
		double rtime = 0.0;
		timeStateMap = new ArrayList<TimeStateMapper>();
		
		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;
			
			while((line=input.readLine())!=null) {
				//tokenize & append
				
				String l = line.trim();
				if ((l.length() == 0 ) || (l.charAt(0) == '#'))
					continue; // Ignore empty and comment lines.
				
				/* (time,state)-pairs are separated by ';'.*/
				StringTokenizer st = new StringTokenizer(l, ";");
				
				while(st.hasMoreTokens()) {
					// time and state of a pair are separated by a ',':
					StringTokenizer str = new StringTokenizer(st.nextToken().trim(),",");
					rtime += Double.parseDouble(str.nextToken().trim());
					String state = str.nextToken().trim();
					MachineStateEnum ms = MachineStateEnum.valueOf(state);
					timeStateMap.add(new TimeStateMapper(rtime, ms));
				}
			}
			input.close();
			endtime = rtime;
			actualindex = 0;
			nextStateChgTime = timeStateMap.get(actualindex).Time;
			actualstate = timeStateMap.get(actualindex).State;
		} catch (Exception e) {
			System.err.println("Format error in file '" + file + "'");
			e.printStackTrace();
			System.exit(-1);
		} 
	}
	
}

/**
 * Data class. Used to stored a pair of a time, machine state value.
 * 
 * @author andreas
 *
 */
class TimeStateMapper
{
	public double Time;
	public MachineState.MachineStateEnum State;
	
	 public TimeStateMapper(double t, MachineState.MachineStateEnum s)
	 {
		 Time = t;
		 State = s;
	 }
}
