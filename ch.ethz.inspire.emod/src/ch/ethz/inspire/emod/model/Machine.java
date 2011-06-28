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
package ch.ethz.inspire.emod.model;

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

/**
 * @author dhampl
 *
 */
@XmlRootElement(namespace = "ch.ethz.inspire.emod.model")
@XmlSeeAlso({Component.class, LinearMotor.class, Spindle.class})
public class Machine {
	
	private static Logger logger = Logger.getLogger(Machine.class.getName());
	
	@XmlElementWrapper(name = "machine")
	@XmlElement(name = "machineComponent")
	private ArrayList<MachineComponent> componentList;
	private static Machine machineModel=null;
	
	public void setArrayList(ArrayList<MachineComponent> componentList) {
		this.componentList = componentList;
	}
	
	public ArrayList<MachineComponent> getComponentList() {
		return componentList;
	}
	
	public static Machine getInstance() {
		if(machineModel==null)
			machineModel=new Machine();
		return machineModel;
	}
	
	public static void initMachineFromFile(String file) {
		if(machineModel==null)
			machineModel=new Machine();
		try {
			JAXBContext context = JAXBContext.newInstance(Machine.class);
			Unmarshaller um = context.createUnmarshaller();
			machineModel = (Machine) um.unmarshal(new FileReader(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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
