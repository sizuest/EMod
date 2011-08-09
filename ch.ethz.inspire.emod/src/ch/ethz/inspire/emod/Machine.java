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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.LinearMotor;
import ch.ethz.inspire.emod.model.ConstantComponent;
import ch.ethz.inspire.emod.model.MachineComponent;

/**
 * Machine model base class.
 * 
 * @author dhampl
 *
 */
@XmlRootElement(namespace = "ch.ethz.inspire.emod.model")
@XmlSeeAlso({APhysicalComponent.class, LinearMotor.class, ConstantComponent.class})
public class Machine {
	
	private static Logger logger = Logger.getLogger(Machine.class.getName());
	
	@XmlElementWrapper(name = "machine")
	@XmlElement(name = "machineComponent")
	private ArrayList<MachineComponent> componentList;
	private static Machine machineModel=null;
	
	/**
	 * Private constructor for singleton implementation.
	 */
	private Machine()
	{
	}
	
	public void setComponentList(ArrayList<MachineComponent> componentList) {
		this.componentList = componentList;
	}
	
	public ArrayList<MachineComponent> getMachineComponentList() {
		return componentList;
	}
	
	/**
	 * returns the first machine component with a specified name.
	 * 
	 * @param name
	 * @return the {@link MachineComponent} with the name. 
	 */
	public MachineComponent getComponent(String name){
		MachineComponent temp=null;
		for(MachineComponent mc : componentList) {
			if(mc.getName().equals(name)) {
				temp=mc;
				break;
			}
		}
		
		return temp;
	}
	
	/**
	 * singleton implementation of the machine model
	 * 
	 * @return instance of the machine model
	 */
	public static Machine getInstance() {
		if(machineModel==null)
			machineModel=new Machine();
		return machineModel;
	}
	
	/**
	 * reads a machine config from a specified xml file
	 * 
	 * @param file
	 */
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
	
	/**
	 * saves the machine config to a xml file.
	 * 
	 * @param file
	 */
	public static void saveMachineToFile(String file) {
		if(machineModel==null){
			logger.warning("no model active");
			return;
		}
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
	
}
