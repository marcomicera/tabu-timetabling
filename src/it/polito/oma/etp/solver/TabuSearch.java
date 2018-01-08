package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.optimization.OptimizationSolution;

public abstract class TabuSearch {
	protected TabuSettings settings;
	protected InstanceData instance;	
	protected Solution currentSolution;
	protected Solution bestSolution;
	protected int iteration = 0;
	protected TabuList tabuList;
	private static boolean TIMER_EXPIRED = false;
	protected int bestSolutionIteration = 0;
	protected MovingAverage fitnessMovingAverage;
	
	public TabuSearch(InstanceData instanceData, TabuSettings settings) {
		this.instance = instanceData;
		this.settings = settings;
		tabuList = new TabuList(settings.tabuListInitialSize, settings.tabuListMaxSize);
		fitnessMovingAverage = new MovingAverage(settings.movingAveragePeriod);
	}
	
	/**
	 * Starts the Tabu Search algorithm starting from the given initial
	 * solution, using the given instance data.
	 * @param instanceData		data describing the problem instance.
	 * @param initialSolution	initial solution from which the Tabu Search
	 * 							algorithm starts.
	 * @return					the best solution found so far.
	 */
	public Solution solve() {
		// Number of consecutive non-improving iterations
		int nonImprovingIterations = 0;
		
		// Tabu List dynamic size: time worsening criterion
		if(settings.worseningCriterion == 3) {
			int delay = (int)(settings.tabuListIncrementTimeInterval * 1000); 
		    
			new Timer().schedule(
		    	new TimerTask() {
		    		@Override
		    		public void run() {
		    			// Increasing Tabu List size
		    			tabuList.increaseSize(settings.tabuListIncrementSize);
	    			}
	    		},
		    	delay,
		        delay
		    );
		}
		
		while(bestSolution.getFitness() > 0 && !TIMER_EXPIRED && !Thread.interrupted()) {
			/*TODO debug (iteration)*///System.out.println(Thread.currentThread().getName() + "\n***** Iteration " + iteration + " *****");
						
			Neighbor validNeighbor = null;
			
			// No valid neighbor in the neighborhood
			while(validNeighbor == null && !TIMER_EXPIRED) {
				ArrayList<Neighbor> neighborhood = getNeighborhood(currentSolution.getPenalizingPairs());
				
				/*TODO debug*/ //System.out.println("Penalizing pairs: " + currentSolution.getPenalizingPairs());
				/*TODO debug (neighborhood)*/ //System.out.println("Neighborhood: " + neighborhood);
				/*TODO debug (neighborhood size)*///System.out.println("Neighborhood size: " + neighborhood.size());
				
				validNeighbor = selectBestValidNeighbor(neighborhood);
			}
			
			if(validNeighbor != null) {
				// Dynamic Tabu List section
				if(settings.dynamicTabuList) { // If the Tabu List has a dynamic size
					switch(settings.worseningCriterion) {
						// deltaFitness worsening criterion
						case 1:
							float currentFitness = currentSolution.getFitness();
							
							// Adding the current solution's fitness to the moving average
							fitnessMovingAverage.addNum(currentFitness);
							
							// Computing the moving average
							float avg = fitnessMovingAverage.getAvg();
							
							// Computing the delta fitness used to detect non-improving situations
							float delta = Math.abs(currentFitness - avg);
							
							// Valid neighbor's fitness
							float newFitness = validNeighbor.getFitness();
							
							/*TODO debug (currentFitness)*///System.out.println("\ncurrentFitness: " + currentFitness + " (movingAverage: " + fitnessMovingAverage.getAvg() + ")");
							/*TODO debug (newFitness)*///System.out.println("newFitness: " + newFitness);
							/*TODO debug (delta/2)*///System.out.println("delta/2: " + (delta/2));
							/*TODO debug (Not improving)*///System.out.println("Not improving: " + (newFitness <= currentFitness + delta/2 && newFitness >= currentFitness - delta/2) + ". Window goes from " + (currentFitness - delta/2) + " to " + (currentFitness + delta/2));
							
							// This iteration did not bring any significant advantage in terms of fitness
							if(	newFitness <= currentFitness + delta/2 &&
								newFitness >= currentFitness - delta/2
							) {
								++nonImprovingIterations;
								/*TODO debug (nonImprovingIterations)*///System.out.println("nonImprovingIterations gets incremented: " + nonImprovingIterations);
								
								// The maximum number of allowed consecutive non-improving iterations has been reached
								if(nonImprovingIterations == settings.maxNonImprovingIterationsAllowed) {
									/*TODO debug*///System.err.println("The algorithm has not improved significantly over " + nonImprovingIterations + " iterations");
									
									// Increasing Tabu List size
									tabuList.increaseSize(settings.tabuListIncrementSize);
									nonImprovingIterations = 0;
								}
							} else {
								//nonImprovingIterations = 0;
							}
							
							break;
								
						// iterations worsening criterion
						case 2: 
							if(	// If the maximum number of non-improving iterations has been reached
								iteration % settings.maxNonImprovingIterationsAllowed == 0 &&
							
								// If it's not the first iteration
								iteration != 0
							)
								// Increasing Tabu List size
								tabuList.increaseSize(settings.tabuListIncrementSize);
							
							break;
						
						default:
							break;
					}	
				}
					
				move(validNeighbor);
			}
			/*TODO debug*///System.out.println("\n");
			
			++iteration;
		}
		
		return bestSolution;
	}
	
