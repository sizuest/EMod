package ch.ethz.inspire.emod.model.fluid;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.simple.SimpleMatrix;

import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidConnection;
import ch.ethz.inspire.emod.utils.FluidContainer;

public class FluidCircuitSolver {
	
	private int numE, numC, numCPre, numCPost, numBCIn, numBCOut;
	private SimpleMatrix H;
	private ArrayList<FluidCircuitProperties> fluidPropertyList;
	private List<FluidConnection> connections;
	private ArrayList<Integer> preE   = new ArrayList<Integer>(),
			                   postE  = new ArrayList<Integer>(),
			                   bcinE  = new ArrayList<Integer>(),
			                   bcoutE = new ArrayList<Integer>();
	private Map<Integer,ArrayList<Integer>> preIndexes  = new HashMap<Integer,ArrayList<Integer>>(),
			                                postIndexes = new HashMap<Integer,ArrayList<Integer>>();
	
	double flowRateLim = 1E-9;
	
	
	public FluidCircuitSolver(ArrayList<FluidCircuitProperties> fluidPropertyList, List<FluidConnection> connections){
		this.fluidPropertyList = fluidPropertyList;
		this.connections       = connections;
		init();
	}
	
	public void init(){
		
		SimpleMatrix SpreB, SpostB, Spre, Spost, Mpre, Mpost, Mbcin, Mbcout;
		
		/* Create sub-matrices */
		numE = this.fluidPropertyList.size();
		numC = this.connections.size();
		
		// Find indexes of all elements with pre or 
		// post connections, as well as elements with bc:
		for(FluidCircuitProperties fp: fluidPropertyList){
			int idx = fluidPropertyList.indexOf(fp);
			if(0!=fp.getPre().size())
				preE.add(idx);
			if(0!=fp.getPost().size())
				postE.add(idx);
			if(!Double.isNaN(fp.getPressureReferenceOut()))
				bcoutE.add(idx);
			if(!Double.isNaN(fp.getPressureReferenceIn()))
				bcinE.add(idx);
		}
		
		numCPre  = preE.size();
		numCPost = postE.size();
		numBCIn  = bcinE.size();
		numBCOut = bcoutE.size();
		
		// SpreB, Mpre (Pre connections for Mass)		
		SpreB = new SimpleMatrix(numCPre, numE);
		Mpre  = new SimpleMatrix(numCPre, numC);
		
		for(int i=0; i<numCPre; i++){
			SpreB.set(i, preE.get(i), 1);
			for(int idx: getPreIndexes(fluidPropertyList.get(preE.get(i)))){
				Mpre.set(i, idx, 1);
			}
		}
		
		// SpostB, Mpost (Post connections for Mass)		
		SpostB = new SimpleMatrix(numCPost, numE);
		Mpost  = new SimpleMatrix(numCPost, numC);
		
		for(int i=0; i<numCPost; i++){
			SpostB.set(i, postE.get(i), 1);
			for(int idx: getPostIndexes(fluidPropertyList.get(postE.get(i)))){
				Mpost.set(i, idx, 1);
			}
		}
		
		/*SpreB.print();
		SpostB.print();
		Mpre.print();
		Mpost.print();*/
		
		// Spre
		Spre = new SimpleMatrix(numC,  numE);
		for(int i=0; i<numC; i++){
			int idxE = fluidPropertyList.indexOf(((FluidContainer)(connections.get(i).getTarget())).getFluidCircuitProperties());
			Spre.set(i, idxE, 1);
		}
		
		// Spost
		Spost = new SimpleMatrix(numC,  numE);
		for(int i=0; i<numC; i++){
			int idxE = fluidPropertyList.indexOf(((FluidContainer)(connections.get(i).getSource())).getFluidCircuitProperties());
			Spost.set(i, idxE, 1);
		}
		
		// Mbcin
		Mbcin = new SimpleMatrix(numBCIn, numE);
		for(int i=0; i<numBCIn; i++){
			Mbcin.set(i, bcinE.get(i), 1);
		}
		
		// Mbcouthttp://www.20min.ch/
		Mbcout = new SimpleMatrix(numBCOut, numE);
		for(int i=0; i<numBCOut; i++){
			Mbcout.set(i, bcoutE.get(i), 1);
		}		
		
		
		
		/* Build constant matrix parts 
		 * 
		 * 		/   A1(dyn)   -E      E     0	 0 \
		 *      |    SpreB     0      0  -Mpre   0 |
		 *      |   SpostB     0      0  -Mpost  0 |
		 * H =  |     0      Spre     0     0   -E |
		 *      |     0        0    Spost   0   -E |
		 *      |     0      Mbcin    0     0    0 |
		 *      |     0        0   Mbcout   0    0 | 
 		 *      \   Mbcmf      0      0     0    0 / 
		 */
		
		H = new SimpleMatrix(numE+numCPre+numCPost+2*numC+numBCIn+numBCOut, 3*numE+2*numC);
		
		H.insertIntoThis(0,   numE, SimpleMatrix.identity(numE).negative());
		H.insertIntoThis(0, 2*numE, SimpleMatrix.identity(numE));
		
		// Convervation of mass over a connection
		H.insertIntoThis(numE,              0, SpreB);
		H.insertIntoThis(numE,         3*numE, Mpre.negative());
		H.insertIntoThis(numE+numCPre,      0, SpostB);
		H.insertIntoThis(numE+numCPre, 3*numE, Mpost.negative());
		
		// Equality of pressure at a connection
		H.insertIntoThis(numE+numCPre+numCPost,             numE, Spre);
		H.insertIntoThis(numE+numCPre+numCPost,      3*numE+numC, SimpleMatrix.identity(numC).negative());
		H.insertIntoThis(numE+numCPre+numCPost+numC,      2*numE, Spost);
		H.insertIntoThis(numE+numCPre+numCPost+numC, 3*numE+numC, SimpleMatrix.identity(numC).negative());
		
		// Boundary conditions (Pressure)
		H.insertIntoThis(numE+numCPre+numCPost+2*numC,           numE, Mbcin);
		H.insertIntoThis(numE+numCPre+numCPost+2*numC+numBCIn, 2*numE, Mbcout);		
		
		
		/* Build index map */
		for(int i=0; i<numE; i++){
			ArrayList<Integer> idx = getPreIndexes(fluidPropertyList.get(i));
			
			for(int j=0; j<idx.size(); j++){
				idx.set(j, fluidPropertyList.indexOf(((FluidContainer) connections.get(idx.get(j)).getSource()).getFluidCircuitProperties()));
			}
			
			preIndexes.put(i, idx);
		}
		
		for(int i=0; i<numE; i++){
			ArrayList<Integer> idx = getPostIndexes(fluidPropertyList.get(i));
			
			for(int j=0; j<idx.size(); j++){
				idx.set(j, fluidPropertyList.indexOf(((FluidContainer) connections.get(idx.get(j)).getTarget()).getFluidCircuitProperties()));
			}
			
			postIndexes.put(i, idx);
		}
	}
	
