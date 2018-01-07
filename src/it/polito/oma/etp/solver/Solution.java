package it.polito.oma.etp.solver;

import java.util.ArrayList;

import it.polito.oma.etp.reader.InstanceData;

public abstract class Solution {
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
	protected float fitness;
	
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
	    		for(int exam2 = exam1+1; exam2 < E; ++exam2)
	    			if(N[exam1][exam2]>0 && te[t][exam1]==1 && te[t][exam2]==1) {
	    				isFeasible = false;
	    				/*TODO debug*///System.out.println("Conflictual exams " + exam1 + " and e"+exam2+" are in the same TM");
	    			}
	    
	    return isFeasible;
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