	/**
	 * Given all exam pairs, returns the corresponding neighborhood
	 * (i.e., the set of all possible feasible moves.) ordered by ascending fitness value. 
	 * @param examPair				exam pairs used for neighborhood generation.
	 * @return			A list of Neighbor objects ordered by ascending fitnss value containing:
	 * 					1)	the exam that should be rescheduled
	 * 					2)	the new timeslot in which the exam should be rescheduled in
	 * 					3)	the corresponding fitness value
	 * 					Return null if no feasible neighbor is found.
	 */
	private ArrayList<Neighbor> getNeighborhood(ArrayList<ExamPair> penalizingPairs)
		throws IllegalArgumentException
	{
		// Arguments checking
		if(	penalizingPairs == null || 
			settings.neighborhoodGeneratingPairsPercentage < 0 || 
			settings.neighborhoodGeneratingPairsPercentage > 1
		)
			throw new IllegalArgumentException();
		else if(penalizingPairs.isEmpty())
			throw new IllegalArgumentException("Empty neighborhood");
		
		// Neighborhood corresponding all exam pairs
		ArrayList<Neighbor> neighborhood = new ArrayList<Neighbor>();
		
		// Checks if all exams have to be considered for the neighborhood generation
		boolean considerAllPairs = (settings.neighborhoodGeneratingPairsPercentage == 1) ? true : false;
		int neighborhoodGeneratingPairs = (int)Math.ceil(penalizingPairs.size() * settings.neighborhoodGeneratingPairsPercentage);
		
		if(settings.considerAllTimeslots) {
			// Creating a set containing all penalizing exams once
			HashSet<Integer> penalizingExams = getPenalizingExams(considerAllPairs, neighborhoodGeneratingPairs);
			
			/**
			 *  For each penalizing exams, examined only once even if it appears
			 *  multiple times in the penalizingPairs data structure
			 */
			for(int movingExam: penalizingExams) {
				// For any other timeslot
				for(int newTimeslot = 0; newTimeslot < instance.getTmax(); ++newTimeslot) {
					// Skipping the current timeslots
					if(newTimeslot != currentSolution.getTimeslot(movingExam)) {
						try {
							neighborhood.add(currentSolution.getNeighbor(movingExam, newTimeslot));
						} catch (InvalidMoveException e) {
							continue;
						}
					}
				}
			}
		} else {
			for(ExamPair examPair: penalizingPairs) {
				// Obtaining a feasible neighbor for exam1 using a random timeslot
				Neighbor neighbor1 = getFeasibleNeighborRandomly(examPair.getExam1());
				if(neighbor1 != null)
					neighborhood.add(neighbor1);
				
				// Obtaining a feasible neighbor for exam1 using a random timeslot
				Neighbor neighbor2 = getFeasibleNeighborRandomly(examPair.getExam2());
				if(neighbor2 != null)
					neighborhood.add(neighbor2);
				
				// Counting exam pairs to be considered for the neighborhood generation
				if(!considerAllPairs && --neighborhoodGeneratingPairs == 0)
					break;
			}
		}
		
		if(!neighborhood.isEmpty())
			// Ordering the neighborhood by increasing fitness value
			Collections.sort(neighborhood);
		
		return neighborhood;
	}
	
