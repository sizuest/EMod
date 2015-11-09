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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.ethz.inspire.emod.model.material.Material;

public class FluidTest {
	
	@Test
	public void testFriction(){
		/* Test set-up
		 * Pipe of 1m length and a diameter of .01m. The Troughput is .055 l/s (Re=7000)
		 * Roughness is selected such that u*k/nu=30 (k=4.286E-5 m)
		 * From Moody-Diagr. lambda is known to be approx .033
		 */
		
		double L,D,Q,k, dp;
		
		// Config
		L = 1;
		D = .01;
		Q = 0.000054968;
		k = 4.286E-5;
		
		// Pressure Loss
		dp = Fluid.pressureLossFrictionPipe( new Material("Water"), 293.15, L, D, Q, k);
		
		assertEquals("Pressure loss", 0.038*1000*L/D/2*Math.pow(Q/(Math.pow(D, 2)/4*Math.PI),2), dp, 0.002*1000*L/D/2*Math.pow(Q/(Math.pow(D, 2)/4*Math.PI),2));
	}
	
	@Test
	public void testConvectionForcedPipe(){
		/* Test set-up (from VDI Wärmeatlas
		 * Pipe iwth L=1m, D=.01m
		 * Flow with v =.5m/s
		 * Tf = 31°C
		 * Tb = 100°C
		 * htc = 3286 W/m2/K
		 */
		
		double L, D, Q, Tf, Tb, htc;
		
		// Config
		L = 1;
		D = .01;
		Q = .5 * Math.pow(D, 2)/4*Math.PI;
		Tf = 273.15+31;
		Tb = 273.15+100;
		
		// HTC
		htc = Fluid.convectionForcedPipe(new Material("Water"), Tb, Tf, L, D, Q);
		
		assertEquals("HTC", 3286, htc, 3286/10);
	}

}
