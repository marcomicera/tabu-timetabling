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

public class TabuSearch {
	private InstanceData instance;	
	private Solution currentSolution;
	private Solution bestSolution;
	private int iteration = 0;
	private TabuList tabuList = new TabuList();
	
	public TabuSearch() {
	}
	
	/**
	 * Solves a given instance of the problem.
	 */
	public void solve(InstanceData instanceData) {
		instance = instanceData;
		
		// Get a first rough feasible solution.
		int[][] te = InitializeSolution.getFeasibleSolution(instanceData);
		
		/*
		do {
			te = initializeTE();
		} while(te == null);
		*/
		currentSolution = new Solution(instanceData, te);
		
		// For now this is our best solution
		bestSolution = new Solution(currentSolution);
		
		// TODO timeout that expires in the given available times
		//while(timeout) {
		
		for (int i = 0; i < 30; i++) {
			
			ExamPair mostPenalizingPair = currentSolution.getMostPenalizingPair();
			
			// Performing the best possible move for the most penalizing exam pair
			try {
				ArrayList<Neighbor> neighborhood = getNeighborhood(mostPenalizingPair);
				
				if(!neighborhood.isEmpty())
					move(neighborhood);
				else {
					ArrayList<ExamPair> penalizingPairs = currentSolution.getConflictCoefficients();
					
					Collections.sort(penalizingPairs);
					
					/*TODO debug*/System.out.println(penalizingPairs);
				}
				
				
				/*TODO debug*/System.out.println("\n");
				
				iteration += 1;
			} catch (InvalidMoveException e) {
				// TODO try... figure out what
			}
		}
		
		//}
		
		//TODO remember to output solution on file 
	}
	
	/**
	 * Creates a first feasible solution.
	 * @return	a feasible solution using a set of random values. Can return null if
	 * no feasible solution is found with that set of random values.
	 */
	private int[][] initializeTE() {
		/*All non-conflicting exams, starting from e1, will be placed in t1;
		 then the other ones (conflicting with exams in t1) will be placed in t2
		 and so on. */
				
		int examnumber = instance.getE();
		int N[][] = instance.getN();
		int te[][] = new int [instance.getTmax()][examnumber];
		
		// boolean array that tells me if a given exam was already assigned in a timeslot.
		int assignedExams[] = new int[examnumber];

		// first step: put e0 in t0
		te[0][0] = 1;
		assignedExams[0] = 1;
		
		// cycling through all exams
		for(int exam = 1; exam < examnumber; exam++) {
			
			/* Every exam will start to look for a timeslot to be assigned randomly. 
			 * rand is the random number picked every time, range [0 - Tmax). */
			int rand = new Random().nextInt(instance.getTmax());
			
			
			/* cycling through all timeslots, starting from a random one until the last one.
			 * The other timeslots (from 0 to rand-1) will be checked only if a slot will not
			 * be able to be found in the range rand - Tmax. (This for every exam). */
			for(int timeslot = rand; timeslot < instance.getTmax(); timeslot++) {
				
				// Check if this exam was already assigned
				if (assignedExams[exam] == 1)
					break;
					
				// checking exams in conflict
				boolean conflict = false;
				
				for(int conflictualExam = 0; conflictualExam < examnumber; conflictualExam++) {
					// are exam and conflictualExam in conflict? If not, search next exam for a conflict.
					if (N[exam][conflictualExam] != 0) {
						/* Here we know exam and conflictualExam are in conflict. I'd like to put exam in
						 * timeslot, but first I check if conflictualExam is already in timeslot.
						 * If it is there, I need to change timeslot, otherwise i look the next conflictualExam. */
						if(te[timeslot][conflictualExam] == 1) {
							if(exam == examnumber - 1)
								//System.out.println((exam+1) + " - " + (conflictualExam+1));
							conflict = true;
							break;
						}						
					}
				} // END FOR conflictualExam
				
				/* We are here for 2 motivations:
				 * 1. we checked all conflictualExams and no one is in timeslot (conflict = false) -> write in timeslot.
				 * 2. we found that a conflictualExam is in timeslot (conflict = true) -> look next timeslot */
				if(conflict == false) {
					te[timeslot][exam] = 1;
					// This exam is assigned, do not assign it again.
					assignedExams[exam] = 1;
				}
		
			} // END FOR timeslot (increasing slots)
			
			
			/* If the previous for cycle couldn't find a slot for -exam-, we check the decreasing
			 * timeslots (from rand-1 to 0) */
			if(assignedExams[exam] == 0) {
				
				// cycling through all timeslots, starting from the random one -1 until the first one.
				for(int timeslot = rand - 1; timeslot >= 0; timeslot--) {
					
					// Check if this exam was already assigned
					if (assignedExams[exam] == 1)
						break;
						
					// checking exams in conflict
					boolean conflict = false;
	
					for(int conflictualExam = 0; conflictualExam < examnumber; conflictualExam++) {
						// are exam and conflictualExam in conflict? If not, search next exam for a conflict.
						if (N[exam][conflictualExam] != 0) {
							/* Here we know exam and conflictualExam are in conflict. I'd like to put exam in
							 * timeslot, but first I check if conflictualExam is already in timeslot.
							 * If it is there, I need to change timeslot, otherwise i look the next conflictualExam. */
							if(te[timeslot][conflictualExam] == 1) {
								if(exam == examnumber - 1)
									//System.out.println((exam+1) + " - " + (conflictualExam+1));
								conflict = true;
								break;
							}						
						}
					} // END FOR conflictualExam
					
					/* We are here for 2 motivations:
					 * 1. we checked all conflictualExams and no one is in timeslot (conflict = false) -> write in timeslot.
					 * 2. we found that a conflictualExam is in timeslot (conflict = true) -> look next timeslot */
					if(conflict == false) {
						te[timeslot][exam] = 1;
						// This exam is assigned, do not assign it again.
						assignedExams[exam] = 1;
					}
			
				} // END FOR timeslot (decreasing slots)
			}
			
			/* If at the end of the timeslots checking, the exam can't still be assigned, a null-pointer  
			 * is given to the solution -te- and the program breaks out of the loop (meaning that with that set
			 * of random values a feasible solution can't be found). The program will be able later to look
			 * for another solution with a different set of random values. */
			if (assignedExams[exam] == 0) {
				te = null;
				break;
			}
			
		} // END FOR exam
		
		return te;
	}
	
