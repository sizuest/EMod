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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import ch.ethz.inspire.emod.IOConnection;
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
	
	public EModSimulationMain(String machineName, String simConfigName) {
		super(); // ?? XXX
		sampleperiod = 0.2; // seconds
		machineState = new SimulationState(machineName, simConfigName);
	}

	/**
	 * Set IO connection list
	 * 
	 * @param list
	 */
	public void setIOConnectionList(List<IOConnection> list)
	{
		connectionList = list;
	}
	
	/**
	 * Set list with object for input parameter generation.
	 * 
	 * @param list
	 */
	public void setInputparamObjectList(List<ASimulationControl> list)
	{
		simulators = list;
	}
	
	/**
	 * Do the simulation
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
		// Simulation mail loop:
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
	 * sets all inputs
	 */
	private void setInputs() {
		for(IOConnection ioc : connectionList) {
			ioc.getTarget().setValue(ioc.getSoure().getValue());
		}
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
		//result.makeInputOutputLinkList();
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
}
