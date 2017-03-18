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

package ch.ethz.inspire.emod.dd;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.dd.model.ADuctElement;
import ch.ethz.inspire.emod.dd.model.AHydraulicProfile;
import ch.ethz.inspire.emod.dd.model.DuctArc;
import ch.ethz.inspire.emod.dd.model.DuctDefinedValues;
import ch.ethz.inspire.emod.dd.model.DuctDrilling;
import ch.ethz.inspire.emod.dd.model.DuctElbowFitting;
import ch.ethz.inspire.emod.dd.model.DuctFitting;
import ch.ethz.inspire.emod.dd.model.DuctFlowAround;
import ch.ethz.inspire.emod.dd.model.DuctHelix;
import ch.ethz.inspire.emod.dd.model.DuctPipe;
import ch.ethz.inspire.emod.dd.model.DuctBypass;
import ch.ethz.inspire.emod.dd.model.HPCircular;
import ch.ethz.inspire.emod.dd.model.HPRectangular;
import ch.ethz.inspire.emod.dd.model.Isolation;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.utils.PropertiesHandler;
import ch.ethz.inspire.emod.utils.Undo;

/**
 * Implements the generic flow rate dependent properties of a duct: - HTC - Zeta
 * 
 * @author sizuest
 * 
 */
@XmlRootElement(namespace = "ch.ethz.inspire.emod")
@XmlSeeAlso({ ADuctElement.class, AHydraulicProfile.class, DuctDrilling.class,
		DuctPipe.class, DuctElbowFitting.class, DuctFlowAround.class,
		DuctFitting.class, DuctHelix.class, DuctElbowFitting.class,
		DuctDefinedValues.class, DuctBypass.class, DuctArc.class,
		HPRectangular.class, HPCircular.class, Isolation.class })
@XmlAccessorType(XmlAccessType.FIELD)
public class Duct implements Cloneable {
	private Material material;
	private String name;
	@XmlTransient
	private Undo<Duct> history;
	@XmlTransient
	private Duct rootDuct;

	private static Logger logger = Logger.getLogger(DuctDesignerMain.class
			.getName());

	@XmlElementWrapper
	@XmlElement
	private ArrayList<ADuctElement> elements = new ArrayList<ADuctElement>();

	/**
	 * Constructor for unmarshaller
	 */
	public Duct() {
	}

	/**
	 * @param name
	 */
	public Duct(String name) {
		this.name = name;
		init();
	}

	/**
	 * Called by unmarshaller after loading from XML
	 * 
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		init();
	}

	private void init() {
		cleanUpFittings();

		history = new Undo<Duct>(10, (new Duct()).clone(getRootDuct()));
	}

	/**
	 * Checks and corrects all fittings (diameters of the adjected elements)
	 */
	private void reconnectFittings() {
		for (int i = 1; i < elements.size() - 1; i++)
			if (getElement(i) instanceof DuctFitting)
				((DuctFitting) getElement(i)).setProfiles(getElement(i - 1)
						.getProfileOut(), getElement(i + 1).getProfileIn());
	}

	/**
	 * Inserts fittings, adapts fittings or removes fittings where ever required
	 */
	public void cleanUpFittings() {
		if (elements.size() == 0)
			return;

		reconnectFittings();
		if (getElement(0) instanceof DuctFitting) {
			removeElement(getElement(0).getName());
		}
		for (int i = elements.size() - 2; i >= 0; i--) {
			if (getElement(i) instanceof DuctFitting) {
				if (((DuctFitting) getElement(i)).hasEqualProfiles()) {
					elements.remove(i);
					break;
				}

				if (getElement(i + 1) instanceof DuctFitting) {
					elements.remove(i + 1);
					break;
				}

				getElement(i).setName(
						"Fitting_" + getElement(i - 1).getName() + "-"
								+ getElement(i + 1).getName());
			} else if (!(getElement(i + 1) instanceof DuctFitting)
					& (getElement(i).getDiameter() != getElement(i + 1)
							.getDiameter())) {
				// addElement(i+1, new
				// DuctFitting("Fitting_"+getElement(i).getName()+"-"+getElement(i+1).getName(),
				// getElement(i).getProfileOut(),
				// getElement(i+1).getProfileIn()));
				elements.add(i + 1, new DuctFitting("Fitting_"
						+ getElement(i).getName() + "-"
						+ getElement(i + 1).getName(), getElement(i)
						.getProfileOut(), getElement(i + 1).getProfileIn()));
			}
		}

		if (getElement(elements.size() - 1) instanceof DuctFitting) {
			removeElement(getElement(elements.size() - 1).getName());
		}
	}

