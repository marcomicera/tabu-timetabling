package it.polito.oma.etp.solver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import it.polito.oma.etp.reader.InstanceData;

public abstract class TabuSearch {
	protected Settings settings;
	protected InstanceData instance;	
	protected Solution currentSolution;
	protected Solution bestSolution;
	protected int iteration = 0;
	protected TabuList tabuList;
	private static boolean TIMER_EXPIRED = false;
	protected int bestSolutionIteration = 0;
	
	public TabuSearch(InstanceData instanceData, Settings settings) {
		this.instance = instanceData;
		this.settings = settings;
		tabuList = new TabuList(settings.tabuListInitialSize, settings.tabuListMaxSize);
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
		
		int nonImprovingIterations = 0;
		
		while(bestSolution.getFitness() > 0 && !TIMER_EXPIRED) {
			/*TODO debug (iteration)*/System.out.println("\n***** Iteration " + iteration + " *****");
			/*TODO debug (nonImprovingIterations)*/System.out.println("***** nonImprovingIterations " + nonImprovingIterations + " *****");
			
			// Number of consecutive non-improving iterations 
			
			float	oldFitness = currentSolution.getFitness(),
					newFitness;
			/*TODO debug (oldFitness)*/System.out.println("oldFitness: " + oldFitness);
			
			Neighbor validNeighbor = null;
			
			// No valid neighbor in the neighborhood
			while(validNeighbor == null) {
				try {
					
					ArrayList<Neighbor> neighborhood = getNeighborhood(currentSolution.getPenalizingPairs());
					
					/*TODO debug*/ //System.out.println("Penalizing pairs: " + currentSolution.getPenalizingPairs());
					/*TODO debug (neighborhood)*/ //System.out.println("Neighborhood: " + neighborhood);
					/*TODO debug (neighborhood size)*/System.out.println("Neighborhood size: " + neighborhood.size());
					
					validNeighbor = selectBestValidNeighbor(neighborhood);
				} catch(IndexOutOfBoundsException e) {
					// TODO what happens here? (empty Tabu List?)
					// There's no valid neighborhood for every exam pair
				} catch(IllegalArgumentException e) {
					System.err.println("Illegal argument exception thrown: ");
					e.printStackTrace();
				}
			}
			
			if(validNeighbor != null) {
				newFitness = validNeighbor.getFitness();
				/*TODO debug (newFitness)*/System.out.println("newFitness: " + newFitness);
				/*TODO debug (Fitness difference)*/System.out.println("Fitness difference: " + (oldFitness - newFitness));
				/*TODO debug (It's not getting better)*/System.out.println("It's not getting better: " + (oldFitness - newFitness < settings.deltaFitnessThreshold));
				
				// This iteration did not bring any significant advantage in terms of fitness
				if(oldFitness - newFitness < settings.deltaFitnessThreshold) {
					++nonImprovingIterations;
					
					// The maximum number of allowed consecutive non-improving iterations has been reached
					if(nonImprovingIterations == settings.maxNonImprovingIterationsAllowed) {
						/*TODO debug*/System.err.println("The algorithm has not improved significantly over " + nonImprovingIterations + " consecutive iterations");
						
						
						/**
						 * TODO choose and implement
						 * 
						 * 1) Returning to best solution
						 * 2) (Disabling aspiration criterion)
						 * DONE) Increasing Tabu List size
						 */
						
						// Increasing Tabu List size
						tabuList.increaseSize(settings.tabuListIncrementSize);
						nonImprovingIterations = 0;
					}
				} else {
					nonImprovingIterations = 0;
				}
					
				move(validNeighbor);
			}
			/*TODO debug*/System.out.println("\n");
			
			++iteration;
		}
		
		//TODO remember to output solution on file
		
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
			randomTimeslot = java.util.concurrent.ThreadLocalRandom.current().nextInt(0, instance.getTmax());
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
		/*TODO debug*/System.out.println("Tabu List size: " + tabuList.getSize());
		
		Neighbor validNeighbor = null;
		for(Neighbor neighbor: neighborhood) {
			
			// This move is in the Tabu List
			if(tabuList.find(neighbor) != -1) {
				/*TODO debug*/ //System.out.println("Neighbor " + neighbor + " has been found in the Tabu List");
				
				// Aspiration criteria satisfied
				if(neighbor.getFitness() < bestSolution.getFitness()) {
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
	 * 
	 * @param neighbor
	 */
	private void move(Neighbor neighbor) {
		/*TODO debug*/System.out.println("Move: <e" + neighbor.getMovingExam() + ", from t" + currentSolution.getTimeslot(neighbor.getMovingExam()) + " to t" + neighbor.getNewTimeslot() + ">");
		
		// Retrieving current solution's infos before performing the move
		int movingExam = neighbor.getMovingExam();
		int oldTimeslot = currentSolution.getTimeslot(movingExam);
		/*TODO debug*/ //System.out.println("movingExam's index inside move(): " + movingExam);
		/*TODO debug*/ //System.out.println("old fitness = " + currentSolution.getFitness());
		
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
				
		/*TODO debug*/float testIncrementalFitness = currentSolution.getFitness();
		/*TODO debug (fitness)*/ System.out.println("\nFitness: " + currentSolution.getFitness());
		/*TODO debug*/ currentSolution.initializeFitness();
		/*TODO debug*/float testFitnessFromScratch = currentSolution.getFitness();
//		/*TODO debug*/if(testIncrementalFitness != testFitnessFromScratch) {
//		/*TODO debug*/	System.err.println("Different fitness values");
//		/*TODO debug*/	System.exit(1);
//		/*TODO debug*/}
		/*TODO debug (fitness from scratch)*/ //System.out.println("Calculating the fitness from scratch: " + currentSolution.getFitness());
		
		// Updating bestSolution if necessary
		updateBestSolution();
	}
	
	/**
	 * Updates the best solution with the current one
	 * if necessary
	 */
	protected abstract void updateBestSolution();
	
	/**
	 * Prints the solution into a file.
	 */
	private void printSolution(int te[][]) {
		try
	     {
	          FileOutputStream bestSolution = new FileOutputStream("bestSolution.txt");
	          PrintStream write = new PrintStream(bestSolution);
	          
	          for(int j = 0; j < instance.getE()+1; ++j) {
	        	  if(j == 0)
	        		  write.print("\t");
	  			  write.print("E" + j + "\t"); 
	  		  }
	          
	          write.println();
	          
	          for(int i = 0; i < instance.getTmax(); ++i) {
	        	  	write.print("T" + (i+1) + "\t");
					for(int j = 0; j < instance.getE(); ++j) {
						write.print(te[i][j] + "\t"); 
					}
					write.println();
	          }
	      }
	      catch (IOException e)
	      {
	          System.out.println("Errore: " + e);
	          System.exit(1);
	      }
	}

	/**
	 * Stopping condition used in the optimization
	 * problem, called by a timer.
	 */
	public static void stopExecution() {
		TIMER_EXPIRED = true;
	}
}