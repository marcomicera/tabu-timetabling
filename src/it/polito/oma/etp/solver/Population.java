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
				Arrays.toString(population.toArray())
		;
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
