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

package ch.ethz.inspire.emod.model.fluid;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Implements the generic flow rate dependent properties of a duct:
 *   - HTC
 *   - Zeta
 * @author sizuest
 *
 */
@XmlRootElement(namespace = "ch.ethz.inspire.emod")
@XmlSeeAlso({ADuctElement.class, AHydraulicProfile.class, DuctDrilling.class, DuctPipe.class, DuctElbowFitting.class, DuctFlowAround.class,
	DuctFitting.class, DuctHelix.class, DuctElbowFitting.class, HPRectangular.class, HPCircular.class, Isolation.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class Duct {
	private Material material;
	private String name;
	@XmlElementWrapper
	@XmlElement
	private ArrayList<ADuctElement> elements = new ArrayList<ADuctElement>();
	
	
	/**
	 * Constructor for unmarshaller
	 * @param parrent
	 */
	public Duct(){}
	
	/**
	 * @param name 
	 */
	public Duct(String name){
		this.name    = name;
	}
	
	/**
	 * Called by unmarshaller after loading from XML
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		cleanUpFittings();
	}
	
	/**
	 * Checks and corrects all fittings (diameters of the adjected elements)
	 */
	private void reconnectFittings(){
		for(int i=1; i<elements.size()-1; i++)
			if(getElement(i) instanceof DuctFitting)
				((DuctFitting) getElement(i)).setProfiles(getElement(i-1).profile, getElement(i+1).profile);
	}
	
	/**
	 * Inserts fittings, adapts fittings or removes fittings where ever required
	 */
	public void cleanUpFittings() {
		if(elements.size()==0)
			return;
		
		reconnectFittings();
		if( getElement(0) instanceof DuctFitting ){
			removeElement(getElement(0).getName());
		}
		for(int i=0; i<elements.size()-1; i++){
			if( getElement(i) instanceof DuctFitting )  {
				if( ((DuctFitting) getElement(i)).hasEqualProfiles()) {
					elements.remove(i);
					cleanUpFittings();
					break;
				}
				
				if( getElement(i+1) instanceof DuctFitting ) {
					elements.remove(i+1);
					cleanUpFittings();
					break;
				}
				
				getElement(i).setName("Fitting_"+getElement(i-1).getName()+"-"+getElement(i+1).getName());
			}
			else if( !(getElement(i+1) instanceof DuctFitting) & (getElement(i).getDiameter()!=getElement(i+1).getDiameter())){
				addElement(i+1, new DuctFitting("Fitting_"+getElement(i).getName()+"-"+getElement(i+1).getName(), getElement(i).getProfile(), getElement(i+1).getProfile()));
				cleanUpFittings();
				break;
			}
		}
		
		if( getElement(elements.size()-1) instanceof DuctFitting ){
			removeElement(getElement(elements.size()-1).getName());
		}
	}
	

	
	/**
	 * Build the duct from the provided component type, component name and object
	 * name
	 * 
	 * Example: Cooling channel of the Spindle 'Example':
	 * Type: Spindle	
	 * Name: Example
	 * Obj:  CoolingDuct
	 * 
	 * --> Spindle_Example_CoolingDuct.xml
	 * 
	 * @param type
	 * @param name
	 * @param obj 
	 * @return {@link Duct.java}
	 */
	public static Duct buildFromFile(String type, String name, String obj){
		return buildFromFile(type+"_"+name+"_"+obj);
	}
	
	/**
	 * Build the duct from the provided name of an xml-file.
	 * If no xml-file is existing, a new duct with the given name will
	 * created.
	 * 
	 * @param name
	 * @return {@link Duct.java}
	 */
	public static Duct buildFromFile(String name){
		Duct duct = initFromFile(getPath(name));
		
		if(null==duct){
			System.out.println("Duct: initFromFile: "+name+ "does not exist. Creating empty duct!");
			return new Duct(name);
		}
		else
			return duct;
	}
	
	/**
	 * Build the duct from the provided path to an xml-file.
	 * 
	 * @param path
	 * @return {@link Duct.java}
	 */
	public static Duct initFromFile(String path){
		Duct duct = null;
		
		File file = new File(path);
		if(!file.exists() || file.isDirectory())
			return null;
			
		
		try {
			JAXBContext context = JAXBContext.newInstance(Duct.class);
			Unmarshaller um = context.createUnmarshaller();
			duct = (Duct) um.unmarshal(new FileReader(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return duct;
	}
	
	/**
	 * Add a new duct element by the NAME of the elements class. The class must be
	 * named DuctNAME and be located in {@link ch.ethz.inspire.emod.model.fluid}
	 * 
	 * @param type
	 * @return {@ADuctElement};
	 */
	public static ADuctElement newDuctElement(String type){
		Object element = null;
		
		// Try to create and parametrize the object
		try {
			// Get class and constructor objects
			Class<?>        cl = Class.forName("ch.ethz.inspire.emod.model.fluid.Duct"+type);
			Constructor<?>  co = cl.getConstructor(String.class);
			// initialize new component
			element = co.newInstance(type);
		} catch (Exception e) {
			Exception ex = new Exception("Unable to create component "+type+" : " + e.getMessage());
			ex.printStackTrace();
			return null;
		} 
		
		return (ADuctElement) element;
	}
	
	/**
	 * Generates and returns the path of the duct with the provided name in
	 * the duct DB
	 * 
	 * @param name
	 * @return path (String)
	 */
	private static String getPath(String name){
		String prefix = PropertiesHandler.getProperty("app.DuctDBPathPrefix");
		String path = prefix + "/Duct_" + name +".xml";
		
		return path;
	}
	
	/**
	 * Returns the path of the config file of the current instance
	 */
	private String getPath(){
		return getPath(name);
	}
	
	/**
	 * Saves the current instance as xml file
	 */
	public void save(){		
		saveToFile(getPath());
	}
	
	/**
	 * Saves the current instance as cml file at the path provided
	 * @param path
	 */
	public void saveToFile(String path){
		removeUnusedIsolations();
		try {
			JAXBContext context = JAXBContext.newInstance(Duct.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			Writer w = new FileWriter(path);
			m.marshal(this, w);
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes unused isolation elements. Unused means either
	 * the material is null, or its type is set to 'none' 
	 */
	private void removeUnusedIsolations() {
		for(ADuctElement e: elements)
			if(null!=e.getIsolation())
				if(null==e.getIsolation().getMaterial() | e.getIsolation().getMaterial().getType().equals("none"))
					e.setIsolation(null);
	}

	/**
	 * Add a new element to the duct
	 * @param e {@link ADuctElement.java}
	 */
	public void addElement(ADuctElement e){
		e.setName(getUniqueElementName(e.getName()));
		addElement(elements.size(), e);
	}
	
	/**
	 * Add a new element to the duct at the given index i
	 * @param i 
	 * @param e {@link ADuctElement.java}
	 */
	public void addElement(int i, ADuctElement e){
		/*
		 * If the last element added has a different diameter, add a fitting
		 */
		if(elements.size()>0 & i>0)
			if(elements.get(i-1).profile.getDiameter() != e.profile.getDiameter()){
				DuctFitting df = new DuctFitting("Fitting_"+elements.get(i-1).getName()+"-"+e.getName(), 
						elements.get(i-1).profile, e.profile);
				elements.add(i, df);
				df.setMaterial(getMaterial());
				i++;
			}
		
		e.setName(getUniqueElementName(e.getName()));
		elements.add(i, e);
		e.setMaterial(getMaterial());
		
		if(elements.size()>i+1)
			if(elements.get(i+1).profile.getDiameter() != e.profile.getDiameter()){
				DuctFitting df = new DuctFitting("Fitting_"+e.getName()+"-"+elements.get(i+1).getName(), 
						e.profile, elements.get(i+1).profile);
				elements.add(i+1, df);
				df.setMaterial(getMaterial());
				cleanUpFittings();
			}
	}
	
	/**
	 * Moves the element with the provided name one rank up 
	 * (fittings will be skiped)
	 * 
	 * @param name
	 */
	public void moveElementUp(String name){
		ADuctElement e = getElement(name);
		
		if(null==e)
			return;
		
		removeAllFittings();
		
		int index = getElementIndex(name);
		
		if(0!=index)
			Collections.swap(this.elements, index, index-1);
		
		cleanUpFittings();
	}
	
	

	/** 
	 * Moves the element e one rank up 
	 * (fittings will be skiped)
	 * 
	 * @param e
	 */
	public void moveElementUp(ADuctElement e){
		moveElementUp(e.getName());
	}
	
	/**
	 * Moves the element with the provided name one rank down 
	 * (fittings will be skiped)
	 * 
	 * @param name
	 */
	public void moveElementDown(String name){
		ADuctElement e = getElement(name);
		
		if(null==e)
			return;
		
		removeAllFittings();
		
		int index = getElementIndex(name);
		
		if(this.elements.size()!=index+1)
			Collections.swap(this.elements, index, index+1);
		
		cleanUpFittings();
		
	}
	
	/**
	 * Moves the element e one rank down
	 * (fittings will be skiped)
	 * 
	 * @param e
	 */
	public void moveElementDown(ADuctElement e){
		moveElementDown(e.getName());
	}
	
	/**
	 * Removes all fittings in the duct 
	 */
	private void removeAllFittings() {
		for(ADuctElement e: this.elements)
			if(e instanceof DuctFitting){
				elements.remove(e);
				removeAllFittings();
				return;
			}
	}
	
	/**
	 * Replaces the element with the given index by the new element e
	 * @param i
	 * @param e
	 */
	public void replaceElement(int i, ADuctElement e){
		elements.set(i, e);
		reconnectFittings();
	}
	
	/**
	 * Replace the element with the given name by the new element e
	 * @param name
	 * @param e
	 */
	public void replaceElement(String name, ADuctElement e){
		int i = getElementIndex(name);
		
		if(i>=0)
			replaceElement(i, e);		
	}
	
	
	/**
	 * Finds the index of an element with the provided name
	 * @param name
	 * @return index
	 */
	public int getElementIndex(String name){
		for(int i=0; i<elements.size(); i++)
			if(elements.get(i).getName().equals(name))
				return i;
		
		return -1;
	}
	
	/**
	 * Get duct element with index i
	 * @param i
	 * @return {@link ADuctElement.java}
	 */
	public ADuctElement getElement(int i){
		return elements.get(i);
	}
	
	/**
	 * Get duct element with given name
	 * @param name
	 * @return {@link ADuctElement.java}
	 */
	public ADuctElement getElement(String name){
		for(ADuctElement e: elements)
			if(e.getName().equals(name))
				return e;
		
		return null;
	}
	
	/**
	 * Changes the name of an element
	 * 
	 * @param name		old name
	 * @param newname	new name
	 */
	public void setElementName(String name, String newname){
		ADuctElement e = getElement(name);
		
		if(null==e) {
			Exception ex = new Exception("Unable to rename element "+name+" : No element with this name");
			ex.printStackTrace();
			return;
		}
		// No rename required if new and old name are the same
		else if(name.equals(newname))
			return;
		else
			e.setName(getUniqueElementName(newname));	
	}
	
	/**
	 * returns a unique element name based on the provided prefix
	 * 
	 * @param prefix
	 * @return prefix(_[0-9]+)?
	 */
	private String getUniqueElementName(String prefix){
		String name = prefix;
		int idx     = 0;
		
		// Loop until name is unique
		while(null!=getElement(name))
			name = prefix+"_"+(++idx);
		
		return name;
	}
	
	/**
	 * Removes the element with the given name
	 * @param name
	 */
	public void removeElement(String name){
		int i = getElementIndex(name);
		
		if(i>=0){
			elements.remove(i);
			cleanUpFittings();
		}
	}
	
	/**
	 * Sets the material of the ducts and all its elements
	 * @param material
	 */
	public void setMaterial(Material material){
		this.material = material;
		
		for(ADuctElement e: elements)
			e.setMaterial(material);
	}
	
	/**
	 * Returns the current material
	 * @return {@link Material.java}
	 */
	public Material getMaterial(){
		return this.material;
	}
	
	/**
	 * Returns the current thermal resistance
	 * @param flowRate [m^3/s]
	 * @param pressureIn [Pa]
	 * @param temperatureFluid [K]
	 * @param temperatureWall [K]
	 * @return [W/K]
	 */
	public double getThermalResistance(double flowRate, double pressureIn, double temperatureFluid, double temperatureWall){
		double htc;
		double Rth     = 0;
		double lastp   = pressureIn;
		
		for(ADuctElement e: elements){
			htc = e.getHTC(flowRate, lastp, temperatureFluid, temperatureWall)*e.getHydraulicSurface();
			if(null!=e.isolation)
				htc =  1/(1/htc + 1/e.isolation.getThermalResistance());
			Rth     += htc;
			lastp   = e.getPressureOut(flowRate, lastp, temperatureFluid);
		}
		
		return Rth;
	}
	
	public double getHTC(double flowRate, double pressure,	double temperatureFluid, double temperatureWall) {
		return getThermalResistance(flowRate, pressure, temperatureFluid, temperatureWall)/getSurface();
	}
	
	/**
	 * Returns the current pressure loss
	 * @param flowRate [m^3/s]
	 * @param pressureIn [Pa]
	 * @param temperatureFluid [K]
	 * @return [Pa s^2/m^6]
	 */
	public double getPressureDrop(double flowRate, double pressureIn, double temperatureFluid){
		/*
		 * Simples case: no flow
		 */
		if(0==flowRate)
			return 0;
		
		double pressureOut = pressureIn;
		
		for(ADuctElement e: elements)
			pressureOut = e.getPressureOut(flowRate, pressureOut, temperatureFluid);
		
		return (pressureIn-pressureOut);
	}
	
	/**
	 * Returns the current hydraulic resistance with following assumption:
	 * p(V) = k*V^2 --> k = p(V)/V²
	 * 
	 * @param flowRate [m^3/s]
	 * @param pressureIn [Pa]
	 * @param temperatureFluid [K]
	 * @param temperatureWall [K]
	 * @return [Pa s^2/m^6]
	 */
	public double getPressureLossCoefficient(double flowRate, double pressureIn, double temperatureFluid){
		return getPressureDrop(flowRate, pressureIn, temperatureFluid)/Math.pow(flowRate, 2);
	}
	
	public double getPressureLossDrivative(double flowRate, double pressureIn, double temperatureFluid){
		double fac=1.05;
		if(flowRate == 0)
			return 0;
		return (getPressureDrop(flowRate*fac, pressureIn, temperatureFluid)-getPressureDrop(flowRate, pressureIn, temperatureFluid))/(fac-1)/flowRate;
	}
	
	/**
	 * Returns the total duct length
	 * @return [m]
	 */
	public double getLength(){
		double length = 0;
		for(ADuctElement e: elements)
			length += e.getLength();
		
		return length;
	}
	
	/**
	 * Returns the total duct surface
	 * @return [m^2]
	 */
	public double getSurface(){
		double area = 0;
		for(ADuctElement e: elements)
			area += e.getSurface();
		
		return area;
	}
	
	/**
	 * Returns the total Duct volume
	 * @return [m^3]
	 */
	public double getVolume(){
		double vol = 0;
		for(ADuctElement e: elements)
			vol += e.getVolume();
		
		return vol;
	}
	
	/**
	 * @return List of duct elements
	 */
	public ArrayList<ADuctElement> getElements(){
		return this.elements;
	}

}
