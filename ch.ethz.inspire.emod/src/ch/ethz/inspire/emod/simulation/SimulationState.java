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
import java.util.logging.Logger;

import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Handles the machine state of a simulation.
 * A list of times and machine states are read from a file and stored in
 * a list. For a given time the corresponding machine state can be asked for.
 * And the end of the simulation is determined.
 * <p>
 * File format of the input list:<br />
 *   Duration1 , State1 ;<br />
 *   Duration2 , State2 ;<br />
 * The duration corresponds to the duration of the state in s.<br />
 * The sum of all durations is the length of the simulation.<br />
 * A comment line begins with '#'.
 * 
 * @author andreas
 *
 */
public class SimulationState {
	
	private static Logger logger = Logger.getLogger(SimulationState.class.getName());
	
	/* Variables */
	private double endtime = 0.0;      /* End of simulation in [s]*/
	/* List mapping the times of a state change to the next state. */
	private ArrayList<TimeStateMapper<MachineState>> timeStateMap = null;
	private int actualindex;           /* Index of actual (time,state) in list */
	private double nextStateChgTime;   /* Time, when next state change occurs. */
	private MachineState actualstate; /* Actual state */
	
	/**
	 * Constructor: Reads state list from a file and sets the
	 * variables (endtime, actualstate, nextStateChgTime).
	 * 
	 */
	public SimulationState(String machineName, String simConfigName) {
		
		/* Generate file name with path:
		 * e.g. Machines/NDM200/MachineConfig/TestConfig1/MachineStateSequence.txt */
		String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix");
		String file = prefix + "/" + machineName + "/"+ Defines.SIMULATIONCONFIGDIR +"/" + 
		              simConfigName + "/" + Defines.MACHINESTATEFNAME;
		
		logger.info("Read machine state sequence from file: " + file);
		
		readSimulationStatesFromFile(file);
	}
	
	/**
	 * Get state at actual time.
	 * Note: Calling this method multiple times, the time parameter must be
	 * increasing. The previous machine state cannot be restored.
	 * 
	 * @param time Time for the desired sate.
	 * @return The machine state at time 'time'.
	 */
	public MachineState getState(double time)
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
	 * <p>
	 * syntax: time[s],{@link SimulationState};time[s],{@link SimulationState};...;<br />
	 * Comment lines begin with '#'.<br />
	 * Spaces are allowed.
	 * 
	 * @param file
	 */
	private void readSimulationStatesFromFile(String file) {
		
		double rtime = 0.0;
		int linenr = 0;
		timeStateMap = new ArrayList<TimeStateMapper<MachineState>>();
		
		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;
			
			while((line=input.readLine())!=null) {
				//tokenize & append
				linenr++;
				
				// A comment is identified by a leading '#'.
				// Remove comments (regex can be use only by replaceAll())
				String l = line.replaceAll("#.*", "").replace("\t", " ").trim();
				
				/* (time,state)-pairs are separated by ';'.*/
				StringTokenizer st = new StringTokenizer(l, ";");
				
				while(st.hasMoreTokens()) {
					// time and state are separated by a ',':
					StringTokenizer str = new StringTokenizer(st.nextToken().trim(),",");
					rtime += Double.parseDouble(str.nextToken().trim());
					String state = str.nextToken().trim();
					MachineState ms = MachineState.valueOf(state);
					timeStateMap.add(new TimeStateMapper<MachineState>(rtime, ms));
				}
			}
			input.close();
			endtime = rtime;
			actualindex = 0;
			nextStateChgTime = timeStateMap.get(actualindex).Time;
			actualstate = timeStateMap.get(actualindex).State;
		} catch (Exception e) {
			System.err.println("Format error in file '" + file + "' line " + linenr);
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
class TimeStateMapper<S>
{
	public double Time;
	public S State;
	
	 public TimeStateMapper(double t, S s)
	 {
		 Time = t;
		 State = s;
	 }
}
