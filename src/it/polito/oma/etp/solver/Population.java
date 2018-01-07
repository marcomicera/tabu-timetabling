package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;

import it.polito.oma.etp.reader.InstanceData;

public class Population {
	protected ArrayList<Solution> population = new ArrayList<Solution>();
	protected float totalFitness;
	protected float totalInverseFitness;
	
	/**
	 * Genetic Algorithm used to find the initial
	 * feasible solution.
	 */
	public Population(InstanceData instance, GaSettings gaSettings, TsSettings tbSettings) {
		// Generating random population
		for(int i = 0; i < gaSettings.initialPopulationSize; ++i) {
			// Adding a new randomly-generated chromosome to the population
			Solution tempSolution = Solution.generateUnfeasibleSolution(instance, tbSettings);
			population.add(tempSolution);
			
			// Total fitness measures update
			totalFitness += tempSolution.getFitness();
			totalInverseFitness += 1/tempSolution.getFitness();
		}
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
}
