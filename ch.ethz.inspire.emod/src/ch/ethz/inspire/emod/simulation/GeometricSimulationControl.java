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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * @author dhampl
 *
 */
public class GeometricSimulationControl extends ASimulationControl {

	private static Logger logger = Logger.getLogger(GeometricSimulationControl.class.getName());
	
	protected List<double[]> samples; //values for every state
	protected int simulationStep; //current sim step
	protected double kappa; //kienzle constant
	protected double kc; //kienzle constant
	protected double z; //kienzle constant
	
	/**
	 * 
	 * @param name
	 * @param configValuesFile
	 */
	public GeometricSimulationControl(String name, String configValuesFile) {
		super(name, Unit.NEWTONMETER, configValuesFile);
		readConfigFromFile(configValuesFile);
		readSamplesFromFile(configValuesFile);
	}
	
	/**
	 * only SI values.<br />
	 * length of all arrays must be the same.
	 * 
	 * @param name name of the simulation control object
	 * @param configFile config values like state mapping, kappa, kc, z
	 * @param n spindle revolutions [1/s]
	 * @param v feed [m/s]
	 * @param ap depth of cut [m]
	 * @param d diameter [m]
	 */
	public GeometricSimulationControl(String name, String configFile, double[] n, double[] v, double[] ap, double[] d) {
		super(name, Unit.NEWTONMETER, configFile);
		readConfigFromFile(configFile);
		calculateMoments(n, v, ap, d);
	}
	
	/**
	 * JAXB constructor
	 */
	public GeometricSimulationControl() {
		
	}
	
	/**
	 * reads config parameters kappa, kc and z from file.
	 * 
	 * @param file
	 */
	private void readConfigFromFile(String file) {
		samples = new ArrayList<double[]>();
		logger.log(LogLevel.DEBUG, "reading config from: "+file);
		try {
			//load file to properties
			Properties p = new Properties();
			InputStream is = new FileInputStream(file);
			p.load(is);
			is.close();
			// loop over all component states
			for(ComponentState cs : ComponentState.values()) {
				String line = p.getProperty(cs.name());
				StringTokenizer st = new StringTokenizer(line);
				double[] vals = new double[st.countTokens()];
				int i = 0;
				//parse samples
				while(st.hasMoreTokens()) {
					vals[i] = Double.parseDouble(st.nextToken());
					i++;
				}
				samples.add(vals);
			}
			kappa = Double.parseDouble(p.getProperty("kappa"));
			z = Double.parseDouble(p.getProperty("z"));
			kc = Double.parseDouble(p.getProperty("kc"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * reads sample values from file. <br />
	 * values are: <br />
	 * n [1/s], v [m/s], ap [m], d [m]
	 * 
	 * @param file
	 */
	private void readSamplesFromFile(String file) {
		double[] n = null, v = null, ap = null, d = null;
		
		logger.log(LogLevel.DEBUG, "reading samples from: "+file);
		try {
			//load file to properties
			Properties p = new Properties();
			InputStream is = new FileInputStream(file);
			p.load(is);
			is.close();
			// loop over all component states
			String[] valNames = {"n", "v", "ap", "d"};
			int valIndex=0;
			for(String valName : valNames) {
				String line = p.getProperty(valName);
				StringTokenizer st = new StringTokenizer(line);
				double[] vals = new double[st.countTokens()];
				int i = 0;
				//parse samples
				while(st.hasMoreTokens()) {
					vals[i] = Double.parseDouble(st.nextToken());
					i++;
				}
				
				switch(valIndex) {
				case 0:
					n=vals;
					break;
				case 1:
					v=vals;
					break;
				case 2:
					ap=vals;
					break;
				case 3:
					d=vals;
					break;
				default:
					throw new Exception("invalid line");
				}
				valIndex++;
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		calculateMoments(n, v, ap, d);
	}
	
	private void calculateMoments(double[] n, double[] v, double[] ap, double[] d) {
		double[] moments = new double[n.length];
		for(int i=0;i<n.length;i++) {
			//TODO: kienzle!
			moments[i] = kc * (ap[i]/Math.sin(kappa));
		}
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.simulation.ASimulationControl#update()
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

}
