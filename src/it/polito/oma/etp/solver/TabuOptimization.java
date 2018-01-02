package it.polito.oma.etp.solver;

import java.util.Collections;

import it.polito.oma.etp.reader.InstanceData;

public class TabuOptimization extends TabuSearch {
	public TabuOptimization(InstanceData instanceData, InitializationSolution initialSolution, Settings settings) {
		super(instanceData, settings);
		
		// Initially, the current solution is the initial one
		currentSolution = new OptimizationSolution(initialSolution);
		
		// By now this is our best solution
		bestSolution = new OptimizationSolution(initialSolution);
	}

	@Override
	protected ExamPair getNextPair(int nextPairIndex) throws IndexOutOfBoundsException {
		/**
		 * The most penalizing pair is returned at the very first
		 * Tabu Search iteration, avoiding sorting all other exam
		 * pairs as done below.
		 */
		if(nextPairIndex == 0)
			return ((OptimizationSolution)currentSolution).getMostPenalizingPair();
		
		/**
		 * If the most penalizing exam has an empty valid neighborhood,
		 * the other exam pairs should be sorted according to their
		 * fitness value just once.
		 */
		if(nextPairIndex == 1)
			Collections.sort(currentSolution.getPenalizingPairs());
		
		return currentSolution.getPenalizingPairs().get(nextPairIndex);
	}

	@Override
	protected void updateBestSolution() {
		if(currentSolution.getFitness() < bestSolution.getFitness())
			bestSolution = new OptimizationSolution((OptimizationSolution)currentSolution);
	}	
}