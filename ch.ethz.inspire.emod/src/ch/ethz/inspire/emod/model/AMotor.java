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

import java.util.ArrayList;

import ch.ethz.inspire.emod.femexport.BoundaryCondition;
import ch.ethz.inspire.emod.femexport.BoundaryConditionType;
import ch.ethz.inspire.emod.model.units.SiUnit;
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
	// Boundary conditions
	protected BoundaryCondition bcHeatSrcStator;
	protected BoundaryCondition bcHeatSrcRotor;

	protected void init() {
		/* Boundary conditions */
		boundaryConditions = new ArrayList<BoundaryCondition>();
		bcHeatSrcStator = new BoundaryCondition("HeatSrcStator",
				new SiUnit("W"), 0, BoundaryConditionType.NEUMANN);
		bcHeatSrcRotor = new BoundaryCondition("HeatSrcRotor", new SiUnit("W"),
				0, BoundaryConditionType.NEUMANN);
		boundaryConditions.add(bcHeatSrcStator);
		boundaryConditions.add(bcHeatSrcRotor);
	}

}
