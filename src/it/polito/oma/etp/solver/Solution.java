package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.initialization.InitializationSolution;
import it.polito.oma.etp.solver.optimization.OptimizationSolution;

/**
 * Solution base-class both for the Tabu Search algorithm
 * and for the Genetic Algorithm.
 */
public abstract class Solution implements Comparable<Solution>{

	/**
	 * Instance to which this solution refers to. 
	 */
	protected InstanceData instance;
	
	/**
	 * Timeslots-exams matrix, having element [i][j] set to
	 * 1 when exam j is scheduled in the i-th timeslot.
	 */
	protected int[][] te;
	
	/**
	 * Alternative 'te' representation.
	 * For each exam i, schedule[i] contains the timeslot number
	 * in which exam i has been scheduled.
	 * This is used for distance calculations.
	 */
	protected int[] schedule;
	
	/**
	 * Objective function value.
	 */
	protected Float fitness;
	
	/**
	 * Exam pairs causing a penalty or an infeasibility,
	 * depending on the Solution implementation.
	 */
	protected ArrayList<ExamPair> penalizingPairs;
	
	/**
	 * Default constructor
	 * @param te	time-slots exams matrix.
	 */
	protected Solution(InstanceData instance, int[][] te) {
		this.instance = instance;
		this.te = te;
		initializeSchedule();
		initializeDistanceMatrix();
		initializePenalizingPairs();
		initializeFitness();
	}
	
	/**
	 * Copy constructor that creates a copy of this Solution object
	 * @param s	Solution to be copied.
	 */
	protected Solution(Solution s) {
		// Read-only object, no copying needed
		instance = s.instance; 
		
		te = Utility.cloneMatrix(s.te);
		schedule = Utility.cloneArray(s.schedule);
		fitness = s.fitness;
		penalizingPairs = new ArrayList<>(s.penalizingPairs);
	}
	
	/**
	 * Constructor used for the first infeasible solution.
	 * Every single field is computed while obtaining the
	 * first infeasible solution. 
	 * @param instance
	 * @param te
	 * @param schedule
	 * @param fitness
	 * @param penalizingPairs
	 */
	public Solution(InstanceData instance, int[][] te, int[] schedule, float fitness,
			ArrayList<ExamPair> penalizingPairs) {
		super();
		this.instance = instance;
		this.te = te;
		this.schedule = schedule;
		this.fitness = fitness;
		this.penalizingPairs = penalizingPairs;
	}
	
	protected abstract void initializeDistanceMatrix();
	
