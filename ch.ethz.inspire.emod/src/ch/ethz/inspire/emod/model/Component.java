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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dhampl
 *
 */
@XmlRootElement
public abstract class Component {

	public abstract float getConsumption();
}
