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
package ch.ethz.inspire.emod.simulation;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Data class for automatically XML<->object generation.
 * 
 * @author andreas
 *
 */
@XmlRootElement(namespace = "ch.ethz.inspire.emod.simulation")
@XmlSeeAlso({ASimulationControl.class, RandomSimulationControl.class, StaticSimulationControl.class, GeometricKienzleSimulationControl.class})
public class InputSimulators {
	
	/* List with simulation objects */
	@XmlElementWrapper(name = "simulators")
	@XmlElement(name = "simController")
	private List<ASimulationControl> simulators;

	/**
	 * Dummy constructor for JABX
	 */
	public InputSimulators()
	{
	}

	/**
	 * 
	 * @return the list with the input simulators.
	 */
	public List<ASimulationControl> getSimulatorList()
	{
		return simulators;
	}
}
