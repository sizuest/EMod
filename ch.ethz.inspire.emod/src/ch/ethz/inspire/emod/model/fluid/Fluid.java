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

import ch.ethz.inspire.emod.model.Material;

/**
 * Implements various models for fluids
 *
 */

public class Fluid {
	
		/**
		 * Calculates the htc on a horizontal plate assuming free convection
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tb  Temperature of the body (plate) [K]
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param h   Height of the plate [m]
		 * @return calculated htc [W/m2/K]
		 */
		public static double convectionFreePlateVert(Material mf, double Tb, double Tf, double h){
			double f, Nu;
			
			/* Simplest case; Tb=Tf */
			if(Tb==Tf)
				return 0;
			
			f  = Math.pow(1+Math.pow(0.492/Fluid.prandtlNumber(mf, Tf), 0.5625), -1.77777);
			Nu = Math.pow(0.825+0.387*Math.pow(Fluid.rayleightNumber(mf, Tf, Math.abs(Tb-Tf), h) * f, .166666), 2);
			
			return Nu*mf.getThermalConductivity()/h;
		}
		
		/**
		 * Calculates the htc on a horizontal plate assuming free convection
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tb  Temperature of the body (plate) [K]
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param l   Length of the plate [m]
		 * @param fluidOnTop is true, if the fluid is on top of the plate
		 * @return calculated htc [W/m2/K]
		 */		
		public static double convectionFreePlateHorz(Material mf, double Tb, double Tf, double l, boolean fluidOnTop){
			double Nu, Ra;
			
			// Simplest case = Tb=Tf
			if(Tb==Tf)
				return 0;
			
			// Rayleight number
			Ra = Fluid.rayleightNumber(mf, Tf, Math.abs(Tf-Tb), l);
			
			// Hot surface facing up or cold surface facing down
			if ( (fluidOnTop & (Tf<Tb)) |  (!fluidOnTop & (Tf>Tb)) )
				if (Ra>20000000)
					Nu = 0.14*Math.pow(Ra, .3333);
				else
					Nu = 0.54*Math.pow(Ra, .25);
			// Hot surface facing up or cold surface facing down
			else
				Nu = 0.27*Math.pow(Ra, .25);
			
			return Nu*mf.getThermalConductivity()/l;
		}
		
		/**
		 * Calculates the htc of a cuboide assuming free convection
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tb  Temperature of the body (plate) [K]
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param l   Length of the plate [m]
		 * @param b	  Bottom of the plate [m]
		 * @param h   Height of the plate [m]
		 * @param withBottom Include Bottom
		 * @return calculated htc [W/m2/K]
		 */
		public static double convectionFreeCuboid(Material mf, double Tb, double Tf, double l, double b, double h, boolean withBottom){
			double htcTop, htcBottom, htcSide;
			double areaTop = l*b, 
				   areaBottom = (withBottom)? l*b : 0 , 
				   areaSide = 2*(l+b)*h;
			
			/* Get different HTCs */
			htcTop    = Fluid.convectionFreePlateHorz(mf, Tb, Tf, (l+b)/2, true);
			htcSide   = Fluid.convectionFreePlateVert(mf, Tb, Tf, h);
			htcBottom = Fluid.convectionFreePlateHorz(mf, Tb, Tf, (l+b)/2, false);
			
			/* Average */
			return (areaTop*htcTop + areaBottom*htcBottom + areaSide*htcSide) / (areaTop+areaBottom+areaSide);
			
		}
		
		/**
		 * Calculates the htc of a vertical cylinder assuming free convection
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tb  Temperature of the body (cylinder) [K]
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param l   Length of the cylinder
		 * @param d   Diameter of the cylinder
		 * @return calculated htc [W/m2/K]
		 */
		public static double convectionFreeCylinderVert(Material mf, double Tb, double Tf, double l, double d){
			/* Simplest case; Tb=Tf */
			if(Tb==Tf)
				return 0;
			
			double Nu = Fluid.convectionFreePlateVert(mf, Tb, Tf, l)+0.435*l/d;
			
			return Nu*mf.getThermalConductivity()/l;
		}
		
