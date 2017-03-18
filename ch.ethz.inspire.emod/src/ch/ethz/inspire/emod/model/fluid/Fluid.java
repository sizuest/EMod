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

import ch.ethz.inspire.emod.dd.model.AHydraulicProfile;
import ch.ethz.inspire.emod.dd.model.HPCircular;
import ch.ethz.inspire.emod.dd.model.HPRectangular;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.utils.Algo;

/**
 * Implements various models for fluids
 * 
 */

public class Fluid {

	/**
	 * Calculates the htc on a horizontal plate assuming free convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (plate) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param h
	 *            Height of the plate [m]
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionFreePlateVert(Material mf, double Tb,
			double Tf, double h) {
		double f, Nu;

		/* Simplest case; Tb=Tf */
		if (Tb == Tf)
			return 0;

		f = Math.pow(1 + Math.pow(0.492 / Fluid.prandtlNumber(mf, Tf), 0.5625),
				-1.77777);
		Nu = Math.pow(0.825 + 0.387 * Math.pow(
				Fluid.rayleightNumber(mf, Tf, Math.abs(Tb - Tf), h) * f,
				.166666), 2);

		return Nu * mf.getThermalConductivity() / h;
	}

	/**
	 * Calculates the htc on a horizontal plate assuming free convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (plate) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the plate [m]
	 * @param fluidOnTop
	 *            is true, if the fluid is on top of the plate
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionFreePlateHorz(Material mf, double Tb,
			double Tf, double l, boolean fluidOnTop) {
		double Nu, Ra;

		// Simplest case = Tb=Tf
		if (Tb == Tf)
			return 0;

		// Rayleight number
		Ra = Fluid.rayleightNumber(mf, Tf, Math.abs(Tf - Tb), l);

		// Hot surface facing up or cold surface facing down
		if ((fluidOnTop & (Tf < Tb)) | (!fluidOnTop & (Tf > Tb)))
			if (Ra > 20000000)
				Nu = 0.14 * Math.pow(Ra, .3333);
			else
				Nu = 0.54 * Math.pow(Ra, .25);
		// Hot surface facing up or cold surface facing down
		else
			Nu = 0.27 * Math.pow(Ra, .25);

		return Nu * mf.getThermalConductivity() / l;
	}

	/**
	 * Calculates the htc of a cuboide assuming free convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (plate) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the plate [m]
	 * @param b
	 *            Bottom of the plate [m]
	 * @param h
	 *            Height of the plate [m]
	 * @param withBottom
	 *            Include Bottom
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionFreeCuboid(Material mf, double Tb,
			double Tf, double l, double b, double h, boolean withBottom) {
		double htcTop, htcBottom, htcSide;
		double areaTop = l * b, areaBottom = (withBottom) ? l * b : 0, areaSide = 2
				* (l + b) * h;

		/* Get different HTCs */
		htcTop = Fluid.convectionFreePlateHorz(mf, Tb, Tf, (l + b) / 2, true);
		htcSide = Fluid.convectionFreePlateVert(mf, Tb, Tf, h);
		htcBottom = Fluid.convectionFreePlateHorz(mf, Tb, Tf, (l + b) / 2,
				false);

		/* Average */
		return (areaTop * htcTop + areaBottom * htcBottom + areaSide * htcSide)
				/ (areaTop + areaBottom + areaSide);

	}

	/**
	 * Calculates the htc of a vertical cylinder assuming free convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (cylinder) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the cylinder
	 * @param d
	 *            Diameter of the cylinder
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionFreeCylinderVert(Material mf, double Tb,
			double Tf, double l, double d) {
		/* Simplest case; Tb=Tf */
		if (Tb == Tf)
			return 0;

		double Nu = Fluid.convectionFreePlateVert(mf, Tb, Tf, l) + 0.435 * l
				/ d;

		return Nu * mf.getThermalConductivity() / l;
	}

	/**
	 * Calculates the htc of a horizontal cylinder assuming free convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (cylinder) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param d
	 *            Diameter of the cylinder
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionFreeCylinderHorz(Material mf, double Tb,
			double Tf, double d) {
		double Nu, Pr, Ra;

		/* Simplest case; Tb=Tf */
		if (Tb == Tf)
			return 0;

		Ra = Fluid.rayleightNumber(mf, Tf, Math.abs(Tb - Tf), d);
		Pr = Fluid.prandtlNumber(mf, Tf);

		Nu = Math.pow(
				0.6
						+ 0.387
						* Math.pow(Ra, .16666)
						/ Math.pow(1 + Math.pow(0.559 / Pr, 0.5625),
								0.296296296), 2);

		return Nu * mf.getThermalConductivity() / d;
	}

	/**
	 * Calculates the htc of a cuboide assuming free convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (plate) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the plate [m]
	 * @param b
	 *            Bottom of the plate [m]
	 * @param h
	 *            Height of the plate [m]
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionFreeCuboid(Material mf, double Tb,
			double Tf, double l, double b, double h) {
		return Fluid.convectionFreeCuboid(mf, Tb, Tf, l, b, h, true);
	}

	/**
	 * Calculates the htc of a sphere assumin free convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (plate) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param r
	 *            Radius [m]
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionFreeSphere(Material mf, double Tb,
			double Tf, double r) {
		double Nu;

		if (Tb == Tf)
			return 0;

		Nu = 2 + 0.43 * Math.pow(
				Fluid.rayleightNumber(mf, Tf, Math.abs(Tb - Tf), 2 * r), 0.25);

		return Nu * mf.getThermalConductivity() / r / 2;
	}

	/**
	 * Calculates the htc of a pipe assuming forced convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (pipe) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the pipe [m]
	 * @param d
	 *            Diameter of the pipe [m]
	 * @param Q
	 *            Throughput [m3/s]
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionForcedPipe(Material mf, double Tb,
			double Tf, double l, double d, double Q) {
		return convectionForcedPipe(mf, Tb, Tf, l, new HPCircular(d / 2), Q);
	}

	/**
	 * Calculates the htc of a pipe assuming forced convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (pipe) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the pipe [m]
	 * @param p
	 *            Profile {@link AHydraulicProfile}
	 * @param Q
	 *            Throughput [m3/s]
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionForcedPipe(Material mf, double Tb,
			double Tf, double l, AHydraulicProfile p, double Q) {
		double Re, Pr, Prw, Nu, x, d;

		/* Simplest case; Tb=Tf */
		/*
		 * if(Tb==Tf) return 0;
		 */

		/* Reynolds, Prandtl number */
		Re = Fluid.reynoldsNumber(mf, Tf, p, Q);
		Pr = Fluid.prandtlNumber(mf, Tf);
		Prw = Fluid.prandtlNumber(mf, Tb);
		d = p.getDiameter();

		/* Calculate Nu while distingushing between laminar and turbulent flow */
		if (Re > 2300) {
			// We have turbulent flow

			if (Re < 10000) {
				x = (Re - 2300) / 7700;
				Nu = (1 - x) * Fluid.nusseltNumberLaminar(2300, Pr, l, d) + x
						* Fluid.nusseltNumberTurbulent(10000, Pr, l, d);
			} else
				Nu = Fluid.nusseltNumberTurbulent(Re, Pr, l, d);
		} else
			Nu = Fluid.nusseltNumberLaminar(Re, Pr, l, d);

		/* Influence of Wall temperature */
		Nu *= Math.pow(Pr / Prw, .11);

		/* Calculate htc */
		return Nu * mf.getThermalConductivity() / d;
	}

	/**
	 * Calculates the htc of a pipe assuming forced convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the pipe [m]
	 * @param d
	 *            Diameter of the pipe [m]
	 * @param Q
	 *            Throughput [m3/s]
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionForcedPipe(Material mf, double Tf, double l,
			double d, double Q) {
		return convectionForcedPipe(mf, Tf, Tf, l, d, Q);
	}

	/**
	 * Calculates the htc of a coil assuming forced convection
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tb
	 *            Temperature of the body (pipe) [K]
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param p
	 *            Hydraulic profile {@link AHydraulicProfile}
	 * @param Dw
	 *            Coil diameter [m]
	 * @param h
	 *            Rise per revolution [m]
	 * @param Q
	 *            Throughput [m3/s]
	 * @return calculated htc [W/m2/K]
	 */
	public static double convectionForcedCoil(Material mf, double Tb,
			double Tf, AHydraulicProfile p, double Dw, double h, double Q) {
		double Re, ReKrit, D, NuLam = 0, NuTurb = 0, Nu, Pr, Prw;

		/* Flow properties */
		Re = reynoldsNumber(mf, Tf, p, Q);
		Pr = Fluid.prandtlNumber(mf, Tf);
		Prw = Fluid.prandtlNumber(mf, Tb);

		/* Average diameter */
		D = Dw * (1 + Math.pow(h / Math.PI / Dw, 2));

		/* Critical Re number */
		ReKrit = 2300 * (1 + 8.6 * Math.pow(p.getDiameter() / D, .45));

		/* Nusselt number */
		if (Re < 2.2E4) {
			double m = .5 + .2903 * Math.pow(p.getDiameter() / D, .194);
			double ReLam = Math.min(Re, ReKrit);
			NuLam = (3.66 + .08 * (1 + .8 * Math.pow(p.getDiameter() / D, .9))
					* Math.pow(ReLam, m) * Math.pow(Pr, .333));
		}
		if (Re > ReKrit) {
			double zeta = .3164 / Math.pow(Re, .25) + .03
					* Math.pow(p.getDiameter() / D, .5);
			double ReTurb = Math.max(Re, 2.2E4);
			NuTurb = zeta
					/ 8
					* ReTurb
					* Pr
					/ (1 + 12.7 * Math.sqrt(zeta / 8)
							* (Math.pow(Pr, .666) - 1));
		}

		if (Re <= ReKrit)
			Nu = NuLam;
		else if (Re >= 2.E4)
			Nu = NuTurb;
		else {
			double x = (2.2E4 - Re) / (2.2E4 - ReKrit);
			Nu = x * NuLam + (1 - x) * NuTurb;
		}

		/* Influence of Wall temperature */
		Nu *= Math.pow(Pr / Prw, .14);

		/* Calculate htc */
		return Nu * mf.getThermalConductivity() / p.getDiameter();
	}

	/**
	 * Calculates the pressureLoss in a square pipe due to friction
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the pipe [m]
	 * @param b1
	 *            Width 1 [m]
	 * @param b2
	 *            Width 2 [m]
	 * @param Q
	 *            Throughput [m3/s]
	 * @param k
	 *            Equivalent wall roughness
	 * @return calculated pressure loss [Pa]
	 */
	public static double pressureLossFrictionPipe(Material mf, double Tf,
			double l, double b1, double b2, double Q, double k) {
		return pressureLossFrictionPipe(mf, Tf, l, new HPRectangular(b1, b2),
				Q, k);
	}

	/**
	 * Calculates the pressureLoss in a pipe due to friction
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the pipe [m]
	 * @param d
	 *            Diameter of the pipe [m]
	 * @param Q
	 *            Throughput [m3/s]
	 * @param k
	 *            Equivalent wall roughness
	 * @return calculated pressure loss [Pa]
	 */
	public static double pressureLossFrictionPipe(Material mf, double Tf,
			double l, double d, double Q, double k) {
		return pressureLossFrictionPipe(mf, Tf, l, new HPCircular(d / 2), Q, k);
	}

	/**
	 * Calculates the pressureLoss in a pipe due to friction
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the pipe [m]
	 * @param d
	 *            Hydraulic profile
	 * @param Q
	 *            Throughput [m3/s]
	 * @param k
	 *            Equivalent wall roughness
	 * @return calculated pressure loss [Pa]
	 */
	public static double pressureLossFrictionPipe(Material mf, double Tf,
			double l, AHydraulicProfile d, double Q, double k) {
		double zeta, Re, v, nu, rho;

		/* Simplest case: No flow */
		if (0 == Q)
			return 0;

		/* Reynolds number, velocity, density */
		Re = Fluid.reynoldsNumber(mf, Tf, d.getDiameter(), Q);
		v = Q / d.getArea();
		rho = mf.getDensity(Tf);

		/* Distinguish between laminar and turbulent flow */
		if (Re < 2300)
			zeta = 64 / Re;
		else {

			nu = mf.getViscosityKinematic(Tf);

			if (v * k / nu <= 5) // smooth
				zeta = 0.3164 / Math.pow(Re, .25);
			else if (v * k / nu >= 70000000) // rough
				zeta = 1 / (Math.pow(
						1.14 - 2 * Math.log10(k / d.getDiameter()), 2));
			else {
				/*
				 * Find a numerical solution of the Darcy equation: Find root of
				 * \lambda^-0.5 + 2.03 log(2.51 Re^-1 \lambda^-.5 + .27 k/d)
				 * Procedure: - Solution space is given by the extremal (smooth,
				 * rough pipe) - A solution is valid, if it is accurate within
				 * 1% of the total solution space - an iterative procedure is
				 * used
				 */
				double dlambda, lambdaMin, lambdaMax, eTol, iMax, e, i;
				// 0. Initial guess and limits
				zeta = 0.3164 / Math.pow(Re, .25);
				lambdaMin = 0.3164 / Math.pow(Re, .25);
				lambdaMax = Math.pow(
						1.14 + 2 * Math.log10(k / d.getDiameter()), -2);
				// 1. Range of the solution
				dlambda = lambdaMax - lambdaMin;
				// 2. Error tolerance an max. iterations
				eTol = dlambda / 500;
				iMax = 20;
				// 3. Iteration
				e = Math.pow(zeta, -.5)
						+ 2
						* Math.log10(2.51 / Re / Math.sqrt(zeta) + k
								/ d.getDiameter() / 3.71);
				i = 0;
				while (lambdaMax - lambdaMin > eTol & i < iMax) {
					if (e > 0) {
						lambdaMin = zeta;
						zeta += (lambdaMax - lambdaMin) / 2;
					} else {
						lambdaMax = zeta;
						zeta -= (lambdaMax - lambdaMin) / 2;
					}
					e = Math.pow(zeta, -.5)
							+ 2.0
							* Math.log10(2.51 / Re / Math.sqrt(zeta) + k
									/ d.getDiameter() / 3.71);
					i++;
				}
			}

		}

		return zeta * rho * l / d.getDiameter() * Math.pow(v, 2) / 2
				* Fluid.sign(v);
	}

	/**
	 * Calculates the pressureLoss in a coil due to friction
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the pipe [m]
	 * @param p
	 *            Hydraulic profile {@link AHydraulicProfile}
	 * @param Dw
	 *            Coil diameter [m]
	 * @param h
	 *            Rise per revolution [m]
	 * @param Q
	 *            Throughput [m3/s]
	 * @return calculated pressure loss [Pa]
	 */
	public static double pressureLossFrictionCoil(Material mf, double Tf,
			double l, AHydraulicProfile p, double Dw, double h, double Q) {
		double zeta, v, Re, ReKrit, D;

		/* Simplest case: No flow */
		if (0 == Q)
			return 0;

		/* Reynolds number, velocity, density */
		Re = Fluid.reynoldsNumber(mf, Tf, p, Q);
		v = Q / p.getArea();

		/* Average diameter */
		D = Dw * (1 + Math.pow(h / Math.PI / Dw, 2));

		ReKrit = 2300 * (1 + 8.6 * Math.pow(p.getDiameter() / D, .45));

		/* Distinguish between laminar and turbulent flow */
		if (Re < ReKrit)
			zeta = 64
					/ Re
					* (1 + .033 * Math.pow(
							Math.log10(Re * Math.sqrt(p.getDiameter() / D)),
							4.0));
		else
			zeta = .3164
					/ Math.pow(Re, .25)
					* (1 + .095 * Math.pow(p.getDiameter() / D, .5)
							* Math.pow(Re, .25));

		return zeta * l / p.getDiameter() * mf.getDensity(Tf) * Math.pow(v, 2)
				/ 2 * Fluid.sign(v);
	}

	/**
	 * Calculates the pressureLoss in a fitting Convention flow direction 1->2
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param l
	 *            Length of the pipe [m]
	 * @param d1
	 *            Hydraulic profile side 1
	 * @param d2
	 *            Hydraulic profile side 2
	 * @param Q
	 *            Throughput [m3/s]
	 * @return calculated pressure loss [Pa]
	 */
	public static double pressureLossFitting(Material mf, double Tf, double l,
			AHydraulicProfile d1, AHydraulicProfile d2, double Q) {
		if (d1.getDiameter() == d2.getDiameter())
			return 0;

		double v, f1, f2, rho;

		v = Q / d2.getArea();
		f1 = d1.getArea();
		f2 = d2.getArea();
		rho = mf.getDensity(Tf);

		if (d1.getDiameter() > d2.getDiameter())
			return .04 * rho * Math.pow(v, 2) / 2;
		else
			return Math.pow(1 - f1 / f2, 2) * rho * Math.pow(v, 2) / 2
					* Fluid.sign(v);
	}

	/**
	 * Calculates the pressure loss for a T-Element (90°) given turbulent flow
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param p
	 *            Hydraulic profile
	 * @param Q
	 *            Throughput [m3/s]
	 * @return calculated pressure loss [Pa]
	 */
	public static double pressureLoss90Angle(Material mf, double Tf,
			AHydraulicProfile p, double Q) {
		double v, zeta = 1.3;

		v = Q / p.getArea();

		return zeta * mf.getDensity(Tf) * Math.pow(v, 2) / 2 * Fluid.sign(v);
	}

	/**
	 * Calculates the pressure loss for a 90°-Arc
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param p
	 *            Hydraulic profile
	 * @param r
	 *            Arc radius [m]
	 * @param Q
	 *            Throughput [m3/s]
	 * @return calculated pressure loss [Pa]
	 */
	public static double pressureLossArc(Material mf, double Tf,
			AHydraulicProfile p, double r, double Q) {
		double[] Re_vec = { 0.211689, 0.228407, 0.246444, 0.263044, 0.289929,
				0.29943, 0.309579, 0.312825, 0.3196, 0.333897, 0.334027,
				0.334247, 0.360405, 0.360636, 0.388866, 0.405994, 0.419567,
				0.423925, 0.438055, 0.45248, 0.472648, 0.499173, 0.499464,
				0.538582, 0.544769, 0.581114, 0.633838, 0.683879, 0.737885,
				0.77043, 0.771014, 0.831271, 0.831885, 0.896934, 0.90736,
				0.9575, 1.04435, 1.10255, 1.18962, 1.36971, 1.37053, 1.49395,
				1.4943, 1.49482, 1.6119, 1.62981, 1.63041, 2.02457, 2.02571,
				2.20817, 2.2334, 2.77386, 2.77462, 3.0263, 3.05827, 3.19396,
				3.33671, 3.48361, 3.63923, 3.75783, 3.84093, 3.96934, 4.09861,
				4.2349, 4.66928, 4.71962, 5.14812, 5.20351, 5.32002, 5.67617,
				5.73714, 5.7407, 5.86559, 6.25814, 6.32548, 6.32905, 6.53746,
				6.89992, 7.60736, 7.77286, 8.38734, 8.66303, 9.34788, 9.44828,
				9.76473, 10.199, 10.4184, 10.8828, 11.3668, 11.4864, 11.9984,
				12.5317, 13.5179, 13.6607, 14.7395, 15.0605, 15.0657, 16.4269,
				16.6094, 18.3074, 18.5109, 20.8483, 20.8503, 20.8576, 22.9948,
				23.235, 23.2373, 25.6272, 25.8975, 29.183, 29.4918, 29.4952,
				32.5238, 32.868, 32.8712, 36.2471, 36.6307, 36.635, 37.8458,
				37.8583, 41.7228, 42.1923, 46.4992, 57.1358, 57.148, 62.9914,
				63.0024, 81.7282, 90.1042, 97.1801, 99.3426, 108.307, 111.924,
				120.709, 123.399, 133.08, 137.534, 148.317, 151.635, 165.3,
				167.186, 174.661, 182.244, 184.323, 190.478, 203.111, 205.425,
				216.763, 219.33, 241.588, 241.823, 252.434, 263.655, 266.347,
				266.616, 281.344, 287.554, 296.85, 320.485, 327.279, 327.642,
				345.734, 364.76, 365.164, 377.057, 393.52, 438.586, 473.571,
				483.545, 522.126, 538.922, 581.932, 594.189, 594.604, 594.905,
				655.085, 655.887, 662.7, 730.107, 730.646, 731.015, 805.558,
				813.72, 841.366, 859.904, 879.021, 927.54, 958.382, 979.689,
				1000.69, 1078.75, 1177.45, 1189.29, 1312.32, 1628.78, 1630.49,
				1630.9, 1798.08, 1815.31, 1817.22, 1939.02, 2137.83, 2256.92,
				2357.98, 2459.21, 2515.39, 2628.02, 2740.84, 2896.79, 2897.41,
				3021.8, 3228.54, 3229.23, 3298.72, 3331.62, 3559.49, 3676.49,
				3713.17, 4053.44, 4138.41, 4517.65, 4562.63, 5085.15, 5606.42,
				6121.5, 6186.88, 6533.71, 6596.96, 6822.41, 6895.42, 7281.82,
				7352.32, 7521.76, 7602.26, 8028.27, 8105.83, 8292.48, 8472.72,
				8947.51, 9034.13, 10864.3, 10873.4, 11480.9, 11593.2, 11987,
				12781.8, 12795.7, 13359.6, 14107.4, 14245.9, 14728.8, 15706.5,
				15722.7, 16762.9, 16779.5, 18481.6, 18499.5, 20598.9, 20617.3,
				22730.3, 23462.4, 23476.1, 25868, 26164.1, 28520.8, 28846.2,
				30110.3, 30133.1, 33197.4, 33220, 37416.3, 38662.7, 41251.8,
				43089.6, 45468.9, 45479.5, 50675.1, 50686.9, 62271.7, 62294.7,
				62301.9, 68679, 69403.2, 69435.6, 70926.9, 76542.9, 78195.9,
				86211.6, 96082.9, 97158.3, 105977, 107082, 108283, 118112,
				119343, 120682, 131633, 133006, 133050, 146640, 146705, 148284,
				161665, 163500 }, zeta1_vec = { 214.792725, 188.59835,
				165.671125, 147.2943, 124.6435, 117.952225, 111.42105,
				109.47775, 105.257475, 97.115275, 97.04885, 96.93565,
				84.887725, 84.7919, 74.26945, 69.0034, 65.404325, 64.281675,
				60.8623, 57.666525, 53.839375, 49.433525, 49.38995, 44.151325,
				43.384, 39.258075, 34.461275, 30.770175, 27.37165, 25.6641,
				25.63635, 23.00775, 22.981175, 20.419775, 20.0688, 18.550075,
				16.414525, 15.162125, 13.6796525, 11.33031, 11.32162,
				10.1673625, 10.16429, 10.16006, 9.2664075, 9.1445675,
				9.1404225, 6.9814425, 6.9768975, 6.390405, 6.3176775,
				5.0135925, 5.0123525, 4.63475, 4.5970275, 4.41113, 4.2411325,
				4.0862125, 3.939785, 3.81633, 3.7338975, 3.6133425, 3.5119375,
				3.4078625, 3.11566, 3.0867325, 2.9046325, 2.8815725, 2.83114,
				2.6887425, 2.668655, 2.66749, 2.6371375, 2.5559425, 2.5415325,
				2.54086, 2.4988625, 2.4318025, 2.32556, 2.3027675, 2.2333625,
				2.2084075, 2.137165, 2.12736, 2.101025, 2.07189, 2.0600425,
				2.035205, 2.0071325, 2.0003725, 1.9741875, 1.95222, 1.9177,
				1.912915, 1.8959925, 1.8941475, 1.8940975, 1.887295, 1.88643,
				1.8788425, 1.877985, 1.8688175, 1.868815, 1.8688125, 1.8692125,
				1.869255, 1.8692575, 1.869665, 1.869705, 1.86252, 1.861895,
				1.8618875, 1.869825, 1.870685, 1.8706925, 1.86355, 1.8623025,
				1.8622575, 1.85242, 1.8523475, 1.8320675, 1.82885, 1.801505,
				1.722365, 1.722295, 1.7012025, 1.7011775, 1.6149225, 1.59483,
				1.5515075, 1.5380125, 1.4833375, 1.4633975, 1.4096675,
				1.3943375, 1.3447125, 1.3240875, 1.27727325, 1.26398575,
				1.20978175, 1.201982, 1.1765285, 1.16251025, 1.159699,
				1.15467325, 1.1319555, 1.127563, 1.09897525, 1.0925835,
				1.04403325, 1.04361125, 1.0262925, 1.01177775, 1.0082815,
				1.007883, 0.98503725, 0.97421425, 0.96040925, 0.927142,
				0.917064, 0.91655075, 0.895022, 0.879018, 0.87872325,
				0.86925625, 0.8541165, 0.813885, 0.785794, 0.7786195, 0.753599,
				0.74376725, 0.711873, 0.70306, 0.70286925, 0.70274825,
				0.6822025, 0.68190075, 0.6792165, 0.65165575, 0.65144975,
				0.6513095, 0.62417375, 0.61839975, 0.59908475, 0.595145,
				0.591946, 0.58539825, 0.5782155, 0.57304775, 0.56795525,
				0.5505535, 0.53943525, 0.53813125, 0.51584325, 0.47087425,
				0.47069125, 0.470651, 0.4556, 0.45413875, 0.453985, 0.44395675,
				0.42854725, 0.42121625, 0.41538775, 0.41002775, 0.40703875,
				0.40085125, 0.39492125, 0.38691575, 0.38688675, 0.38134,
				0.370851, 0.370817, 0.36751525, 0.366079, 0.35885475,
				0.35525725, 0.35401, 0.34319575, 0.340986, 0.33135175,
				0.33027375, 0.31924275, 0.30916425, 0.29711225, 0.2958255,
				0.2893595, 0.28828225, 0.2865215, 0.28586525, 0.2824195,
				0.2817345, 0.280072, 0.2794775, 0.27704425, 0.27669725,
				0.27549425, 0.27387975, 0.26847225, 0.2675275, 0.23667,
				0.23673075, 0.2434755, 0.2448045, 0.2429055, 0.237749,
				0.2376685, 0.2344455, 0.230403, 0.22975925, 0.227414,
				0.22284425, 0.222747, 0.216584, 0.2165135, 0.20942875,
				0.2093505, 0.202252, 0.202193, 0.1952775, 0.1928895, 0.1928515,
				0.1868905, 0.18607175, 0.1793295, 0.1785155, 0.176094,
				0.1760565, 0.17304625, 0.17302075, 0.1669825, 0.16491025,
				0.160916, 0.15860025, 0.1557595, 0.15575325, 0.15278325,
				0.15277225, 0.145105, 0.14509625, 0.1450935, 0.143095,
				0.14288875, 0.14285575, 0.14132025, 0.1394355, 0.13892075,
				0.13529875, 0.132925, 0.1328135, 0.1319165, 0.131832,
				0.13161175, 0.12990675, 0.12974525, 0.129699, 0.1293015,
				0.1292145, 0.12920325, 0.125999375, 0.1259957, 0.125953275,
				0.1256113, 0.125566675 }, r_D_vec = { 0.826014, 0.892506,
				0.94255, 0.975715, 1.0562, 1.07383, 1.09114, 1.14135, 1.15839,
				1.17541, 1.20908, 1.24263, 1.2584, 1.25967, 1.34134, 1.37696,
				1.42701, 1.44078, 1.46049, 1.51054, 1.54022, 1.65616, 1.67733,
				1.74383, 1.81032, 1.87681, 1.94336, 2.03694, 2.1094, 2.16922,
				2.19239, 2.27533, 2.318, 2.37482, 2.46678, 2.47426, 2.5737,
				2.61551, 2.76424, 2.82209, 2.91291, 2.93798, 3.04509, 3.05386,
				3.1862, 3.19371, 3.31853, 3.35883, 3.45087, 3.50745, 3.58315,
				3.67252, 3.71548, 3.82114, 3.86421, 3.9862, 4.03571, 4.20078,
				4.34351, 4.36584, 4.49218, 4.51447, 4.65741, 4.67959, 4.80603,
				4.82815, 4.97116, 5.04327, 5.05372, 5.12545, 5.21884, 5.27407,
				5.36746, 5.43919, 5.53253, 5.53831, 5.68693, 5.6976, 5.84616,
				5.85205, 6.03362, 6.04423, 6.1928, 6.19869, 6.35781, 6.36375,
				6.50638, 6.52877, 6.67139, 6.67728, 6.83641, 6.84224, 7.0072,
				7.03442, 7.0402, 7.19944, 7.20522, 7.35368, 7.36445, 7.51869,
				7.52941, 7.66715, 7.69437, 7.83205, 7.84283, 8.02996, 8.04079,
				8.17842, 8.20575, 8.34343, 8.37071, 8.50839, 8.51917, 8.67341,
				8.68408, 8.82181, 8.83248, 8.98683, 9.01977, 9.03039, 9.16829,
				9.19529, 9.31664, 9.34375, 9.4816, 9.50866, 9.63, 9.65706,
				9.79496, 9.95992 }, zeta2_vec = { 0.235882, 0.228533, 0.220362,
				0.217915, 0.207711, 0.205475, 0.20328, 0.196914, 0.194754,
				0.192596, 0.189616, 0.186647, 0.185252, 0.185152, 0.178725,
				0.176389, 0.173107, 0.172203, 0.17091, 0.167628, 0.165681,
				0.159163, 0.158121, 0.154851, 0.151581, 0.14831, 0.145037,
				0.140435, 0.138211, 0.136375, 0.135744, 0.133483, 0.13232,
				0.130772, 0.128265, 0.128102, 0.125938, 0.125028, 0.121792,
				0.120851, 0.119373, 0.118914, 0.116951, 0.116856, 0.115431,
				0.11535, 0.114144, 0.113754, 0.112763, 0.112154, 0.111797,
				0.111376, 0.110914, 0.109776, 0.109573, 0.108998, 0.10901,
				0.108233, 0.10756, 0.107455, 0.106095, 0.105855, 0.104473,
				0.104258, 0.103593, 0.103477, 0.0975276, 0.0945278, 0.0951548,
				0.099458, 0.0984524, 0.0978578, 0.096955, 0.0962617, 0.0947434,
				0.0946493, 0.0930492, 0.092946, 0.09151, 0.091453, 0.0906793,
				0.0906293, 0.0899292, 0.0899015, 0.0891517, 0.0891237,
				0.0891587, 0.0891642, 0.0891992, 0.0892006, 0.0900291,
				0.0900595, 0.0909183, 0.090925, 0.0909264, 0.0909655,
				0.0909669, 0.0918217, 0.0918243, 0.0918622, 0.0919239,
				0.092717, 0.0929938, 0.0943941, 0.0944859, 0.0960794,
				0.0961417, 0.0969341, 0.0969409, 0.0969747, 0.0971167,
				0.0978335, 0.0978361, 0.097874, 0.0979943, 0.0995471,
				0.0995497, 0.0995876, 0.100414, 0.100417, 0.10045, 0.100904,
				0.102942, 0.103083, 0.103801, 0.104106, 0.105474, 0.105615,
				0.106333, 0.107191 };

		double Re, v, zeta = 1.3;

		v = Q / p.getArea();

		Re = reynoldsNumber(mf, Tf, p, Q);

		if (1E6 < Re)
			zeta = Algo.linearInterpolation(r / p.getDiameter(), r_D_vec,
					zeta2_vec);
		else
			zeta = Algo.linearInterpolation(Re, Re_vec, zeta1_vec);

		return zeta * mf.getDensity(Tf) * Math.pow(v, 2) / 2 * Fluid.sign(v);
	}

	/**
	 * Calculates the pressure loss in a T-Splitt for the primary flow
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param p
	 *            Hydraulic profile
	 * @param Qa
	 *            Primary Throughput [m3/s]
	 * @param Qz
	 *            Total Throughput [m3/s]
	 * @return
	 */
	public static double pressureLossTBranchPrimary(Material mf, double Tf,
			AHydraulicProfile p, double Qa, double Qz) {

		if (Qz == 0)
			return 0;

		if (null == p)
			return Double.NaN;

		double v, zeta;
		double[] r_vec = { 0, 0.0526316, 0.105263, 0.157895, 0.210526,
				0.263158, 0.315789, 0.368421, 0.421053, 0.473684, 0.526316,
				0.578947, 0.631579, 0.684211, 0.736842, 0.789474, 0.842105,
				0.894737, 0.947368, 1 }, zeta_vec = { 0.0528785, -0.0124157,
				-0.0472879, -0.0682365, -0.0796901, -0.0807118, -0.0740795,
				-0.0568793, -0.0323749, -0.00272367, 0.0240347, 0.0543648,
				0.0846287, 0.121634, 0.156577, 0.196532, 0.235241, 0.276651,
				0.319452, 0.364065 };

		v = Qz / p.getArea();
		zeta = Algo.linearInterpolation(Qa / Qz, r_vec, zeta_vec);

		return zeta * mf.getDensity(Tf) * Math.pow(v, 2) / 2 * Fluid.sign(v);
	}

	/**
	 * Calculates the pressure loss in a T-Splitt for the primary flow
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param p
	 *            Hydraulic profile
	 * @param Qa
	 *            Primary Throughput [m3/s]
	 * @param Qz
	 *            Total Throughput [m3/s]
	 * @return
	 */
	public static double pressureLossTBranchSecondary(Material mf, double Tf,
			AHydraulicProfile p, double Qa, double Qz) {

		if (Qz == 0)
			return 0;

		if (null == p)
			return Double.NaN;

		double v, zeta;
		double[] r_vec = { 0, 0.0526316, 0.105263, 0.157895, 0.210526,
				0.263158, 0.315789, 0.368421, 0.421053, 0.473684, 0.526316,
				0.578947, 0.631579, 0.684211, 0.736842, 0.789474, 0.842105,
				0.894737, 0.947368, 1 }, zeta_vec = { 0.989137, 0.939119,
				0.902534, 0.879996, 0.868181, 0.864476, 0.86984, 0.879225,
				0.893792, 0.913807, 0.937738, 0.96536, 0.996447, 1.03029,
				1.06591, 1.10704, 1.14823, 1.19144, 1.24097, 1.29016 };

		v = Qz / p.getArea();
		zeta = Algo.linearInterpolation(Qa / Qz, r_vec, zeta_vec);

		return zeta * mf.getDensity(Tf) * Math.pow(v, 2) / 2 * Fluid.sign(v);
	}

	/**
	 * Calculates the pressure loss in a T-Splitt for the primary flow
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param p
	 *            Hydraulic profile
	 * @param Qa
	 *            Primary Throughput [m3/s]
	 * @param Qz
	 *            Total Throughput [m3/s]
	 * @return
	 */
	public static double pressureLossTMergePrimary(Material mf, double Tf,
			AHydraulicProfile p, double Qa, double Qz) {

		if (Qz == 0)
			return 0;

		if (null == p)
			return Double.NaN;

		double v, zeta;
		double[] r_vec = { 0, 0.0526316, 0.105263, 0.157895, 0.210526,
				0.263158, 0.315789, 0.368421, 0.421053, 0.473684, 0.526316,
				0.578947, 0.631579, 0.684211, 0.736842, 0.789474, 0.842105,
				0.894737, 0.947368, 1 }, zeta_vec = { 0.049575, 0.0815342,
				0.115941, 0.149553, 0.181201, 0.214667, 0.246131, 0.277118,
				0.305642, 0.335522, 0.36166, 0.389359, 0.418409, 0.445317,
				0.473664, 0.500479, 0.527192, 0.5541, 0.578417, 0.607916 };

		v = Qz / p.getArea();
		zeta = Algo.linearInterpolation(Qa / Qz, r_vec, zeta_vec);

		return zeta * mf.getDensity(Tf) * Math.pow(v, 2) / 2 * Fluid.sign(v);
	}

	/**
	 * Calculates the pressure loss in a T-Splitt for the primary flow
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param p
	 *            Hydraulic profile
	 * @param Qa
	 *            Primary Throughput [m3/s]
	 * @param Qz
	 *            Total Throughput [m3/s]
	 * @return
	 */
	public static double pressureLossTMergeSecondary(Material mf, double Tf,
			AHydraulicProfile p, double Qa, double Qz) {

		if (Qz == 0)
			return 0;

		if (null == p)
			return Double.NaN;

		double v, zeta;
		double[] r_vec = { 0, 0.0526316, 0.105263, 0.157895, 0.210526,
				0.263158, 0.315789, 0.368421, 0.421053, 0.473684, 0.526316,
				0.578947, 0.631579, 0.684211, 0.736842, 0.789474, 0.842105,
				0.894737, 0.947368, 1 }, zeta_vec = { -1.01732, -0.851863,
				-0.692796, -0.538715, -0.396938, -0.255913, -0.125456,
				0.00127893, 0.119066, 0.224643, 0.322957, 0.413827, 0.494682,
				0.575219, 0.644842, 0.70424, 0.760146, 0.811475, 0.85837,
				0.904737 };

		v = Qz / p.getArea();
		zeta = Algo.linearInterpolation(Qa / Qz, r_vec, zeta_vec);

		return zeta * mf.getDensity(Tf) * Math.pow(v, 2) / 2 * Fluid.sign(v);
	}

	/**
	 * Calculates the heat flux resulting from a given wall, inlet and outlet
	 * temperature at a defined htc
	 * 
	 * @param htc
	 *            Heat transfer coefficient [W/m^2/K]
	 * @param Tw
	 *            Wall temperature [K]
	 * @param Tfin
	 *            Inlet temepratur [K]
	 * @param Tfout
	 *            Outlet temperature [K]
	 * @return Heat Flux [W]
	 */
	public static double heatFlux(double htc, double Tw, double Tfin,
			double Tfout) {
		double DeltaTln = ((Tw - Tfin) - (Tw - Tfout))
				/ Math.log((Tw - Tfin) / (Tw - Tfout));
		return htc * DeltaTln;
	}

	/**
	 * Reynolds Number (v*D/nu)
	 * 
	 * @param d
	 *            Characteristic length [m]
	 * @param v
	 *            Velocity [m/s]
	 * @param nu
	 *            Kin. viscosity [m2/s]
	 * @return Re
	 */
	public static double reynoldsNumber(double d, double v, double nu) {
		return v * d / nu;
	}

	/**
	 * Reynolds Number
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param d
	 *            Characteristic length [m]
	 * @param Q
	 *            Throughput [m3/s]
	 * @return Re
	 */
	public static double reynoldsNumber(Material mf, double Tf, double d,
			double Q) {
		double v, nu;

		/* Calculate parameters */
		v = Q / (Math.pow(d, 2) / 4 * Math.PI); // Q/A
		nu = mf.getViscosityKinematic(Tf);

		/* Calculate Re */
		return Fluid.reynoldsNumber(d, v, nu);
	}

	/**
	 * Reynolds Number
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @param p
	 *            Profile {@link AHydraulicProfile}
	 * @param Q
	 *            Throughput [m3/s]
	 * @return Re
	 */
	public static double reynoldsNumber(Material mf, double Tf,
			AHydraulicProfile p, double Q) {
		double v, nu;

		/* Calculate parameters */
		v = Q / p.getArea(); // Q/A
		nu = mf.getViscosityKinematic(Tf);

		/* Calculate Re */
		return Fluid.reynoldsNumber(p.getDiameter(), v, nu);
	}

	/**
	 * Prandtl Number (cp*eta/lambda)
	 * 
	 * @param cp
	 *            Specific heat capacity [J/kg/K]
	 * @param eta
	 *            Dyn. viscosity [Pa s]
	 * @param lambda
	 *            Heat conductivity [W/m/K]
	 * @return Pr
	 */
	public static double prandtlNumber(double cp, double eta, double lambda) {
		return cp * eta / lambda;
	}

	/**
	 * Prandtl Number
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Temperature of the fluid (bulk) [K]
	 * @return Pr
	 */
	public static double prandtlNumber(Material mf, double Tf) {
		double cp, eta, lambda;

		/* Calculate parameters */
		cp = mf.getHeatCapacity();
		eta = mf.getViscosityDynamic(Tf);
		lambda = mf.getThermalConductivity();

		/* Calculate Pr */
		return Fluid.prandtlNumber(cp, eta, lambda);
	}

	/**
	 * Nusselt number for laminar flow Source: VDI Wärmeatlas
	 * 
	 * @param Re
	 *            Reynolds number
	 * @param Pr
	 *            Prandtl number
	 * @param l
	 *            Pipe length [m]
	 * @param d
	 *            Pipe diameter [m]
	 * 
	 * @return Nu
	 */
	public static double nusseltNumberLaminar(double Re, double Pr, double l,
			double d) {
		double Nu = 1.615 * Math.pow(Re * Pr * d / l, .3333);
		return Nu;
	}

	/**
	 * Nusselt number for turbulent flow Source: VDI Wärmeatlas
	 * 
	 * @param Re
	 *            Reynolds number
	 * @param Pr
	 *            Prandtl number
	 * @param l
	 *            Pipe length [m]
	 * @param d
	 *            Pipe diameter [m]
	 * @return Nu
	 */
	public static double nusseltNumberTurbulent(double Re, double Pr, double l,
			double d) {
		double xi, Nu;

		/* xi */
		xi = Math.pow(1.8 * Math.log10(Re) - 1.5, -2);

		/* Nu */
		Nu = (xi / 8) * Re * Pr
				/ (1 + 12.7 * Math.sqrt(xi / 8) * (Math.pow(Pr, .6666) - 1))
				* (1 + Math.pow(d / l, .6666));
		return Nu;

	}

	/**
	 * Rayleight Number
	 * 
	 * @param beta
	 *            Thermal expansion coeff. [1/K]
	 * @param nu
	 *            Kin. viscosity [m2/s]
	 * @param cp
	 *            Specific heat capacity [J/kg/K]
	 * @param eta
	 *            Dyn. viscosity [Pa s]
	 * @param lambda
	 *            Specific heat transfer coeff. [W/m/K]
	 * @param dT
	 *            Temperature difference [K]
	 * @param d
	 *            Characteristic length [m]
	 * @return Ra
	 */
	public static double rayleightNumber(double beta, double nu, double cp,
			double eta, double lambda, double dT, double d) {
		return Fluid.grashofNumber(beta, nu, dT, d)
				* Fluid.prandtlNumber(cp, eta, lambda);
	}

	/**
	 * Rayleight Number
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Fluid temperature (bulk) [K]
	 * @param dT
	 *            Temperature difference [K]
	 * @param d
	 *            Characteristic length [m]
	 * @return Ra
	 */
	public static double rayleightNumber(Material mf, double Tf, double dT,
			double d) {
		return Fluid.grashofNumber(mf, Tf, dT, d) * Fluid.prandtlNumber(mf, Tf);
	}

	/**
	 * Grashof number
	 * 
	 * @param beta
	 *            Thermal expansion coeff. [1/K]
	 * @param nu
	 *            Kin. viscosity [m2/s]
	 * @param dT
	 *            Temperature difference [K]
	 * @param d
	 *            Characteristic length [m]
	 * @return Gr
	 */
	public static double grashofNumber(double beta, double nu, double dT,
			double d) {
		double g = 9.81; // Acceleration due to gravity [m/s2]
		return g * beta * dT * Math.pow(d, 3) / Math.pow(nu, 2);
	}

	/**
	 * Grashof number
	 * 
	 * @param mf
	 *            Type of the fluid {@link Material}
	 * @param Tf
	 *            Fluid temperature (bulk) [K]
	 * @param dT
	 *            Temperature difference [K]
	 * @param d
	 *            Characteristic length [m]
	 * @return Gr
	 */
	public static double grashofNumber(Material mf, double Tf, double dT,
			double d) {
		double beta, nu;

		/* Calculate parameter */
		beta = 1 / Tf; // Approximation for ideal gases
		nu = mf.getViscosityKinematic(Tf);

		/* Calculate Gr */
		return Fluid.grashofNumber(beta, nu, dT, d);
	}

	/**
	 * Hydraulic diameter (4*A/U)
	 * 
	 * @param A
	 *            Cross section [m2]
	 * @param U
	 *            Perimeter [m]
	 * @return Dh Hydraulic diameter [m]
	 */
	public static double hydraulicDiameter(double A, double U) {
		return 4 * A / U;
	}

	/**
	 * Hydraulic diameter (4*A/U)
	 * 
	 * @param p
	 *            Profile {@link AHydraulicProfile}
	 * @return Dh Hydraulic diameter [m]
	 */
	public static double hydraulicDiameter(AHydraulicProfile p) {
		return hydraulicDiameter(p.getArea(), p.getPerimeter());
	}

	/**
	 * @param p
	 *            Profile {@link AHydraulicProfile}
	 * @return Ph [m]
	 */
	public static double hydraulicPerimeter(AHydraulicProfile p) {
		return Math.PI * hydraulicDiameter(p);
	}

	/**
	 * Approximation for sign function
	 * 
	 * @param value
	 * @return
	 * 
	 */
	public static double sign(double value) {
		return Math.tanh(1E36 * value);
	}

}
