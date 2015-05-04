/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2015 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.utils;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.MachineComponent;
/**
 * Class to perform several checks and routines on fluid circuits
 * 
 * @author manick
 */
public class FluidCircuit {
	/**
	 * used in FluidConnection to flood the components following a tank
	 * @param source 
	 * @param target 
	 */
	public static void floodCircuit(APhysicalComponent source, APhysicalComponent target){
		// Set Fluid properties
		if(source instanceof Floodable & target instanceof Floodable){
			((Floodable)source).getFluidProperties().setPost(((Floodable)target).getFluidProperties());
		}		
	}
	
	
	/**
	 * get all floodable machine components, and check if they are linked
	 */
	public void checkCircuit(){
		//get component list & Connection list
		ArrayList<MachineComponent> mcList = Machine.getInstance().getFloodableMachineComponentList();
		List<IOConnection> ioList = Machine.getInstance().getIOLinkList();

		
		//get starting point, i.e. the tank in the component list
		int indexTank = 0;
		for(MachineComponent mc:mcList){
			if(mc.getComponent() instanceof ch.ethz.inspire.emod.model.Tank){
				indexTank = mcList.indexOf(mc);
			}
		}
		
		//set the source to the tank, init the target
		MachineComponent source = mcList.get(indexTank);

		boolean connected = false;
		int count = 0;
		//maximum count of connections can be the number of components
		//as long as connected is set to false, do:
		while(count <= mcList.size() && !connected){
			//for all connections do
			for(IOConnection io:ioList){
				//it the current source is part of the connection
				if(io.getSource().equals(source.getComponent().getOutput("FluidOut"))){
					//search the list of machine components
					for(MachineComponent mc:mcList){
						//and get the component that is the target of the connection
						if(io.getTarget().equals(mc.getComponent().getInput("FluidIn"))){
							//go one step further, i.e. the old target is the new source
							source = mc;
							//if the target is the tank, the circuit is connected
							if(mc.getComponent() instanceof ch.ethz.inspire.emod.model.Tank){
								connected = true;
								System.out.println("Circuit checked!");
								break;
							}
						}
					}
				}
				count++;
			}
		}
	}
}