	/**
	 * Given an exam index, specified as an argument, it returns a
	 * neighbor using a random timeslot.
	 * It returns null if the random timeslot produces an
	 * infeasible neighbor. 
	 * @param exam	the exam to be rescheduled in a random timeslot.
	 * @return		a feasible neighbor using a random timeslot.
	 * 				null if the generated neighbor is infeasible.
	 */
	private Neighbor getFeasibleNeighborRandomly(int exam) {
		// One-time-only random timeslot
		int randomTimeslot;
		
		// The randomly-generated neighbor will be assigned to this variable (if feasible)
		Neighbor neighbor = null;
		
		do {
			// Random timeslot index generation
			randomTimeslot = Utility.getRandomInt(0, instance.getTmax());
		} while(currentSolution.getTimeslot(exam) == randomTimeslot);
		
		if(currentSolution.getTimeslot(exam) == randomTimeslot)
			throw new AssertionError("Exam " + exam + " is not moving");
		
		try {
			neighbor = currentSolution.getNeighbor(exam, randomTimeslot);
		} catch(InvalidMoveException e) {
			// If no feasible neighbor is found, return null.
		}
		
		return neighbor;
	}
	
	/**
	 * Returns a set containing all penalizing exams appearing
	 * just once, derived from the penalizingPairs data structure.
	 * @param considerAllPairs				true if all exam pairs have to be considered.
	 * @param neighborhoodGeneratingPairs	how many exam pairs have to be considered in case considerAllPairs is false.
	 * @return								the penalizing exams set.
	 */
	private HashSet<Integer> getPenalizingExams(boolean considerAllPairs, int neighborhoodGeneratingPairs) {
		HashSet<Integer> penalizingExams = new HashSet<Integer>();
		
		for(ExamPair examPair: currentSolution.getPenalizingPairs()) {
			penalizingExams.add(examPair.getExam1());
			penalizingExams.add(examPair.getExam2());
			
			// Counting exam pairs to be considered for the neighborhood generation
			if(!considerAllPairs && --neighborhoodGeneratingPairs == 0)
				break;
		}
		
		return penalizingExams;
	}
	
	/**
	 * Determines whether all neighbors belonging to the neighborhood passed
	 * as argument (ordered by ascending fitness value) can be used for the 
	 * next move or not. 
	 * @param neighborhood	the neighborhood (ordered by ascending fitness value) 
	 * 						to be checked with respected to the Tabu list.
	 * @return				the first acceptable neighbor (that will be the best
	 * 						one: this assumes that the neighborhood is already
	 * 						ordered by ascending fitness value) that can be used 
	 * 						for the next move according to the Tabu list or
	 * 						null if no valid neighbor was found.
	 */
	private Neighbor selectBestValidNeighbor(ArrayList<Neighbor> neighborhood) {
		if(neighborhood == null || neighborhood.isEmpty())
			return null;
		
		/*TODO debug (tabu list)*/ //System.out.println("Tabu List: " + tabuList);
		/*TODO debug*///System.out.println("Tabu List size: " + tabuList.getSize());
		
		Neighbor validNeighbor = null;
		for(Neighbor neighbor: neighborhood) {
			
			// This move is in the Tabu List
			if(tabuList.find(neighbor) != -1) {
				/*TODO debug*/ //System.out.print("Neighbor " + neighbor + " has been found in the Tabu List. ");
				/*TODO debug*/ //System.out.println("Best solution's fitness is " + bestSolution.getFitness());
				
				// Aspiration criterion satisfied
				if(neighbor.getFitness() < bestSolution.getFitness()) {
					/*TODO debug*/ //System.out.println("Aspiration criterion satisfied by " + neighbor);
					validNeighbor = neighbor;
					break;
				}
			}
			// This move is not in the Tabu List
			else {
				validNeighbor = neighbor;
				break;
			}
		}
			
		return validNeighbor;
	}
	
