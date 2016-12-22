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

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.Process;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.SamplePeriodConverter;
import ch.ethz.inspire.emod.utils.SimulationConfigReader;

/**
 * Simulation control class for moments based on the kienzle approximation.
 * 
 * @author dhampl
 * 
 */
public class GeometricKienzleSimulationControl extends ASimulationControl {

	private static Logger logger = Logger
			.getLogger(GeometricKienzleSimulationControl.class.getName());

	protected List<double[]> samples; // values for every state
	protected int simulationStep = 0; // current sim step
	protected double kappa; // kienzle constant
	protected double kc; // kienzle constant
	protected double z; // kienzle constant

	/* Process parameter names */
	private String n_name;
	private String v_name;
	private String d_name;
	private String ap_name;

	/**
	 * 
	 * @param name
	 * @param unit
	 * @throws Exception
	 */
	public GeometricKienzleSimulationControl(String name, SiUnit unit)
			throws Exception {
		super(name, new SiUnit(Unit.NEWTONMETER));
		this.simulationPeriod = -1;

		readConfigFromFile();
	}

	/**
	 * only SI values.<br />
	 * length of all arrays must be the same.
	 * 
	 * @param name
	 *            name of the simulation control object
	 * @param n
	 *            spindle revolutions [1/s]
	 * @param f
	 *            feed [m/U]
	 * @param ap
	 *            depth of cut [m]
	 * @param d
	 *            diameter [m]
	 * @throws Exception
	 */
	public GeometricKienzleSimulationControl(String name, double[] n,
			double[] f, double[] ap, double[] d) throws Exception {
		super(name, new SiUnit(Unit.NEWTONMETER));
		if (n.length != f.length || n.length != ap.length
				|| n.length != d.length)
			throw new Exception("input violation: params must have same length");
		readConfigFromFile();
		calculateMoments(f, ap, d);
	}

	/**
	 * JAXB constructor: Sets name and unit
	 */
	public GeometricKienzleSimulationControl() {
		super();
	}

	@Override
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		super.afterUnmarshal(u, parent);

