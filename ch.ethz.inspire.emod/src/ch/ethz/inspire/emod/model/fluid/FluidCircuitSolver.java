package ch.ethz.inspire.emod.model.fluid;

import java.util.ArrayList;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import ch.ethz.inspire.emod.utils.FluidCircuitProperties;

public class FluidCircuitSolver {
	
	private int numConnections, numBCIn, numBCOut;
	private SimpleMatrix Mpre, Mpost, Mbcin, Mbcout;
	private ArrayList<FluidCircuitProperties> fluidPropertyList;
	
	public FluidCircuitSolver(ArrayList<FluidCircuitProperties> fluidPropertyList){
		this.fluidPropertyList = fluidPropertyList;
		init();
	}
	
	public void addConnection(int[] in, int[] out){
		numConnections++;
		
	}
	
	public void init(){
		
	}
	
	public void solveLinearSystem(){
		double[] a1, a0, prefIn, prefOut;
		
		a1 = new double[fluidPropertyList.size()];
		a0 = new double[fluidPropertyList.size()];
		prefIn  = new double[numBCIn];
		prefOut = new double[numBCOut];
		
		// Solve lin. problem
		SimpleMatrix sol = solveLinearSystem(a1, a0, prefIn, prefOut);
		
		// Write back data
		
	}
	
	private SimpleMatrix solveLinearSystem(double[] a1, double[] a0, double[] prefIn, double[] prefOut){
		SimpleMatrix A1, E;
		SimpleMatrix H, y;
		
		int numElements = this.fluidPropertyList.size();
		
		H = new SimpleMatrix(numElements+2*numConnections+numBCIn+numBCOut, 3*numElements);
		y = new SimpleMatrix(numElements+2*numConnections+numBCIn+numBCOut, 1);
		
		A1 = SimpleMatrix.diag(a1);
		E  = SimpleMatrix.identity(a1.length);
		
		/* 
		 * 		/     A1      -E      E     \
		 *      |     0      Mpre  -Mpost   |
		 * H =  | Mpre-Mpost   0      0     |
		 *      |     0      Mbcin    0     |
 		 *      \     0        0    Mbcout  / 
		 */
		H.insertIntoThis(0,               0,           A1);
		H.insertIntoThis(0,               numElements, E.negative());
		H.insertIntoThis(0,             2*numElements, E);
		H.insertIntoThis(  numElements,   numElements, Mpre);
		H.insertIntoThis(  numElements, 2*numElements, Mpost.negative());
		H.insertIntoThis(numElements+numConnections, 0,	Mpre.minus(Mpost));
		H.insertIntoThis(numElements+numConnections+numBCIn, numElements, Mbcin);
		H.insertIntoThis(numElements+numConnections+numBCIn, 2*numElements, Mbcout);
		
		/*
		 *     / mDot \
		 *     |  0   |
		 * y = | pin  |
		 *     \ pout /
		 */
		for(int i=0; i<numElements; i++)
			y.set(i, a1[i]);
		
		for(int i=0; i<numBCIn; i++)
			y.set(i+numElements+2*numConnections, prefIn[i]);
		
		for(int i=0; i<numBCOut; i++)
			y.set(i+numElements+2*numConnections+numBCIn, prefOut[i]);
		
		
		return H.solve(y);
	}
	

}