	/**
	 * Returns a neighborhood given an exam to be rescheduled.
	 * @param movingExam	the exam to be rescheduled index.
	 * @param lowestIndex	the lowest timeslot index of the exam pair.
	 * @param highestIndex	the highest timeslot index of the exam pair.
	 * @return				the corresponding neighborhood.
	 */
	private ArrayList<Neighbor> examNeighborhood(int movingExam, int lowestIndex, int highestIndex) {
		/*	Best fitness initialization: it is set to plus infinity so it will be overwritten 
		during the first comparison */
		ArrayList<Neighbor> neighborhood = new ArrayList<Neighbor>();
		
		// For all possible timeslots
		for(int newTimeslot = 0; newTimeslot < instance.getTmax(); ++newTimeslot) {
			if(	/*	Skipping timeslots between the two exams: these positions will
					increase the fitness value for sure */
				newTimeslot < lowestIndex || newTimeslot > highestIndex
			) {
				try {
					// Retrieving the neighbor object, containing its fitness and its corresponding schedule
					Neighbor neighbor = currentSolution.getNeighbor(movingExam, newTimeslot);
					
					// Adding the new feasible neighbor to the neighborhood set
					neighborhood.add(neighbor);
				} catch(InvalidMoveException e) {
					continue;
				}
			}
		}
		
		return neighborhood;
	}
	