	public void solve() throws Exception{
		double[] a1, a0, e, prefIn, prefOut;
		
		a1 = new double[fluidPropertyList.size()];
		a0 = new double[fluidPropertyList.size()];
		e = new double[fluidPropertyList.size()];
		prefIn  = new double[numBCIn];
		prefOut = new double[numBCOut];		
		
		// Read new boundaries
		for(int i=0; i<numBCIn; i++)
			prefIn[i] = fluidPropertyList.get(bcinE.get(i)).getPressureReferenceIn();
		
		for(int i=0; i<numBCOut; i++)
			prefOut[i] = fluidPropertyList.get(bcoutE.get(i)).getPressureReferenceOut();
		
		int iteration = 0;
		double relChange = Double.POSITIVE_INFINITY;
		
		while(iteration<20 & relChange>1E-4){
			
			// Read new op
			for(int i=0; i<numE; i++){			
				a0[i] = fluidPropertyList.get(i).getCharacterisitc().getA0(fluidPropertyList.get(i).getFlowRate(), fluidPropertyList.get(i).getPressureIn(), fluidPropertyList.get(i).getPressureOut());
				a1[i] = fluidPropertyList.get(i).getCharacterisitc().getA1(fluidPropertyList.get(i).getFlowRate(), fluidPropertyList.get(i).getPressureIn(), fluidPropertyList.get(i).getPressureOut());
				e[i]  = fluidPropertyList.get(i).getCharacterisitc().getEp(fluidPropertyList.get(i).getFlowRate(), fluidPropertyList.get(i).getPressureIn(), fluidPropertyList.get(i).getPressureOut());
			}
		
		
			// Solve lin. problem
			SimpleMatrix sol = solveLinearSystem(a1, a0, e, prefIn, prefOut);
			
			// Get avg. rel. change:
			relChange = 0;
			double cand = 0;
			for(int i=0; i<numE; i++){
				
				if( (sol.get(i)<=flowRateLim & fluidPropertyList.get(i).getFlowRate()> flowRateLim))
					cand = 1;
				else if(sol.get(i)<=flowRateLim & fluidPropertyList.get(i).getFlowRate()<=flowRateLim) 
					cand = 0;
				else
					cand = Math.abs(1-fluidPropertyList.get(i).getFlowRate()/sol.get(i));
				relChange = Math.max(cand, relChange);
			}
			
			iteration++;
			
			
			// Write back data
			for(int i=0; i<numE; i++){
				fluidPropertyList.get(i).setPressureIn(Math.round(sol.get(i+numE)));
				fluidPropertyList.get(i).setPressureOut(Math.round(sol.get(i+2*numE)));
				
				double[] flowRates;
				ArrayList<Integer> idx = preIndexes.get(i);
				
				if(idx.size()==0){
					double flowRatesSum = 0;
					idx = postIndexes.get(i);
					
					for(int j=0; j<idx.size(); j++)
						flowRatesSum += sol.get(idx.get(j));
					
					flowRates = new double[1];
					flowRates[0] = flowRatesSum;
				}
				else if(idx.size()==1){
					flowRates = new double[1];
					flowRates[0] = sol.get(i);
				}
				else{
					flowRates = new double[idx.size()];
					for(int j=0; j<idx.size(); j++)
						flowRates[j] = sol.get(idx.get(j));
					
				}
				
				fluidPropertyList.get(i).setFlowRatesIn(flowRates);

			}
		}
		
		if(iteration>=20){
			System.out.println("Warning: Fluid circuit solution didn't coverged. Max change rate: "+relChange);
		}
	}
	
