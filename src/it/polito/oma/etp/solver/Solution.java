package it.polito.oma.etp.solver;

import java.util.HashMap;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map.Entry;

import it.polito.oma.etp.reader.InstanceData;

public class Solution {
	/**
	 * Instance to which this solution refers to. 
	 */
	private InstanceData instance;
	
	/**
	 * Timeslots-exams matrix, having element [i][j] set to
	 * 1 when exam j is scheduled in the i-th timeslot.
	 */
	private int[][] te;
	
	/**
	 * Alternative 'te' representation.
	 * For each exam i, schedule[i] contains the timeslot number
	 * in which exam i has been scheduled.
	 * This is used for distance calculations.
	 */
	private int[] schedule;
	
	/**
	 * Exams distance matrix, having element [i][j] set to a value k
	 * when exams i and j are scheduled k timeslots apart.
	 */
	private int[][] y;
	
	/**
	 * Objective function value
	 */
	private float fitness;
	
	/**
	 * A conflict coefficient is mapped for each pair of exams
	 * (order does not matter).
	 * The higher, the more they are going to penalize.
	 */
	private HashMap<ExamPair, Float> conflictCoefficients;
	
	/**
	 * Map entry corresponding to the exam that causes the
	 * highest penalty fee.
	 */
	private Entry<ExamPair, Float> mostPenalizingPair;

	/**
	 * Default constructor
	 * @param te	time-slots exams matrix.
	 */
	public Solution(InstanceData instance, int[][] te) {
		this.instance = instance;
		this.te = te;
		int E = instance.getE();
		y = new int[E][E];
		updateDistanceMatrix();
		updateConflictCoefficients();
		updateFitness();
	}
	
	/**
	 * Updates the exams distance matrix given the current
	 * decision matrix te.
	 */
	private void updateDistanceMatrix() {
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
		
		for(int i = 0; i < E; ++i)
			for(int j = 0; j < E; ++j) {
				int distance = Math.abs(schedule[i] - schedule[j]);
				y[i][j] = distance; 
			}
	}
	
	/**
	 * Builds a ranking of most penalizing exams, mapping a 
	 * conflict coefficient to an exam pair.
	 */
	private void updateConflictCoefficients() {
		int[][] N = instance.getN();
		int E = instance.getE();
		
		conflictCoefficients = new HashMap<ExamPair, Float>();
		mostPenalizingPair = new AbstractMap.SimpleEntry<ExamPair, Float>(
			new ExamPair(-1, -2), new Float(-1)
		);
		
		// For each pair of exams (order does not matter)
		for(int i = 0; i < E; ++i)
			for(int j = i + 1; j < E; ++j) {
				// Conflict coefficient is computed only when exams will generate a fee
				if(arePenalized(i, j)) {
					Float newCoefficient = new Float(N[i][j]) / getDistance(i, j);
					
					// Inserting the conflict coefficient for the corresponding exam pair
					conflictCoefficients.put(
						// The corresponding exam pair acts as a key in this Map
						new ExamPair(i, j),
						
						// The conflict coefficient value
						newCoefficient
					);
					
					// Replacing the new most penalizing exam if necessary
					if(newCoefficient > mostPenalizingPair.getValue())
						mostPenalizingPair = new AbstractMap.SimpleEntry<ExamPair, Float>(
							// The corresponding exam pair acts as a key in this Map
							new ExamPair(i, j),
								
							// The conflict coefficient value
							newCoefficient
						);;
				}
			}
	}

	
	/**
	 * Updates the current solution objective function value
	 */
	private void updateFitness() {
		int E = instance.getE();
		int S = instance.getS();
		int K = instance.getK();
		int[][] N = instance.getN();
		 
		fitness = 0;
		
		for(int i = 0; i < E; ++i) {
			for(int j = i + 1; j < E; ++j) {
				if(i != j && y[i][j] > 0 && y[i][j] <= K) {
					fitness += Math.pow(2, K - y[i][j]) * new Float(N[i][j]) / S;
				}
			}
		}
	}
	
