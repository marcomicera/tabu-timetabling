package it.polito.oma.etp.solver;

/**
 * Tabu Search settings used for tuning
 */
public class TabuSettings extends Settings {
	/**
	 * Percentage of exam pairs to be used for the neighborhood generation.
	 * Admitted values: from 0 to 1.
	 * The highest, the less number of iterations.
	 * The lowest, the faster iterations.
	 */
	public double neighborhoodGeneratingPairsPercentage;
	
	/**
	 * If true, each considered exam will produce as many neighbors
	 * as all possible timeslots in which it can be reassigned.
	 * Otherwise, a random timeslot (different from the one in which
	 * the exam is currently assigned) will be used. 
	 */
	public boolean considerAllTimeslots;
	
	/**
	 * Tabu List initial size.
	 */
	public int tabuListInitialSize;
	
	/**
	 * True if the Tabu List size will be modified dynamically.
	 */
	public boolean dynamicTabuList;
	
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
	public int worseningCriterion;
	
	/**
	 * Tabu List maximum size.
	 */
	public int tabuListMaxSize;
	
	/**
	 * The maximum number of non-improving iterations allowed by
	 * the Tabu Search algorithm.
	 */
	public int maxNonImprovingIterationsAllowed;
	
	/**
	 * How many elements will be added every time the Tabu List size
	 * gets increased.
	 */
	public int tabuListIncrementSize;
	
	/**
	 * Moving average window used for the delta fitness automatic
	 * calculation.
	 */
	public int movingAveragePeriod;
	
	/**
	 * Time interval by which the Tabu List size could be incremented.
	 */
	public double tabuListIncrementTimeInterval;
	
	/**
	 * Number of threads that will be used to populate the solution space.
	 */
	protected int numberOfThreads;
	
	public TabuSettings(Settings commonSettings,
						double neighborhoodGeneratingPairsPercentage,
						boolean considerAllTimeslots, 
						int tabuListInitialSize, 
						boolean dynamicTabuList, 
						int worseningCriterion,
						int tabuListMaxSize, 
						int maxNonImprovingIterationsAllowed, 
						int tabuListIncrementSize,
						int deltaFitnessThreshold, 
						double tabuListIncrementTimeInterval,						 
						int numberOfThreads
	) {
		// Common settings
		super(commonSettings);
				
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
		this.numberOfThreads = numberOfThreads;
	}
}
