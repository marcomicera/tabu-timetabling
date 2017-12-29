package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InstanceData;

public class InitializeSolution {
	
	private static InstanceData idata;
	private static TabuList tabulist;
	/** Boolean matrix whose elements ij are set to 1 if exam i and exam j
	 * are conflictual and in the same timeslot */
	private static int[][] U;
	/** Sum of all the conflictual couple of exams assigned to the same timeslot */
	private static int fitness;
	private static int[][] te;
	/** Counter to scan the neighborhood without choosing the same neighbor twice */
	private static int check;
	private static boolean moved;
	
	/**
	 * Given a set of data relative to an instance of the problem, it returns back a first
	 * rough feasible solution obtained using the Tabu Search algorithm.
	 * @param instanceData instance of the problem
	 * @return A feasible solution of the instance in the form of the matrix TE
	 */
	public static int[][] getFeasibleSolution(InstanceData instanceData){
		long startTime = System.currentTimeMillis();
		idata = instanceData;
		int E = idata.getE();
		U = new int[E][E];
		
		InitializationSolution currentSolution = generateUnfeasibleTE();
		
		//te = generateUnfeasibleTE();
		moved = false;
		tabulist = new TabuList();
		
		//fitness = updateFitness(U);

		check = 0;
		while(currentSolution.getFitness() > 0) {
			if(check == E) check = 0;
			
			ExamPair pair = getUnfeaseblePair(check, currentSolution);
			
			if(pair != null) {
				
				UnfeasibilityNeighbor bestNeighbor = getNeighbor(currentSolution, pair); 
				currentSolution = move(currentSolution, bestNeighbor);
			}	
			else {
				System.out.println("Something wrong: fitness > 0 but no unfeasible pair found!");
				//System.exit(1);
			}
			check++;
		}
		
		
		/*TODO check to not take always the same pair of exams*/
		//UnfeasibilityNeighbor bestNeighbor = null;
		/*
		check = 0;
		do {	
			if (check == E) {
				check = 0;
				if(!moved) {
					System.out.println("\t\t\tMove: "+ bestNeighbor);
					move(bestNeighbor, 1);
					
				}
				moved = false;
			} 
			// get the exam pair to edit
			ExamPair unfeaseablePair = getUnfeaseblePair(check, currentSolution);
			// find best neighbor for the pair
			if(unfeaseablePair!=null) {
				bestNeighbor = getNeighbor(unfeaseablePair);
				move(bestNeighbor, 0);
			}
			// TODO debug
			else {
				
				//System.out.println("the solution is already feasible without applying the heuristic");
				//System.exit(1);
			}
			check++;
			
		}
		while(fitness > 0);*/
		long stopTime = System.currentTimeMillis();
		/*TODO debug*/System.out.println("\n" + instanceData.getInstanceName() + ": ");
		/*TODO debug*/System.out.println("elapsed time to make the first solution feasible: " + (stopTime - startTime)/1000 + " seconds");
		/*TODO debug*/System.out.println("Tabu list size: " + tabulist.getMAX_SIZE() + " elements");
		System.exit(1);
		return te;
	}
	
