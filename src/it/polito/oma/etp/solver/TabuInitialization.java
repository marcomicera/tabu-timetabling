package it.polito.oma.etp.solver;

public class TabuInitialization extends TabuSearch {
	public TabuInitialization(InitializationSolution initialSolution) {
		// Initially, the current solution is the initial one
		currentSolution = new InitializationSolution(initialSolution);
		
		// By now this is our best solution
		bestSolution = new InitializationSolution(initialSolution);
	}
}
