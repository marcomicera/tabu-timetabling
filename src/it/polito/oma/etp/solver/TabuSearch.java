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
		// Get a first rough feasible solution.
		int[][] te = initializeTE();
		
		currentSolution = new Solution(instanceData, te);
		// For now this is our best solution
		bestSolution = currentSolution;
		
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
	private static int[][] initializeTE() {
	
		/*All non-conflicting exams, starting from e1, will be placed in t1;
		 then the other ones (conflicting with exams in t1) will be placed in t2
		 and so on. */
		
		
		int examnumber = idata.getE();
		int N[][] = idata.getN();
		int te[][] = new int [idata.getTmax()][examnumber];
		
		// array that tells me if a given exam was already assigned in a timeslot.
		int assignedExams[] = new int[examnumber];
		// counter that stores the number of exams assigned
		int counter = 1;
		// first step: put e0 in t0
		te[0][0] = 1;
		assignedExams[0] = 1;
		
		// cycling through all exams
		for(int exam = 1; exam < examnumber; exam++) {
			
			// cycling through all timeslots
			for(int timeslot = 0; timeslot < idata.getTmax(); timeslot++) {
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
					counter++;
				}
		
			} // END FOR timeslot		
		} // END FOR exam
		
		// Check if all exams were assigned, if not it means the solution wasn't found
		if(counter != examnumber) {
			System.err.println("Couldn't find any feasible solution");
			System.exit(1);
		}
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
	private static void updateSolution() {
		
	}
	
	
}