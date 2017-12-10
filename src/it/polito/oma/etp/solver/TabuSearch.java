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
	
	/**
	 * Solves a given instance of the problem.
	 */
	public static void solve(InstanceData instanceData){
		idata = instanceData;
		initialize();
		
		// Testing TODO delete
		// This will be useless when initialize() is implemented
		currentSolution = new Solution(
				instanceData,
				new int[][] { // te
					{0, 0, 1, 0, 1},
					{1, 0, 0, 1, 0},
					{0, 1, 0, 0, 0},
					{0, 0, 0, 0, 0}
				}
			);
		
		Entry<ExamPair, Float> mostPenalizingPair = currentSolution.getMostPenalizingPair();
		
		// Test printing TODO delete
		System.out.println(currentSolution);
		
		// Neighbor fitness testing TODO delete
		try {
			int movingExam = 2;
			int newTimeslot = 4;
			System.out.println(
				"Moving e" + movingExam + " in t" + newTimeslot + ". New fitness: " +
				currentSolution.neighborFitness(movingExam - 1, newTimeslot - 1)
			);
		} catch(InvalidMoveException ime) {
			System.err.println("Invalid move!");
		}
		
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