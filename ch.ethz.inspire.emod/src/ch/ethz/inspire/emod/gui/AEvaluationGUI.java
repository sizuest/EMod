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
package ch.ethz.inspire.emod.gui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.ethz.inspire.emod.gui.utils.ConsumerData;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.simulation.MachineState;

/**
 * abstract class for guis, containing logic to read sim data files.
 * 
 * @author dhampl
 * 
 */
public abstract class AEvaluationGUI extends AGUITab {

	private static Logger logger = Logger.getLogger(AEvaluationGUI.class.getName());
	private String dataFile;
	protected List<ConsumerData> availableConsumers = new ArrayList<ConsumerData>();
	private List<String[]> lines;
	
	private double[] time;
	private MachineState[] states;

	private boolean isReadingData = false;

	/**
	 * @param parent
	 * @param dataFile
	 */
	public AEvaluationGUI(Composite parent, String dataFile) {
		super(parent, SWT.NONE);
		this.dataFile = dataFile;
		readData();
	}

	/**
	 * Set the path of the data file
	 * @param path
	 */
	public void setDataFile(String path) {
		this.dataFile = path;
		// readData();
	}

	/**
	 * reads the data from a specified datafile
	 */
	protected void readData() {
		if (this.dataFile.equals(""))
			return;

		threadedReadData();
	}

	/**
	 * check whether a given consumer already exists.
	 * 
	 * @param consumer
	 * @return true if consumer is already present in availableConsumers list
	 */
	private boolean consumerExists(String consumer) {
		boolean result = false;
		for (ConsumerData cd : availableConsumers) {
			if (cd.getConsumer().equals(consumer)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	private void getTimeVector(int col){
		double[] values = new double[lines.size() - 3];
		for (int i = 4; i < lines.size(); i++)
			try {
				values[i - 4] = Double.parseDouble(lines.get(i)[col]);
			} catch (Exception e) {
				System.err.print("Result file: Could not parse entier result file. Line " + i + " failed due to bad format.");
				e.printStackTrace();
			}
		
		time = values;
	}
	
	private void getStateVector(int col){
		MachineState[] values = new MachineState[lines.size() - 3];
		for (int i = 4; i < lines.size(); i++)
			try {
				values[i - 4] = MachineState.valueOf(lines.get(i)[col]);
			} catch (Exception e) {
				System.err.print("Result file: Could not parse entier result file. Line " + i + " failed due to bad format.");
				e.printStackTrace();
			}
		
		states = values;
	}

	/**
	 * creates a new consumer with a specified name
	 * 
	 * @param name
	 * @param col
	 *            column in the data file
	 */
	private void createConsumer(String name, int col) {
		ConsumerData data = new ConsumerData(name, time, states);
		String ioName = lines.get(1)[col].replace(name, "");
		ioName = ioName.substring(1);
		data.addName(ioName);
		String unit = lines.get(2)[col].replace("[", "").replace("]", "");
		data.addUnit(new SiUnit(unit));
		double[] values = new double[lines.size() - 3];
		for (int i = 4; i < lines.size(); i++)
			try {
				values[i - 4] = Double.parseDouble(lines.get(i)[col]);
			} catch (Exception e) {
				System.err
						.print("Result file: Could not parse entier result file. Line "
								+ i + " failed due to bad format.");
				e.printStackTrace();
			}
		data.addInputValues(values);
		availableConsumers.add(data);
	}

	/**
	 * adds sample values from the datafile to a consumer
	 * 
	 * @param consumer
	 *            parent structure for the data
	 * @param col
	 *            column in the data file
	 */
	private void addDataToConsumer(String consumer, int col) {
		ConsumerData temp = null;
		for (ConsumerData cd : availableConsumers) {
			if (cd.getConsumer().equals(consumer)) {
				temp = cd;
				break;
			}
		}
		String ioName = lines.get(1)[col].replace(consumer, "");
		ioName = ioName.substring(1);
		temp.addName(ioName);
		String unit = lines.get(2)[col].replace("[", "").replace("]", "");
		temp.addUnit(new SiUnit(unit));
		double[] values = new double[lines.size() - 3];
		for (int i = 4; i < lines.size(); i++)
			try {
				values[i - 4] = Double.parseDouble(lines.get(i)[col]);
			} catch (Exception e) {
				System.err
						.print("Result file: Could not parse entier result file. Line "
								+ i + " failed due to bad format.");
				e.printStackTrace();
			}
		temp.addInputValues(values);
	}

	/**
	 * 
	 * @return list with {@link ConsumerData} elements
	 */
	public List<ConsumerData> getConsumerDataList() {
		return availableConsumers;
	}

	/**
	 * This method is called after the threaded data import
	 */
	protected abstract void postDataImportAction();

	/**
	 * Thread to import data
	 */
	private void threadedReadData() {
		if (isReadingData)
			return;

		Thread updateThread = new Thread() {
			@Override
			public void run() {
				getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						isReadingData = true;

						logger.info("reading simulation data from file '"
								+ dataFile + "'");

						EModStatusBarGUI.getProgressBar().setText("Loading results file ...");
						EModStatusBarGUI.getProgressBar().updateProgressbar(0);

						availableConsumers = new ArrayList<ConsumerData>();
						BufferedReader reader = null;
						try {
							reader = new BufferedReader(new FileReader(dataFile));
						} catch (Exception e) {
							System.err.print("Result file " + dataFile + " is non existent");
							return;
						}

						lines = new ArrayList<String[]>();
						String line = null;
						try {
							while ((line = reader.readLine()) != null) {
								lines.add(line.split("\t")); // reading and splitting the first line
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						

						// Check if result file is non-empty
						if (0 == lines.size())
							System.err.print("Result file " + dataFile + " is empty");
						else {
							String[] headerLine = lines.get(0);
							
							// Find time & state vector
							for (int i = 0; i < headerLine.length; i++) {
								String token = headerLine[i];
								token = token.trim();
								if (token.equals("Time")){
									getTimeVector(i);
									continue;
								}
								if (token.equals("State"))
									getStateVector(i);
									break;
							}

							// Add consumers
							for (int i = 0; i < headerLine.length; i++) {
								String token = headerLine[i];
								token = token.trim();
								if (token.equals("Time"))
									continue;
								if (token.equals("State"))
									continue;
								if (token.contains("Sim"))
									continue;
								String consumer = token.replaceAll("-.*", "")
										.trim();
								if (!consumerExists(consumer))
									createConsumer(consumer, i);
								else
									addDataToConsumer(consumer, i);

								EModStatusBarGUI.getProgressBar().updateProgressbar(i * 100 / headerLine.length);
							}
						}

						try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						for (ConsumerData cd : availableConsumers) {
							cd.calculateEnergy();
							cd.calculate();
						}

						EModStatusBarGUI.getProgressBar().reset();

						isReadingData = false;

						postDataImportAction();
					}
				});
			}
		};
		// background thread
		updateThread.setDaemon(true);
		updateThread.start();
	}
}