		/**
		 * Calculates the htc of a horizontal cylinder assuming free convection
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tb  Temperature of the body (cylinder) [K]
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param d   Diameter of the cylinder
		 * @return calculated htc [W/m2/K]
		 */
		public static double convectionFreeCylinderHorz(Material mf, double Tb, double Tf, double d){
			double Nu, Pr, Ra;
			
			/* Simplest case; Tb=Tf */
			if(Tb==Tf)
				return 0;
			
			Ra = Fluid.rayleightNumber(mf, Tf, Math.abs(Tb-Tf), d);
			Pr = Fluid.prandtlNumber(mf, Tf);
			
			Nu = Math.pow(0.6+0.387*Math.pow(Ra, .16666) / Math.pow(1+Math.pow(0.559/Pr, 0.5625), 0.296296296), 2);
			
			return Nu*mf.getThermalConductivity()/d;
		}
		
		/**
		 * Calculates the htc of a cuboide assuming free convection
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tb  Temperature of the body (plate) [K]
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param l   Length of the plate [m]
		 * @param b	  Bottom of the plate [m]
		 * @param h   Height of the plate [m]
		 * @return calculated htc [W/m2/K]
		 */
		public static double convectionFreeCuboid(Material mf, double Tb, double Tf, double l, double b, double h){
			return Fluid.convectionFreeCuboid(mf, Tb, Tf, l, b, h, true);
		}
		
		
		/**
		 * Calculates the htc of a sphere assumin free convection
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tb  Temperature of the body (plate) [K]
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param r   Radius [m]
		 * @return calculated htc [W/m2/K]
		 */
		public static double convectionFreeSphere(Material mf, double Tb, double Tf, double r){
			double Nu;
			
			if(Tb==Tf)
				return 0;
			
			Nu = 2+0.43*Math.pow(Fluid.rayleightNumber(mf, Tf, Math.abs(Tb-Tf), 2*r), 0.25);
			
			return Nu*mf.getThermalConductivity()/r/2;
		}
		
		/**
		 * Calculates the htc of a pipe assuming forced convection
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tb  Temperature of the body (pipe) [K]
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param l   Length of the pipe [m]
		 * @param d   Diameter of the pipe [m]
		 * @param Q   Throughput [m3/s]
		 * @return calculated htc [W/m2/K]
		 */
		public static double convectionForcedPipe(Material mf, double Tb, double Tf, double l, double d, double Q){
			double Re, Pr, Prw, Nu, x;
			
			/* Simplest case; Tb=Tf */
			/*if(Tb==Tf)
				return 0;*/
			
			/* Reynolds, Prandtl number */
			Re  = Fluid.reynoldsNumber(mf, Tf, d, Q);
			Pr  = Fluid.prandtlNumber(mf, Tf);
			Prw = Fluid.prandtlNumber(mf, Tb);
			
			/* Calculate Nu while distingushing between laminar and turbulent flow */
			if (Re>2300){
				// We have turbulent flow
				
				if (Re<10000) {
					x = (Re-2300)/7700;
					Nu = (1-x)*Fluid.nusseltNumberLaminar(2300, Pr, l, d) + x*Fluid.nusseltNumberTurbulent(10000, Pr, l, d);
				}
				else
					Nu = Fluid.nusseltNumberTurbulent(Re, Pr, l, d);
			}
			else
				Nu = Fluid.nusseltNumberLaminar(Re, Pr, l, d);
			
			/* Influence of Wall temperature */
			Nu *= Math.pow(Pr/Prw, .11);
			
			/* Calculate htc */
			return Nu*mf.getThermalConductivity()/d;
		}
		
		/**
		 * Calculates the htc of a pipe assuming forced convection
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param l   Length of the pipe [m]
		 * @param d   Diameter of the pipe [m]
		 * @param Q   Throughput [m3/s]
		 * @return calculated htc [W/m2/K]
		 */
		public static double convectionForcedPipe(Material mf, double Tf, double l, double d, double Q){
			return convectionForcedPipe(mf, Tf, Tf, l, d, Q);
		}
			
