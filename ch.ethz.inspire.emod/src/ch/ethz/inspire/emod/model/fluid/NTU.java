package ch.ethz.inspire.emod.model.fluid;

/**
 * Implements the NTU method for heat exchanger calculations
 * Source: Frank P. Incropera, Introduction to heat transfer. New York:Wiley, 1985.
 * 
 * @author simon
 *
 */
public class NTU {
	
	/**
	 * Computes the outlet temperatures for the given heat exchanger and operational point
	 * @param T1in 		[K]
	 * @param cp1		[J/kg/K]
	 * @param mDot1		[kg/s]
	 * @param T2in		[K]
	 * @param cp2		[J/kg/K]
	 * @param mDot2		[kg/s]
	 * @param U			[W/m2/K]
	 * @param A			[m2]
	 * @param N 
	 * @param type
	 * @return
	 */
	public static double[] outletTemperatures(double T1in, double cp1, double mDot1, double T2in, double cp2, double mDot2, double U, double A, int N, HeatExchangerType type){
		double[] temperatures = new double[2];
		
		// Heat flux from 1 to 2
		double Q = heatFlux(T1in, cp1, mDot1, T2in, cp2, mDot2, U, A, N, type);
		
		// Temperatures
		temperatures[0] = T1in-Q/mDot1/cp1;
		temperatures[1] = T2in+Q/mDot2/cp2;
		
		return temperatures;
	}
	
	
	/**
	 * Computes the heat flux from side 1 to side 2 of the given heat exchanger and operational point
	 * @param T1in 		[K]
	 * @param cp1		[J/kg/K]
	 * @param mDot1		[kg/s]
	 * @param T2in		[K]
	 * @param cp2		[J/kg/K]
	 * @param mDot2		[kg/s]
	 * @param U			[W/m2/K]
	 * @param A			[m2]
	 * @param N 
	 * @param type		
	 * @return
	 */
	public static double heatFlux(double T1in, double cp1, double mDot1, double T2in, double cp2, double mDot2, double U, double A, int N, HeatExchangerType type){
		double Q, Qmax, ntu, Cr, C1, C2, Cmin, Cmax, epsilon;
		
		// Heat capacities
		C1 = mDot1*cp1;
		C2 = mDot2*cp2;
		
		Cmax = Math.max(C1, C2);
		Cmin = Math.min(C1, C2);
		
		// Heat capacity ratio
		Cr = Cmin/Cmax;
		
		// ntu and epsilon
		ntu = U*A/Cmin;
		epsilon = effectiveness(ntu, Cr, N, type);
		
		// Maximum possible heat flux
		Qmax = Cmin*(T1in-T2in);
		
		// Effective heat flux
		Q = Qmax*epsilon;
		
		return Q;
	}
	
	/**
	 * Computes the HE effectiveness for the given parameters and type
	 * @param ntu
	 * @param Cr
	 * @param N
	 * @param type
	 * @return
	 */
	public static double effectiveness(double ntu, double Cr, int N, HeatExchangerType type){
		/*
		 * Case boiling fluid
		 */
		
		if(0==Cr)
			return 1-Math.exp(ntu);
		
		switch(type){
		case COUNTERFLOW:
			if(1==Cr)
				return ntu/(1+ntu);
			else
				return (1-Math.exp(-ntu*(1-Cr))) / (1-Cr*Math.exp(-ntu*(1-Cr)));
		case CROSSCMAXMIXED:
			return 1/Cr*(1-Math.exp(-Cr*(1-Math.exp(-ntu))));
		case CROSSCMINMIXED:
			return 1-Math.exp(-1/Cr*(1-Math.exp(-Cr*ntu)));
		case CROSSUNMIXED:
			return 1-Math.exp(1/Cr * Math.pow(ntu, .22) * (Math.exp(-Cr*Math.pow(ntu, .78))-1));
		case NSHELLPASS:
			double ntun = ntu/N;
			double epsilon1 = 2 / (1+Cr+Math.sqrt(1+Math.pow(Cr, 2)*(1+Math.exp(-ntun*Math.sqrt(1+Math.pow(Cr,2))))/(1+Math.exp(-ntun*Math.sqrt(1+Math.pow(Cr, 2))))));
			return Math.pow((1-epsilon1*Cr)/(1-epsilon1),N-1) / (((1-epsilon1*Cr) / Math.pow(1-epsilon1, N))-Cr);
		case ONESHELLPASS:
			return 2 / (1+Cr+Math.sqrt(1+Math.pow(Cr, 2))) * (1+Math.exp(-ntu*Math.sqrt(1+Math.pow(Cr, 2)))) / (1+Math.exp(-ntu*Math.sqrt(1+Math.pow(Cr, 2))));
		case PARALLELFLOW:
			return (1-Math.exp(-ntu*(1+Cr)))/(1+Cr);
		default:
			return Double.NaN;
		
		}
	}
	
}