	/**
	 * Computes the objective function value in case exam 'movingExam'
	 * is scheduled in timeslot 'newTimeslot'.
	 * This corresponds to a neighbor's fitness value. 
	 * @param movingExam				exam that would be moved across the timetable. 
	 * @param newTimeslot				timeslot in which it would end. 
	 * @return							the new fitness value.
	 * @throws InvalidMoveException		if the new move produces and infeasible result
	 */
	public float neighborFitness(int movingExam, int newTimeslot) throws InvalidMoveException {
		float result = fitness;
		
		int E = instance.getE();
		int S = instance.getS();
		int K = instance.getK();
		int[][] N = instance.getN();
		
		// Adding new penalties
		int[] newSchedule = schedule;
		/*TODO delete*/System.out.println("Old schedule: " + Arrays.toString(schedule));
		newSchedule[movingExam] = newTimeslot;	// updating the new schedule according to the move
		/*TODO delete*/System.out.println("new schedule: " + Arrays.toString(newSchedule));
		for(int otherExam = 0; otherExam < E; ++otherExam) {
			if(	// Avoids confronting the movingExam with itself
				otherExam != movingExam &&
					
				// If there are students enrolled in both exams (conflicting exams)
				N[movingExam][otherExam] > 0
			) {
				int distance = Math.abs(schedule[movingExam] - schedule[otherExam]);
				
				if(distance == 0)
					throw new InvalidMoveException();
				
				/* If exams are scheduled less than K timeslots apart, they do not 
				 * generate any fee at all */
				if(distance <= K) {
					result += Math.pow(2, K - distance) * N[movingExam][otherExam] / S;
					/*TODO delete*/System.out.println("Distance: " + distance);
				}
			}
		}
		
		/*TODO delete*/System.out.println("Intermediate result: " + result);
		
		// Removing old penalties
		for(int otherExam = 0; otherExam < E; ++otherExam)
			if(	// Avoids confronting the movingExam with itself
				otherExam != movingExam &&
				
				// If there are students enrolled in both exams (conflicting exams)
				N[movingExam][otherExam] > 0 &&
				
				/* If exams are scheduled less than K timeslots apart, they do not 
				 * generate any fee at all */
				y[movingExam][otherExam] <= K
			) {
				result -= Math.pow(2, K - y[movingExam][otherExam]) * N[movingExam][otherExam] / S;
			}
		
		return result;
	}
	
	/**
	 * Boolean function indicating if two exams i and j will generate
	 * a fee to be paid. 
	 * @param i	first exam index.
	 * @param j	second exam index.
	 * @return	true if exam i and j are in conflict and are scheduled
	 * 			between 1 and 5 timeslots apart	
	 */
	public boolean arePenalized(int i, int j) {
		if(y[i][j] > 0 && y[i][j] <= instance.getK() && instance.getN()[i][j] > 0)
			return true;
		
		return false;
	}
	
	/**
	 * Returns the distance in terms of timeslots between
	 * exams i and j according to the solution that this
	 * object represents.
	 * @param i	first exam index.
	 * @param j	second exam index.
	 * @return	distance between the two exams i and j
	 */
	public int getDistance(int i, int j) {
		return y[i][j];
	}
	
	/**
	 * Getter returning the most penalizing exams pair with its
	 * corresponding conflict coefficient
	 * @return	an entry having the conflict coefficient - exam pair of
	 * 			the most penalizing exam pair 
	 */
	public Entry<ExamPair, Float> getMostPenalizingPair() {
		if(conflictCoefficients.isEmpty())
			return null;
		
		return mostPenalizingPair;
	}
	
	/**
	 * Overridden method which displays info about this solution.
	 */
	@Override
	public String toString() {
		return	"Printing S: " + instance.getS() + "\n" +
				"Printing E: " + instance.getE() + "\n" +
				"Printing Tmax: " + instance.getTmax() + "\n" +
				"\nPrinting schedule:\n" + Arrays.toString(schedule) +
				"\n\nPrinting Te:\n" + printMatrix(te) +
				"Printing y:\n" + printMatrix(y) +
				"Printing conflictCoefficients:\n" + printConflictCoefficients() +
				"Most penalizing exam pair: " + mostPenalizingPair.getKey() + ", confl. = " + mostPenalizingPair.getValue() +
				"\n\nFitness value: " + fitness
		;
	}
	
	/**
	 * Conflict coefficients pretty printing.
	 * @return	a string representing the map of conflict coefficients
	 * 			in a readable format.
	 */
	private String printConflictCoefficients() {
		String result = "";
		
		for(Entry<ExamPair, Float> entry: conflictCoefficients.entrySet()) 
			result += "[Exams " + entry.getKey() + " coeff. = " + entry.getValue() + " ]\n";
		
		return result + "\n";
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
}