	// TODO JavaDoc
	// TODO change name to generateInfeasibleSolution
	public static InitializationSolution generateInfeasibleSolution(InstanceData instance, 
																	boolean firstRandomSolution
	) {
		/*TODO debug*///System.out.println("Generating infeasible solution...");
		
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
		if(!firstRandomSolution) {
			// first step: put e0 in t0
			te[0][0] = 1;
			schedule[0] = 0;
			assignedExams[0] = 1;
			texamsCounter[0]++;
		}
		else {
			timeslotOrder = getTimeslotOrderRandomly(instance);
		}
		// cycling through all exams
		for(int exam = (firstRandomSolution) ? 0 : 1; exam < E; exam++) {
			
				/* IN: texamsCounter, OUT: timeslotOrder*
				 * Given in input the number of exams in every timeslot, it generates the timeslot ordering. */
				if(!firstRandomSolution) {
					timeslotOrder = getTimeslotOrderByExamsCount(instance, texamsCounter);
				}
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
					 * remembering it is introducing an infeasibility in the solution.*/
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
			
		} // END FOR exam

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
	private static int[] getTimeslotOrderByExamsCount(InstanceData instance, int[] TEcounter) {
		
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
	
	/**
	 * @return an array containing for every element a random timeslot.
	 * This array will be used as a sequence of timeslot where to put 
	 * the examined exam.
	 */
	private static int[] getTimeslotOrderRandomly(InstanceData instance) {
		int tmax = instance.getTmax();
		int[] orderTec = new int[tmax];
		ArrayList<Integer> timeslotPool = new ArrayList<Integer>();
		// Filling the timeslot pool.
		for(int i = 0; i < tmax; i++) {
			timeslotPool.add(i);
		}
		// Filling orderTec with random timeslots from the pool.
		int randomPoolIndex;
		for(int i = 0; i < tmax; i++) {
			randomPoolIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(0, timeslotPool.size());
			orderTec[i] = timeslotPool.get(randomPoolIndex);
			timeslotPool.remove(randomPoolIndex);
		}
		return orderTec;
	}
	
	/**
	 * Computes the schedule data structure from scratch, containing, 
	 * for each exam, the timeslot number in which it has been assigned to.
	 */
	public void initializeSchedule() {
		int E = instance.getE();
		int tmax = instance.getTmax();
		
		// For each exam, the timeslot number is stored
		schedule = new int[E];
		
		for(int j = 0; j < E; ++j) // for each exam
			for(int i = 0; i < tmax; ++i) 
				if(te[i][j] == 1) {
					schedule[j] = i;
					continue;
				}
	}
	
	/**
	 * Computes the data structure containing exam pairs from 
	 * scratch causing a penalty or an infeasibility depending 
	 * on the Solution implementation.
	 */
	protected abstract void initializePenalizingPairs();
	
	/**
	 * Updates the the data structure containing exam pairs starting
	 * from the modifications brought by the neighbor passed
	 * as argument, causing a penalty or an infeasibility depending 
	 * on the Solution implementation.
	 * @param neighbor
	 */
	protected abstract void updatePenalizingPairs(Neighbor neighbor);
	
	/**
	 * Updates the schedule according to the move corresponding
	 * to the neighbor specified as the first argument.
	 * @param neighbor	move according to which the schedule
	 * 					will be updated.
	 */
	public void updateSchedule(Neighbor neighbor) {
		schedule[neighbor.getMovingExam()] = neighbor.getNewTimeslot();
	}
	
	/**
	 * Computes the current solution objective function value
	 * from scratch.
	 */
	protected abstract void initializeFitness();
	
	/**
	 * Updates the te table according to a move.
	 * @param movingExam	exam to be rescheduled
	 * @param oldTimeslot	timeslot in which the exam was
	 * @param newTimeslot	timeslot in which the exam will be
	 */
	public void updateTe(int movingExam, int oldTimeslot, int newTimeslot) {
		te[oldTimeslot][movingExam] = 0;
		te[newTimeslot][movingExam] = 1;
	}
	
	/**
	 * Retrieves neighbor information such as its fitness value and its corresponding
	 * schedule, given a move to be done (i.e., the exam to be rescheduled and the new
	 * timeslot in which the latter will be moved to).
	 * 
	 * @param movingExam				exam that would be moved across the timetable. 
	 * @param newTimeslot				timeslot in which it would end. 
	 * @return							the corresponding Neighbor object containing the
	 * 									new fitness value and its corresponding schedule
	 * @throws InvalidMoveException		if the new move produces and infeasible result
	 */
	protected abstract Neighbor getNeighbor(int movingExam, int newTimeslot) throws InvalidMoveException;
	
	/**
	 * Returns whether the solution represented by this object
	 * is feasible or not.
	 * @return	true if this solution is feasible.
	 */
	public boolean isFeasible() {
	    int E = instance.getE();
	    int[][] N = instance.getN();
	    
	    boolean isFeasible = true;
	    
	    for(int t = 0; t < instance.getTmax(); ++t)
	    	for(int exam1 = 0; exam1 < E; ++exam1)
	    		for(int exam2 = 0; exam2 < E; ++exam2)
	    			if(exam1 != exam2) {
		    			if(N[exam1][exam2] > 0 && schedule[exam1] == schedule[exam2]) {
		    				isFeasible = false;
		    				/*TODO debug*/ //System.out.println("Conflictual exams " + exam1 + " and e"+exam2+" are in the same TM");
		    			}
	    			}
	    return isFeasible;
	}

	// Ordering (increasing fitness)
	@Override
	public int compareTo(Solution otherSolution) {
		return Float.valueOf(fitness).compareTo(Float.valueOf(otherSolution.getFitness()));
	}
	
	/**
	 * Overridden method which displays info about this solution.
	 */
	@Override
	public String toString() {
		return	"<fitness: " + fitness +
				", " + ((!isFeasible()) ? "not " : "") + "feasible>"
		;
	}
	
	public InstanceData getInstance() {
		return instance;
	}
	
	public int[][] getTe() {
		return te;
	}
	
	/**
	 * Returns the timeslot in which the given exam has been scheduled.
	 * @param exam	exam whose corresponding scheduled timeslot is requested.
	 * @return		the timeslot in which the given exam is scheduled.
	 */
	public int getTimeslot(int exam) {
		if(exam < 0 || exam >= instance.getE()) {
			System.err.println("Trying to retrieve a timeslot of an invalid exam.");
			System.exit(1);
		}
			
		return schedule[exam];
	}
	
	/**
	 * TODO JavaDoc
	 * @param neighbor
	 */
	public void move(Neighbor neighbor) {
		/*TODO debug*/ //System.out.println("Move: <e" + neighbor.getMovingExam() + ", from t" + currentSolution.getTimeslot(neighbor.getMovingExam()) + " to t" + neighbor.getNewTimeslot() + ">");
		
		// Retrieving current solution's infos before performing the move
		int movingExam = neighbor.getMovingExam();
		int oldTimeslot = getTimeslot(movingExam);
		/*TODO debug*/ //System.out.println("movingExam's index inside move(): " + movingExam);
		/*TODO debug*/ //System.out.println("old fitness = " + currentSolution.getFitness());
		
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
		updateTe(movingExam, oldTimeslot, neighbor.getNewTimeslot());
		updateSchedule(neighbor); 
		setFitness(neighbor.getFitness());
		updatePenalizingPairs(neighbor);
		
		if(this instanceof OptimizationSolution)
			((OptimizationSolution)this).initializeDistanceMatrix();
				
//		/*TODO debug*/float testIncrementalFitness = getFitness();
		/*TODO debug (fitness)*/ System.out.println("\nFitness: " + getFitness());
//		/*TODO debug*/ initializeFitness();
//		/*TODO debug*/float testFitnessFromScratch = getFitness();
//		/*TODO debug*/if(testIncrementalFitness != testFitnessFromScratch) {
//		/*TODO debug*/	System.err.println("Different fitness values");
//		/*TODO debug*/	System.exit(1);
//		/*TODO debug*/}
		/*TODO debug (fitness from scratch)*/ //System.out.println("Calculating the fitness from scratch: " + currentSolution.getFitness());
	}
	
	// Searching in Lists
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Solution)) return false;
		
		Solution otherSolution = (Solution)o;
		
		return Arrays.equals(schedule, otherSolution.schedule);
	}
	
	public float getFitness() {
		return fitness;
	}

	public void setFitness(float fitness) {
		this.fitness = fitness;
	}

	public ArrayList<ExamPair> getPenalizingPairs() {
		return penalizingPairs;
	}
}