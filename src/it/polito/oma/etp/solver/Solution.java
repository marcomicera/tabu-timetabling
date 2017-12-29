package it.polito.oma.etp.solver;

import java.util.Arrays;

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
	 * ExamPair object used to generate the neighborhood
	 * during each Tabu Search iteration.
	 */
	protected ExamPair neighborhoodGeneratingPair;

	/**
	 * Default constructor
	 * @param te	time-slots exams matrix.
	 */
	protected Solution(InstanceData instance, int[][] te) {
		this.instance = instance;
		this.te = te;
		updateSchedule();
		updateFitness();
	}
	
	/**
	 * Copy constructor that creates a copy of this Solution object
	 * @param s	Solution to be copied.
	 */
	protected Solution(Solution s) {
		instance = s.instance;
		te = s.te;
		schedule = s.schedule;
		fitness = s.fitness;
		neighborhoodGeneratingPair = s.neighborhoodGeneratingPair;
	}
	
	/**
	 * Updates the schedule data structure, containing, for each
	 * exam, the timeslot number in which it has been assigned to.
	 */
	public void updateSchedule() {
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
	 * Updates the schedule according to the move corresponding
	 * to the neighbor specified as the first argument.
	 * @param neighbor	move according to which the schedule
	 * 					will be updated.
	 */
	public void updateSchedule(Neighbor neighbor) {
		schedule[neighbor.getMovingExam()] = neighbor.getNewTimeslot();
	}
	
	/**
	 * Updates the current solution objective function value
	 */
	protected abstract void updateFitness();
	
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
	 * Checks whether this solution is feasible or not.
	 */
	public void checkFeasibility() {
	    int E = instance.getE();
	    int[][] N = instance.getN();
	    
	    for(int t = 0; t < instance.getTmax(); ++t)
	    	for(int exam1 = 0; exam1 < E; ++exam1)
	    		for(int exam2 = exam1+1; exam2 < E; ++exam2)
	    			if(N[exam1][exam2]>0 && te[t][exam1]==1 && te[t][exam2]==1)
	    				System.out.println("conflictual exams "+exam1+" and "+exam2+" are in the same TM");
	  }
	
	/**
	 * Returns the exam pair used to generate the neighborhood,
	 * chosen by a a different criteria for each Solution implementation.
	 * @return	the exam pair used to create the neighborhood.
	 */
	protected abstract ExamPair getNeighborhoodGeneratingPair();
	
	/**
	 * Overridden method which displays info about this solution.
	 */
	@Override
	public String toString() {
		return	"Printing S: " + instance.getS() + "\n" +
				"Printing E: " + instance.getE() + "\n" +
				"Printing Tmax: " + instance.getTmax() + "\n" +
				//"\nPrinting schedule:\n" + Arrays.toString(schedule) +
				//"\n\nPrinting Te:\n" + printMatrix(te) +
				//"Printing y:\n" + printMatrix(y) +
				"Most penalizing exam pair: " + neighborhoodGeneratingPair +
				"\n\nFitness value: " + fitness
		;
	}
	
	/**
	 * General function which prints a matrix in a readable way.
	 * @param m		matrix to be printed.
	 * @return		a string showing the matrix in a readable way.
	 */
	private String printMatrix(int[][] m) {
		String result = "";
		for(int[] row: m) {
			result += Arrays.toString(row) + "\n";
		}
		return result + "\n";
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
}