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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.ConstantComponent;
import ch.ethz.inspire.emod.model.IOContainer;
import ch.ethz.inspire.emod.model.LinearMotor;
import ch.ethz.inspire.emod.model.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.simulation.RandomSimulationControl;
import ch.ethz.inspire.emod.simulation.StaticSimulationControl;
import ch.ethz.inspire.emod.IOConnection;
import ch.ethz.inspire.emod.simulation.GeometricKienzleSimulationControl;

/**
 * 
 * @author andreas
 *
 */
public class MachineConfig {

	private static Logger logger = Logger.getLogger(MachineConfig.class.getName());
	
	/* List with all machine components */
	private ArrayList<MachineComponent> mclist;
	
	/* List with component output->input connection */
	private List<IOConnection> connectionList;
	
	/* List with simulation objects */
	private List<ASimulationControl> simulators;
	
	/**
	 * 
	 * @param machineName
	 * @param machineConfig
	 */
	public MachineConfig(String machineName, String machineConfig)
	{
		/* ****************************************************************** */
		/* Make list with all machine components */
		/* ****************************************************************** */
		ArrayList<MachineComponent> mclist = new ArrayList<MachineComponent>();
		
		/* Create machine components and add to mclist */
		MachineComponent mc1 = new MachineComponent("spindel");
		mc1.setComponent(new LinearMotor("siemens123"));
		mclist.add(mc1);
		
		MachineComponent mc2 = new MachineComponent("x");
		mc2.setComponent(new LinearMotor("siemens123"));
		mclist.add(mc2);
		
		MachineComponent mc3 = new MachineComponent("y");
		mc3.setComponent(new LinearMotor("siemens123"));
		mclist.add(mc3);
		
		MachineComponent mc4 = new MachineComponent("Fan1");
		mc4.setComponent(new ConstantComponent("80mmFan"));
		mclist.add(mc4);
		
		//test loading configs
		//Machine.initMachineFromFile("testmach.xml");
		
		/* Set machine instance */
		Machine.getInstance().setComponentList(mclist);
		
		/* ****************************************************************** */
		/* Make list with all simulation objects */
		/* ****************************************************************** */
		simulators = new ArrayList<ASimulationControl>();
		//sim.generateSimulation(20);
		simulators.add(new RandomSimulationControl("spindelRPM", Unit.RPM, "RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("spindelTorque", Unit.NEWTONMETER, "RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("xRPM", Unit.RPM, "RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("xTorque", Unit.NEWTONMETER, "RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("yRPM", Unit.RPM, "RandomSimulationControl_noname.txt"));
		simulators.add(new RandomSimulationControl("yTorque", Unit.NEWTONMETER, "RandomSimulationControl_noname.txt"));
		simulators.add(new StaticSimulationControl("test", Unit.NONE, "StaticSimulationControl_spindel1.txt"));
		double[] n = {2000, 2200, 2300, 3000};
		double[] f = {0.0001, 0.00008, 0.0009, 0.001};
		double[] ap = {0.003, 0.004, 0.009, 0.0005};
		double[] d = {0.006, 0.02, 0.004, 0.0001};
		try {
			simulators.add(new GeometricKienzleSimulationControl("test2", "L:/wrkspace/ch.ethz.inspire.emod/test/ch/ethz/inspire/emod/simulation/GeometricKienzleSimulationControl_tester.txt", n, f, ap, d));
		} catch (Exception e) {
			e.printStackTrace();
		}
		simulators.add(new StaticSimulationControl("test3", Unit.NONE, "StaticSimulationControl_80mmFan.txt"));
		
		/* ****************************************************************** */
		/*   Link outputs to inputs
		/* ****************************************************************** */
		makeInputOutputLinkList(machineName, machineConfig);
		
		/* TODO Check link list */
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
	 * 
	 * @param machineName       Name of the machine
	 * @param machineConfigName Name of the machine configuration
	 * 
	 */
	private static final String MACHINECONFIG = "MachineConfig";
	private static final String LINKFILENAME = "IOLinking.txt";
	public void makeInputOutputLinkList(String machineName, String machineConfigName) {
		
		/* Generate file name with path:
		 * e.g. Machines/NDM200/MachineConfig/TestConfig1/IOLinking.txt */
		String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix");
		String file = prefix + "/" + machineName + "/"+ MACHINECONFIG +"/" + 
		              machineConfigName + "/" + LINKFILENAME;
		
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

					MachineComponent inmc = Machine.getInstance().getComponent(inObj);
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
						
						MachineComponent outmc = Machine.getInstance().getComponent(outObj);
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
							Exception ex = new Exception("Undefined simulation object '" + outstruct+ "' " 
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
	 * @return Get machine component list
	 */
	public ArrayList<MachineComponent> getMachineComponentList()
	{
		return mclist;
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
}