	/**
	 * Returns the next exam pair belonging to the penalizingPairs list.
	 * This is used when an exam pair has an empty valid neighborhood, hence
	 * requiring searching for another neighborhood.
	 * @param nextPairIndex					Next exam pair index to be retrieved.		
	 * @return								The exam pair that will be used to generate
	 * 										another neighborhood to be explored by the Tabu
	 * 										Search algorithm.
	 * @throws IndexOutOfBoundsException	If the exam pair index is out of bound, meaning
	 * 										that there are no more exam pairs having a
	 * 										valid non-empty neighborhood.
	 */
	protected abstract ExamPair getNextPair(int nextPairIndex) throws IndexOutOfBoundsException;
	
	/**
	 * TODO JavaDoc
	 * @param neighbor
	 */
	private void move(Neighbor neighbor) {
		// Performing the actual move on the current solution
		currentSolution.move(neighbor);
		
		// Retrieving current solution's infos before performing the move
		int movingExam = neighbor.getMovingExam();
		int oldTimeslot = currentSolution.getTimeslot(movingExam);
		
		// Inserting this move in the Tabu List
		tabuList.add(new Neighbor(movingExam, oldTimeslot));
		
		/*TODO debug (tabu list)*/ //System.out.print("Tabu list: "+ tabuList);
		
		updateSolution(movingExam, oldTimeslot, neighbor);
	}
	
	/**
	 * update the current solution with the chosen move
	 * @param int						the exam to be moved
	 * @param oldTimeSlod				the time-slot where the exam is moved
	 * @param neighbor					the neighbor chosen by the algorithm
	 */
	private void updateSolution(int movingExam, int oldTimeslot, Neighbor neighbor) {		
		// Updating the current solution
		currentSolution.updateTe(movingExam, oldTimeslot, neighbor.getNewTimeslot());
		currentSolution.updateSchedule(neighbor); 
		currentSolution.setFitness(neighbor.getFitness());
		currentSolution.updatePenalizingPairs(neighbor);
		
		if(currentSolution instanceof OptimizationSolution)
			((OptimizationSolution)currentSolution).initializeDistanceMatrix();
		
		// Updating bestSolution if necessary
		updateBestSolution();
	}
	
	/**
	 * Updates the best solution with the current one
	 * if necessary
	 */
	protected abstract void updateBestSolution();

	/**
	 * Stopping condition used in the optimization
	 * problem, called by a timer.
	 */
	public static void stopExecution() {
		TIMER_EXPIRED = true;
	}
	
	public static boolean getTimerExpired() {
		return TIMER_EXPIRED;
	}
	
	class MovingAverage {
	    private final Queue<Float> window = new LinkedList<Float>();
	    private final int period;
	    private float sum;

	    protected MovingAverage(int period) {
	        if(period <= 0)
	        	throw new AssertionError("Period must be a positive integer");
	        this.period = period;
	    }

	    protected void addNum(float num) {
	        sum += num;
	        window.add(num);
	        if(window.size() > period) {
	            sum -= window.remove();
	        }
	    }

	    protected float getAvg() {
	        if(window.isEmpty()) return 0; // technically the average is undefined
	        return sum / window.size();
	    }
	}
}