		/**
		 * Calculates the pressureLoss in a pipe due to friction
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param l   Length of the pipe [m]
		 * @param d   Diameter of the pipe [m]
		 * @param Q   Throughput [m3/s]
		 * @param k   Equivalent wall roughness
		 * @return calculated pressure loss [Pa]
		 */
		public static double pressureLossFriction(Material mf, double Tf, double l, double d, double Q, double k){
			double lambda, Re, v, nu, rho;
			
			/* Simplest case: No flow */
			if(0==Q)
				return 0;
			
			/* Reynolds number, velocity, density */
			Re  = Fluid.reynoldsNumber(mf, Tf, d, Q);
			v   = Q/(Math.pow(d, 2)/4*Math.PI);
			rho = mf.getDensity(Tf);
			
			/* Distinguish between laminar and turbulent flow */
			if(Re<2300)
				lambda = 64/Re;
			else {
				
				nu = mf.getViscosityKinematic(Tf);
				
				if(v*k/nu<=5) 	    // smooth
					lambda = 0.3164/Math.pow(Re, .25);
				else if(v*k/nu>=70) // rough
					lambda = 1/(Math.pow(1.14-2*Math.log10(k/d), 2));
				else {
					/* Find a numerical solution of the Darcy equation:
                     * Find root of
                     *  \lambda^-0.5 + 2.03 log(2.51 Re^-1 \lambda^-.5 + .27 k/d)
                     * Procedure:
                     *  - Solution space is given by the extremal (smooth, rough pipe)
                     *  - A solution is valid, if it is accurate within 1% of the total solution space
                     *  - an iterative procedure is used  */
                    double dlambda, lambdaMin, lambdaMax, eTol, iMax, e, i;
                    // 0. Initial guess and limits
                    lambda = 0.3164/Math.pow(Re, .25);
                    lambdaMin = 0.3164/Math.pow(Re, .25);
                    lambdaMax = Math.pow(1.14-2*Math.log10(.01/d), -2);
                    // 1. Range of the solution
                    dlambda = lambdaMax - lambdaMin;
                    // 2. Error tolerance an max. iterations
                    eTol = dlambda/500;
                    iMax = 20;
                    // 3. Iteration
                    e = Math.pow(lambda, -.5) + 2.03*Math.log10(2.51/Re/Math.sqrt(lambda)+.27*k/d);
                    i = 0;
                    while(lambdaMax-lambdaMin>eTol & i<iMax){
                        if(e>0){
                            lambdaMin = lambda;
                            lambda += (lambdaMax-lambdaMin)/2;
                        }
                        else {
                            lambdaMax = lambda;
                            lambda -= (lambdaMax-lambdaMin)/2;
                        }
                        e = Math.pow(lambda, -.5) + 2.03*Math.log10(2.51/Re/Math.sqrt(lambda)+.27*k/d);
                        i++;
                    }
				}
					
				
			}
				
			
			
			return lambda*rho*l/d*Math.pow(v, 2)/2;
		}
		
		/**
		 * Calculates the pressureLoss in a square pipe due to friction
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param l   Length of the pipe [m]
		 * @param b1  Width 1 [m]
		 * @param b2  Width 2 [m]
		 * @param Q   Throughput [m3/s]
		 * @param k   Equivalent wall roughness
		 * @return calculated pressure loss [Pa]
		 */
		public static double pressureLossFriction(Material mf, double Tf, double l, double b1, double b2, double Q, double k){
			double dh, Qh;
			
			dh = Fluid.hydraulicDiameter(b1*b2, 2*(b1+b2));
			Qh = Q/b1/b2 * Math.pow(dh, 2)/4/Math.PI;
			
			return Fluid.pressureLossFriction(mf, Tf, l, dh, Qh, k);
		}
		
		//public static double pressureLossElement TODO
		
		/**
		 * Reynolds Number (v*D/nu)
		 * 
		 * @param d  Characteristic length [m]
		 * @param v  Velocity [m/s]
		 * @param nu Kin. viscosity [m2/s]
		 * @return Re
		 */
		public static double reynoldsNumber(double d, double v, double nu){
			return v*d/nu;
		}
		
		/**
		 * Reynolds Number
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @param d   Characteristic length [m]
		 * @param Q   Throughput [m3/s]
		 * @return Re
		 */
		public static double reynoldsNumber(Material mf, double Tf, double d, double Q){
			double v, nu;
			
			/* Calculate parameters */
			v  = Q / (Math.pow(d, 2)/4*Math.PI); 			// Q/A
			nu = mf.getViscosityKinematic(Tf);
			
			/* Calculate Re */
			return Fluid.reynoldsNumber(d, v, nu);
		}
		
		/**
		 * Prandtl Number (cp*eta/lambda)
		 * 
		 * @param cp     Specific heat capacity [J/kg/K]
		 * @param eta    Dyn. viscosity [Pa s]
		 * @param lambda Heat conductivity [W/m/K]
		 * @return Pr
		 */
		public static double prandtlNumber(double cp, double eta, double lambda){
			return cp*eta/lambda;
		}
		
