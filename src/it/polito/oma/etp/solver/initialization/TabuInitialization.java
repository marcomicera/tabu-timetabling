package it.polito.oma.etp.solver.initialization;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.ExamPair;
import it.polito.oma.etp.solver.Solution;
import it.polito.oma.etp.solver.TabuSearch;
import it.polito.oma.etp.solver.TsSettings;

public class TabuInitialization extends TabuSearch {
	/**
	 * Default constructor, creating a Tabu Search implementation for computing
	 * the first feasible solution.
	 * @param instance			The given problem instance data.
	 * @param settings				This Tabu Search implementation settings.
	 * @param firstRandomSolution	If true, the first infeasible solution is
	 * 								computed randomly; otherwise, our ad-hoc
	 * 								deterministic algorithm tries to obtain an
	 * 								initial solution with the minimum number of
	 * 								conflicts.
	 */
	public TabuInitialization(InstanceData instance, TsSettings settings) {
		super(instance, settings);
		
		// Initially, the current solution is the initial one
		currentSolution = Solution.generateUnfeasibleSolution(instance, settings);
		
		// By now this is our best solution
		bestSolution = new InitializationSolution(currentSolution);
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
}
