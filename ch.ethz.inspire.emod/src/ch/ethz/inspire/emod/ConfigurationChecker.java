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

import java.util.List;

import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.simulation.ConfigCheckResult;
import ch.ethz.inspire.emod.simulation.ConfigState;
import ch.ethz.inspire.emod.utils.IOConnection;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * Bundle of static functions to check a configuration.
 * All functions will return a {@link ConfigCheckResult}
 * 
 * @author simon
 *
 */
public class ConfigurationChecker {
	
	
	/**
	 * Performs all machine model relevant checks:
	 * - checkMachineComponentConfigurations()
	 * - checkSimulatorConfigurations()
	 * - checkIOLinking()
	 * - checkFluidCircuits()
	 * @return
	 */
	public static ConfigCheckResult checkMachineConfig(){
		ConfigCheckResult result = new ConfigCheckResult();
		
		// Perform all machine relevant checks
		result.addAll(checkMachineComponentConfigurations());
		result.addAll(checkSimulatorConfigurations());
		result.addAll(checkSimulatorConfigurations());
		result.addAll(checkIOLinking());
		result.addAll(checkFluidCircuits());
		
		return result;
	}
	
	
	/**
	 * Performs all simulation config relevant checks:
	 * - checkInitialConditions()
	 * - checkStateSequence()
	 * @return
	 */
	public static ConfigCheckResult checkSimulationConfig(){
		ConfigCheckResult result = new ConfigCheckResult();
		
		// Perform all relevant checks
		result.addAll(checkInitialConditions());
		result.addAll(checkStateMap());
		
		return result;
	}
	
	/**
	 * Check machine components
	 * @return
	 */
	public static ConfigCheckResult checkMachineComponentConfigurations(){
		ConfigCheckResult result = new ConfigCheckResult();
		
		if(Machine.getInstance().getMachineComponentList().size() == 0)
			result.add(ConfigState.ERROR, "ComponetModels", "Model does not include any components");
		
		return result;
	}
	
	/**
	 * Check simulator components
	 * @return
	 */
	public static ConfigCheckResult checkSimulatorConfigurations(){
		ConfigCheckResult result = new ConfigCheckResult();
		//TODO
		return result;
	}
	
	/**
	 * Check IOLinking
	 * @return
	 */
	public static ConfigCheckResult checkIOLinking(){
		ConfigCheckResult result = new ConfigCheckResult();
		
		/*
		 * 1. Check: Every input of all machine componenets must be set exactly
		 * once in the connectionlist.
		 */
		int mc_in_iolist_cnt = 0;
		
		if(null==Machine.getInstance().getIOLinkList()){
			result.add(ConfigState.WARNING, "IOLinking", "No connections defined!");
		}
		else
			for (MachineComponent mc : Machine.getInstance().getMachineComponentList()) {
				for (IOContainer mcinput : mc.getComponent().getInputs()) {
					mc_in_iolist_cnt = 0;
					for (IOConnection iolink : Machine.getInstance().getIOLinkList()) {
						if (mcinput == iolink.getTarget()) {
							mc_in_iolist_cnt++;
						}
					}
					if (mc_in_iolist_cnt == 0) {
						result.add(ConfigState.WARNING, "IOLinking", "Input "
								+ mc.getName() + "." + mcinput.getName()
								+ " is not connected!");
					} else if (mc_in_iolist_cnt >= 2) {
						result.add(ConfigState.ERROR, "IOLinking", "Input "
								+ mc.getName() + "." + mcinput.getName()
								+ " is linked multiple times!");
					}
				}
			}
		
		if(result.getMessages().size() == 0)
			result.add(ConfigState.OK, "IOLinking", "Test passed");
		

		return result;
	}
	
	/**
	 * Check fluid circutis
	 * @return
	 */
	public static ConfigCheckResult checkFluidCircuits(){
		ConfigCheckResult result = new ConfigCheckResult();
		//TODO
		return result;
	}
	
	/**
	 * Check if all initial conditions are set
	 * @return
	 */
	public static ConfigCheckResult checkInitialConditions(){
		ConfigCheckResult result = new ConfigCheckResult();
		//TODO
		return result;
	}
	
	/**
	 * Check the state sequence
	 * @return
	 */
	public static ConfigCheckResult checkStateMap(){
		ConfigCheckResult result = new ConfigCheckResult();
		
		// At least one state must be set
		if(States.getStateCount()==0)
			result.add(ConfigState.ERROR, "StateMap", "State map does not include any state definition");
		
		// Durations of length 0 should be avoided
		for(int i=0; i<States.getStateCount(); i++)
			if(States.getDuration(i)==0)
				result.add(ConfigState.WARNING, "StateMap", "State map does contain states with duration 0");
		
		// Default message
		if(result.getMessages().size() == 0)
			result.add(ConfigState.OK, "StateMap", "Test passed");
		
		return result;
	}
	
	/**
	 * Check process file
	 * @return 
	 */
	public static ConfigCheckResult checkProcess(){
		ConfigCheckResult result = new ConfigCheckResult();
		
		// Load the process first
		Process.loadProcess(EModSession.getProcessName());
		
		List<ASimulationControl> simulators = Machine.getInstance().getVariableInputObjectList();
		
		// For each simulator, the process file must contain a value vector
		for(ASimulationControl sc: simulators)
			if(!(Process.getVariableNames().contains(sc.getName())))
				result.add(ConfigState.ERROR, "Process", "Process configuration does not includes a value vector for '"+sc.getName()+"'");
		
		// At least one time step is required
		if(Process.getTime().length==0)
			result.add(ConfigState.ERROR, "Process", "Process configuration does not include any time step");
		else if(Process.getTime().length==1)
			result.add(ConfigState.WARNING, "Process", "Process configuration includes only one time step");
		
		// Default message
		if(result.getMessages().size() == 0)
			result.add(ConfigState.OK, "Process", "Test passed");
		
		
		
		return result;
	}
	

}