	/**
	 * An exam will be placed in the timeslot having the most non-conflictual exams in it. So there is a call
	 * to a function that calculates which is the fullest timeslot. When an exam will not be able to find 
	 * a timeslot, an unfeasibility is introduced and it is assigned to the timeslot with less conflictual
	 * exams in it. The field U is updated accordingly.
	 * @return An unfeasible solution for the given instance (having conflictual exams
	 * in the same timeslot).
	 */
	private static InitializationSolution generateUnfeasibleTE(){
						
		int examnumber = idata.getE();
		int N[][] = idata.getN();
		int tmax = idata.getTmax();
		int te[][] = new int [tmax][examnumber];
		int U[][] = new int[examnumber][examnumber];
		
		// array that counts how much exams are assigned in every timeslot
		int texamsCounter[] = new int[tmax];
		// array that stores which is the timeslot to visit first, according to its assigned exams
		int timeslotOrder[] = new int[tmax];
		// boolean array that tells me if a given exam was already assigned in a timeslot.
		int assignedExams[] = new int[examnumber];

		// first step: put e0 in t0
		te[0][0] = 1;
		assignedExams[0] = 1;
		texamsCounter[0]++;
		
		// cycling through all exams
		for(int exam = 1; exam < examnumber; exam++) {
			
			/* IN: texamsCounter, OUT: timeslotOrder*
			 * Given in input the number of exams in every timeslot, it generates the timeslot ordering. */
			timeslotOrder = getTimeslotOrder(texamsCounter);
			
			// cycling through all timeslots
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
					
			/* If at the end of the timeslots checking, the exam can't still be assigned, we introuduce infeasibility.
			 * We assign exam to the timeslot with less conflictual exams and update U accordingly. */
			if (assignedExams[exam] == 0) {
				
				// number of conflicts for each timeslot
				int[] numberOfConflicts = new int[tmax];
				
				// cycling timeslots, counting the number of conflicts
				for(int t = 0; t < tmax; t++) {
					numberOfConflicts[t] = 0;
					
					for(int confExam = 0; confExam < examnumber; confExam++) {
						
						// are exam and confExam in conflict?
						if(N[exam][confExam] != 0) {
							// yes; is confExam in timeslot t? Increase the number of conflicts for that timeslot
							if(te[t][confExam] == 1) {
								numberOfConflicts[t]++;
							}		
						}
					}// end FOR confExam
					
				}// end FOR timeslots
				
				int minConflicts = examnumber;
				int myTimeslot = 0;
				// searching for the timeslot with the minimum of conflict for the given exam
				for(int t = 0; t < numberOfConflicts.length; t++) {
					if(numberOfConflicts[t] < minConflicts) {
						minConflicts = numberOfConflicts[t];
						myTimeslot = t;
					}
				}
				
				/* Now the exam is placed in the timeslot myTimeslot and the relative U elements are set at 1, remembering it is
				 * introducing an unfeasibility in the solution.*/
				te[myTimeslot][exam] = 1;
				assignedExams[exam] = 1;
				texamsCounter[myTimeslot]++;
				// cycling through exams in myTimeslot
				for(int e = 0; e < examnumber; e++) {
					// looks only allocated exams
					if(te[myTimeslot][e] == 1) {
						// and check if they are conflictual with exam
						if(N[exam][e] != 0) {
							U[exam][e] = 1;
							U[e][exam] = 1;
						}
					}
				}
				
			}// end IF exam cannot be placed
			
		} // END FOR exam
		
		return new InitializationSolution(idata, te, U);
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
		
		int tec[] = new int[TEcounter.length];
		int orderTec[] = new int[idata.getTmax()];
		int max = -1;
		int ind = 0;
		for(int i = 0; i < tec.length; i++)
			tec[i] = TEcounter[i];
		
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
		return orderTec;
	}
	
	/**
	 * @return The first pair of conflictual exams that are assigned to the same timeslot 
	 */
	private static ExamPair getUnfeaseblePair(int c, InitializationSolution s) {
		ExamPair pair = null;
		InstanceData idata = s.getInstanceData();
		int[][] U = Utility.cloneMatrix(s.getU());
		
		for(int i = c; i < idata.getE(); i++) {
			for(int j = 0; j < idata.getE(); j++) {
				if(U[i][j] == 1) {

					pair = new ExamPair(i, j);
					check = i;
					break;
				}	
			}	
			if(pair != null)
				break;
		}
		
		return pair;
	}
	
	/**
	 * @param unfeasbilityM the unfeasibility matrix used for the calculation
	 * @return the updated value of the fitness
	 */
	private static int updateFitness(int[][] unfeasbilityM) {
		int fitnessV = 0;
		int E = idata.getE();
		for(int i = 0; i < E; i++) {
			for(int j = i + 1; j < E; j++) {
				fitnessV += unfeasbilityM[i][j];
				/*TODO debug if(unfeasbilityM[i][j] == 1)*/
				/*TODO debug System.out.println("unfeasbilityM[i][j] = " + unfeasbilityM[i][j] + " i, j = " + i + ", " + j);*/
			}
		}
		/*TODO debug*/ //System.out.println("final fitness: " + fitnessV);
		return fitnessV;
	}
	
