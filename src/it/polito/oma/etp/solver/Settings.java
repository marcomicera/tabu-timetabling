package it.polito.oma.etp.solver;

public class Settings {
	/**
	 * If true, the first infeasible solution is computed randomly; 
	 * otherwise, our ad-hoc deterministic algorithm tries to obtain 
	 * an initial solution with the minimum number of conflicts.
	 */
	protected boolean firstRandomSolution;
	
	/**
	 * Percentage of exam pairs to be used for the neighborhood generation.
	 * Admitted values: from 0 to 1.
	 * The highest, the less number of iterations.
	 * The lowest, the faster iterations.
	 */
	protected double neighborhoodGeneratingPairsPercentage;
	
	/**
	 * If true, each considered exam will produce as many neighbors
	 * as all possible timeslots in which it can be reassigned.
	 * Otherwise, a random timeslot (different from the one in which
	 * the exam is currently assigned) will be used. 
	 */
	protected boolean considerAllTimeslots;
	
	/**
	 * Tabu List initial size.
	 */
	public int tabuListInitialSize;

	public Settings(boolean firstRandomSolution, double neighborhoodGeneratingPairsPercentage,
			boolean considerAllTimeslots, int tabuListInitialSize) {
		this.firstRandomSolution = firstRandomSolution;
		this.neighborhoodGeneratingPairsPercentage = neighborhoodGeneratingPairsPercentage;
		this.considerAllTimeslots = considerAllTimeslots;
		this.tabuListInitialSize = tabuListInitialSize;
	}	
}
