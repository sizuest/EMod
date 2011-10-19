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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.ConstantComponent;
import ch.ethz.inspire.emod.model.Clamp;
import ch.ethz.inspire.emod.model.LinAxis;
import ch.ethz.inspire.emod.model.Motor;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.simulation.GeometricKienzleSimulationControl;
import ch.ethz.inspire.emod.simulation.ProcessSimulationControl;
import ch.ethz.inspire.emod.simulation.RandomSimulationControl;
import ch.ethz.inspire.emod.simulation.StaticSimulationControl;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * 
 * @author andreas
 *
 */
@XmlRootElement(namespace = "ch.ethz.inspire.emod")
@XmlSeeAlso({MachineComponent.class, APhysicalComponent.class, Motor.class, LinAxis.class,
	Clamp.class, ConstantComponent.class, 
	ASimulationControl.class, RandomSimulationControl.class, StaticSimulationControl.class, 
	ProcessSimulationControl.class, GeometricKienzleSimulationControl.class})
@XmlAccessorType(XmlAccessType.NONE)
public class Machine {

	private static Logger logger = Logger.getLogger(Machine.class.getName());
	
	/* List with machine components */
	@XmlElementWrapper(name = "machine")
	@XmlElement(name = "machineComponent")
	private ArrayList<MachineComponent> componentList;

	/* List with simulation objects */
	@XmlElementWrapper
	@XmlElement(name = "simController")
	private List<ASimulationControl> simulators;
	
	/* List with component output->input connection */
	private List<IOConnection> connectionList;
	
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
		if(machineModel!=null)
			logger.info("Overwrinting current machine with '"+machineName+"' - '"+machineConfig+"'");
		
		/* Generate path to machine config:
		 * e.g. Machines/NDM200/MachineConfig/TestConfig1/ */
		String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix");
		String path = prefix + "/" + machineName + "/"+ Defines.MACHINECONFIGDIR +"/" + 
		              machineConfig + "/";
		
		/* ****************************************************************** */
		/* Init machine form config */
		/* ****************************************************************** */
		logger.info("Load machine from file: " + path + Defines.MACHINEFILENAME);
		initMachineFromFile(path + Defines.MACHINEFILENAME);
				
		/* ****************************************************************** */
		/*   Link outputs to inputs
		/* ****************************************************************** */
		makeInputOutputLinkList(path);
		
		/* Check link list: Every input must be connected. */
		checkMachineConfig();
		
	}
	
	public static void newMachine(String machineName, String machineConfigDir) {
		//TODO: create new machine model, prepare lists, config dirs etc
	}

	public static void initMachineFromFile(String file) {
		try {
			JAXBContext context = JAXBContext.newInstance(Machine.class);
			Unmarshaller um = context.createUnmarshaller();
			machineModel = (Machine) um.unmarshal(new FileReader(file));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void saveMachineToFile(String file) {
		try {
			JAXBContext context = JAXBContext.newInstance(Machine.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			Writer w = new FileWriter(file);
			m.marshal(machineModel, w);
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
		String file = path + Defines.LINKFILENAME;
		
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
		
		Machine.getInstance().connectionList = new ArrayList<IOConnection>();
		
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

					MachineComponent inmc = getMachineComponent(inObj);
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
						
						MachineComponent outmc = getMachineComponent(outObj);
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
						for(ASimulationControl sc : Machine.getInstance().simulators) {
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
						Machine.getInstance().connectionList.add(new IOConnection(tempSource, tempTar));
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
		int mc_in_iolist_cnt = 0;
		
		/* 1. Check: Every input of all machine componenets must be set exactly once in the
		 *           connectionlist.
		 */
		for (MachineComponent mc : Machine.getInstance().componentList) {
			for (IOContainer mcinput : mc.getComponent().getInputs()) {
				mc_in_iolist_cnt = 0;
				for (IOConnection iolink : Machine.getInstance().connectionList) {
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
	 * returns the first machine component with a specified name.
	 * 
	 * @param name
	 * @return the {@link MachineComponent} with the name. 
	 */
	public static MachineComponent getMachineComponent(String name){
		MachineComponent temp=null;
		for(MachineComponent mc : Machine.getInstance().componentList) {
			if(mc.getName().equals(name)) {
				temp=mc;
				break;
			}
		}
		
		return temp;
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

	/**
	 * ================================================================================
	 * Methods for test only!
	 * ================================================================================
	 */
	
	/**
	 * Create machine instance. 
	 * For test purpose only!
	 * 
	 */
	public static void dummyBuildMachine()
	{
		/* Create machine */
		if(machineModel==null)
			machineModel=new Machine();
	}
	/**
	 * Set or overwrite componentList: Use for test purpose only!
	 * @param list
	 */
	public void setMachineComponentList(ArrayList<MachineComponent> list)
	{
		componentList = list;
	}
}
