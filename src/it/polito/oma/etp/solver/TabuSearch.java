package it.polito.oma.etp.solver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.Random;

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
		int[][] te = null;
		// Get a first rough feasible solution.
		do {
			te = initializeTE();
		}while(te == null);
		
		
		currentSolution = new Solution(instanceData, te);
		// For now this is our best solution
		bestSolution = new Solution(currentSolution);
		
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
	 * @return A feasible solution using a set of random values. Can return null if
	 * no feasible solution is found with that set of random values.
	 */
	private static int[][] initializeTE() {
	
		/*All non-conflicting exams, starting from e1, will be placed in t1;
		 then the other ones (conflicting with exams in t1) will be placed in t2
		 and so on. */
				
		int examnumber = idata.getE();
		int N[][] = idata.getN();
		int te[][] = new int [idata.getTmax()][examnumber];
		
		// boolean array that tells me if a given exam was already assigned in a timeslot.
		int assignedExams[] = new int[examnumber];

		// first step: put e0 in t0
		te[0][0] = 1;
		assignedExams[0] = 1;
		
		// cycling through all exams
		for(int exam = 1; exam < examnumber; exam++) {
			
			/* Every exam will start to look for a timeslot to be assigned randomly. 
			 * rand is the random number picked every time, range [0 - Tmax). */
			int rand = new Random().nextInt(idata.getTmax());
			
			
			/* cycling through all timeslots, starting from a random one until the last one.
			 * The other timeslots (from 0 to rand-1) will be checked only if a slot will not
			 * be able to be found in the range rand - Tmax. (This for every exam). */
			for(int timeslot = rand; timeslot < idata.getTmax(); timeslot++) {
				
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
	 * Checks if current solution is feasible and if it is better than the best one;
	 * in this case it becomes the new best solution.
	 */
	private static void checkSolution() {
		
		
		//updateSolution();
	}
	
	/**
	 * Updates the best solution with the current one.
	 */
	private static void updateSolution(int te[][]) {
		try
	     {
	          FileOutputStream bestSolution = new FileOutputStream("bestSolution.txt");
	          PrintStream write = new PrintStream(bestSolution);
	          
	          for(int j = 0; j < idata.getE()+1; ++j) {
	        	  if(j == 0)
	        		  write.print("\t");
	  			  write.print("E" + j + "\t"); 
	  		  }
	          
	          write.println();
	          
	          for(int i = 0; i < idata.getTmax(); ++i) {
	        	  	write.print("T" + (i+1) + "\t");
					for(int j = 0; j < idata.getE(); ++j) {
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