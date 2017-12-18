package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InstanceData;

public class InitializeSolution {
	
	private static InstanceData idata;
	
	public static int[][] getFeasibleSolution(InstanceData instanceData){
		idata = instanceData;
		generateUnfeasibleTE();
		return null;
	}
	
	/**
	 * @return An unfeasible solution for the given instance (having conflictual exams
	 * in the same timeslot).
	 */
	private static int [][] generateUnfeasibleTE(){
		/*All non-conflicting exams, starting from e1, will be placed in t1;
		 then the other ones (conflicting with exams in t1) will be placed in t2
		 and so on. */
				
		int examnumber = idata.getE();
		int N[][] = idata.getN();
		int tmax = idata.getTmax();
		int te[][] = new int [tmax][examnumber];
		int texamsCounter[] = new int[tmax];
		int timeslotOrder[] = new int[tmax];
		
		// boolean array that tells me if a given exam was already assigned in a timeslot.
		int assignedExams[] = new int[examnumber];

		// first step: put e0 in t0
		te[0][0] = 1;
		assignedExams[0] = 1;
		texamsCounter[0]++;
		
		// cycling through all exams
		for(int exam = 1; exam < examnumber; exam++) {
			
			// IN: texamsCounter, OUT: timeslotOrder
			timeslotOrder = getTimeslotOrder(texamsCounter);
			
			/* cycling through all timeslots, starting from a random one until the last one.
			 * The other timeslots (from 0 to rand-1) will be checked only if a slot will not
			 * be able to be found in the range rand - Tmax. (This for every exam). */
			for(int t = 0; t < tmax; t++) {
				
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
						if(te[timeslotOrder[t]][conflictualExam] == 1) {
							conflict = true;
							break;
						}						
					}
				} // END FOR conflictualExam
				
				/* We are here for 2 motivations:
				 * 1. we checked all conflictualExams and no one is in timeslot (conflict = false) -> write in timeslot.
				 * 2. we found that a conflictualExam is in timeslot (conflict = true) -> look next timeslot */
				if(conflict == false) {
					te[timeslotOrder[t]][exam] = 1;
					// This exam is assigned, do not assign it again.
					assignedExams[exam] = 1;
					texamsCounter[timeslotOrder[t]]++;
				}
		
			} // END FOR timeslot (increasing slots)
			
			
						
			/* If at the end of the timeslots checking, the exam can't still be assigned, a null-pointer  
			 * is given to the solution -te- and the program breaks out of the loop (meaning that with that set
			 * of random values a feasible solution can't be found). The program will be able later to look
			 * for another solution with a different set of random values. */
			if (assignedExams[exam] == 0) {
				System.out.println("e" + exam /*+ " - " + ++failCounter*/);
				//updateSolution(te);
				System.exit(1);
				te = null;
				break;
			}
			
		} // END FOR exam
		
		return te;
	}
	
	/**
	 * Given an array containing the occurrencies of exams for every timeslot (index) it calculates 
	 * the timeslot with most exams. Then returns an array with an increasing number of exams in the
	 * following format:
	 * 3, 6, 12, 5, 1, 0 ...
	 * This means the algorithm will visit in order t3, t6, t12, --- 
	 * @param TEcounter
	 * @return
	 */
	private static int[] getTimeslotOrder(int[] TEcounter) {
		
		int tec[] = TEcounter.clone();
		//System.out.print("Number of exams in timeslot i:  ");
		/*for(int i = 0; i < idata.getTmax(); i++)
			System.out.print(tec[i] + ", ");
		System.out.println("");*/
		int orderTec[] = new int[idata.getTmax()];
		int max = -1;
		int ind = 0;
		
		// For every element of the Timeslot Order array
		for(int x = 0; x < idata.getTmax(); x++) {			
			// For every elemnt of the Exams in timeslot counter, find the max
			for(int i = 0; i < idata.getTmax(); i++) {
				
				if(tec[i] > max) { 
					max = tec[i];
					ind = i;
				}
			}
			max = -1;
			tec[ind] = -1;
			orderTec[x] = ind;
			
		}
		/*System.out.print("OUTPUT:  ");
		for(int i = 0; i < idata.getTmax(); i++)
			System.out.print(orderTec[i] + ", ");
		
		System.out.println("\n");*/
		
		return orderTec;
	}

}
