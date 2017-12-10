package it.polito.oma.etp.solver;

import java.util.Map.Entry;

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
	
	/**
	 * Solves a given instance of the problem.
	 */
	public static void solve(InstanceData instanceData){
		idata = instanceData;
		initialize();
		
		// Testing TODO delete
		currentSolution = new Solution(
				instanceData,
				new int[][] { // te
					{0, 0, 1, 0, 1},
					{1, 0, 0, 1, 0},
					{0, 1, 0, 0, 0}
				}
			);
		
		
		Entry<ExamPair, Float> mostPenalizingPair = currentSolution.getMostPenalizingPair();
		
		
		//TODO remember to output solution on file 
	}
	
	/**
	 * Get the first feasible solution.
	 */
	private static void initialize() {
		/*	TODO insert first feasible solution in both 
		  	currentSolution and bestSolution variables */
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