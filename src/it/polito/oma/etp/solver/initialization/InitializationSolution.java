package it.polito.oma.etp.solver.initialization;

import java.util.ArrayList;
import java.util.Iterator;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.ExamPair;
import it.polito.oma.etp.solver.InvalidMoveException;
import it.polito.oma.etp.solver.Neighbor;
import it.polito.oma.etp.solver.Solution;

public class InitializationSolution extends Solution {
	/*TODO IT WAS PROTECTED */public InitializationSolution(InstanceData instance, int[][] te) {
		super(instance, te);
	}
	
	public InitializationSolution(Solution s) {
		super(s);
	}
	
	/**
	 * Constructor used for the first infeasible solution.
	 * @param instance
	 * @param te
	 * @param schedule
	 * @param fitness
	 * @param penalizingPairs
	 */
	public InitializationSolution(InstanceData instance, int[][] te, int[] schedule, float fitness,
			ArrayList<ExamPair> penalizingPairs) {
		super(instance, te, schedule, fitness, penalizingPairs);
	}

	@Override
	/*TODO has to be private*/public void initializeFitness() {
		fitness = penalizingPairs.size();
	}
	
	/**
	 * Computes the array containing all exam pairs causing
	 * this solution to be infeasible. 
	 */
	@Override
	protected void initializePenalizingPairs() {
		// Instance data
		int E = instance.getE();
		int[][] N = instance.getN();
		
		penalizingPairs = new ArrayList<ExamPair>();

		
		for(int exam1 = 0; exam1 < E; ++exam1)
			for(int exam2 = exam1 + 1; exam2 < E; ++exam2)
				if(	// If both exams have been scheduled in the same timeslot (conflicting)
					schedule[exam1] == schedule[exam2] && 
					
					// If both exams have more than one student enrolled in both exams
					N[exam1][exam2] > 0
				)
					penalizingPairs.add(
						new ExamPair(exam1, exam2)
					);
	}
	
	@Override
	protected void updatePenalizingPairs(Neighbor neighbor) {
		int movingExam = neighbor.getMovingExam();
		
		// Removing old exam pairs
		Iterator<ExamPair> iterator = penalizingPairs.iterator();
		while(iterator.hasNext()) {
			ExamPair examPair = iterator.next();
			
			if(examPair.getExam1() == movingExam || examPair.getExam2() == movingExam)
				iterator.remove();
		}		
		
		// Adding new exam pairs
		for(int otherExam = 0; otherExam < instance.getE(); ++otherExam) {
			if(	// If both exams have been scheduled in the same timeslot (conflicting)
				schedule[movingExam] == schedule[otherExam] && 
				
				// If both exams have more than one student enrolled in both exams
				instance.getN()[movingExam][otherExam] > 0
			)
				penalizingPairs.add(
					new ExamPair(movingExam, otherExam)
				);
		}
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
		
		// Used when removing old infeasibilities
		int oldTimeslot = schedule[movingExam];
		
		// Computing the neighbor's fitness value
		for(int otherExam = 0; otherExam < E; ++otherExam)
			if(	// Avoids confronting the movingExam with itself
				otherExam != movingExam &&
				
				// If there are students enrolled in both exams (conflicting exams)
				N[movingExam][otherExam] > 0
			) {
				// Removing old infeasibilities
				if(	// If both exams have been scheduled in the same timeslot
					te[oldTimeslot][otherExam] == 1
				)
					--neighborFitnessValue;
				
				// Adding new infeasibilities
				if(	// If there will be new exams in the same timeslot
					te[newTimeslot][otherExam] == 1
				)
					++neighborFitnessValue;
			}
		
		return new Neighbor(movingExam, newTimeslot, neighborFitnessValue);
	}
}
