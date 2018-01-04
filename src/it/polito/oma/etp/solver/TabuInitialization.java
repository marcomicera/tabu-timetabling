package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;

import it.polito.oma.etp.reader.InstanceData;

public class TabuInitialization extends TabuSearch {
	/**
	 * Default constructor, creating a Tabu Search implementation for computing
	 * the first feasible solution.
	 * @param instanceData			The given problem instance data.
	 * @param settings				This Tabu Search implementation settings.
	 * @param firstRandomSolution	If true, the first infeasible solution is
	 * 								computed randomly; otherwise, our ad-hoc
	 * 								deterministic algorithm tries to obtain an
	 * 								initial solution with the minimum number of
	 * 								conflicts.
	 */
	public TabuInitialization(InstanceData instanceData, Settings settings) {
		super(instanceData, settings);
		
		// Initially, the current solution is the initial one
		currentSolution = generateUnfeasibleSolution();
		
		// By now this is our best solution
		bestSolution = new InitializationSolution(currentSolution);
	}

	private InitializationSolution generateUnfeasibleSolution() {
		/*TODO debug*/System.out.println("Generating infeasible solution...");
		
		// Instance data
		int E = instance.getE();
		int N[][] = instance.getN();
		int Tmax = instance.getTmax();
		
		// Infeasible solution fields
		int te[][] = new int[Tmax][E];
		int schedule[] = new int[E];
		float fitness = 0;
		ArrayList<ExamPair> penalizingPairs = new ArrayList<ExamPair>();
		
		// Array that counts how much exams are assigned in every timeslot
		int texamsCounter[] = new int[Tmax];
		
		// Array that stores which is the timeslot to visit first, according to its assigned exams
		int timeslotOrder[] = new int[Tmax];
		
		// Boolean array that tells me if a given exam was already assigned in a timeslot.
		int assignedExams[] = new int[E];

		// First exam e0
		if(!settings.firstRandomSolution) {
			// first step: put e0 in t0
			te[0][0] = 1;
			schedule[0] = 0;
			assignedExams[0] = 1;
			texamsCounter[0]++;
		}
		
		// cycling through all exams
		for(int exam = (settings.firstRandomSolution) ? 0 : 1; exam < E; exam++) {
			
			// First infeasible solution computed randomly
			if(settings.firstRandomSolution) {
				// Random timeslot index generation
				int randomTimeslot = java.util.concurrent.ThreadLocalRandom.current().nextInt(0, Tmax);
				
				// Exam assignment
				te[randomTimeslot][exam] = 1;
				schedule[exam] = randomTimeslot;
				
				// Fitness value calculation
				assignedExams[exam] = 1;
				texamsCounter[randomTimeslot]++;
				for(int e = 0; e < E; e++) {
					// looks only allocated exams
					if(te[randomTimeslot][e] == 1) {
						// and check if they are conflictual with exam
						if(N[exam][e] != 0) {
							penalizingPairs.add(new ExamPair(e, exam));
							++fitness;
						}
					}
				}
			}
			// Ad-hoc deterministic algorithm
			else {
				/* IN: texamsCounter, OUT: timeslotOrder*
				 * Given in input the number of exams in every timeslot, it generates the timeslot ordering. */
				timeslotOrder = getTimeslotOrder(texamsCounter);
				
				// cycling through all timeslots
				for(int t = 0; t < Tmax; t++) {
					
					// Check if this exam was already assigned
					if (assignedExams[exam] == 1)
						break;
						
					// checking exams in conflict
					boolean conflict = false;
					
					for(int conflictualExam = 0; conflictualExam < E; conflictualExam++) {
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
						schedule[exam] = timeslotOrder[t];
						// This exam is assigned, do not assign it again.
						assignedExams[exam] = 1;
						texamsCounter[timeslotOrder[t]]++;
					}		
				} // END FOR timeslot (increasing slots)
						
				/* If at the end of the timeslots checking, the exam can't still be assigned, we introuduce infeasibility.
				 * We assign exam to the timeslot with less conflictual exams and update U accordingly. */
				if (assignedExams[exam] == 0) {
					
					// number of conflicts for each timeslot
					int[] numberOfConflicts = new int[Tmax];
					
					// cycling timeslots, counting the number of conflicts
					for(int t = 0; t < Tmax; t++) {
						numberOfConflicts[t] = 0;
						
						for(int confExam = 0; confExam < E; confExam++) {
							
							// are exam and confExam in conflict?
							if(N[exam][confExam] > 0) {
								// yes; is confExam in timeslot t? Increase the number of conflicts for that timeslot
								if(te[t][confExam] == 1) {
									numberOfConflicts[t]++;
								}		
							}
						}// end FOR confExam
						
					}// end FOR timeslots
					
					int minConflicts = E;
					int myTimeslot = 0;
					// searching for the timeslot with the minimum of conflict for the given exam
					for(int t = 0; t < numberOfConflicts.length; t++) {
						if(numberOfConflicts[t] < minConflicts) {
							minConflicts = numberOfConflicts[t];
							myTimeslot = t;
						}
					}
					
					/* Now the exam is placed in the timeslot myTimeslot and the relative U elements are set at 1,
					 * remembering it is introducing an unfeasibility in the solution.*/
					te[myTimeslot][exam] = 1;
					schedule[exam] = myTimeslot;
					assignedExams[exam] = 1;
					texamsCounter[myTimeslot]++;
					// cycling through exams in myTimeslot
					for(int e = 0; e < E; e++) {
						// looks only allocated exams
						if(te[myTimeslot][e] == 1) {
							// and check if they are conflictual with exam
							if(N[exam][e] != 0) {
								penalizingPairs.add(new ExamPair(e, exam));
								++fitness;
							}
						}
					}
					
				}// end IF exam cannot be placed
			}
			
		} // END FOR exam
		
		/*TODO debug*/
		System.out.println(
				"Returning infeasible solution\npenalizingPairs" + Arrays.toString(penalizingPairs.toArray()) + 
				"\nIncremental fitness: " + fitness
		);
		
		
		return new InitializationSolution(
			instance, 
			te,
			schedule,
			fitness,
			penalizingPairs
		);
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
	private int[] getTimeslotOrder(int[] TEcounter) {
		
		int tec[] = new int[TEcounter.length];
		int orderTec[] = new int[instance.getTmax()];
		int max = -1;
		int ind = 0;
		for(int i = 0; i < tec.length; i++)
			tec[i] = TEcounter[i];
		
		// For every element of the Timeslot Order array
		for(int x = 0; x < instance.getTmax(); x++) {			
			// For every element of the Exams in timeslot counter, find the max
			for(int i = 0; i < instance.getTmax(); i++) {
				
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
	
	@Override
	protected ExamPair getNextPair(int nextPairIndex) throws IndexOutOfBoundsException {
		//return currentSolution.getPenalizingPairs().get(nextPairIndex);
		
		// Randomly is way more better
		return currentSolution.getPenalizingPairs().get(
			java.util.concurrent.ThreadLocalRandom.current().nextInt(0, currentSolution.getPenalizingPairs().size() + 1)
		);  
	}
	
	@Override
	protected void updateBestSolution() {
		if(currentSolution.getFitness() < bestSolution.getFitness()) {
			bestSolution = new InitializationSolution((InitializationSolution)currentSolution);
			bestSolutionIteration = iteration;
		}
	}
	
	@Override
	protected void returnToBestSolution() {
		currentSolution = new InitializationSolution((InitializationSolution)bestSolution);
	}
}