		/**
		 * Prandtl Number 
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tf  Temperature of the fluid (bulk) [K]
		 * @return Pr
		 */
		public static double prandtlNumber(Material mf, double Tf){
			double cp, eta, lambda;
			
			/* Calculate parameters */
			cp     = mf.getHeatCapacity();
			eta    = mf.getViscosityDynamic(Tf)/1000;
			lambda = mf.getThermalConductivity();
			
			/* Calculate Pr */
			return Fluid.prandtlNumber(cp, eta, lambda);
		}
	
		/**
		 * Nusselt number for laminar flow
		 * Source: VDI Wärmeatlas
		 * @param Re   Reynolds number
		 * @param Pr   Prandtl number
		 * @param l    Pipe length [m]
		 * @param d    Pipe diameter [m]
		 * 
		 * @return Nu
		 */
		public static double nusseltNumberLaminar(double Re, double Pr, double l, double d){
			double Nu = 1.615*Math.pow(Re*Pr*d/l,.3333);
			return Nu;
		}
		
		/**
		 * Nusselt number for turbulent flow
		 * Source: VDI Wärmeatlas
		 * 
		 * @param Re   Reynolds number
		 * @param Pr   Prandtl number
		 * @param l    Pipe length [m]
		 * @param d    Pipe diameter [m]
		 * @return Nu
		 */
		public static double nusseltNumberTurbulent(double Re, double Pr, double l, double d){
			double xi, Nu;
			
			/* xi */
			xi = Math.pow(1.8*Math.log10(Re)-1.5, -2);
			
			/* Nu */
			Nu = (xi/8)*Re*Pr / ( 1+12.7*Math.sqrt(xi/8)*(Math.pow(Pr, .6666)-1) ) * (1+Math.pow(d/l, .6666));
			return Nu;
			
		}

		/**
		 * Rayleight Number
		 * 
		 * @param beta   Thermal expansion coeff. [1/K]
		 * @param nu     Kin. viscosity [m2/s]
		 * @param cp     Specific heat capacity [J/kg/K]
		 * @param eta    Dyn. viscosity [Pa s]
		 * @param lambda Specific heat transfer coeff. [W/m/K]
		 * @param dT     Temperature difference [K]
		 * @param d      Characteristic length [m]
		 * @return Ra
		 */
		public static double rayleightNumber(double beta, double nu, double cp, double eta, double lambda, double dT, double d){
			return Fluid.grashofNumber(beta, nu, dT, d)*Fluid.prandtlNumber(cp, eta, lambda);
		}
		
		
		/**
		 * Rayleight Number
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tf  Fluid temperature (bulk) [K]
		 * @param dT  Temperature difference [K]
		 * @param d   Characteristic length [m]
		 * @return Ra
		 */
		public static double rayleightNumber(Material mf, double Tf, double dT, double d){
			return Fluid.grashofNumber(mf, Tf, dT, d)*Fluid.prandtlNumber(mf, Tf);
		}
		
		/**
		 * Grashof number
		 * 
		 * @param beta  Thermal expansion coeff. [1/K]
		 * @param nu    Kin. viscosity [m2/s]
		 * @param dT    Temperature difference [K]
		 * @param d		Characteristic length [m]
		 * @return Gr
		 */
		public static double grashofNumber(double beta, double nu, double dT, double d){
			double g = 9.81; // Acceleration due to gravity [m/s2]
			return g*beta*dT*Math.pow(d, 3)/Math.pow(nu, 2);
		}
		
		/**
		 * Grashof number
		 * 
		 * @param mf  Type of the fluid {@link Material.java} 
		 * @param Tf  Fluid temperature (bulk) [K]
		 * @param dT  Temperature difference [K]
		 * @param d   Characteristic length [m]
		 * @return Gr
		 */
		public static double grashofNumber(Material mf, double Tf, double dT, double d){
			double beta, nu;
			
			/* Calculate parameter */
			beta = 1/Tf; // Approximation for ideal gases
			nu   = mf.getViscosityKinematic(Tf);
			
			/* Calculate Gr */
			return Fluid.grashofNumber(beta, nu, dT, d);
		}

		/**
		 * Hydraulic diameter (4*A/U)
		 * 
		 * @param A Cross section [m2]
		 * @param U Perimeter [m]
		 * @return Dh Hydraulic diameter [m]
		 */
		public static double hydraulicDiameter(double A, double U){
			return 4*A/U;
		}

}
