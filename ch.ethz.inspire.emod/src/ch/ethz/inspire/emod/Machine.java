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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.MachineComponents;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.simulation.InputSimulators;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * 
 * @author andreas
 *
 */
public class Machine {

	private static Logger logger = Logger.getLogger(Machine.class.getName());
	
	/* List with machine components */
	private static ArrayList<MachineComponent> componentList;
	private static MachineComponents machinecomponents;
	
	/* List with component output->input connection */
	private static List<IOConnection> connectionList;
	
	/* List with simulation objects */
	private static List<ASimulationControl> simulators;
	private static InputSimulators inputsimulators;
	
	/* Path definitions */
	private static final String MACHINECONFIGDIR = "MachineConfig";
	private static final String COMPONENTFILENAME = "ComponentList.xml";
	private static final String LINKFILENAME = "IOLinking.txt";
	private static final String INPUTSIMFILENAME = "InputSimulators.xml";
	
	/* Model reference */
	private static Machine machineModel=null;
	
	/**
	 * Private constructor for singleton implementation.
	 */
	private Machine()
	{
		
	}
	
	/**
	 * Reads the machine config of the machine.
	 * 
	 * @param machineName   Name of machine
	 * @param machineConfig Name of machine config
	 */
	public static void buildMachine(String machineName, String machineConfig)
	{
		/* Create machine */
		if(machineModel==null)
			machineModel=new Machine();
		
		/* Generate path to machine config:
		 * e.g. Machines/NDM200/MachineConfig/TestConfig1/ */
		String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix");
		String path = prefix + "/" + machineName + "/"+ MACHINECONFIGDIR +"/" + 
		              machineConfig + "/";
		
		/* ****************************************************************** */
		/* Make list containing all machine components */
		/* ****************************************************************** */
		logger.info("Load machine components from file: " + path + COMPONENTFILENAME);
		machinecomponents = initMachineComponentsFromFile(path + COMPONENTFILENAME);
		componentList = machinecomponents.getMachineComponentList();
		
		/* ****************************************************************** */
		/* Make list with all simulator objects */
		/* ****************************************************************** */

		/*simulators = new ArrayList<ASimulationControl>();
		//sim.generateSimulation(20);
		simulators.add(new RandomSimulationControl("spindelRPM", Unit.RPM, path+"RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("spindelTorque", Unit.NEWTONMETER, path+"RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("xRPM", Unit.RPM, path+"RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("xTorque", Unit.NEWTONMETER, path+"RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("yRPM", Unit.RPM, path+"RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("yTorque", Unit.NEWTONMETER, path+"RandomSimulationControl_noname.txt"));
		simulators.add(new StaticSimulationControl("test", Unit.NONE, path+"StaticSimulationControl_tester.txt"));
		double[] n = {2000, 2200, 2300, 3000};
		double[] f = {0.1, 0.08, 0.9, 1};
		double[] ap = {3, 4, 9, 0.5};
		double[] d = {0.006, 0.02, 0.004, 0.0001};
		try {
			simulators.add(new GeometricKienzleSimulationControl("test2", "L:/wrkspace/ch.ethz.inspire.emod/test/ch/ethz/inspire/emod/simulation/GeometricKienzleSimulationControl_tester.txt", n, f, ap, d));
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		logger.info("Load input simulators from file: " + path + INPUTSIMFILENAME);
		inputsimulators = initInputSimulatorsFromFile(path + INPUTSIMFILENAME);
		simulators = inputsimulators.getSimulatorList();
		for (ASimulationControl sim : simulators) {
			sim.afterJABX();
		}
		
		
		
		/* ****************************************************************** */
		/*   Link outputs to inputs
		/* ****************************************************************** */
		makeInputOutputLinkList(path);
		
		/* Check link list: Every input must be connected. */
		checkMachineConfig();
		
	}

