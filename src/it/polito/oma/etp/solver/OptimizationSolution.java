package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;

import it.polito.oma.etp.reader.InstanceData;

public class OptimizationSolution extends Solution {
	/**
	 * Exams distance matrix, having element [i][j] set to a value k
	 * when exams i and j are scheduled k timeslots apart.
	 */
	private int[][] distanceMatrix;
	
	/**
	 * The most penalizing exam pair is stored outside
	 * Solution.penalizingPairs to avoid sorting all exam pairs
	 * in case this one has a valid non-empty neighborhood.
	 */
	private ExamPair mostPenalizingPair;
	
	public OptimizationSolution(InstanceData instance, int[][] te) {
		super(instance, te);
		
		int E = instance.getE();
		distanceMatrix = new int[E][E];
				
		initializeDistanceMatrix();
	}
	
	public OptimizationSolution(OptimizationSolution s) {
		super(s);
		
		distanceMatrix = s.distanceMatrix;
		mostPenalizingPair = new ExamPair(s.mostPenalizingPair);
	}
	
	/**
	 * Conversion constructor used to transform the first
	 * feasible solution obtained by the Tabu Search to
	 * another specific type.
	 * @param s		The solution to be converted.
	 */
	public OptimizationSolution(InitializationSolution s) {
		super(s);
		
		// New data structure needed in this type of solution
		int E = instance.getE();
		distanceMatrix = new int[E][E];
		initializeDistanceMatrix();
		
		// From number of infeasibilities to penalizing fee
		initializeFitness();
		
		/**
		 * From exam pairs generating infeasibilities to
		 * exam pairs causing penalties
		 */
		initializePenalizingPairs();
	}

	/**
	 * Computes the exams distance matrix given the current
	 * decision matrix te.
	 */
	public void initializeDistanceMatrix() {
		int E = instance.getE();
		
		for(int i = 0; i < E; ++i)
			for(int j = 0; j < E; ++j) {
				int distance = Math.abs(schedule[i] - schedule[j]);
				distanceMatrix[i][j] = distance; 
			}
	}
	
	/**
	 * Builds a ranking of most penalizing exams, mapping a 
	 * conflict coefficient to an exam pair.
	 * The higher, the more they are going to penalize.
	 */
	protected void initializePenalizingPairs() {
		int[][] N = instance.getN();
		int E = instance.getE();
		
		penalizingPairs = new ArrayList<ExamPair>();
		
		/*	neighborhoodGeneratingPair initialization: the conflict coefficient is set to -1
		 	so it will be replaced by the first conflicting exam pair as soon as possible */
		mostPenalizingPair = new ExamPair(-1, -2, -1);
		
		// For each pair of exams (order does not matter)
		for(int i = 0; i < E; ++i)
			for(int j = i + 1; j < E; ++j) {
				// Conflict coefficient is computed only when exams will generate a fee
				if(arePenalized(i, j)) {
					Float newCoefficient = new Float(N[i][j]) / getDistance(i, j);
					
					// Inserting the conflict coefficient for the corresponding exam pair
					penalizingPairs.add(
						// The corresponding exam pair with its conflict coefficient 
						new ExamPair(i, j, newCoefficient)
					);
					
					// Replacing the new most penalizing exam if necessary
					// TODO what to do in case the most penalizing exam pair cannot be rescheduled?
					if(newCoefficient > mostPenalizingPair.getConflictCoefficient())
						// The corresponding exam pair with its conflict coefficient
						mostPenalizingPair = new ExamPair(i, j, newCoefficient);
				}
			}
	}

	@Override
	protected void updatePenalizingPairs(Neighbor neighbor) {
		// TODO to improve eventually
		initializePenalizingPairs();
	}
	
	@Override
	/*TODO has to be private*/public void initializeFitness() {
		int E = instance.getE();
		int S = instance.getS();
		int K = instance.getK();
		int[][] N = instance.getN();
		 
		fitness = 0;
		
		for(int i = 0; i < E; ++i)
			for(int j = i + 1; j < E; ++j)
				if(i != j && distanceMatrix[i][j] > 0 && distanceMatrix[i][j] <= K)
					fitness += Math.pow(2, K - distanceMatrix[i][j]) * new Float(N[i][j]) / S;
	}
	
	@Override
	public Neighbor getNeighbor(int movingExam, int newTimeslot) throws InvalidMoveException {
		// This function's result, based on the current fitness value
		float neighborFitnessValue = fitness;
		
		// Instance variables
		int E = instance.getE();
		int S = instance.getS();
		int K = instance.getK();
		int[][] N = instance.getN();
		
		// Updating the new schedule according to the move
		int[] newSchedule = Arrays.copyOf(schedule, E);
		newSchedule[movingExam] = newTimeslot;
		
		// TODO these 2 for loops can be merged into a single one
		
		// Adding new penalties
		for(int otherExam = 0; otherExam < E; ++otherExam) {
			if(	// Avoids confronting the movingExam with itself
				otherExam != movingExam &&
					
				// If there are students enrolled in both exams (conflicting exams)
				N[movingExam][otherExam] > 0
			) {
				int distance = Math.abs(newSchedule[movingExam] - newSchedule[otherExam]);
				
				if(distance == 0)
					throw new InvalidMoveException(
						"exam " + (movingExam + 1) + " cannot be placed in timeslot number " + (newTimeslot + 1) +
						" since it is in conflict with " + (otherExam + 1) +
						" for having " + N[movingExam][otherExam] + " students enrolled in both exams."
					);
				
				/* If exams are scheduled less than K timeslots apart, they do not 
				 * generate any fee at all */
				if(distance <= K)
					neighborFitnessValue += Math.pow(2, K - distance) * N[movingExam][otherExam] / S;
			}
		}
		
		// Removing old penalties
		for(int otherExam = 0; otherExam < E; ++otherExam)
			if(	// Avoids confronting the movingExam with itself
				otherExam != movingExam &&
				
				// If there are students enrolled in both exams (conflicting exams)
				N[movingExam][otherExam] > 0 &&
				
				/* If exams are scheduled less than K timeslots apart, they do not 
				 * generate any fee at all */
				distanceMatrix[movingExam][otherExam] <= K
			) {
				if(distanceMatrix[movingExam][otherExam] == 0)
					throw new AssertionError("Previous solution is unfeasible");
				
				neighborFitnessValue -= Math.pow(2, K - distanceMatrix[movingExam][otherExam]) * N[movingExam][otherExam] / S;
			}
		
		return new Neighbor(movingExam, newTimeslot, neighborFitnessValue);
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
		if(distanceMatrix[i][j] > 0 && distanceMatrix[i][j] <= instance.getK() && instance.getN()[i][j] > 0)
			return true;
		
		return false;
	}
	
	@Override
	public String toString() {
		return	super.toString() + 
				"\nPrinting conflictCoefficients:\n" + printConflictCoefficients() +
				"\nMost penalizing exam pair: " + mostPenalizingPair
		;
	}

	/**
	 * Conflict coefficients pretty printing.
	 * @return	a string representing the map of conflict coefficients
	 * 			in a readable format.
	 */
	private String printConflictCoefficients() {
		String result = "[";
		
		for(ExamPair entry: penalizingPairs) 
			result += entry.toString();
		
		return result + "]\n";
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
		return distanceMatrix[i][j];
	}
	
	public ArrayList<ExamPair> getConflictCoefficients() {
		return penalizingPairs;
	}

	public ExamPair getMostPenalizingPair() {
		return mostPenalizingPair;
	}
}
