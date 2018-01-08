package it.polito.oma.etp.solver;

public class Settings {
	/**
	 * If true, the first infeasible solution is computed randomly; 
	 * otherwise, our ad-hoc deterministic algorithm tries to obtain 
	 * an initial solution with the minimum number of conflicts.
	 */
	public boolean firstRandomSolution;
	
	/**
	 * How many solutions are needed to start the algorithm.
	 * It will be forced to 1 if the optimization problem will be
	 * solved by the Tabu Search algorithm, since the latter just 
	 * needs a single feasible solution to start with.
	 */
	public int initialPopulationSize;
	
	/**
	 * True if the initialization problem will be solved by
	 * the Genetic Algorithm.
	 * False if it will be solved by the Tabu Search algorithm.
	 */
	public boolean geneticInitialization;
	
	/**
	 * True if the optimization problem will be solved by
	 * the Genetic Algorithm.
	 * False if it will be solved by the Tabu Search algorithm.
	 */
	public boolean geneticOptimization;

	public Settings(boolean firstRandomSolution, 
					int initialPopulationSize, 
					boolean geneticInitialization,
					boolean geneticOptimization
	) {
		this.firstRandomSolution = firstRandomSolution;
		
		/**
		 * If the optimization problem will be solved by the Tabu Search algorithm,
		 * the latter will just need a single feasible solution to start with.
		 */
		this.initialPopulationSize = (geneticOptimization == true) ? initialPopulationSize : 1;
		
		this.geneticInitialization = geneticInitialization;
		this.geneticOptimization = geneticOptimization;
	}

	/**
	 * Copy constructor used to establish common settings among
	 * different solving algorithms.
	 * @param otherSettings		commong settings to be set among
	 * 							different solving algorithms.
	 */
	public Settings(Settings otherSettings) {
		firstRandomSolution = otherSettings.firstRandomSolution;
		initialPopulationSize = otherSettings.initialPopulationSize;
		geneticInitialization = otherSettings.geneticInitialization;
		geneticOptimization = otherSettings.geneticOptimization;
	}
}