	private SimpleMatrix solveLinearSystem(double[] a1, double[] a0, double[] e, double[] pBCIn, double[] pBCOut) throws Exception{
		SimpleMatrix y = new SimpleMatrix(numE+2*numC+numCPre+numCPost+numBCIn+numBCOut, 1);

		/* Insert variable elements to matrix */
		H.insertIntoThis(0,      0, SimpleMatrix.diag(a1));
		H.insertIntoThis(0,   numE, SimpleMatrix.diag(e).negative());
		H.insertIntoThis(0, 2*numE, SimpleMatrix.diag(e));

		
		/* Build vector
		 * 
		 *     / -a0  \
		 *     |  0   |
		 *     |  0   |
		 * y = |  0   |
		 *     |  0   |
		 *     | pbcin|
		 *     |pbcout|
		 *     \ mbc  /
		 */
		for(int i=0; i<numE; i++)
			y.set(i, -a0[i]);
		
		for(int i=0; i<numBCIn; i++)
			y.set(i+numE+2*numC+numCPre+numCPost, pBCIn[i]);
		
		for(int i=0; i<numBCOut; i++)
			y.set(i+numE+2*numC+numCPre+numCPost+numBCIn, pBCOut[i]);
		
		//H.print("%e");
		//y.print("%e");
		
		/* Solve and return */
		DenseMatrix64F solution = new DenseMatrix64F(H.numCols(), 1);
		LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.leastSquares(H.numRows(), H.numCols());
		solver.setA(H.getMatrix());
		solver.solve(y.getMatrix(), solution);
		return new SimpleMatrix(solution);
	}
	
	private ArrayList<Integer> getPreIndexes(FluidCircuitProperties p){
		ArrayList<Integer> out = new ArrayList<Integer>();
		
		for(int i=0; i<connections.size(); i++)
			if(((FluidContainer)(connections.get(i).getTarget())).getFluidCircuitProperties().equals(p))
				out.add(i);
		
		return out;
	}
	
	private ArrayList<Integer> getPostIndexes(FluidCircuitProperties p){
		ArrayList<Integer> out = new ArrayList<Integer>();
		
		for(int i=0; i<connections.size(); i++)
			if(((FluidContainer)(connections.get(i).getSource())).getFluidCircuitProperties().equals(p))
				out.add(i);
		
		return out;
	}
	
	public int getMissingBC(){
		return 2*numE-numCPost-numCPre-numBCIn-numBCOut; 
	}

}