	/**
	 * 
	 * @param exPair
	 * @return
	 */
	private static UnfeasibilityNeighbor getNeighbor(InitializationSolution s, ExamPair exPair) {
		int E = s.getInstanceData().getE();
		int tmax = s.getInstanceData().getTmax();
		int[][] N = s.getInstanceData().getN();
		int[][] te = Utility.cloneMatrix(s.getTE());
		int[][] U = Utility.cloneMatrix(s.getU());
		
		int exam1 = exPair.getExam1();
		int exam2 = exPair.getExam2();
				
		// timeslot where are now assigned exam1 and exam2
		int t_old = 0;
		// looking for the timeslot where exam1 is placed now
		for(int ts = 0; ts < tmax; ts++) {
			if(te[ts][exam1] == 1)
				t_old = ts;
		}
		
		int[][] te_copy = new int[E][E];
		int[][] U_copy = new int[E][E];
		int fitness_copy = 0;
		int fitness_min = Integer.MAX_VALUE;
		int timeslot_min = 0;
		int[][] U_min = new int[E][E];
		
		
		// cycling timeslots
		for(int t = 0; t < tmax; t++) {
			
			if(!tabulist.contains(exam1, t) && t != t_old) {
				te_copy = Utility.cloneMatrix(te);
				U_copy = Utility.cloneMatrix(U);
				
				// I put exam1 in t
				U_copy[exam1][exam2] = 0; 
				U_copy[exam2][exam1] = 0;
				te_copy[t_old][exam1] = 0;
				te_copy[t][exam1] = 1;
				
				// cycling all the exams e in the current timeslot t
				for(int e = 0; e < E; e++) {
					// if exam1 and e are conflictual and e is in t
					if(N[exam1][e] != 0 && te_copy[t][e] == 1) {
							U_copy[e][exam1] = 1;
							U_copy[exam1][e] = 1;
					}
				}
				//TODO do not calculate this fitness from zero!!
				fitness_copy = updateFitness(U_copy);
				
				// it remebers just the best timeslot in the neighborhood
				if(fitness_min >= fitness_copy) {
					fitness_min = fitness_copy;
					timeslot_min = t;
					U_min = Utility.cloneMatrix(U_copy);
				}
			}
		}// end FOR timeslots
				
		//TODO Insert same procedure for exam2
		
		UnfeasibilityNeighbor neighbor = new UnfeasibilityNeighbor(exam1, timeslot_min, fitness_min, t_old, U_min);
		
		return neighbor;		
	}
	
	/**
	 * Move the exam selected by the getNeighbor method
	 * @param n neighbor
	 */
	private static InitializationSolution move(InitializationSolution currentS, UnfeasibilityNeighbor n) {
		int exam = n.getMovingExam();
		int[][] te = Utility.cloneMatrix(currentS.getTE());	
		int[][] U = Utility.cloneMatrix(n.getU());
		
		te[n.getOldTimeslot()][exam] = 0;
		te[n.getNewTimeslot()][exam] = 1;
		
	
		InitializationSolution s = new InitializationSolution(currentS.getInstanceData(), te, U);
		System.out.println(s.getFitness());
		//int exam = n.getMovingExam();
		//moved = true;
		//fitness = (int)n.getFitness();
		//te[n.getOldTimeslot()][exam] = 0;
		//te[n.getNewTimeslot()][exam] = 1;
		//U = Utility.cloneMatrix(n.getU());
		
		tabulist.add(new Neighbor(exam, n.getOldTimeslot(), s.getFitness())); // TODO what should we put in the last arg?
		
		System.out.println(tabulist.getLastEntry());
		
		return s;
	}
}
