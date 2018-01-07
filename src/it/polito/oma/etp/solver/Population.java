package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.initialization.InitializationSolution;
import it.polito.oma.etp.solver.optimization.OptimizationSolution;

public class Population {
	protected ArrayList<Solution> population = new ArrayList<Solution>();
	protected float totalFitness;
	protected float totalInverseFitness;
	
	protected Solution bestSolution;
	protected Solution worstSolution;
	
	/**
	 * Genetic Algorithm used to find the initial
	 * feasible solution.
	 */
	public Population(InstanceData instance, GaSettings gaSettings, TsSettings tbSettings) {
		bestSolution = null;
		worstSolution = null;
		// Generating random population
		for(int i = 0; i < gaSettings.initialPopulationSize; ++i) {
			// Adding a new randomly-generated chromosome to the population
			InitializationSolution tempSolution = Solution.generateUnfeasibleSolution(instance, tbSettings);
			population.add(tempSolution);
			
			// Total fitness measures update
			totalFitness += tempSolution.getFitness();
			totalInverseFitness += 1/tempSolution.getFitness();
			
			// Initializing bestSolution
			if(bestSolution == null) {
				bestSolution = new InitializationSolution(tempSolution);
			}
			else if(bestSolution.getFitness() > tempSolution.getFitness()) {
				bestSolution = new InitializationSolution(tempSolution);
			}
			
			// Initializing worstSolution
			if(worstSolution == null) {
				worstSolution = new InitializationSolution(tempSolution);
			}
			else if(worstSolution.getFitness() < tempSolution.getFitness()) {
				worstSolution = new InitializationSolution(tempSolution);
			}

		}
	}

	/**
	 * Generate the population for the optimization problem.
	 * @param population population of feasible solutions.
	 */
	public Population(Population population) {
		//TODO create body
	}

	@Override
	public String toString() {
		return	"Population infos: " + 
				population.size() + " chromosomes, " + 
				"totalFitness: " + totalFitness + ", " +
				"totalInverseFitness: " + totalInverseFitness + "\n" +
				"Chromosomes: " + Arrays.toString(population.toArray())
		;
	}
	
	/**
	 * @param newChromosome to be added in the current Solution.
	 */
	public void add(Solution newChromosome) {
		population.add(newChromosome);
		totalInverseFitness += 1/newChromosome.getFitness();
		totalFitness += newChromosome.getFitness();
	}
	
	/**
	 * @param killedChromosome to be removed from the current population.
	 */
	public void delete(Solution killedChromosome) {
		population.remove(killedChromosome);
		totalInverseFitness -= 1/killedChromosome.getFitness();
		totalFitness -= killedChromosome.getFitness();
	}

	public ArrayList<Solution> getPopulation() {
		return population;
	}

	public float getTotalFitness() {
		return totalFitness;
	}

	public float getTotalInverseFitness() {
		return totalInverseFitness;
	}
	
	public Solution getBestSolution() {
		return bestSolution;
	}
	
	public Solution getWorstSolution() {
		return worstSolution;
	}
}
