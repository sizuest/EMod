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

import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * General Motor model abstract class.
 **/

public abstract class AMotor extends APhysicalComponent {
	// Input parameters:
	protected IOContainer rotspeed;
	protected IOContainer torque;
	// Output parameters:
	protected IOContainer pmech;
	protected IOContainer ploss;
	protected IOContainer pel;
	protected IOContainer efficiency;
}
