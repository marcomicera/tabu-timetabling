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
	protected int tabuListInitialSize;
	
	/**
	 * True if the Tabu List size will be modified dynamically.
	 */
	protected boolean dynamicTabuList;
	
	/**
	 * How the Tabu Search algorithm detects a non-improving situation.
	 * Allowed values:
	 * 
	 * • 1 (deltaFitness):	for 'maxNonImprovingIterationsAllowed' iterations,
	 * 						it never happens that two consecutive iterations
	 * 						bring an advantage of 'deltaFitnessThreshold'
	 * 						concerning the fitness value.
	 * 
	 * • 2 (iterations):	for 'maxNonImprovingIterationsAllowed' iterations,
	 * 						no other condition has to be satisfied
	 * 
	 * • 3 (time):			every 'tabuListIncrementTimeInterval' seconds, the
	 * 						Tabu List size gets incremented
	 */
	protected int worseningCriterion;
	
	/**
	 * Tabu List maximum size.
	 */
	protected int tabuListMaxSize;
	
	/**
	 * The maximum number of non-improving iterations allowed by
	 * the Tabu Search algorithm.
	 */
	protected int maxNonImprovingIterationsAllowed;
	
	/**
	 * How many elements will be added every time the Tabu List size
	 * gets increased.
	 */
	protected int tabuListIncrementSize;
	
	/**
	 * Moving average window used for the delta fitness automatic
	 * calculation.
	 */
	protected int movingAveragePeriod;
	
	/**
	 * Time interval by which the Tabu List size could be incremented.
	 */
	protected double tabuListIncrementTimeInterval;
	
	/**
	 * Number of chromosome to start from for the genetic algorithm.
	 */
	protected int initialPopulationSize;
	
	/**
	 * Number of threads that will be used to populate the solution space.
	 */
	protected int numberOfThreads;

	public Settings(boolean firstRandomSolution, double neighborhoodGeneratingPairsPercentage,
			boolean considerAllTimeslots, int tabuListInitialSize, boolean dynamicTabuList, int worseningCriterion,
			int tabuListMaxSize, int maxNonImprovingIterationsAllowed, int tabuListIncrementSize,
			int deltaFitnessThreshold, double tabuListIncrementTimeInterval, 
			int initialPopulationSize, int numberOfThreads) {
		this.firstRandomSolution = firstRandomSolution;
		this.neighborhoodGeneratingPairsPercentage = neighborhoodGeneratingPairsPercentage;
		this.considerAllTimeslots = considerAllTimeslots;
		this.tabuListInitialSize = tabuListInitialSize;
		this.dynamicTabuList = dynamicTabuList;
		this.worseningCriterion = worseningCriterion;
		this.tabuListMaxSize = tabuListMaxSize;
		this.maxNonImprovingIterationsAllowed = maxNonImprovingIterationsAllowed;
		this.tabuListIncrementSize = tabuListIncrementSize;
		this.movingAveragePeriod = deltaFitnessThreshold;
		this.tabuListIncrementTimeInterval = tabuListIncrementTimeInterval;
		this.initialPopulationSize = initialPopulationSize;
		this.numberOfThreads = numberOfThreads;
	}
}
