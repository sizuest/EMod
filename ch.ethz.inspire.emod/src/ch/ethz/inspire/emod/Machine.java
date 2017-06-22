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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

import ch.ethz.inspire.emod.model.*;
import ch.ethz.inspire.emod.model.math.*;
import ch.ethz.inspire.emod.model.thermal.*;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.model.control.*;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.simulation.ConstantSimulationControl;
//import ch.ethz.inspire.emod.simulation.GeometricKienzleSimulationControl_old;
import ch.ethz.inspire.emod.simulation.GeometricKienzleSimulationControl;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.simulation.ProcessSimulationControl;
import ch.ethz.inspire.emod.simulation.RandomSimulationControl;
import ch.ethz.inspire.emod.simulation.StaticSimulationControl;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.FluidConnection;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

import java.lang.reflect.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 
 * @author andreas
 * 
 */
@XmlRootElement(namespace = "ch.ethz.inspire.emod")
@XmlSeeAlso({ MachineComponent.class, APhysicalComponent.class, Motor.class,
		MotorAC.class, LinAxis.class, RotAxis.class, ClampTest.class,
		MotorDC.class, Revolver.class, Fan.class, HydraulicAccumulator.class,
		Cooler.class, Transmission.class, CompressedFluid.class,
		Amplifier.class, ConstantComponent.class, Servodrive.class,
		Cylinder.class, Valve.class, Pipe.class, HeatExchanger.class,
		Bypass.class, HeatExchangerAir.class, MovingMass.class, Bearing.class,
		Spindle.class, Tank.class, Pump.class, ForcedFluidFlow.class,
		CooledHeatSource.class,
		HysteresisControl.class, SwitchControl.class, Sum.class, Gain.class,
		HomogStorage.class, LayerStorage.class, ForcedHeatTransfere.class,
		FreeHeatTransfere.class, ASimulationControl.class,
		RandomSimulationControl.class, ConstantSimulationControl.class,
		StaticSimulationControl.class, ProcessSimulationControl.class,
		GeometricKienzleSimulationControl.class,
		IOConnection.class, FluidConnection.class})
