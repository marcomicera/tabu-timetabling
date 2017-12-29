package it.polito.oma.etp.solver;

import java.util.ArrayList;

import it.polito.oma.etp.reader.InstanceData;

public class InitializationSolution extends Solution {
	/**
	 * List containing exam pairs causing this solution to
	 * be infeasible.
	 */
	private ArrayList<ExamPair> unfeasiblePairs;
	
	protected InitializationSolution(InstanceData instance, int[][] te) {
		super(instance, te);
	}
	
	public InitializationSolution(Solution s) {
		super(s);
	}

	@Override
	/*TODO has to be private*/public void updateFitness() {
		updateUnfeasiblePairs();
		
		fitness = unfeasiblePairs.size();
	}
	
	/**
	 * Computes the array containing all exam pairs causing
	 * this solution to be infeasible. 
	 */
	private void updateUnfeasiblePairs() {
		// Instance data
		int E = instance.getE();
		int[][] N = instance.getN();
		
		for(int exam1 = 0; exam1 < E; ++exam1)
			for(int exam2 = exam1 + 1; exam2 < E; ++exam2)
				if(	// If both exams have been scheduled in the same timeslot (conflicting)
					schedule[exam1] == schedule[exam2] && 
					
					// If both exams have more than one student enrolled in both exams
					N[exam1][exam2] > 0
				)
					unfeasiblePairs.add(
						new ExamPair(exam1, exam2)
					);
	}
	
	@Override
	protected Neighbor getNeighbor(int movingExam, int newTimeslot) throws InvalidMoveException {
		/**
		 * Example:
		 * _____________________________________
		 * 
		 * te matrix =
		 * 
		 * 1	1		0	1
		 * 0	0		1	0
		 * 0	0		0	0
		 * 
		 * schedule = [0, 0, 1, 0]
		 * _____________________________________
		 * 
		 * movingExam = 1
		 * newTimeslot = 2
		 * _____________________________________
		 * 
		 * new te matrix =
		 * 
		 * 1	(0)		0	1
		 * 0	0		1	0
		 * 0	(1)		0	0
		 * 
		 * new schedule = [0, 2, 1, 0]
		 * 
		 */
		
		// This function's result, based on the current fitness value
		float neighborFitnessValue = fitness;
		
		// Instance variables
		int E = instance.getE();
		int[][] N = instance.getN();
		
		int oldTimeslot = schedule[movingExam];
		
		// Computing the neighbor's fitness value
		for(int otherExam = 0; otherExam < E; ++otherExam)
			if(	// Avoids confronting the movingExam with itself
				otherExam != movingExam &&
				
				// If there are students enrolled in both exams (conflicting exams)
				N[movingExam][otherExam] > 0
			) 
				// Removing old conflicts
				if(	// If both exams have been scheduled in the same timeslot
					te[oldTimeslot][otherExam] == 1
				)
					--neighborFitnessValue;
				
				else if(// If there will be new exams in the same timeslot
						te[newTimeslot][otherExam] == 1
				)
					++neighborFitnessValue;
				
		return new Neighbor(movingExam, newTimeslot, neighborFitnessValue);
	}

	@Override
	protected ExamPair getNeighborhoodGeneratingPair() {
		// Randomly, no ordering needed
		return unfeasiblePairs.get(0);
	}
}
