package it.polito.oma.etp.solver;

import java.util.HashMap;
import java.util.Map;

import it.polito.oma.etp.reader.InstanceData;

public class TabuSearch {
	
	private static InstanceData idata;	
	// private TabuListEntry tlentry;
	private static Solution currentSolution;
	private static Solution bestSolution;
	private static int iteration;
	
	private TabuSearch() {
	}
	
	public static int[][] feasibleSolution() {
		// TODO implement
		return null;
	}
	
	/*private double fitness() {
		
	}*/
	
	

	/**
	 * Boolean decision variables pretty printing
	 */
	/*
	public void printY() {
		
		int E = benchmark.getE();
		int tmax = benchmark.getTmax();
		int K = benchmark.getK();
		int te[][] = benchmark.getTe();

		for(int k = 0; k < K; ++k) {
			System.out.println("****** Exams distant " + (k + 1) + " timeslots apart ******\n");
			for(int j = 0; j < E; ++j) {
				for(int i = 0; i < tmax; ++i) {
					System.out.print(te[i][j] + "");
				}
				System.out.print("\n");
			}
			System.out.print("\n\n");
		}
		
	}
*/
	
	
	/**
	 * Solves a given instance of the problem.
	 */
	public static void solve(InstanceData instancedata){
		idata = instancedata;
		initialize();
		
		Map<ExamPair, Float> conflictCoefficients = new HashMap<ExamPair, Float>();
		int[][] N = idata.getN();
		int E = idata.getE();
		
		for(int i = 0; i < E; ++i)
			for(int j = i + 1; j < E; ++j) {
				conflictCoefficients.put(
					// The corresponding exam pair acts as a key in this Map
					new ExamPair(i, j),  
					
					// The conflict coefficient value
					new Float(N[i][j] / currentSolution.getDistance(i, j))
				);
			}
		
		//TODO remember to output solution on file 
	}
	
	/**
	 * Get the first feasible solution.
	 */
	private static void initialize() {
		/*	TODO insert first feasible solution in both 
		  	in currentSolution and in bestSolution */

	}
	
	/**
	 * Checks if current solution is feasible and if it is better than the best one;
	 * in this case it becomes the new best solution.
	 */
	private static void checkSolution() {
		
		
		//updateSolution();
	}
	
	/**
	 * Updates the best solution with the current one.
	 */
	private static void updateSolution() {
		
	}
	
	
}