	/**
	 * Build the duct from the provided component type, component name and
	 * object name
	 * 
	 * Example: Cooling channel of the Spindle 'Example': Type: Spindle Name:
	 * Example Obj: CoolingDuct
	 * 
	 * --> Spindle_Example_CoolingDuct.xml
	 * 
	 * @param type
	 * @param name
	 * @param obj
	 * @return {@link Duct}
	 */
	public static Duct buildFromFile(String type, String name, String obj) {
		return buildFromDB(type + "_" + name + "_" + obj);
	}

	/**
	 * Build the duct from the provided name of an xml-file. If no xml-file is
	 * existing, a new duct with the given name will created.
	 * 
	 * @param path
	 * @return {@link Duct}
	 */
	public static Duct buildFromFile(String path) {
		Duct duct = initFromFile(path);

		if (null == duct) {
			logger.warning("Duct: initFromFile: " + path
					+ "does not exist. Creating empty duct!");
			return new Duct();
		} else
			return duct;
	}

	/**
	 * Build the duct from the DB provided name of an xml-file. If no xml-file
	 * is existing, a new duct with the given name will created.
	 * 
	 * @param name
	 * @return {@link Duct}
	 */
	public static Duct buildFromDB(String name) {
		Duct duct = initFromFile(getPath(name));

		if (null == duct) {
			logger.warning("Duct: initFromFile: " + name
					+ "does not exist. Creating empty duct!");
			return new Duct(name);
		} else{
			duct.setName(name);
			return duct;
		}
	}

