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

import ch.ethz.inspire.emod.gui.utils.ConsumerData;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * abstract class for guis, containing logic to read sim data files.
 * 
 * @author dhampl
 *
 */
public abstract class AEvaluationGUI {

	private static Logger logger = Logger.getLogger(AEvaluationGUI.class.getName());
	private String dataFile;
	protected List<ConsumerData> availableConsumers;
	private List<String[]> lines;
	
	public AEvaluationGUI(String dataFile) {
		this.dataFile = dataFile;
		readData();
	}
	
	/**
	 * reads the data from a specified datafile
	 */
	private void readData() {
		logger.info("reading simulation data from file '"+dataFile+"'");
		availableConsumers = new ArrayList<ConsumerData>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(dataFile));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		lines = new ArrayList<String[]>();
		String line = null;
		try {
			while((line=reader.readLine())!=null) {
				lines.add(line.split("\t")); //reading and splitting the first line
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
		String[] headerLine = lines.get(0);
		for(int i=0;i<headerLine.length;i++) {
			String token = headerLine[i];
			token=token.trim();
			if(token.equals("Time")) 
				continue; 
			if(token.contains("Sim"))
				continue;
			String consumer = token.replaceAll("-.*", "").trim();
			if(!consumerExists(consumer))
				createConsumer(consumer, i);
			else
				addDataToConsumer(consumer, i);
		}
		
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(ConsumerData cd:availableConsumers)
			cd.calculateEnergy();
	}
	
	/**
	 * check whether a given consumer already exists. 
	 * @param consumer
	 * @return true if consumer is already present in availableConsumers list
	 */
	private boolean consumerExists(String consumer) {
		boolean result = false;
		for(ConsumerData cd : availableConsumers) {
			if(cd.getConsumer().equals(consumer)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * creates a new consumer with a specified name 
	 * @param name 
	 * @param col column in the data file
	 */
	private void createConsumer(String name, int col) {
		ConsumerData data = new ConsumerData(name);
		String ioName = lines.get(1)[col].replace(name, "");
		ioName = ioName.substring(1);
		data.addName(ioName);
		String unit = lines.get(2)[col].replace("[", "").replace("]", "");
		data.addUnit(Unit.valueOf(unit));
		double[] values = new double[lines.size()-3];
		for(int i = 3;i<lines.size();i++)
			values[i-3] = Double.parseDouble(lines.get(i)[col]);
		data.addInputValues(values);
		availableConsumers.add(data);
	}
	
	/**
	 * adds sample values from the datafile to a consumer
	 * 
	 * @param consumer parent structure for the data 
	 * @param col column in the data file
	 */
	private void addDataToConsumer(String consumer, int col) {
		ConsumerData temp = null;
		for(ConsumerData cd : availableConsumers){
			if(cd.getConsumer().equals(consumer)){
				temp=cd;
				break;
			}
		}
		String ioName = lines.get(1)[col].replace(consumer, "");
		ioName = ioName.substring(1);
		temp.addName(ioName);
		String unit = lines.get(2)[col].replace("[", "").replace("]", "");
		temp.addUnit(Unit.valueOf(unit));
		double[] values = new double[lines.size()-3];
		for(int i = 3;i<lines.size();i++)
			values[i-3] = Double.parseDouble(lines.get(i)[col]);
		temp.addInputValues(values);
	}
	
	/**
	 * 
	 * @return list with {@link ConsumerData} elements
	 */
	public List<ConsumerData> getConsumerDataList() {
		return availableConsumers;
	}
}
