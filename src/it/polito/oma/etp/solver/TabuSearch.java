package it.polito.oma.etp.solver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Random;

import it.polito.oma.etp.reader.InstanceData;

public class TabuSearch {
	private InstanceData instance;	
	// private TabuListEntry tlentry;
	private Solution currentSolution;
	private Solution bestSolution;
	private int iteration;
	
	public TabuSearch() {
	}
	
	/**
	 * Solves a given instance of the problem.
	 */
	public void solve(InstanceData instanceData) {
		instance = instanceData;
		int[][] te = null;
		// Get a first rough feasible solution.
		do {
			te = initializeTE();
		} while(te == null);
		
		currentSolution = new Solution(instanceData, te);
		
		// For now this is our best solution
		bestSolution = new Solution(currentSolution);
		
		// TODO timeout that expires in the given available times
		//while(timeout) {
			Entry<ExamPair, Float> mostPenalizingPair = currentSolution.getMostPenalizingPair();
			
			// Performing the best possible move for the most penalizing exam pair
			move(findBestNeighbor(mostPenalizingPair.getKey()));
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
	 * Given a penalizing pair of exams, returns the best possible move to do.
	 * @param examPair	exam pair to be rescheduled.
	 * @return			A Neighbor object containing:
	 * 					1)	the exam that should be rescheduled
	 * 					2)	the new timeslot in which the exam should be rescheduled in
	 * 					3)	the corresponding fitness value
	 * 					4)	the corresponding schedule
	 * 					The last two points are returned to avoid the recalculation of this data when
	 * 					updating the currentSolution object.
	 */
	private Neighbor findBestNeighbor(ExamPair examPair) {
		/*		Schedule
		 * 		 ____________________________________________________________________________________
		 * 		|____.:iXXj:.________________________________________________________________________|
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
		int movingExam = ((Math.abs(firstExamSlot - Tmax/2)) < (Math.abs(secondExamSlot - Tmax/2))) ? 
							examPair.getExam1() : examPair.getExam2();
		/*TODO debug*/System.out.println("movingExam's index: " + movingExam);
		/*TODO debug*/System.out.println("movingExam's timeslot: " + currentSolution.getTimeslot(movingExam));
		
		// Computing support variables for excluding timeslots between the two exams 
		int lowestIndex = (firstExamSlot < secondExamSlot) ? firstExamSlot : secondExamSlot;
		int highestIndex = (firstExamSlot < secondExamSlot) ? secondExamSlot : firstExamSlot;
		/*TODO debug*/System.out.println("lowestIndex = " + lowestIndex + "\nhighestIndex = " + highestIndex);
		
		/*	Best fitness initialization: it is set to plus infinity so it will be overwritten 
			during the first comparison */
		Neighbor bestNeighbor = new Neighbor();
		float bestNeighborFitness = Float.MAX_VALUE;
		int bestMove[] = null;
							
		// For all possible timeslots
		for(int newTimeslot = 0; newTimeslot < Tmax; ++newTimeslot) {
			if(	// The Tabu list allows this move
				// TODO implement tabu list check
					
				/*	Skipping timeslots between the two exams: these positions will
					increase the fitness value for sure */
				newTimeslot < lowestIndex || newTimeslot > highestIndex
			) {
				try {
					// Retrieving the neighbor object, containing its fitness and its corresponding schedule
					Neighbor neighbor = currentSolution.getNeighbor(movingExam, newTimeslot);
					float neighborFitness = neighbor.getFitness();
					
					// If a new better neighbor is found
					if(neighborFitness < bestNeighborFitness) {
						bestNeighbor.update(neighbor);
						bestNeighborFitness = neighborFitness;
						bestMove = new int[]{movingExam, newTimeslot};
					}
					
					/*TODO debug*/System.out.println(neighbor);
				} catch(InvalidMoveException e) {
					/*TODO debug*/System.out.println("conflict!");
					continue;
				}
			}
		}
		
		/*TODO debug*/System.out.println("\n\nbestNeighbor is = " + bestNeighbor);
		
		return bestNeighbor;
	}
	
	/**
	 * Performs the given move passed as first argument.
	 * @param neighbor	the neighbor to be reached by the Tabu search algorithm
	 */
	private void move(Neighbor neighbor) {
		if(neighbor == null) {
			// TODO think and implement
			/**
			 * We have a few options here:
			 * 
			 * 1) We could try to move the other exam (the one closer to the beginning/ending of the schedule array)
			 * 2) Trying to move the second most penalizing exam pair, changing all our data structures about that
			 * 3) Resetting the whole Tabu list
			 */
			
			return;
		}
		
		int movingExam = neighbor.getMovingExam();
		/*TODO debug*/System.out.println("movingExam's index inside move(): " + movingExam);
		
		// Updating the current solution
		int oldTimeslot = currentSolution.getTimeslot(movingExam);
		currentSolution.updateTe(movingExam, oldTimeslot, neighbor.getNewTimeslot());
		currentSolution.setFitness(neighbor.getFitness());
		currentSolution.updateSchedule();
		currentSolution.updateDistanceMatrix();
		
		/*TODO debug*/System.out.println("oldTimeslot = " + oldTimeslot);
		/*TODO debug*/currentSolution.updateFitness();
		/*TODO debug*/System.out.println("Calculating the fitness from scratch: " + currentSolution.getFitness());
	}
	
	/**
	 * Checks if current solution is feasible and if it is better than the best one;
	 * in this case it becomes the new best solution.
	 */
	private void checkSolution() {
		
		
		//updateSolution();
	}
	
	/**
	 * Updates the best solution with the current one.
	 */
	private void updateSolution(int te[][]) {
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