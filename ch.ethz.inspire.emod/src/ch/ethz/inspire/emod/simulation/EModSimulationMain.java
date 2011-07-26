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
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.IOContainer;
import ch.ethz.inspire.emod.model.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;

/**
 * Main simulation class
 * 
 * @author dhampl
 *
 */
@XmlRootElement(namespace = "ch.ethz.inspire.emod.simulation")
@XmlSeeAlso({ASimulationControl.class, RandomSimulationControl.class, StaticSimulationControl.class, MachineState.class, SimulationState.class, GeometricKienzleSimulationControl.class})
public class EModSimulationMain {
	
	private static Logger logger = Logger.getLogger(EModSimulationMain.class.getName());

	private double sampleperiod;
	@XmlElement
	private SimulationState machineState;
	private List<IOConnection> connectionList;
	@XmlElementWrapper(name = "simulators")
	@XmlElement(name = "simControler")
	private List<ASimulationControl> simulators;
	@XmlElement
	private String connectionsFile;
	
	public EModSimulationMain() {
		super();
		sampleperiod = 0.2; // seconds
		connectionList = new ArrayList<IOConnection>();
		simulators = new ArrayList<ASimulationControl>();
		machineState = new SimulationState();
	}

	/**
	 * adds an implementation of {@link ASimulationControl} to the simulation.
	 * 
	 * @param sim the simulation controller to be added 
	 */
	public void addSimulator(ASimulationControl sim) {
		simulators.add(sim);
	}
	
	/**
	 * reads the connections from {@link ASimulationControl} or {@link APhysicalComponent} outputs 
	 * to {@link APhysicalComponent} inputs from a file.
	 * <p>
	 * the file is required to adhere to the syntax 
	 * target_component_name.input_name=source_simulation/component_name.output_name
	 * 
	 * @param file config file for connections.
	 */
	public void readInputOutputConnectionsFromFile(String file) {
		logger.info("reading simulation connections setup from file: "+file);
		this.connectionsFile=file;
		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;
			
			while((line=input.readLine())!=null) {
				//tokenize & append
				
				StringTokenizer st = new StringTokenizer(line, "=");
				
				StringTokenizer stin = new StringTokenizer(st.nextToken(), ".");
				String inObj=stin.nextToken();
				String inVar=stin.nextToken();
				StringTokenizer stout = new StringTokenizer(st.nextToken(), ".");
				String outObj=stout.nextToken();
				String outVar=stout.nextToken();
				
				IOContainer tempTar = Machine.getInstance().getComponent(inObj).getComponent().getInput(inVar);
				IOContainer tempSource = null;
				if(Machine.getInstance().getComponent(outObj)!=null)
					tempSource = Machine.getInstance().getComponent(outObj).getComponent().getOutput(outVar);
				else {
					for(ASimulationControl sc : simulators) {
						if(sc.getName().equals(outObj)) {
							tempSource = sc.getOutput();
						}
					}
				}
				try {
					connectionList.add(new IOConnection(tempSource, tempTar));
				} catch (Exception e) {
					logger.log(Level.WARNING, inObj+" "+outObj);
					e.printStackTrace();
					System.exit(-1);
				}
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	/**
	 * starts the simulation
	 */
	public void runSimulation() {
		
		double time; 
		
		/* Create simulation output file to store the simulation
		 * data. 
		 */
		SimulationOutput simoutput = new SimulationOutput("simulation_output.dat", 
			Machine.getInstance().getMachineComponentList(), simulators);
		
		logger.info("starting simulation");
		time = 0.0;
		
		initSimulation();
		
		/* Init simulation control objects. */
		MachineState mstate = machineState.getState(time);
		for(ASimulationControl sc:simulators) {
			sc.setState(mstate);
			sc.update(); // TODO: write init method.
		}
		

		/* Time 0.0 s:
		 * All model and simulation outputs must be initiated. */
		// Log data at time 0.0 s
		simoutput.logData(time);
		while (time < machineState.simEndTime()) {
			
			/* Increment actual simulation time */
			time += sampleperiod;
			
			/* Set the inputs of all component models. */
			setInputs();
			
			/* Iterate all models. The outputs of all component models are updated.*/
			for(MachineComponent mc : Machine.getInstance().getMachineComponentList())
				mc.getComponent().update();
			
			/* Get next value from simulation control. */
			mstate = machineState.getState(time);
			for(ASimulationControl sc:simulators) {
				sc.setState(mstate);
				sc.update();
			}
			
			/*	Log data of the actual time sample */
			simoutput.logData(time);
		}
		/* Close simulation output */
		simoutput.close();
	}
	
	/**
	 * initialize the Simulation
	 */
	private void initSimulation() {
		logger.info("init simulation");
		Random rnd = new Random();
		for(MachineComponent mc : Machine.getInstance().getMachineComponentList()) {
			for(IOContainer ioc : mc.getComponent().getInputs())
				ioc.setValue(rnd.nextDouble());
		}
	}
	
	/**
	 * sets all inputs
	 */
	private void setInputs() {
		for(IOConnection ioc : connectionList) {
			ioc.getTarget().setValue(ioc.getSoure().getValue());
		}
	}
	
	/**
	 * reads a simulation config from a specified xml file
	 * 
	 * @param file
	 */
	public static EModSimulationMain initSimulationFromFile(String file) {
		EModSimulationMain result = null;
		try {
			JAXBContext context = JAXBContext.newInstance(EModSimulationMain.class);
			Unmarshaller um = context.createUnmarshaller();
			result = (EModSimulationMain) um.unmarshal(new FileReader(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		result.readInputOutputConnectionsFromFile(result.connectionsFile);
		return result;
	}
	
	/**
	 * saves the simulation config to a xml file.
	 * 
	 * @param file
	 */
	public void saveSimulationToFile(String file) {
		
		try {
			JAXBContext context = JAXBContext.newInstance(EModSimulationMain.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				
			Writer w = new FileWriter(file);
			m.marshal(this, w);
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * contains information on simulation input sources and targets
	 * through references to IOContainers of MachineComponents and
	 * SimulationControls.
	 * 
	 * @author dhampl
	 *
	 */
	protected class IOConnection {

		private IOContainer source;
		private IOContainer target;
		
		/**
		 * 
		 * @param source
		 * @param target 
		 * @throws Exception thrown if units don't match
		 */
		public IOConnection(IOContainer source, IOContainer target) throws Exception {
			this.source = source;
			this.target = target;
			if(source.getUnit()!=target.getUnit())
				throw new Exception("units do not match "+source.getName()+
						": "+source.getUnit()+" <-> "+target.getName()+": "+
						target.getUnit());
		}
		
		public IOContainer getSoure() {
			return source;
		}
		
		public IOContainer getTarget() {
			return target;
		}
	}
	
}