	/**
	 * reads a machine config from a specified xml file
	 * 
	 * @param file
	 */
	public static MachineComponents initMachineComponentsFromFile(String file) {
		MachineComponents machineModel = null;
		try {
			JAXBContext context = JAXBContext.newInstance(MachineComponents.class);
			Unmarshaller um = context.createUnmarshaller();
			machineModel = (MachineComponents) um.unmarshal(new FileReader(file));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return machineModel;
	}
	
	/**
	 * saves the machine config to a xml file.
	 * 
	 * @param file
	 */
	public void saveMachineComponentsToFile(String file) {

		try {
			JAXBContext context = JAXBContext.newInstance(MachineComponents.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				
			Writer w = new FileWriter(file);
			m.marshal(machinecomponents, w);
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read the linking of component outputs to component inputs from file.
	 * Output objects are from type {@link ASimulationControl} or {@link APhysicalComponent},
	 * input objects from type {@link APhysicalComponent}.
	 * <p>
	 * the file is required to adhere to the syntax 
	 * target_component_name.input_name = component_name.output_name
	 * or
	 * target_component_name.input_name = source_simulation
	 * Comments have a leading '#'
	 * Spaces and tabs are allowed.
	 * <p>
	 * The linklist is stored in connectionList
	 * 
	 * @param path  Path to the linking file
	 * 
	 */
	private static void makeInputOutputLinkList(String path) {
		
		/* Path and filename with linking definitions. */
		String file = path + LINKFILENAME;
		
		logger.info("Make input-output component link list from file: " + file);
		
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(file));
		}
		catch (Exception e) {
			Exception ex = new Exception("Could not open machine config file: " + e.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		}
		
		connectionList = new ArrayList<IOConnection>();
		
		/* Read file: line by line */
		int linenr=0;
		try {
			String line = null;
			
			while((line=input.readLine())!=null) {
				linenr++;
				
				// Remove comments: A comment is identified by a leading '#'.
				String l = line.replaceAll("#.*", "").replace("\t", " ").trim();
				
				/* Input and outputs are separated by '=' */
				StringTokenizer st = new StringTokenizer(l, "=");
				
				if (st.hasMoreTokens()) {
					
					/* Get input component and input object */
					StringTokenizer stin = new StringTokenizer(st.nextToken().trim(), ".");
					String inObj=stin.nextToken();
					String inVar=stin.nextToken();

					MachineComponent inmc = machinecomponents.getComponent(inObj);
					if (inmc == null) {
						Exception ex = new Exception("Undefined input component '" + inObj+ "' in file " + file + " on line " + linenr);
						ex.printStackTrace();
						System.exit(-1);
					}

					IOContainer tempTar = inmc.getComponent().getInput(inVar);
					if (tempTar == null) {	
						Exception ex = new Exception("Undefined input '" + inVar+ "' of component '" + inObj 
								+ "' in file " + file + " on line " + linenr);
						ex.printStackTrace();
						System.exit(-1);
					}

					/* Get output component and output object */
					String outstruct = st.nextToken().trim();
					StringTokenizer stout = new StringTokenizer(outstruct, ".");
					String outObj=stout.nextToken();
					IOContainer tempSource = null;
					if (stout.hasMoreTokens()) {

						/* Output object comes from a component*/
						String outVar=stout.nextToken();
						
						MachineComponent outmc = machinecomponents.getComponent(outObj);
						if (outmc == null) {
							Exception ex = new Exception("Undefined output component '" + outObj+ "' in file " 
											+ file + " on line " + linenr);
							ex.printStackTrace();
							System.exit(-1);
						}
						
						tempSource = outmc.getComponent().getOutput(outVar);
						if (tempSource == null) {	
							Exception ex = new Exception("Undefined output '" + outVar+ "' of component '" + outObj 
										+ "' in file " + file + " on line " + linenr);
							ex.printStackTrace();
							System.exit(-1);
						}
					}
					else {
						/* If no output component, it must be a simulation object*/
						for(ASimulationControl sc : simulators) {
							if(sc.getName().equals(outstruct)) {
								tempSource = sc.getOutput();
								break;
							}
						}
						if (tempSource == null) {	
							Exception ex = new Exception("Undefined simulation object '" + outstruct 
										+ "' in file " + file + " on line " + linenr);
							ex.printStackTrace();
							System.exit(-1);
						}
					}
					
					try {
						connectionList.add(new IOConnection(tempSource, tempTar));
					} catch (Exception e) {
						System.err.println("Could not add input-output mapping, file " + file + " line " + linenr);
						e.printStackTrace();
						System.exit(-1);
					}
				}
			}
			input.close();
		} catch (Exception e) {
			Exception ex = new Exception("Parse error in " + file + " on line " + linenr + " : " + e.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		} 
		
	}
	
	/**
	 * Check the machine config
	 * 
	 * 1. Check: Every input of all machine componenets must be set exactly once in the
	 *           connectionlist.
	 */
	private static void checkMachineConfig()
	{
		ArrayList<MachineComponent> mclist = machinecomponents.getMachineComponentList();
		int mc_in_iolist_cnt = 0;
		
		/* 1. Check: Every input of all machine componenets must be set exactly once in the
		 *           connectionlist.
		 */
		for (MachineComponent mc : mclist) {
			for (IOContainer mcinput : mc.getComponent().getInputs()) {
				mc_in_iolist_cnt = 0;
				for (IOConnection iolink : connectionList) {
					if (mcinput == iolink.getTarget()) {
						mc_in_iolist_cnt++;
					}
				}
				if (mc_in_iolist_cnt == 0) {
					Exception ex = new Exception("checkMachineConfig: Input " + 
							mc.getName() + "." + mcinput.getName() +
							" is not connected!");
					ex.printStackTrace();
				}
				else if (mc_in_iolist_cnt >= 2) {
					Exception ex = new Exception("checkMachineConfig: Input " + 
							mc.getName() + "." + mcinput.getName() +
							" is linked multiple times!");
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * reads a simulation config from a specified xml file
	 * 
	 * @param file
	 */
	public static InputSimulators initInputSimulatorsFromFile(String file) {
		InputSimulators is = null;
		try {
			JAXBContext context = JAXBContext.newInstance(InputSimulators.class);
			Unmarshaller um = context.createUnmarshaller();
			is = (InputSimulators) um.unmarshal(new FileReader(file));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return is;
	}
	
	
	/**
	 * saves the input simulators to a xml file.
	 * 
	 * @param file
	 */
	public void saveInputSimulatorsToFile(String file) {
		
		try {
			JAXBContext context = JAXBContext.newInstance(InputSimulators.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				
			Writer w = new FileWriter(file);
			m.marshal(inputsimulators, w);
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return list of all input-Output connections
	 */
	public ArrayList<MachineComponent> getMachineComponentList()
	{
		return componentList;
	}
	
	/**
	 * 
	 * @return List with input parameter simulation objects
	 */
	public List<ASimulationControl> getInputObjectList()
	{
		return simulators;
	}
	
	/**
	 * 
	 * @return list of all input-Output connections
	 */
	public List<IOConnection> getIOLinkList()
	{
		return connectionList;
	}
	
	/**
	 * singleton implementation of the machine model
	 * 
	 * @return instance of the machine model
	 */
	public static Machine getInstance() {
		if(machineModel==null) {
			Exception ex = new Exception("Machine not yet built!");
			ex.printStackTrace();
			System.exit(-1);
		}
		return machineModel;
	}

}
