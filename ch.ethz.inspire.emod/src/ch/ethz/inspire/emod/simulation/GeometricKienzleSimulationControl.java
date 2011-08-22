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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.SamplePeriodConverter;
import ch.ethz.inspire.emod.utils.SimulationConfigReader;

/**
 * Simulation control class for moments based on the kienzle approximation. 
 * 
 * @author dhampl
 *
 */
@XmlRootElement
public class GeometricKienzleSimulationControl extends ASimulationControl {

	private static Logger logger = Logger.getLogger(GeometricKienzleSimulationControl.class.getName());
	
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
	public GeometricKienzleSimulationControl(String name, double simulationPeriod) {
		super(name, Unit.NEWTONMETER);
		this.simulationPeriod=simulationPeriod;
		readConfigFromFile();
		try {
			readSamplesFromFile();
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
	public GeometricKienzleSimulationControl(String name, double[] n, double[] f, double[] ap, double[] d) throws Exception {
		super(name, Unit.NEWTONMETER);
		if(n.length!=f.length || n.length!=ap.length || n.length!=d.length)
			throw new Exception("input violation: params must have same length");
		readConfigFromFile();
		calculateMoments(f, ap, d);
	}
	
	/**
	 * JAXB constructor
	 */
	public GeometricKienzleSimulationControl() {
		
	}
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		simulationOutput = new IOContainer(name, unit, 0);
		
		if(samples==null) {
			readConfigFromFile();
			try {
				readSamplesFromFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * reads config parameters kappa, kc and z from file.
	 * 
	 * @param file
	 */
	protected void readConfigFromFile() {
		samples = new ArrayList<double[]>();
		logger.log(LogLevel.DEBUG, "reading config for: "+this.getClass().getSimpleName()+"_"+name);
		SimulationConfigReader scr=null;
		try {
			scr = new SimulationConfigReader(this.getClass().getSimpleName(), name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			
			// loop over all component states
			for(ComponentState cs : ComponentState.values()) {
				
				samples.add(scr.getSamplesArray(cs.name()));
			}
			kappa = scr.getDoubleValue("kappa")*Math.PI/180;
			z = scr.getDoubleValue("z");
			kc = scr.getDoubleValue("kc");
		} catch(Exception e) {
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
	protected void readSamplesFromFile() throws Exception {
		double[] n = null, v = null, ap = null, d = null;
		
		logger.log(LogLevel.DEBUG, "reading samples for: "+this.getClass().getSimpleName()+"_"+name);
		SimulationConfigReader scr=null;
		try {
			scr = new SimulationConfigReader(this.getClass().getSimpleName(), name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			n=scr.getSamplesArray("n");
			v=scr.getSamplesArray("v");
			ap=scr.getSamplesArray("ap");
			d=scr.getSamplesArray("d");
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(n.length!=v.length || n.length!=ap.length || n.length!=d.length)
			throw new Exception("input violation: params must have same length");
		
		for(int i=0;i<n.length;i++){
			v[i] = v[i]/(n[i]); //mm/min -> m/U
			n[i] = n[i]/60; //1/min -> 1/s
			ap[i] = ap[i]; //mm -> m
			d[i] = d[i]/1000; //mm -> m
		}
		calculateMoments(v, ap, d);
		for(int i=0;i<samples.size();i++) {
			samples.set(i, SamplePeriodConverter.convertSamples(scr.getDoubleValue("samplePeriod"), simulationPeriod, samples.get(i)));
		}
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
		if(this.state!=stateMap.get(state)) {
			super.setState(state);
			simulationStep=0;
		}
	}

}
