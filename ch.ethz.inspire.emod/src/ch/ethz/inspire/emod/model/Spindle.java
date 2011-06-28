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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dhampl
 *
 */
@XmlRootElement
public class Spindle extends Component {

	@XmlElement
	protected String type;
	
	public Spindle() {
		super();
	}
	
	public Spindle(String type) {
		super();
		this.type=type;
	}
	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.Component#getConsumption()
	 */
	@Override
	public float getConsumption() {
		// TODO Dummy Method
		return 10000;
	}
}
