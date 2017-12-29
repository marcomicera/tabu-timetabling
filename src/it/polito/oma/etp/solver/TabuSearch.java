package it.polito.oma.etp.solver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import it.polito.oma.etp.reader.InstanceData;

public abstract class TabuSearch {
	protected InstanceData instance;	
	protected Solution currentSolution;
	protected Solution bestSolution;
	protected int iteration = 0;
	protected TabuList tabuList = new TabuList();
	
	public TabuSearch() {
	}
	
	/**
	 * Starts the Tabu Search algorithm starting from the given initial
	 * solution, using the given instance data.
	 * @param instanceData		data describing the problem instance.
	 * @param initialSolution	initial solution from which the Tabu Search
	 * 							algorithm starts.
	 */
	public void solve(InstanceData instanceData) {
		instance = instanceData;
		
		int iteration = 1;
		
		while(true) {
			/*TODO debug*/System.out.println("\n***** Iteration " + iteration + " *****");
			
			// TODO implement it on InitializationSolution.java
			ExamPair mostPenalizingPair = currentSolution.getNeighborhoodGeneratingPair();
			
			ArrayList<Neighbor> neighborhood = getNeighborhood(mostPenalizingPair);
			
			Neighbor validNeighbor = checkTabuList(neighborhood);
			
			if(validNeighbor == null) {
				/*TODO debug*/System.out.println("The most penalizing exam pair has no valid neighbor");
				ArrayList<ExamPair> penalizingPairs = currentSolution.getConflictCoefficients();
				Collections.sort(penalizingPairs);
				
				int counter = 1;
				ExamPair nextPair;
				while(validNeighbor == null) {
					nextPair = penalizingPairs.get(counter++);
					neighborhood = getNeighborhood(nextPair);
					validNeighbor = checkTabuList(neighborhood);
				}
			}
			
			if(validNeighbor != null)
				move(validNeighbor);
			else {
				/*TODO debug*/System.err.println("No valid neighbors found for any exam pair: should we reset the Tabu list?");
			}
			
			/*TODO debug*/System.out.println("\n");
		}
		
		//TODO remember to output solution on file 
	}
	
	/**
	 * Given a penalizing pair of exams, returns the corresponding neighborhood, i.e. the
	 * set of all possible feasible moves.
	 * @param examPair	exam pair to be rescheduled.
	 * @return			A list of Neighbor objects containing:
	 * 					1)	the exam that should be rescheduled
	 * 					2)	the new timeslot in which the exam should be rescheduled in
	 * 					3)	the corresponding fitness value
	 * 					Return null if no feasible neighbor is found.
	 */
	private ArrayList<Neighbor> getNeighborhood(ExamPair examPair) {
		// Retrieving the timeslots in which the two exams have been scheduled
		int firstExamSlot = currentSolution.getTimeslot(examPair.getExam1());
		int secondExamSlot = currentSolution.getTimeslot(examPair.getExam2());
		/*TODO debug*/ //System.out.println("firstExamSlot = " + firstExamSlot + "\nsecondExamSlot = " + secondExamSlot);
		
		// Computing support variables for excluding timeslots between the two exams 
		int lowestIndex = (firstExamSlot < secondExamSlot) ? firstExamSlot : secondExamSlot;
		int highestIndex = (firstExamSlot < secondExamSlot) ? secondExamSlot : firstExamSlot;
		/*TODO debug*/ //System.out.println("lowestIndex = " + lowestIndex + "\nhighestIndex = " + highestIndex);
		
		// Neighborhood corresponding to both exams
		ArrayList<Neighbor> neighborhood = new ArrayList<Neighbor>();
		
		// For all possible timeslots
		for(int newTimeslot = 0; newTimeslot < instance.getTmax(); ++newTimeslot) {
			if(	/*	Skipping timeslots between the two exams: these positions will
					increase the fitness value for sure */
				newTimeslot < lowestIndex || newTimeslot > highestIndex
			) {
				try {
					/**
					 *  Retrieving neighbor objects, containing its fitness and its corresponding schedule
					 *  and adding them to the neighborhood set
					 */
					neighborhood.add(currentSolution.getNeighbor(examPair.getExam1(), newTimeslot));
					neighborhood.add(currentSolution.getNeighbor(examPair.getExam2(), newTimeslot));
				} catch(InvalidMoveException e) {
					continue;
				}
			}
		}
		
		if(!neighborhood.isEmpty())
			// Ordering the neighborhood by increasing fitness value
			Collections.sort(neighborhood);
		
		/*TODO debug*/System.out.println("Neighborhood: " + neighborhood);
		
		return neighborhood;
	}
	
	/**
	 * Determines whether all neighbors belonging to the neighborhood passed
	 * as argument can be used for the next move or not. 
	 * @param neighborhood	the neighborhood to be checked with respected to
	 * 						the Tabu list.
	 * @return				the first acceptable neighbor that can be used 
	 * 						for the next move according to the Tabu list or
	 * 						null if no valid neighbor was found.
	 */
	private Neighbor checkTabuList(ArrayList<Neighbor> neighborhood) {
		if(neighborhood == null || neighborhood.isEmpty())
			return null;
		
		Neighbor validNeighbor = null;
		for(Neighbor neighbor: neighborhood) {
			// Is this move in the Tabu list?
			if(tabuList.find(neighbor) != -1) {
				// TODO aspiration criteria
				
				continue;
			}
			else {
				validNeighbor = neighbor;
				break;
			}
		}
			
		return validNeighbor;
	}
	
	/**
	 * TODO missing JavaDoc
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
		tabuList.add(
			new Neighbor(
				movingExam,
				oldTimeslot,
				currentSolution.getFitness() // TODO what should we put here?
			)
		);
		
		//TODO debug
		System.out.print("Tabu list: "+ tabuList);
		
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
		currentSolution.setFitness(neighbor.getFitness());
		currentSolution.updateSchedule(neighbor); 
		currentSolution.updateDistanceMatrix(); // TODO update only involved row and column
				
		/*TODO debug*/ //System.out.println("oldTimeslot = " + oldTimeslot);
		/*TODO debug*/currentSolution.updateFitness();
		/*TODO debug*/System.out.println("\nCalculating the fitness from scratch: " + currentSolution.getFitness());

		// TODO implement: check if the current solution is better then the optimal one
		
		currentSolution.updateConflictCoefficients();
	}
	
	/**
	 * Checks if current solution is feasible and if it is better than the best one;
	 * in this case it becomes the new best solution.
	 */
	private void checkSolution() {
		
		
		//updateSolution();
	}
	
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
	
	public Solution getSolution() {
		return bestSolution;
	}
}