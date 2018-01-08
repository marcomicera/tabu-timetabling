package it.polito.oma.etp.solver.initialization;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.Population;
import it.polito.oma.etp.solver.Solution;

public class InitializationPopulation extends Population {
	/**
	 * Population constructor for finding a feasible solution (initialization
	 * problem), creating a random initial population.
	 * @param instance					instance problem data.
	 * @param initialPopulationSize		how many chromosome the initial population should have.
	 * @param firstRandomSolution		true if initial infeasible solutions must be computed randomly.
	 * 									If false, our ad-hoc deterministic algorithm tries to obtain 
	 * 									an initial solution with the minimum number of conflicts.
	 */
	public InitializationPopulation(	InstanceData instance, 
										int initialPopulationSize, 
										boolean firstRandomSolution
	) {
		super(instance);
		
		// By now, there's not worst and better solution
		bestSolution = null;
		worstSolution = null;
		
		// Generating random population
		/*for(int i = 0; i < initialPopulationSize; ++i) {
			// Infeasible solution generation
			InitializationSolution tempSolution = Solution.generateInfeasibleSolution(instance, firstRandomSolution);
			
			// Adding an infeasible solution to the population
			chromosomes.add(tempSolution);
			
			// Total fitness measures update
			totalFitness += tempSolution.getFitness();
			totalInverseFitness += 1/tempSolution.getFitness();
			
			// Initializing bestSolution
			if(bestSolution == null)
				bestSolution = new InitializationSolution(tempSolution);
			else if(bestSolution.getFitness() > tempSolution.getFitness())
				bestSolution = new InitializationSolution(tempSolution);
			
			// Initializing worstSolution
			if(worstSolution == null)
				worstSolution = new InitializationSolution(tempSolution);
			else if(worstSolution.getFitness() < tempSolution.getFitness())
				worstSolution = new InitializationSolution(tempSolution);
		}*/
	}
}