	/**
	 * Given a penalizing pair of exams, returns the corresponding neighborhood, i.e. the
	 * set of all possible feasible moves.
	 * @param examPair	exam pair to be rescheduled.
	 * @return			A list of Neighbor objects containing:
	 * 					1)	the exam that should be rescheduled
	 * 					2)	the new timeslot in which the exam should be rescheduled in
	 * 					3)	the corresponding fitness value
	 * 					4)	the corresponding schedule
	 * 					The last two points are returned to avoid the recalculation of this data when
	 * 					updating the currentSolution object.
	 * 					Return null if no feasible neighbor is found.
	 */
	private ArrayList<Neighbor> getNeighborhood(ExamPair examPair) {
		/*		Schedule
		 * 		 _________________________________________________________________________
		 * 		|____.:iXXj:._____________________________________________________________|
		 * 		 
		 * 		i and j are the two involved exams, scheduled 3 timeslots apart
		 * 		Xs are timeslots to be avoided for the next move, as they will worse the solution for sure
		 * 		: are timeslots that still cause a penalty (4 timeslots apart)
		 * 		. are timeslots that still cause a penalty (5 timeslots apart) 
		*/
		
		// Instance variables
		int Tmax = instance.getTmax();
		
		// Retrieving the timeslots in which the two exams have been scheduled
		int firstExamSlot = currentSolution.getTimeslot(examPair.getExam1());
		int secondExamSlot = currentSolution.getTimeslot(examPair.getExam2());
		/*TODO debug*/System.out.println("firstExamSlot = " + firstExamSlot + "\nsecondExamSlot = " + secondExamSlot);
		
		// The exam to be rescheduled will be the one closer to the schedule's center
		int firstMovingExamChoice = ((Math.abs(firstExamSlot - Tmax/2)) < (Math.abs(secondExamSlot - Tmax/2))) ? 
										examPair.getExam1() : examPair.getExam2();
		/*TODO debug*/System.out.println("movingExam's index: " + firstMovingExamChoice);
		/*TODO debug*/System.out.println("movingExam's timeslot: " + currentSolution.getTimeslot(firstMovingExamChoice));
		
		// Computing support variables for excluding timeslots between the two exams 
		int lowestIndex = (firstExamSlot < secondExamSlot) ? firstExamSlot : secondExamSlot;
		int highestIndex = (firstExamSlot < secondExamSlot) ? secondExamSlot : firstExamSlot;
		/*TODO debug*/System.out.println("lowestIndex = " + lowestIndex + "\nhighestIndex = " + highestIndex);
		
		// Retrieving the neighborhood corresponding to the first moving exam choice
		ArrayList<Neighbor> neighborhood = examNeighborhood(firstMovingExamChoice, lowestIndex, highestIndex);
		
		// If the first moving exam choice produces no neighborhood
		if(neighborhood.isEmpty()) {
			// Computing the second moving exam choice index
			int secondMovingExamChoice = (firstMovingExamChoice == examPair.getExam1()) ? 
										examPair.getExam2() : examPair.getExam1();
										
			// Retrieving the neighborhood corresponding to the other moving exam choice
			neighborhood = examNeighborhood(secondMovingExamChoice, lowestIndex, highestIndex);
		}
		
		if(!neighborhood.isEmpty())
			// Ordering the neighborhood by increasing fitness value
			Collections.sort(neighborhood);
		
		/*TODO debug*/System.out.println(neighborhood);
		
		return neighborhood;
	}
	
	/**
	 * Tries to perform the best move among the given neighborhood passed as first argument, if possible.
	 * @param neighborhood				the neighborhood to be explored by the Tabu search algorithm
	 * @throws InvalidMoveException 	// TODO when?
	 */
	private void move(ArrayList<Neighbor> neighborhood) throws InvalidMoveException {
		if(neighborhood.isEmpty()) {
			/*TODO debug*/System.out.println("Empty neighborhood");
			
			/**
			 * We have a few options here:
			 * 
			 * 1) Trying to move the second most penalizing exam pair, changing all our data structures about that
			 * 2) Resetting the whole Tabu list
			 */
			
			return;
		} else {
			Neighbor bestNeighbor = neighborhood.get(0);
			
			// This move is in the Tabu List?
			if(tabuList.find(bestNeighbor) != -1) {
				// TODO aspiration criteria
				
				/**
				 * TODO findBestNeighbor shouldn't return this neighbor during the next iteration.
				 * This could be done by letting the findBestNeighbor function return a list
				 * of Neighbor objects (the 'neighborhood') and trying all of them if necessary. 
				 */
				
				throw new InvalidMoveException("Move in Tabu List");
			}
			
			// Retrieving current solution's infos before performing the move
			int movingExam = bestNeighbor.getMovingExam();
			int oldTimeslot = currentSolution.getTimeslot(movingExam);
			/*TODO debug*/System.out.println("movingExam's index inside move(): " + movingExam);
			/*TODO debug*/System.out.println("old fitness = " + currentSolution.getFitness());
			
			// Inserting this move in the Tabu List
			tabuList.add(
				new Neighbor(
					movingExam,
					oldTimeslot,
					currentSolution.getFitness() // TODO what should we put here?
				)
			);
			
			//TODO debug
			System.out.print("the tabu list is "+ tabuList.toString());
			
			updateSolution(movingExam, oldTimeslot, bestNeighbor);
		}
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
	
	
}