		if (samples == null) {
			readConfigFromFile();
		}
	}

	/**
	 * reads config parameters kappa, kc and z from file.
	 */
	public void readConfigFromFile() {
		samples = new ArrayList<double[]>();
		logger.log(LogLevel.DEBUG, "reading config for: "
				+ this.getClass().getSimpleName() + "_" + name);
		SimulationConfigReader scr = null;
		try {
			scr = new SimulationConfigReader(this.getClass().getSimpleName(),
					name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {

			// loop over all component states
			for (ComponentState cs : ComponentState.values()) {

				samples.add(scr.getDoubleArray(cs.name()));
			}
			kappa = scr.getDoubleValue("kappa") * Math.PI / 180;
			z = scr.getDoubleValue("z");
			kc = scr.getDoubleValue("kc");

			/* Read process parameter names */
			n_name = scr.getString("n_name");
			v_name = scr.getString("v_name");
			d_name = scr.getString("d_name");
			ap_name = scr.getString("ap_name");

			if (n_name.equals(""))
				n_name = "NONE";
			if (v_name.equals(""))
				n_name = "NONE";
			if (d_name.equals(""))
				d_name = "NONE";
			if (ap_name.equals(""))
				ap_name = "NONE";

			/* If diameter is set to none, we have to calculate a force! */
			if (d_name.equals("NONE"))
				setUnit(new SiUnit("N"));
			else
				setUnit(new SiUnit("Nm"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the input parameters (n,ap,d,...) of the Kienzle simulator. The
	 * parameter are read and processed and a time series containing the torque
	 * is generated.
	 * 
	 * @param process
	 *            Process object containing all process parameters.
	 */
	public void installKienzleInputParameters(Process process) {
		double[] n = null;
		double[] v = null;
		double[] ap = null;
		double[] d = null;
		double[] time = null;

		/*
		 * Look, if a rotational speed and a diameter is defined. If not, a
		 * force is required as output
		 */

		try {
			time = process.getDoubleArray("Time");
			simulationPeriod = Algo.greatestCommonDivisor(Algo
					.getIncrements(time));

			if (!n_name.matches("NONE"))
				n = SamplePeriodConverter.convertSamples(simulationPeriod,
						time, process.getDoubleArray(n_name));
			if (!d_name.matches("NONE"))
				d = SamplePeriodConverter.convertSamples(simulationPeriod,
						time, process.getDoubleArray(d_name));

			v = SamplePeriodConverter.convertSamples(simulationPeriod, time,
					process.getDoubleArray(v_name));
			ap = SamplePeriodConverter.convertSamples(simulationPeriod, time,
					process.getDoubleArray(ap_name));
		} catch (Exception e) {
			e.printStackTrace();
		}
		/* Check length */
		if ((!d_name.matches("NONE") && (n.length != v.length))
				|| (!n_name.matches("NONE") && (n.length != ap.length))
				|| ap.length != v.length) {
			Exception ex = new Exception(
					"input violation: params must have same length");
			ex.printStackTrace();
		}

		for (int i = 0; i < n.length; i++) {
			// TODO: check units
			if (!n_name.matches("NONE")) {
				if (0 == n[i])
					v[i] = 0;
				else
					v[i] = v[i] / (n[i]) * 1000; // m/s -> mm/U
			}

			v[i] = Math.abs(v[i]);
		}

		if (d_name.matches("NONE"))
			calculateForces(v, ap);
		else
			calculateMoments(v, ap, d);

	}

	protected void calculateMoments(double[] f, double[] ap, double[] d) {
		double[] moments = new double[f.length];
		double sinkappa = Math.sin(kappa);

		for (int i = 0; i < f.length; i++) {
			// calculate moments for every time step: Fc = kc * b * h^(1-z)
			// moment = Fc * d/2
			moments[i] = kc * (ap[i] / sinkappa)
					* Math.pow(f[i] * sinkappa, 1 - z) * d[i] / 2;
		}
		samples.set(ComponentState.PERIODIC.ordinal(), moments);
	}

	protected void calculateForces(double[] f, double[] ap) {
		double[] forces = new double[f.length];
		double sinkappa = Math.sin(kappa);

		for (int i = 0; i < f.length; i++) {
			// calculate forces for every time step: Fc = kc * b * h^(1-z)
			forces[i] = kc * (ap[i] / sinkappa)
					* Math.pow(f[i] * sinkappa, 1 - z);
		}
		samples.set(ComponentState.PERIODIC.ordinal(), forces);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.inspire.emod.simulation.ASimulationControl#update()
	 */
	@Override
	public void update() {
		logger.log(LogLevel.DEBUG, "update on " + getName() + " step: "
				+ simulationStep);
		// as samples are in the same order as machinestates, the machinestate
		// index (ordinal) can be used.
		simulationOutput.setValue(samples.get(state.ordinal())[simulationStep]);
		simulationStep++;
		if (simulationStep >= samples.get(state.ordinal()).length)
			simulationStep = 0;

	}

	/**
	 * sets and maps the {@link MachineState} to the appropriate
	 * {@link SimulationState}
	 * 
	 * @param state
	 */
	@Override
	public void setState(MachineState state) {
		if (this.state != stateMap.get(state)) {
			super.setState(state);
			simulationStep = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.inspire.emod.simulation.ASimulationControl#setSimulationPeriod
	 * (double)
	 */
	@Override
	public void setSimulationPeriod(double periodLength) {

		/* Resample the samples if the sampleperiod changed. */
		if (simulationPeriod != periodLength) {
			logger.log(LogLevel.DEBUG, "Resamling from" + simulationPeriod
					+ " to " + periodLength);
			try {
				for (int i = 0; i < samples.size(); i++) {
					samples.set(i, SamplePeriodConverter.convertSamples(
							simulationPeriod, periodLength, samples.get(i),
							true));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(-1);
			}
			simulationPeriod = periodLength;
		}
	}

}
