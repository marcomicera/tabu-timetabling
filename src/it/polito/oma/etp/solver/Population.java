package it.polito.oma.etp.solver;

import java.util.Set;

import it.polito.oma.etp.reader.InstanceData;

public class Population {
	protected Set<Solution> population;
	protected float totalFitness;
	
	/**
	 * Genetic Algorithm used to find the initial
	 * feasible solution.
	 */
	public Population(InstanceData instance, Settings settings) {
		// Generating random population
		for(int i = 0; i < settings.gaInitialPopulationSize; ++i) {
			Solution tempSolution = Solution.generateUnfeasibleSolution(instance, settings);
			population.add(tempSolution);
			totalFitness += tempSolution.getFitness();
		}
		
		initializeRelativeFitnessValues();
	}
	
	/**
	 * Computes the relative fitness value for all chromosomes
	 * belonging to the population
	 */
	private void initializeRelativeFitnessValues() {
		for(Solution solution: population)
			solution.setParentSelectionProbability(solution.getFitness() / totalFitness);
	}
}
