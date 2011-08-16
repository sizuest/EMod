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
package ch.ethz.inspire.emod.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import ch.ethz.inspire.emod.simulation.ComponentState;

/**
 * @author dhampl
 *
 */
public class SimulationConfigReader {

	private Properties props;
	private String fileName;
	
	/**
	 * 
	 * @param type the simulator's class type (randomsimulationcontrol, static..., kienzle..., etc)
	 * @param component the component name (80mmfan, x axis, etc)
	 * @throws Exception 
	 */
	public SimulationConfigReader(String type, String component) throws Exception {
		
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix")+
				"/"+PropertiesHandler.getProperty("app.MachineName")+"/MachineConfig/"+
				PropertiesHandler.getProperty("app.MachineConfigName");
		fileName = path+"/"+type+"_"+component+".xml";
		
		InputStream iostream = new FileInputStream(fileName);
		props = new Properties();
		try {
			props.loadFromXML(iostream);
		} catch (Exception e) {
			throw new Exception("Error in reading properties from file '"+fileName+
					"' bad format. \n"
					+e.getMessage());
		}
		iostream.close();
	}
	
	public double[] getSamplesArray(String samplesName) {
		String dataString = props.getProperty(samplesName);
		String[] dataStringArray = dataString.split(" ");
		double[] result = new double[dataStringArray.length];
		for(int i=0; i<result.length;i++)
			result[i] = Double.parseDouble(dataStringArray[i]);
		return result;
	}
	
	public ComponentState getComponentState(String machineState) {
		return ComponentState.valueOf(props.getProperty(machineState+"_state"));
	}
	
	public double getDoubleValue(String propertyName) {
		return Double.parseDouble(props.getProperty(propertyName));
	}
}