	/**
	 * Build the duct from the provided path to an xml-file.
	 * 
	 * @param path
	 * @return {@link Duct}
	 */
	public static Duct initFromFile(String path) {
		Duct duct = null;
		
		

		File file = new File(path);
		if (!file.exists() || file.isDirectory())
			return null;

		try {
			JAXBContext context = JAXBContext.newInstance(Duct.class);
			Unmarshaller um = context.createUnmarshaller();
			duct = (Duct) um.unmarshal(new FileReader(path));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		String[] pathParts = path.split(Pattern.quote(File.separator));
		
		if(pathParts.length>0)
			duct.setName(pathParts[pathParts.length-1]);
		
		return duct;
	}

	/**
	 * Add a new duct element by the NAME of the elements class. The class must
	 * be named DuctNAME and be located in
	 * {@link ch.ethz.inspire.emod.model.fluid}
	 * 
	 * @param type
	 * @return {@ADuctElement};
	 */
	public static ADuctElement newDuctElement(String type) {
		Object element = null;

		// Try to create and parametrize the object
		try {
			// Get class and constructor objects
			Class<?> cl = Class.forName("ch.ethz.inspire.emod.dd.model.Duct"
					+ type);
			Constructor<?> co = cl.getConstructor(String.class);
			// initialize new component
			element = co.newInstance(type);
		} catch (Exception e) {
			logger.severe("Duct: Unable to create component " + type);
			e.printStackTrace();
			return null;
		}

		return (ADuctElement) element;
	}

	/**
	 * Generates and returns the path of the duct with the provided name in the
	 * duct DB
	 * 
	 * @param name
	 * @return path (String)
	 */
	private static String getPath(String name) {
		String prefix = PropertiesHandler.getProperty("app.DuctDBPathPrefix");
		String path = prefix + "/Duct_" + name + ".xml";

		return path;
	}

	/**
	 * Returns the path of the config file of the current instance
	 */
	private String getPath() {
		return getPath(name);
	}

	/**
	 * Saves the current instance as xml file
	 */
	public void save() {
		saveToFile(getPath());
	}

	/**
	 * Saves the current instance as xml file at the path provided
	 * 
	 * @param path
	 */
	public void saveToFile(String path) {
		removeUnusedIsolations();
		
		try{
			String[] pathParts = path.split(File.separator);
			
			if(pathParts.length>0)
				this.setName(pathParts[pathParts.length-1]);
		} catch(Exception e){}
		
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
	 * Removes unused isolation elements. Unused means either the material is
	 * null, or its type is set to 'none'
	 */
	private void removeUnusedIsolations() {
		for (ADuctElement e : elements)
			if (null != e.getIsolation())
				if (null == e.getIsolation().getMaterial())
					e.setIsolation(null);
				else if (null == e.getIsolation().getMaterial().getType())
					e.setIsolation(null);
				else if (e.getIsolation().getMaterial().getType()
						.equals("none"))
					e.setIsolation(null);
	}

	/**
	 * Add a new element to the duct
	 * 
	 * @param e {@link ADuctElement}
	 */
	public void addElement(ADuctElement e) {
		e.setName(getUniqueElementName(e.getName()));
		addElement(elements.size(), e);
	}

	/**
	 * Add a new element to the duct at the given index i
	 * 
	 * @param i
	 * @param e {@link ADuctElement}
	 */
	public void addElement(int i, ADuctElement e) {

		/*
		 * If the last element added has a different diameter, add a fitting
		 */
		if (elements.size() > 0 & i > 0)
			if (elements.get(i - 1).getProfileOut().getDiameter() != e
					.getProfileIn().getDiameter()) {
				DuctFitting df = new DuctFitting("Fitting_"
						+ elements.get(i - 1).getName() + "-" + e.getName(),
						elements.get(i - 1).getProfileOut(), e.getProfileIn());
				elements.add(i, df);
				df.setMaterial(getMaterial());
				i++;
			}

		e.setName(getUniqueElementName(e.getName()));
		elements.add(i, e);
		e.setMaterial(getMaterial());

		if (elements.size() > i + 1)
			if (elements.get(i + 1).getProfileIn().getDiameter() != e
					.getProfileOut().getDiameter()) {
				DuctFitting df = new DuctFitting("Fitting_" + e.getName() + "-"
						+ elements.get(i + 1).getName(), e.getProfileOut(),
						elements.get(i + 1).getProfileIn());
				elements.add(i + 1, df);
				df.setMaterial(getMaterial());
				cleanUpFittings();
			}

		if (null == history)
			history = new Undo<Duct>(10, (new Duct()).clone(getRootDuct()));

		getHistory().add((new Duct()).clone(getRootDuct()), "app.dd.actions.add");
		
		if(e instanceof DuctBypass)
			((DuctBypass) e).setRootDuct(getRootDuct());
	}

	/**
	 * @return
	 */
	private Duct getRootDuct() {
		if(null==rootDuct)
			return this;
		return rootDuct;
	}

	/**
	 * Moves the element with the provided name one rank up (fittings will be
	 * skiped)
	 * 
	 * @param name
	 */
	public void moveElementUp(String name) {
		ADuctElement e = getElement(name);

		if (null == e)
			return;

		removeAllFittings();

		int index = getElementIndex(name);

		if (0 != index)
			Collections.swap(this.elements, index, index - 1);

		cleanUpFittings();

		getHistory().add((new Duct()).clone(getRootDuct()), "app.dd.actions.move");
	}

	/**
	 * Moves the element e one rank up (fittings will be skiped)
	 * 
	 * @param e
	 */
	public void moveElementUp(ADuctElement e) {
		moveElementUp(e.getName());
	}

	/**
	 * Moves the element with the provided name one rank down (fittings will be
	 * skiped)
	 * 
	 * @param name
	 */
	public void moveElementDown(String name) {

		ADuctElement e = getElement(name);

		if (null == e)
			return;

		removeAllFittings();

		int index = getElementIndex(name);

		if (this.elements.size() != index + 1)
			Collections.swap(this.elements, index, index + 1);

		cleanUpFittings();

		getHistory().add((new Duct()).clone(getRootDuct()), "app.dd.actions.move");

	}

	/**
	 * Moves the element e one rank down (fittings will be skiped)
	 * 
	 * @param e
	 */
	public void moveElementDown(ADuctElement e) {
		moveElementDown(e.getName());
	}

	/**
	 * Removes all fittings in the duct
	 */
	private void removeAllFittings() {
		for (ADuctElement e : this.elements)
			if (e instanceof DuctFitting) {
				elements.remove(e);
				removeAllFittings();
				return;
			}
	}

	/**
	 * Replaces the element with the given index by the new element e
	 * 
	 * @param i
	 * @param e
	 */
	public void replaceElement(int i, ADuctElement e) {

		elements.set(i, e);
		reconnectFittings();

		getHistory().add((new Duct()).clone(getRootDuct()), "replace " + e.getName());
	}

	/**
	 * Replace the element with the given name by the new element e
	 * 
	 * @param name
	 * @param e
	 */
	public void replaceElement(String name, ADuctElement e) {
		int i = getElementIndex(name);

		if (i >= 0)
			replaceElement(i, e);
	}

	/**
	 * Finds the index of an element with the provided name
	 * 
	 * @param name
	 * @return index
	 */
	public int getElementIndex(String name) {
		for (int i = 0; i < elements.size(); i++)
			if (elements.get(i).getName().equals(name))
				return i;

		return -1;
	}

	/**
	 * Get duct element with index i
	 * 
	 * @param i
	 * @return {@link ADuctElement}
	 */
	public ADuctElement getElement(int i) {
		return elements.get(i);
	}

	/**
	 * Get duct element with given name
	 * 
	 * @param name
	 * @return {@link ADuctElement}
	 */
	public ADuctElement getElement(String name) {
		for (ADuctElement e : getAllElements())
			if (e.getName().equals(name))
				return e;

		return null;
	}

	/**
	 * Changes the name of an element
	 * 
	 * @param name
	 *            old name
	 * @param newname
	 *            new name
	 */
	public void setElementName(String name, String newname) {

		ADuctElement e = getElement(name);

		if (null == e) {
			Exception ex = new Exception("Unable to rename element " + name
					+ " : No element with this name");
			ex.printStackTrace();
			return;
		}
		// No rename required if new and old name are the same
		else if (name.equals(newname))
			return;
		else
			e.setName(getUniqueElementName(newname));

		getHistory().add((new Duct()).clone(getRootDuct()), "app.dd.actions.editname");
	}

	/**
	 * returns a unique element name based on the provided prefix
	 * 
	 * @param prefix
	 * @return prefix(_[0-9]+)?
	 */
	private String getUniqueElementName(String prefix) {
		String name = prefix;
		int idx = 0;

		// Loop until name is unique
		while (null != getElement(name))
			name = prefix + "_" + (++idx);

		return name;
	}

	/**
	 * Removes the element with the given name
	 * 
	 * @param name
	 */
	public void removeElement(String name) {

		int i = getElementIndex(name);

		if (i >= 0) {
			elements.remove(i);
			cleanUpFittings();
		}

		getHistory().add((new Duct()).clone(getRootDuct()), "app.dd.actions.remove");
	}

	/**
	 * Sets the material of the ducts and all its elements
	 * 
	 * @param material
	 */
	public void setMaterial(Material material) {
		this.material = material;

		for (ADuctElement e : elements)
			e.setMaterial(material);
	}

	/**
	 * Returns the current material
	 * 
	 * @return {@link Material}
	 */
	public Material getMaterial() {
		return this.material;
	}

	/**
	 * Returns the current thermal resistance
	 * 
	 * @param flowRate
	 *            [m^3/s]
	 * @param pressureIn
	 *            [Pa]
	 * @param temperatureIn
	 *            [K]
	 * @return [W/K]
	 */
	public double getThermalResistance(double flowRate, double pressureIn,
			double temperatureIn) {
		double htc;
		double Rth = 0;
		double lastp = pressureIn;
		double lastT = temperatureIn;

		for (ADuctElement e : elements) {
			htc = e.getRth(Math.abs(flowRate), lastp, lastT);
			Rth += htc * e.getHydraulicSurface();
			lastp = e.getPressureOut(flowRate, lastp, lastT);
			lastT = e.getTemperatureOut(lastT, flowRate, lastp);
		}

		return Rth;
	}

	/**
	 * Returns the Thermal resistance for the given operational condition
	 * 
	 * @param flowRate
	 * @param pressureIn
	 * @param temperatureIn
	 * @param temperatureWall
	 * @return
	 */
	public double getThermalResistance(double flowRate, double pressureIn,
			double temperatureIn, double temperatureWall) {
		for (ADuctElement e : elements)
			e.setWallTemperature(temperatureWall);

		return getThermalResistance(flowRate, pressureIn, temperatureIn);
	}

	/**
	 * Returns the HTC for the given operational condition
	 * 
	 * @param flowRate
	 * @param pressure
	 * @param temperatureFluid
	 * @return
	 */
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid) {
		return getThermalResistance(flowRate, pressure, temperatureFluid)
				/ getSurface();
	}

	/**
	 * Returns the HTC for the given operational condition
	 * 
	 * @param flowRate
	 * @param pressure
	 * @param temperatureFluid
	 * @param temperatureWall
	 * @return
	 */
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {

		for (ADuctElement e : elements)
			e.setWallTemperature(temperatureWall);

		return getHTC(flowRate, pressure, temperatureFluid);
	}

	/**
	 * Returns the current pressure loss
	 * 
	 * @param flowRate
	 *            [m^3/s]
	 * @param pressureIn
	 *            [Pa]
	 * @param temperatureIn
	 *            [K]
	 * @return [Pa s^2/m^6]
	 */
	public double getPressureDrop(double flowRate, double pressureIn,
			double temperatureIn) {
		/*
		 * Simples case: no flow
		 */
		if (0 == flowRate)
			return 0;

		double pressureOut = pressureIn, pressureInLast = pressureIn;

		for (ADuctElement e : elements) {
			pressureIn = e.getPressureOut(flowRate, pressureInLast,
					temperatureIn);
			pressureInLast = pressureIn;
		}

		return -(pressureIn - pressureOut);
	}

	/**
	 * Returns the current hydraulic resistance with following assumption: p(V)
	 * = k*V^2 --> k = p(V)/V²
	 * 
	 * @param flowRate
	 *            [m^3/s]
	 * @param pressureIn
	 *            [Pa]
	 * @param temperatureFluid
	 *            [K]
	 * @return [Pa s^2/m^6]
	 */
	public double getPressureLossCoefficient(double flowRate,
			double pressureIn, double temperatureFluid) {
		return getPressureDrop(flowRate, pressureIn, temperatureFluid)
				/ Math.pow(flowRate, 2);
	}

	/**
	 * Returns the derivative of the pressure loss
	 * 
	 * @param flowRate
	 * @param pressureIn
	 * @param temperatureFluid
	 * @return
	 */
	public double getPressureLossDrivative(double flowRate, double pressureIn,
			double temperatureFluid) {
		double fac = 1.05;
		if (flowRate == 0)
			return 0;
		return (getPressureDrop(flowRate * fac, pressureIn, temperatureFluid) - getPressureDrop(
				flowRate, pressureIn, temperatureFluid)) / (fac - 1) / flowRate;
	}

	/**
	 * Returns the outlet temperature
	 * 
	 * @param flowRate
	 *            [kg/s]
	 * @param pressureIn
	 *            [Pa]
	 * @param temperatureIn
	 *            [K]
	 * @return
	 */
	public double getTemperatureOut(double flowRate, double pressureIn,
			double temperatureIn) {

		if (0 == flowRate)
			return temperatureIn;

		double pressureInLast = pressureIn;

		for (ADuctElement e : elements) {
			pressureIn = e.getPressureOut(flowRate, pressureInLast,
					temperatureIn);
			temperatureIn = e.getTemperatureOut(temperatureIn, flowRate,
					pressureInLast);
			pressureInLast = pressureIn;
		}

		return temperatureIn;
	}

	/**
	 * Returns the total duct length
	 * 
	 * @return [m]
	 */
	public double getLength() {
		double length = 0;
		for (ADuctElement e : elements)
			length += e.getLength();

		return length;
	}

	/**
	 * Returns the total duct surface
	 * 
	 * @return [m^2]
	 */
	public double getSurface() {
		double area = 0;
		for (ADuctElement e : elements)
			area += e.getSurface();

		return area;
	}

	/**
	 * Returns the total Duct volume
	 * 
	 * @return [m^3]
	 */
	public double getVolume() {
		double vol = 0;
		for (ADuctElement e : elements)
			vol += e.getVolume();

		return vol;
	}

	/**
	 * @return List of duct elements
	 */
	public ArrayList<ADuctElement> getElements() {
		return this.elements;
	}
	
	/**
	 * @return List of duct elements
	 */
	public ArrayList<ADuctElement> getElementsExceptFittings() {
		ArrayList<ADuctElement> retr = new ArrayList<ADuctElement>();
		
		for(ADuctElement e: getElements())
			if(!(e instanceof DuctFitting))
				retr.add(e);
		
		return retr;
	}

	/**
	 * Inlet profile
	 * 
	 * @return
	 */
	public AHydraulicProfile getInletProfile() {
		if (elements.size() == 0)
			return null;
		else
			return getElement(0).getProfile();
	}

	/**
	 * Outlet profile
	 * 
	 * @return
	 */
	public AHydraulicProfile getOutletProfile() {
		if (elements.size() == 0)
			return null;
		else
			return getElement(getElements().size() - 1).getProfile();
	}

	/**
	 * @return List of duct element parents, including elements of bypasses, but
	 *         without fittings
	 */
	public ArrayList<ADuctElement> getAllElements() {
		ArrayList<ADuctElement> elements = new ArrayList<ADuctElement>();

		for (ADuctElement e : getElements()) {
			if (!(e instanceof DuctFitting))
				elements.add(e);
			if (e instanceof DuctBypass) {
				elements.addAll(((DuctBypass) e).getPrimary().getAllElements());
				if (((DuctBypass) e).getPrimary().getElements().size() == 0)
					elements.add(new DuctPipe(e.getName() + "_Branch1"));

				elements.addAll(((DuctBypass) e).getSecondary()
						.getAllElements());
				if (((DuctBypass) e).getSecondary().getElements().size() == 0)
					elements.add(new DuctPipe(e.getName() + "_Branch2"));
			}
		}

		return elements;
	}

	/**
	 * Clear the duct (removes all elements)
	 */
	public void clear() {
		for (int i = elements.size() - 1; i >= 0; i--)
			elements.remove(i);

		history.clear((new Duct()).clone(getRootDuct()));
	}

	/**
	 * Clones the duct (create a clone of the elements list)
	 * @param duct
	 * @return
	 */
	public Duct clone(Duct duct) {
		this.elements = new ArrayList<ADuctElement>();

		for (ADuctElement e : duct.getElements())
			this.elements.add(e.clone());

		cleanUpFittings();

		return this;
	}
	
	/**
	 * Returns the history of the duct editting
	 * @return
	 */
	public Undo<Duct> getHistory(){
		return getRootDuct().history;
	}

	/**
	 * Undo the last edit
	 */
	public void undo() {
		clone(this.history.undo());
		
		this.setRootDuct();
	}
	
	/**
	 * Undo the last edit
	 */
	public void redo() {
		clone(this.history.redo());
		
		this.setRootDuct();
	}


	/**
	 * Returns the name of the duct
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * @param rootDuct 
	 */
	@XmlTransient
	public void setRootDuct(Duct rootDuct){
		this.rootDuct = rootDuct;
		
		for(ADuctElement e: elements)
			if(e instanceof DuctBypass)
				((DuctBypass) e).setRootDuct(rootDuct);
	}
	
	/**
	 * 
	 */
	public void setRootDuct(){
		this.setRootDuct(getRootDuct());
	}
}
