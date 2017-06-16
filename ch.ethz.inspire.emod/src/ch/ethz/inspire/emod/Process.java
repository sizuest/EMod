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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.Defines;

/**
 * Read the process definition from file.
 * 
 * @author andreas
 * 
 */
public class Process extends ConfigReader {

	private static Process process = null;
	private double[] bufferedTime = null;

	private Process() {
	};

	/**
	 * @return Current Process object
	 */
	public static Process getInstance() {
		if (null == process)
			process = new Process();

		return process;
	}

	/**
	 * @param name
	 * 
	 */
	public static void loadProcess(String name) {
		
		EModSession.setProcessName(name);
		getInstance().filePath = EModSession.getProcessConfigPath();

		try {
			getInstance().ConfigReaderOpen();
			getInstance().bufferedTime = null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

	/**
	 * Adds a new process with the given name
	 * @param name
	 */
	public static void newProcess(String name) {

		/* Set new file name */
		getInstance().filePath = EModSession.getProcessConfigPath();
		
		/* Create file */
		try {
			getInstance().createFile();
			getInstance().ConfigReaderOpen();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* empty time and variable verctors */
		clearProcess();

		/* save */
		try {
			getInstance().saveValues();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove Process
	 * 
	 * @param name
	 */
	public static void removeProcess(String name) {
		/* Set new file name */
		String path = EModSession.getRootPath()
				+ File.separator
				+ Defines.SIMULATIONCONFIGDIR 
				+ File.separator
				+ EModSession.getSimulationConfig();

		File processDir = new File(path);
		File processFile = new File(path + "/" + Defines.PROCESSDEFFILE_PREFIX
				+ name + ".xml");

		/* Delete process file */
		processFile.delete();

		/* Look for next process file */
		String newProcessName = "";
		for (String s : processDir.list())
			if (s.contains(Defines.PROCESSDEFFILE_PREFIX)) {
				newProcessName = s.replace(Defines.PROCESSDEFFILE_PREFIX, "")
						.replace(".xml", "");
				break;
			}

		/* Set new path */
		if (newProcessName.equals("")) {
			newProcessName = "default";

			clearProcess();
			getInstance().filePath = path + "/" + Defines.PROCESSDEFFILE_PREFIX
					+ newProcessName + ".xml";

			try {
				getInstance().saveValues();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			loadProcess(newProcessName);

		EModSession.setProcessName(newProcessName);

	}

	private static void clearProcess() {
		try {
			Process.setTimeVector(new double[] { 0.0 });
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		for (String key : Process.getVariableNames())
			getInstance().setValue(key, 0);
	}

	/**
	 * Read the process parameters from file
	 * 
	 * @param name
	 *            Name of process
	 */
	private Process(String name) {

		Process.loadProcess(name);

		try {
			Process.setTimeVector(getInstance().getDoubleArray("Time"));
		} catch (Exception e1) {
			System.out.printf("Process: No time vector found");

			try {
				// Work around for old sim files
				double samplePeriod = getInstance().getDoubleValue(
						"SamplePeriod");
				if (Process.getVariableNames().size() == 0)
					Process.setTimeVector(new double[1]);
				else {
					double[] time = new double[Process
							.getProcessVariable(getVariableNames().get(0)).length];
					for (int i = 0; i < time.length; i++)
						time[i] = i * samplePeriod;
					Process.setTimeVector(time);

				}
				Process.deleteProcessVariable("SamplePeriod");

				System.out
						.printf("Process: Time vector added based on sample time");

			} catch (Exception e2) {
				e1.printStackTrace();
				e2.printStackTrace();
			}
		}

	}

	/**
	 * @return Array of all variable names
	 */
	public static ArrayList<String> getVariableNames() {
		ArrayList<String> keys = getInstance().getKeys();
		keys.remove("SamplePeriod");
		keys.remove("Time");

		return keys;
	}

	/**
	 * Delete an existing process variable
	 * 
	 * @param name
	 */
	public static void deleteProcessVariable(String name) {
		if (getInstance().existsProcessVariable(name))
			try {
				getInstance().deleteValue(name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		else
			System.out.println("Delete process variable '" + name
					+ "' failed: No such variable found!");
	}

	/**
	 * Add a new process variable initialized ad {0}
	 * 
	 * @param name
	 * @throws Exception
	 */
	public static void addProcessVariable(String name) throws Exception {
		double[] values = { 0 };
		addProcessVariable(name, values);
	}

	/**
	 * Add a new process variable initionized at 'values'
	 * 
	 * @param name
	 * @param values
	 * @throws Exception
	 */
	public static void addProcessVariable(String name, double[] values)
			throws Exception {
		getInstance().setValue(getInstance().getUniqueVariableName(name),
				values);
	}

	/**
	 * Set values of an existing process variable
	 * 
	 * @param name
	 * @param values
	 * @throws Exception
	 */
	public static void setProcessVariable(String name, double[] values)
			throws Exception {
		if (getInstance().existsProcessVariable(name))
			getInstance().setValue(name, values);
		else
			System.out.println("Setting process variable '" + name
					+ "' failed: No such variable found!");
	}

	/**
	 * Set the name of an existing process variable
	 * 
	 * @param name
	 * @param newName
	 */
	public static void setProcessVariableName(String name, String newName) {
		if (getInstance().existsProcessVariable(name)) {
			newName = getInstance().getUniqueVariableName(newName);
			getInstance().props.put(newName, getInstance().props.get(name));
			getInstance().props.remove(name);
		} else
			System.out.println("Renaming process variable '" + name
					+ "' failed: No such variable found!");
	}

	private boolean existsProcessVariable(String name) {
		if (null == props)
			return false;
		return props.containsKey(name);
	}

	private String getUniqueVariableName(String name) {
		if (!existsProcessVariable(name))
			return name;

		// Add numer s.t. name becomes unique
		int i = 1;
		while (existsProcessVariable(name + "_" + i))
			i++;

		return name + "_" + i;
	}

	/**
	 * @return Number of samples
	 */
	public static int getNumberOfTimeStamps() {
		try {
			return Process.getTime().length;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * @param name
	 * @return Process variable with key 'name'
	 */
	public static double[] getProcessVariable(String name) {
		try {
			return getInstance().getDoubleArray(name);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return Sample period in [s]
	 */
	public static double[] getTime() {
		try {
			if (getInstance().bufferedTime == null)
				getInstance().bufferedTime = getInstance().getDoubleArray("Time");
			return getInstance().bufferedTime;
		} catch (Exception e) {
			return new double[] {};
		}
	}

	/**
	 * setTimeVector
	 * 
	 * @param time
	 * @throws Exception
	 */
	public static void setTimeVector(double[] time) throws Exception {

		// Sample lenth
		if (time == null)
			throw new Exception("Process: setTimeVector failed: null!");

		// Time differences
		for (int i = 1; i < time.length; i++) {
			if (time[i] - time[i - 1] <= 0) {
				throw new Exception(
						"Process: setTimeVector failed: Values must be strictly monotonic!");
			}
		}
		// All ok
		getInstance().setValue("Time", time);
		
		if(time.length<2)
			getInstance().setValue("SamplePeriod", 1);
		else
			getInstance().setValue("SamplePeriod", Algo.greatestCommonDivisor(Algo.getIncrements(time)));

		getInstance().bufferedTime = null;

	}

}
