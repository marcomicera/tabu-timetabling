package it.polito.oma.etp.solver;

public class TabuOptimization extends TabuSearch {
	public TabuOptimization(InitializationSolution initialSolution) {
		// Initially, the current solution is the initial one
		currentSolution = new OptimizationSolution(initialSolution);
		
		// By now this is our best solution
		bestSolution = new OptimizationSolution(initialSolution);
	}
}