@XmlAccessorType(XmlAccessType.FIELD)
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
	@XmlElementWrapper(name = "linking")
	@XmlElement(name = "ioConnection")
	private List<IOConnection> connectionList;

	/* Model reference */
	private static Machine machineModel = null;

	/**
	 * Private constructor for singleton implementation.
	 */
	private Machine() {
	}

	/**
	 * Reads the machine config of the machine.
	 * 
	 * @param machineName
	 *            Name of machine
	 * @param machineConfig
	 *            Name of machine config
	 */
	public static void buildMachine(String machineName, String machineConfig) {
		/* Create machine */
		if (machineModel != null)
			logger.info("Overwrinting current machine with '" + machineName
					+ "' - '" + machineConfig + "'");

		/*
		 * Generate path to machine config: e.g.
		 * Machines/NDM200/MachineConfig/TestConfig1/
		 */
		String prefix = PropertiesHandler
				.getProperty("app.MachineDataPathPrefix");
		String path = prefix + "/" + machineName + "/"
				+ Defines.MACHINECONFIGDIR + "/" + machineConfig + "/";

		/* ****************************************************************** */
		/* Init machine form config */
		/* ****************************************************************** */
		logger.info("Load machine from file: " + path + Defines.MACHINEFILENAME);
		initMachineFromFile(path + Defines.MACHINEFILENAME);


	}

	/**
	 * Saves the machine
	 * 
	 * @param machineName
	 *            Name of machine
	 * @param machineConfig
	 *            Name of machine config
	 */
	public static void saveMachine(String machineName, String machineConfig) {
		/*
		 * Generate path to machine config: e.g.
		 * Machines/NDM200/MachineConfig/TestConfig1/
		 */
		String prefix = EModSession.getRootPath();
		String path = prefix + File.separator + Defines.MACHINECONFIGDIR + File.separator + machineConfig + File.separator;

		/* Saves the machine */
		saveMachineToFile(path + Defines.MACHINEFILENAME);

		/* Clean up old simulator configs */
		cleanUpConfigurations(path);
	}

	/**
	 * @param machineName
	 *            Name of the machine
	 * @param machineConfigDir
	 *            Name of the machine configuration
	 */
	public static void newMachine(String machineName, String machineConfigDir) {

		/*
		 * Check, if the directories already exists
		 */
		String prefix = PropertiesHandler
				.getProperty("app.MachineDataPathPrefix");

		File path = new File(prefix + "/" + machineName + "/"
				+ Defines.MACHINECONFIGDIR + "/" + machineConfigDir);
		// Creat directory if required
		if (path.exists()) {
			logger.warning("Can't create new machine " + machineName + ":"
					+ machineConfigDir + ": Machine already exists");
			return;
		}

		// Create directory if required
		path.mkdirs();

		// Create empty machine
		// clearMachine();

		// Save machine
		saveMachine(machineName, machineConfigDir);

		// Save IO Linking

	}

	/**
	 * Clears the current machine
	 */
	public static void clearMachine() {
		machineModel = new Machine();

		getInstance().componentList = new ArrayList<MachineComponent>();
		getInstance().simulators = new ArrayList<ASimulationControl>();
		getInstance().connectionList = new ArrayList<IOConnection>();
	}

	/**
	 * Deletes the machine
	 * 
	 * @param machineName
	 * @param machineConfigDir
	 */
	public static void deleteMachine(String machineName, String machineConfigDir) {
		// Clear current machine
		clearMachine();

		// Check for config dir
		String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix");

		// TOOD manick: the correct filepath is
		// machineName/MachineConfig/machineConfigDir!
		// File path = new File(prefix+"/"+machineName+"/"+machineConfigDir);
		File path = new File(prefix + "/" + machineName + "/MachineConfig/"
				+ machineConfigDir);
		// Creat directory if required
		if (!path.exists()) {
			logger.warning("Can't delete machine " + machineName + ":"
					+ machineConfigDir + ": Machine does not exists");
			return;
		}

		// Empty directory
		for (File f : path.listFiles()) {
			try {
				f.delete();
			} catch (Exception e) {
				logger.warning("Can't delete machine " + machineName + ":"
						+ machineConfigDir
						+ ": Failed to remove configuration file "
						+ f.getName());
			}
		}

		try {
			path.delete();
		} catch (Exception e) {
			logger.warning("Can't delete machine " + machineName + ":"
					+ machineConfigDir
					+ ": Failed to remove configuration directory");
		}
	}

	/**
	 * Initializes a new machin based on the machine name provided
	 * 
	 * @param file
	 */
	public static void initMachineFromFile(String file) {
		machineModel = null;
		try {
			JAXBContext context = JAXBContext.newInstance(Machine.class);
			Unmarshaller um = context.createUnmarshaller();
			machineModel = (Machine) um.unmarshal(new FileReader(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* ****************************************************************** */
		/*
		 * Link outputs to inputs /*
		 * ******************************************************************
		 */
		
		if(null != getInstance().getIOLinkList())
			for(int i=getInstance().getIOLinkList().size()-1; i>=0; i--)
				if(!(getInstance().getIOLinkList().get(i).createIOConnection()))
					getInstance().getIOLinkList().remove(i);
				
		/* Not used anymore, only for cold configs
		 * 
		 * Assumption: If ioList is still empty, try to load old config
		 */
		/*if(null == getInstance().getIOLinkList() | 
				getInstance().getIOLinkList().size() == 0 ){
			logger.info("No connections found. Trying to read old file");
			try {
				makeInputOutputLinkList(file);
			} catch (Exception e) {
				logger.warning("Trying to read old file failed");
			}
		}*/
			

		/* Check link list: Every input must be connected. */
		checkMachineConfig();
	}

	/**
	 * Saves the existing machine at file name provided
	 * 
	 * @param file
	 */
	public static void saveMachineToFile(String file) {
		// Save Machine Configuration
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
	 * Saves the existing machine at a new location
	 * 
	 * @param file
	 */
	public static void saveMachineToNewFile(String file) {
		// Save Machine Configuration
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
	 * Saves the initial conditions
	 */
	public static void saveInitialConditions() {
		String name;
		for (DynamicState ds : getInstance().getDynamicStatesList()) {
			name = "*unknown*";
			try {
				name = ds.getInitialConditionName();
				ds.saveInitialCondition();
			} catch (Exception e) {
				logger.warning("Failed to save initial condition " + name
						+ ": " + e.getMessage());
			}
		}
	}

	/**
	 * Loads the initial conditions
	 */
	public static void loadInitialConditions() {
		String name;
		for (DynamicState ds : getInstance().getDynamicStatesList()) {
			name = "*unknown*";
			try {
				name = ds.getInitialConditionName();
				ds.loadInitialCondition();
			} catch (Exception e) {
				logger.warning("Failed to load initial condition " + name
						+ ": " + e.getMessage());
			}
		}
	}

	/**
	 * Load the linking list from the given location
	 * 
	 * @param file
	 */
	@Deprecated
	public static void loadIOLinking(String file) {
		makeInputOutputLinkList(file);
	}

	/**
	 * Cleans up old simulator configs
	 * 
	 * All files names at the state path are checked for - Equal to Machine.xml
	 * (Defines.MACHINEFILENAME) - Equal to IOLinking.txt (Defines.LINKFILENAME)
	 * - Equal to an existing simulation control
	 * 
	 * if non of the statements above is true, the file will be removed
	 * 
	 * @param path
	 */
	public static void cleanUpConfigurations(String path) {
		File dir = new File(path);
		File[] files = dir.listFiles();

		ArrayList<String> simConfigFileNames = new ArrayList<String>();

		/*
		 * Create the set of required simulator config files
		 */
		if(null!=getInstance().simulators)
			for (ASimulationControl sc : getInstance().simulators)
				try {
					simConfigFileNames.add(sc.getSimulationConfigReader()
							.getFileName());
				} catch (Exception e) {
					e.printStackTrace();
				}

		/*
		 * Loop through all files and check if they are still required
		 */
		fileloop: for (File f : files) {
			/*
			 * Equal to machine config file?
			 */
			if (f.getName().contentEquals(Defines.MACHINEFILENAME))
				continue;
			/*
			 * Equal to io linking file?
			 */
			if (f.getName().contentEquals(Defines.LINKFILENAME))
				continue;

			/*
			 * All other files must be simulator configuration: Let's check if
			 * they are still required
			 */
			for (String s : simConfigFileNames)
				if (f.getName().contentEquals(s))
					continue fileloop;

			// System.out.println(f.getName());
			/*
			 * The file is not needed any more: delete it!
			 */
			f.delete();
		}
	}

	/**
	 * Saves the component linking
	 * 
	 * @param file
	 */
	@Deprecated
	public static void saveIOLinking(String file) {
		List<IOConnection> connections = getInstance().getIOLinkList();
		List<MachineComponent> components = getInstance()
				.getMachineComponentList();
		List<ASimulationControl> simulators = getInstance()
				.getInputObjectList();

		String source = "", target = "";

		try {
			Writer w = new FileWriter(file + Defines.LINKFILENAME);

			/*
			 * TODO: at first, simply close the file to delete file content!
			 */
			// w.close();

			/*
			 * open file again
			 */
			// w = new FileWriter(file+Defines.LINKFILENAME);

			/*
			 * Loop through all connections and write them as plain text
			 */

			for (IOConnection io : connections) {
				source = "";
				target = "";
				// Simulators
				for (ASimulationControl sc : simulators) {
					if (sc.getOutput().equals(io.getSource().getReference())) {
						source = sc.getName();
						break;
					}
				}
				// Components
				for (MachineComponent mc : components) {
					if (mc.getComponent().getInputs()
							.contains(io.getTarget().getReference()))
						target = mc.getName() + "."
								+ io.getTarget().getReference().getName();
					if (mc.getComponent().getOutputs()
							.contains(io.getSource().getReference()))
						source = mc.getName() + "."
								+ io.getSource().getReference().getName();
					if (!source.isEmpty() & !target.isEmpty())
						break;
				}

				if (source.isEmpty() | target.isEmpty()) {
					Exception ex = new Exception(
							"Can not parse IOConnection: Missing Component or Input");
					ex.printStackTrace();
				} else
					w.write(target + " = " + source + "\n");
			}

			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read the linking of component outputs to component inputs from file.
	 * Output objects are from type {@link ASimulationControl} or
	 * {@link APhysicalComponent}, input objects from type
	 * {@link APhysicalComponent}.
	 * <p>
	 * the file is required to adhere to the syntax
	 * target_component_name.input_name = component_name.output_name or
	 * target_component_name.input_name = source_simulation Comments have a
	 * leading '#' Spaces and tabs are allowed.
	 * <p>
	 * The linklist is stored in connectionList
	 * 
	 * @param path
	 *            Path to the linking file
	 * 
	 */
	@Deprecated
	private static void makeInputOutputLinkList(String path) {

		/* Path and filename with linking definitions. */
		String file = path + Defines.LINKFILENAME;

		logger.info("Make input-output component link list from file: " + file);

		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(file));
		} catch (Exception e) {
			Exception ex = new Exception("Could not open machine config file: "
					+ e.getMessage());
			ex.printStackTrace();
			return;
		}

		/* Clear old conncetions */
		Machine.getInstance().connectionList = new ArrayList<IOConnection>();

		/* Remove all inputs / outputs with reference */
		for (MachineComponent mc : Machine.getInstance().getMachineComponentList()) {
			for (int i = mc.getComponent().getInputs().size() - 1; i >= 0; i--)
				if (mc.getComponent().getInputs().get(i).hasReference())
					mc.getComponent().getInputs().remove(i);

			for (int i = mc.getComponent().getOutputs().size() - 1; i >= 0; i--)
				if (mc.getComponent().getOutputs().get(i).hasReference())
					mc.getComponent().getOutputs().remove(i);
		}

		/* Read file: line by line */
		int linenr = 0;
		try {
			String line = null;

			while ((line = input.readLine()) != null) {
				linenr++;

				// Remove comments: A comment is identified by a leading '#'.
				String l = line.replaceAll("#.*", "").replace("\t", " ").trim();

				/* Input and outputs are separated by '=' */
				StringTokenizer st = new StringTokenizer(l, "=");

				if (st.hasMoreTokens()) {

					/* Get input component and input object */
					StringTokenizer stin = new StringTokenizer(st.nextToken()
							.trim(), ".");
					String inObj = stin.nextToken();
					String inVar = stin.nextToken();

					MachineComponent inmc = getMachineComponent(inObj);
					if (inmc == null) {
						Exception ex = new Exception(
								"Undefined input component '" + inObj
										+ "' in file " + file + " on line "
										+ linenr);
						ex.printStackTrace();
						continue;
					}

					// when a fluidconnection is necessary ->
					// create two FluidContainers instead
					IOContainer tempTar = inmc.getComponent().getInput(inVar);
					if (tempTar == null) {
						Exception ex = new Exception("Undefined input '"
								+ inVar + "' of component '" + inObj
								+ "' in file " + file + " on line " + linenr);
						ex.printStackTrace();
						continue;
					}

					// check if source and target can handle a fluidConnection
					boolean targetFluid = false;
					boolean sourceFluid = false;

					// when handling a FluidConnection
					// if(tempTar.getName().equals("FluidIn")){
					if (tempTar instanceof FluidContainer) {
						targetFluid = true;
					}

					/* Get output component and output object */
					String outstruct = st.nextToken().trim();
					StringTokenizer stout = new StringTokenizer(outstruct, ".");
					String outObj = stout.nextToken();
					MachineComponent outmc = null;

					IOContainer tempSource = null;
					if (stout.hasMoreTokens()) {

						/* Output object comes from a component */
						String outVar = stout.nextToken();

						// MachineComponent outmc = getMachineComponent(outObj);
						outmc = getMachineComponent(outObj);
						if (outmc == null) {
							Exception ex = new Exception(
									"Undefined output component '" + outObj
											+ "' in file " + file + " on line "
											+ linenr);
							ex.printStackTrace();
							continue;
						}

						tempSource = outmc.getComponent().getOutput(outVar);
						if (tempSource == null) {
							Exception ex = new Exception("Undefined output '"
									+ outVar + "' of component '" + outObj
									+ "' in file " + file + " on line "
									+ linenr);
							ex.printStackTrace();
							continue;
						}

						// when handling a FluidConnection
						// if(tempSource.getName().equals("FluidOut")){
						if (tempSource instanceof FluidContainer) {
							sourceFluid = true;
						}

					} else {
						/*
						 * If no output component, it must be a simulation
						 * object
						 */
						for (ASimulationControl sc : Machine.getInstance().simulators) {
							if (sc.getName().equals(outstruct)) {
								tempSource = sc.getOutput();
								break;
							}
						}
						if (tempSource == null) {
							Exception ex = new Exception(
									"Undefined simulation object '" + outstruct
											+ "' in file " + file + " on line "
											+ linenr);
							ex.printStackTrace();
							continue;
						}
					}

					try {
						if (targetFluid && sourceFluid) {// create a
															// FluidConnection
							// Machine.getInstance().connectionList.add(new
							// FluidConnection(inmc.getComponent(),
							// outmc.getComponent()));
							// Machine.getInstance().connectionList.add(new
							// FluidConnection(outmc.getComponent(),
							// inmc.getComponent()));
							Machine.getInstance().connectionList
									.add(new FluidConnection(
											(FluidContainer) tempSource,
											(FluidContainer) tempTar));
						} else {// create a IOConnection
							Machine.getInstance().connectionList
									.add(new IOConnection(tempSource, tempTar));
						}
					} catch (Exception e) {
						logger.warning("Could not add input-output mapping, file "
								+ file + " line " + linenr);
						e.printStackTrace();
						continue;
					}
				}
			}
			input.close();
		} catch (Exception e) {
			Exception ex = new Exception("Parse error in " + file + " on line "
					+ linenr + " : " + e.getMessage());
			ex.printStackTrace();
		}

	}

	/**
	 * Check the machine config
	 * 
	 * 1. Check: Every input of all machine componenets must be set exactly once
	 * in the connectionlist. 2. Check: Every dynamic state must have a
	 * numerical initial condition
	 */
	private static void checkMachineConfig() {

		/*
		 * 1. Check: Every input of all machine componenets must be set exactly
		 * once in the connectionlist.
		 */
		ConfigurationChecker.checkIOLinking().loggger();
	}

	/**
	 * returns the first machine component with a specified name.
	 * 
	 * @param name
	 * @return the {@link MachineComponent} with the name.
	 */
	public static MachineComponent getMachineComponent(String name) {

		// Case: Empty machine
		if (null == machineModel)
			return null;
		else if (null == Machine.getInstance().componentList)
			return null;

		// Default case:
		MachineComponent temp = null;
		for (MachineComponent mc : Machine.getInstance().componentList) {
			if (mc.getName().equals(name)) {
				temp = mc;
				break;
			}
		}

		return temp;
	}

	/**
	 * returns the first simulator with a specified name.
	 * 
	 * @param name
	 * @return the {@link ASimulationControl} with the name.
	 */
	public static ASimulationControl getInputObject(String name) {

		// Case: Empty machine
		if (null == machineModel)
			return null;
		else if (null == Machine.getInstance().simulators)
			return null;

		// Default case:

		for (int i = 0; i < getInstance().simulators.size(); i++) {
			if (getInstance().simulators.get(i).getName().equals(name))
				return getInstance().simulators.get(i);
		}

		return null;
	}

	/**
	 * 
	 * @return list of all input-Output connections
	 */
	public ArrayList<MachineComponent> getMachineComponentList() {
		return componentList;
	}

	/**
	 * manick
	 * 
	 * @return list of all floodable components
	 */
	public ArrayList<MachineComponent> getFloodableMachineComponentList() {
		ArrayList<MachineComponent> fmc = new ArrayList<MachineComponent>();
		for (MachineComponent mc : componentList) {
			if (mc.getComponent() instanceof ch.ethz.inspire.emod.utils.Floodable) {
				fmc.add(mc);
			}
		}
		return fmc;
	}

	/**
	 * 
	 * @return List with input parameter simulation objects
	 */
	public List<ASimulationControl> getInputObjectList() {
		return simulators;
	}

	/**
	 * Returns the list with input parameter simulation objects with the stated
	 * unit
	 * 
	 * @param unit
	 *            SiUnit
	 * @return List with input parameter simulation
	 */
	public static List<ASimulationControl> getInputObjectList(SiUnit unit) {
		List<ASimulationControl> simulatorsFiltered = new ArrayList<ASimulationControl>();

		for (ASimulationControl sc : getInstance().simulators)
			if (sc.getUnit().equals(unit))
				simulatorsFiltered.add(sc);

		return simulatorsFiltered;
	}

	/**
	 * Returns the list with process simulation control objects with the stated
	 * unit
	 * 
	 * @param unit
	 *            SiUnit
	 * @return List with input parameter simulation
	 */
	public static List<ASimulationControl> getProcessSimulationControlList(
			SiUnit unit) {
		List<ASimulationControl> simulatorsFiltered = new ArrayList<ASimulationControl>();

		for (ASimulationControl sc : getInstance().simulators)
			if (sc.getUnit().equals(unit)
					& sc instanceof ProcessSimulationControl)
				simulatorsFiltered.add(sc);

		return simulatorsFiltered;
	}

	/**
	 * Returns a list with all input objects requiring a time dependent process
	 * signal
	 * 
	 * @return out
	 */
	public List<ASimulationControl> getVariableInputObjectList() {
		List<ASimulationControl> out = new ArrayList<ASimulationControl>();

		if (Machine.getInstance().getInputObjectList() == null)
			return out;

		for (ASimulationControl sc : Machine.getInstance().getInputObjectList()) {
			if (sc.getClass().getSimpleName()
					.equals("ProcessSimulationControl"))
				out.add(sc);
		}

		return out;
	}

	/**
	 * 
	 * @return list of all input-Output connections
	 */
	public List<IOConnection> getIOLinkList() {
		return connectionList;
	}

	/**
	 * @return list of all fluid-connections
	 */
	public List<FluidConnection> getFluidConnectionList() {
		List<FluidConnection> fcList = new ArrayList<FluidConnection>();

		for (IOConnection io : connectionList) {
			if (io instanceof FluidConnection) {
				fcList.add((FluidConnection) io);
			}
		}

		return fcList;
	}

	/**
	 * singleton implementation of the machine model
	 * 
	 * @return instance of the machine model
	 */
	public static Machine getInstance() {
		if (machineModel == null) {
			System.out.print("No machine existing: Creating empty machine");
			machineModel = new Machine();
			
			machineModel.componentList  = new ArrayList<MachineComponent>();
			machineModel.connectionList = new ArrayList<IOConnection>();
			machineModel.simulators     = new ArrayList<ASimulationControl>();
		}
		return machineModel;
	}

	/**
	 * Get unique component name. If the name stated in the argument already
	 * exists, a index number will be added and incremented
	 * 
	 * @param prefix
	 *            Varbiable prefix
	 * @return Unique component name
	 */
	public static String getUniqueComponentName(String prefix) {

		String name = prefix;
		int idx = 0;

		// Loop until name is unique
		while (null != getMachineComponent(name))
			name = prefix + "_" + (++idx);

		return name;

	}

	/**
	 * Returns a unique component name based on the prefix and available
	 * component names Allready used prefixes will be supplemented by an
	 * incremented number
	 * 
	 * @param prefix
	 * @return name
	 */
	public static String getUniqueInputObjectName(String prefix) {
		String name = prefix;
		int idx = 0;

		// Loop unitil name is unique
		while (null != getInputObject(name))
			name = prefix + "_" + (++idx);

		return name;
	}

	/**
	 * Add new component. The function is "name save": If a component with the
	 * same name exists, a unique new name will be generated
	 * 
	 * @param component
	 *            Component to be added
	 */
	public static void addMachineComponent(MachineComponent component) {

		// If it is an empty machine, create a new machine
		if (machineModel == null)
			machineModel = new Machine();

		// If it is the first component, create list
		if (getInstance().componentList == null)
			getInstance().componentList = new ArrayList<MachineComponent>();

		// Make name unique
		component.setName(getUniqueComponentName(component.getName()));
		// Add to component list
		getInstance().componentList.add(component);
	}
	

	/**
	 * Adds a new (non existent) machine component by its model type and
	 * parameter set.
	 * 
	 * @param mdlType
	 *            model type name (same as class name)
	 * @param paramType
	 *            parameter set name
	 * @return created machine component object
	 */
	public static MachineComponent addNewMachineComponent(String mdlType, String paramType) {

		Object component = null;

		// Try to create and parametrize the object
		try {
			// Get class and constructor objects
			Class<?> cl = Class
					.forName("ch.ethz.inspire.emod.model." + mdlType);
			Constructor<?> co = cl.getConstructor(String.class);
			// initialize new component
			component = co.newInstance(paramType);
		} catch (Exception e) {
			Exception ex = new Exception("Unable to create component "
					+ mdlType + "(" + paramType + ")" + " : " + e.getMessage());
			ex.printStackTrace();
			return null;
		}

		// Create new machine component object
		MachineComponent mc = new MachineComponent(mdlType);
		mc.setComponent((APhysicalComponent) component);

		// Add to machine
		addMachineComponent(mc);

		return mc;

	}
	
	/**
	 * Creates  a new component by its model type and parameter set.
	 * 
	 * @param mdlType
	 * @param paramType
	 * @return
	 */
	public static APhysicalComponent createNewMachineComponent(String mdlType, String paramType) {

		Object component = null;

		// Try to create and parametrize the object
		try {
			// Get class and constructor objects
			Class<?> cl = Class.forName("ch.ethz.inspire.emod.model." + mdlType);
			Constructor<?> co = cl.getConstructor(String.class);
			// initialize new component
			component = co.newInstance(paramType);
			return (APhysicalComponent) component;
		} catch (Exception e) {
			Exception ex = new Exception("Unable to create component " + mdlType + "(" + paramType + ")" + " : " + e.getMessage());
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Add new simulator. The function is "name save": If a component with the
	 * same name exists, a unique new name will be generated
	 * 
	 * @param simulator
	 *            Simulator to be added
	 */
	public static void addInputObject(ASimulationControl simulator) {

		// If it is an empty machine, create a new machine
		if (machineModel == null)
			machineModel = new Machine();

		// If it is the first component, create list
		if (getInstance().simulators == null)
			getInstance().simulators = new ArrayList<ASimulationControl>();

		// Make name unique
		simulator.setName(getUniqueInputObjectName(simulator.getName()));
		// Add to component list
		getInstance().simulators.add(simulator);
	}

	/**
	 * Creates a new simulator by its name
	 * 
	 * @param type
	 *            Simulator type (class name)
	 * @param name
	 *            Simulator name
	 * @param unit
	 *            Simulator unit
	 * @return {@link ASimulationControl} with the simulator
	 * @throws Exception
	 */
	public static ASimulationControl createNewInputObject(String type,
			String name, SiUnit unit) throws Exception {
		Object simulator = null;

		// Try to create and parametrize the object
		try {
			// Get class and constructor objects
			Class<?> cl = Class.forName("ch.ethz.inspire.emod.simulation."
					+ name);
			Constructor<?> co = cl.getConstructor(String.class, SiUnit.class);

			System.out.println("*** Machine.addNewInputObject: "
					+ cl.toString() + " " + co.toString());
			// initialize new component
			simulator = co.newInstance(name, unit);
		} catch (Exception e) {
			throw new Exception("Unable to create component " + name + "("
					+ unit.toString() + ")" + " : " + e.getMessage());
		}

		return (ASimulationControl) simulator;
	}

	/**
	 * Adds a new simulator by its name
	 * 
	 * @param name
	 *            Simulator name
	 * @param unit
	 *            Simulator unit
	 * @return {@link ASimulationControl} with the simulator
	 */
	public static ASimulationControl addNewInputObject(String name, SiUnit unit) {
		
		final String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix")+ "/";
		Path source = Paths.get(path + "/SimulationControl/" + name + "_Example.xml");
		Path target = Paths.get(EModSession.getMachineConfigDirPath() + "/", name + "_" + name + ".xml");
		// overwrite existing file, if existsR
		CopyOption[] options = new CopyOption[] {
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES };
		try {
			Files.copy(source, target, options);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ASimulationControl simulator = null;

		try {
			simulator = createNewInputObject(name, name, unit);
			addInputObject(simulator);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return simulator;
	}

	/**
	 * Removes the component with the given name
	 * 
	 * @param mc
	 *            Machine Component object
	 * @return success
	 */
	public static boolean removeMachineComponent(MachineComponent mc) {

		if (null != getInstance().getIOLinkList())
			getInstance().removeConnections(mc);

		// Try to remove the component
		if (!getInstance().getMachineComponentList().remove(mc)) {
			Exception ex = new Exception("Unable to remove component "
					+ mc.getName() + " : Can't remove component from list");
			ex.printStackTrace();
			return false;
		}

		return true;

	}

	/**
	 * Removes the component with the given name
	 * 
	 * @param name
	 *            Name of the component
	 * @return success
	 */
	public static boolean removeMachineComponent(String name) {
		// Check if component exists
		if (null == getMachineComponent(name)) {
			Exception ex = new Exception("Unable to remove component " + name
					+ " : No component with this name");
			ex.printStackTrace();
			return false;
		} else
			return removeMachineComponent(getMachineComponent(name));
	}

	/**
	 * Removes the simulator with the given name
	 * 
	 * @param sc
	 *            {@link ASimulationControl} simulator object
	 * @return sucess
	 */
	public static boolean removeInputObject(ASimulationControl sc) {

		if (null != getInstance().getIOLinkList())
			getInstance().removeConnections(sc);

		// Try to remove the component
		if (!getInstance().getInputObjectList().remove(sc)) {
			Exception ex = new Exception("Unable to remove component "
					+ sc.getName() + " : Can't remove component from list");
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Removes the simulator with the given name
	 * 
	 * @param name
	 *            Name of the simulator
	 * @return success
	 */
	public static boolean removeInputObject(String name) {
		// Check if component exists
		if (null == getInputObject(name)) {
			Exception ex = new Exception("Unable to remove simulator " + name
					+ " : No simulator with this name");
			ex.printStackTrace();
			return false;
		} else
			return removeInputObject(getInputObject(name));
	}

	/**
	 * Removes all the IOConnections of the stated machine component from the
	 * list
	 * 
	 * @param mc
	 *            Machine component object
	 */
	public void removeConnections(MachineComponent mc) {

		// Inputs
		try {
			for (int i = 0; i < mc.getComponent().getInputs().size(); i++) {
				// Go through all links, at test if current input is part of it
				// If so, delete it
				for (int j = 1; j < getInstance().getIOLinkList().size(); j++) {
					if (getInstance().getIOLinkList().get(j).getSource().equals(mc.getComponent().getInputs().get(i))
							|| getInstance().getIOLinkList().get(j).getTarget().equals(mc.getComponent().getInputs().get(i)))
						getInstance().getIOLinkList().remove(j);
				}
			}
		} catch (Exception x) {
			Exception ex = new Exception("Unable to remove link on input list");
			ex.printStackTrace();
			return;
		}

		// Outputs
		try {
			for (int i = 0; i < mc.getComponent().getOutputs().size(); i++) {
				// Go through all links, at test if current input is part of it
				// If so, delete it
				for (int j = 0; j < getInstance().getIOLinkList().size(); j++) {
					if (getInstance().getIOLinkList().get(j).getSource()
							.equals(mc.getComponent().getOutputs().get(i))
							|| getInstance()
									.getIOLinkList()
									.get(j)
									.getTarget()
									.equals(mc.getComponent().getOutputs()
											.get(i)))
						getInstance().getIOLinkList().remove(j);
				}
			}
		} catch (Exception x) {
			Exception ex = new Exception("Unable to remove link on output list");
			ex.printStackTrace();
			return;
		}
	}

	/**
	 * Removes all the IOConnections of the stated simulator from the list
	 * 
	 * @param sc
	 *            Simulator object
	 */
	public void removeConnections(ASimulationControl sc) {

		try {
			for (int j = 0; j < getInstance().getIOLinkList().size(); j++) {
				if (getInstance().getIOLinkList().get(j).getSource()
						.equals(sc.getOutput()))
					getInstance().getIOLinkList().remove(j);
			}
		} catch (Exception x) {
			Exception ex = new Exception("Unable to remove link on input list");
			ex.printStackTrace();
			return;
		}
	}

	/**
	 * Renames the component with the given name to a new name
	 * 
	 * @param name
	 *            Old name
	 * @param newname
	 *            New name
	 */
	public static void renameMachineComponent(String name, String newname) {

		// Make new name name save
		newname = newname.replaceAll("[\\000]", "");

		MachineComponent mc = getMachineComponent(name);

		if (null == mc) {
			Exception ex = new Exception("Unable to rename component " + name
					+ " : No component with this name");
			ex.printStackTrace();
			return;
		}
		// No rename required if new and old name are the same
		else if (name.equals(newname))
			return;
		else
			mc.setName(getUniqueComponentName(newname));

	}

	/**
	 * Renames the input object with the given name to a new name
	 * 
	 * @param name
	 *            Old name
	 * @param newname
	 *            New name
	 */
	public static void renameInputObject(String name, String newname) {

		// Make new name name save
		newname = newname.replaceAll("[\\000]", "");

		ASimulationControl sc = getInputObject(name);

		if (null == sc) {
			Exception ex = new Exception("Unable to rename input " + name
					+ " : No input with this name");
			ex.printStackTrace();
			return;
		}
		// No renamerenameInputObject required if new and old name are the same
		else if (name.equals(newname))
			return;
		else
			sc.setName(getUniqueInputObjectName(newname));

		// change the name of file that belongs to the given simulator
		String prefix = EModSession.getRootPath()
				+ File.separator
				+ Defines.MACHINECONFIGDIR
				+ File.separator
				+ EModSession.getMachineConfig()
				+ File.separator;

		Path source = Paths.get(prefix, sc.getType() + "_" + name + ".xml");

		// overwrite existing file, if exists
		CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, };
		// try to rename the existing xml-file of the simulator to the new name
		try {
			Files.copy(source, source.resolveSibling(sc.getType() + "_"
					+ newname + ".xml"), options);
		} catch (IOException ee) {
			ee.printStackTrace();
		}
	}

	/**
	 * Adds a new IOConnection between the source and the target
	 * 
	 * @param source
	 * @param target
	 * @return ioc
	 */
	public static IOConnection addIOLink(IOContainer source, IOContainer target) {

		IOConnection ioc = null;

		// Add Element to List
		if (machineModel == null)
			machineModel = new Machine();
		if (null == getInstance().getIOLinkList())
			getInstance().connectionList = new ArrayList<IOConnection>();

		// Create new IOConnection
		try {
			if (source instanceof FluidContainer
					& target instanceof FluidContainer)
				ioc = new FluidConnection((FluidContainer) source,
						(FluidContainer) target);
			else
				ioc = new IOConnection(source, target);

			getInstance().getIOLinkList().add(ioc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ioc;

	}

	/**
	 * Removes the first IOConnection in the list containing the target stated
	 * in the argument
	 * 
	 * @param target
	 */
	public static void removeIOLink(IOContainer target) {
		// Find candidates to be removed
		for (IOConnection ioc : getInstance().connectionList)
			if (ioc.getTarget().equals(target)) {
				getInstance().connectionList.remove(ioc);
				// Break after first candidate
				break;
			}
	}

	/**
	 * Remove the stated ioc
	 * 
	 * @param ioc
	 */
	public static void removeIOLink(IOConnection ioc) {
		getInstance().connectionList.remove(ioc);
	}

	/**
	 * adds a new FluidConnection to the machine
	 * 
	 * @param source
	 * @param target
	 */
	public static void addFluidLink(FluidContainer source, FluidContainer target) {
		FluidConnection fio;

		try {
			fio = new FluidConnection(source, target);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// Add Element to List
		if (machineModel == null)
			machineModel = new Machine();
		if (null == getInstance().getIOLinkList())
			getInstance().connectionList = new ArrayList<IOConnection>();

		getInstance().getIOLinkList().add(fio);
	}

	/**
	 * @return {@link IOContainer} List of all components and simulators outputs
	 */
	public static ArrayList<String> getOutputList() {
		return getOutputList(null, null);
	}

	/**
	 * @param unit
	 *            {@link Unit} Unit of the outputs
	 * @return {@link IOContainer} List of all components and simulators outputs
	 *         with the declared unit
	 */
	public static ArrayList<String> getOutputList(SiUnit unit) {
		return getOutputList(null, unit);
	}

	/**
	 * Return a list of all IOContainers with the unit which do not include
	 * Outputs of the stated machine component mc. Both conditions are ignored
	 * when feeding null.
	 * 
	 * @param mc_excl
	 *            {@link MachineComponent} who's elements are to be excluded
	 * @param unit
	 *            {@link Unit} of the desired outputs
	 * @return List of {@link IOContainer}
	 */
	public static ArrayList<String> getOutputList(MachineComponent mc_excl,
			SiUnit unit) {
		ArrayList<String> outputs = new ArrayList<String>();

		// Get all machine components
		ArrayList<MachineComponent> components = getInstance()
				.getMachineComponentList();
		// Get all simulator outputs
		List<ASimulationControl> simulators = getInstance()
				.getInputObjectList();

		// Fetch all outputs
		if (null != components)
			for (MachineComponent mc : components)
				if (!mc.equals(mc_excl))
					for (IOContainer ic : mc.getComponent().getOutputs())
						if (!ic.equals(null)
								& (ic.getUnit().equals(unit) | null == unit)
								& !(ic instanceof FluidContainer))
							outputs.add(mc.getName() + "." + ic.getName());

		if (null != simulators)
			for (ASimulationControl sc : simulators)
				if (sc.getOutput().getUnit().equals(unit) | null == unit)
					outputs.add(sc.getName());

		return outputs;
	}

	/**
	 * Get a list of all names of the fluid inpputs available, except the one of
	 * the stated machine component
	 * 
	 * @param mc_excl
	 * @return
	 */
	public static ArrayList<String> getFluidOutputList(MachineComponent mc_excl) {
		ArrayList<String> outputs = new ArrayList<String>();

		// Get all machine components
		ArrayList<MachineComponent> components = getInstance()
				.getMachineComponentList();

		// Fetch all outputs
		if (null != components)
			for (MachineComponent mc : components)
				if (!mc.equals(mc_excl))
					for (IOContainer ic : mc.getComponent().getOutputs())
						if (ic instanceof FluidContainer)
							outputs.add(mc.getName() + "." + ic.getName());

		return outputs;
	}

	/**
	 * Returns a List of all {@link DynamicState}
	 * 
	 * @return {@link DynamicState}
	 */
	public ArrayList<DynamicState> getDynamicStatesList() {
		ArrayList<DynamicState> output = new ArrayList<DynamicState>();

		for (MachineComponent mc : Machine.getInstance().componentList) {
			if (mc.getComponent().getDynamicStateList() != null)
				output.addAll(mc.getComponent().getDynamicStateList());
		}

		return output;
	}

	/**
	 * Returns the dynamic state "name" of the component "parent"
	 * 
	 * @param name
	 * @param parent
	 * @return {@link DynamicState}
	 */
	public DynamicState getDynamicState(String name, String parent) {
		return getMachineComponent(parent).getComponent().getDynamicState(name);
	}

	/**
	 * Returns the full output name of the container
	 * 
	 * @param container
	 *            {@link IOContainer}
	 * @return name {@link String}
	 */
	public static String getOutputFullName(IOContainer container) {
		String out = null;

		for (MachineComponent mc : getInstance().getMachineComponentList()) {
			for (IOContainer io : mc.getComponent().getOutputs()) {
				if (io.equals(container.getReference())) {
					out = mc.getName() + "." + io.getName();
					break;
				}
			}
			if (out != null)
				break;
		}

		if (out == null)
			for (ASimulationControl sc : getInstance().getInputObjectList()) {
				if (sc.getOutput().equals(container.getReference())) {
					out = sc.getName();
					break;
				}
			}

		return out;
	}

	/**
	 * ========================================================================
	 * ======== Methods for test only!
	 * ==========================================
	 * ======================================
	 */

	/**
	 * Create machine instance. For test purpose only!
	 * 
	 */
	public static void dummyBuildMachine() {
		/* Create machine */
		if (machineModel == null)
			machineModel = new Machine();
	}

	/**
	 * Set or overwrite componentList: Use for test purpose only!
	 * 
	 * @param list
	 */
	public void setMachineComponentList(ArrayList<MachineComponent> list) {
		componentList = list;
	}

}
