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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * Simulation control class for moments based on the kienzle approximation. 
 * 
 * @author dhampl
 *
 */
@XmlRootElement
public class GeometricKienzleSimulationControl extends ASimulationControl {

	private static Logger logger = Logger.getLogger(GeometricKienzleSimulationControl.class.getName());
	
	@XmlElementWrapper
	@XmlElement
	protected List<double[]> samples; //values for every state
	protected int simulationStep=0; //current sim step
	protected double kappa; //kienzle constant
	protected double kc; //kienzle constant
	protected double z; //kienzle constant
	
	/**
	 * 
	 * @param name
	 * @param configValuesFile
	 */
	public GeometricKienzleSimulationControl(String name, String configValuesFile) {
		super(name, Unit.NEWTONMETER, configValuesFile);
		readConfigFromFile(configValuesFile);
		try {
			readSamplesFromFile(configValuesFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * only SI values.<br />
	 * length of all arrays must be the same.
	 * 
	 * @param name name of the simulation control object
	 * @param configFile config values like state mapping, kappa, kc, z
	 * @param n spindle revolutions [1/s]
	 * @param f feed [m/U]
	 * @param ap depth of cut [m]
	 * @param d diameter [m]
	 * @throws Exception 
	 */
	public GeometricKienzleSimulationControl(String name, String configFile, double[] n, double[] f, double[] ap, double[] d) throws Exception {
		super(name, Unit.NEWTONMETER, configFile);
		if(n.length!=f.length || n.length!=ap.length || n.length!=d.length)
			throw new Exception("input violation: params must have same length");
		readConfigFromFile(configFile);
		calculateMoments(f, ap, d);
	}
	
	/**
	 * JAXB constructor
	 */
	public GeometricKienzleSimulationControl() {
		
	}
	
	/**
	 * reads config parameters kappa, kc and z from file.
	 * 
	 * @param file
	 */
	protected void readConfigFromFile(String file) {
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
			kappa = Double.parseDouble(p.getProperty("kappa"))*Math.PI/180;
			z = Double.parseDouble(p.getProperty("z"));
			kc = Double.parseDouble(p.getProperty("kc"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * reads sample values from file. <br />
	 * values are: <br />
	 * n [1/min], v [mm/min], ap [mm], d [mm]<br />
	 * syntax:<br />
	 * n=1 2 3 4 5<br />
	 * v=12 34 56 78 90
	 * 
	 * @param file with samples for n, v, ap, d
	 * @throws Exception thrown if |n| != |v| != |ap| != |d|
	 */
	protected void readSamplesFromFile(String file) throws Exception {
		double[] n = null, v = null, ap = null, d = null;
		
		logger.log(LogLevel.DEBUG, "reading samples from: "+file);
		try {
			//load file to properties
			Properties p = new Properties();
			InputStream is = new FileInputStream(file);
			p.load(is);
			is.close();
			// read all kienzle variables
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
		if(n.length!=v.length || n.length!=ap.length || n.length!=d.length)
			throw new Exception("input violation: params must have same length");
		
		for(int i=0;i<n.length;i++){
			v[i] = v[i]/(n[i]); //mm/min -> m/U
			n[i] = n[i]/60; //1/min -> 1/s
			ap[i] = ap[i]; //mm -> m
			d[i] = d[i]; //mm -> m
		}
		calculateMoments(v, ap, d);
	}
	
	protected void calculateMoments(double[] f, double[] ap, double[] d) {
		double[] moments = new double[f.length];
		double sinkappa = Math.sin(kappa);
		for(int i=0;i<f.length;i++) {
			//calculate moments for every time step: Fc = kc * b * h^(1-z) 
			//moment = Fc * d/2
			moments[i] = kc * (ap[i]/sinkappa)* Math.pow(f[i] * sinkappa,1-z) * d[i]/2;
		}
		samples.set(ComponentState.PERIODIC.ordinal(), moments);
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.simulation.ASimulationControl#update()
	 */
	@Override
	public void update() {
		logger.log(LogLevel.DEBUG, "update on "+getName()+" step: "+simulationStep);
		//as samples are in the same order as machinestates, the machinestate index (ordinal) can be used.
		simulationOutput.setValue(samples.get(state.ordinal())[simulationStep]);
		simulationStep++;
		if(simulationStep>=samples.get(state.ordinal()).length)
			simulationStep=0;
	}
	
	/**
	 * sets and maps the {@link MachineState} to the appropriate {@link SimulationState}
	 * 
	 * @param state
	 */
	@Override 
	public void setState(MachineState state) {
		super.setState(state);
		simulationStep=0;
	}

}
