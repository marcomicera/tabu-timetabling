package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;

import it.polito.oma.etp.reader.InstanceData;

public class Population {
	protected ArrayList<Solution> population = new ArrayList<Solution>();
	protected float inverseTotaleFitness;
	
	/**
	 * Genetic Algorithm used to find the initial
	 * feasible solution.
	 */
	public Population(InstanceData instance, GaSettings gaSettings, Settings tbSettings) {
		// Generating random population
		for(int i = 0; i < gaSettings.PopulationSize; ++i) {
			Solution tempSolution = Solution.generateUnfeasibleSolution(instance, tbSettings);
			population.add(tempSolution);
			inverseTotaleFitness += 1/tempSolution.getFitness();
		}
		
		initializeRelativeFitnessValues();
	}
	
	/**
	 * Computes the relative fitness value for all chromosomes
	 * belonging to the population.
	 *  
	 * 					    1/single element fitness
	 * relative	fitness = ------------------------------
	 * 			      	  SUM[1/single element fitness]
	 * 
	 * we keep 1/F because we wont higher relative fitness for lower obj_function.
	 */
	private void initializeRelativeFitnessValues() {
		for(Solution solution: population) {
			//solution.setParentSelectionProbability((1/solution.getFitness()) / (1/totalFitness));
			solution.setParentSelectionProbability(1/solution.getFitness()/ inverseTotaleFitness);
			
			/*TODO debug*/
			System.out.println("fitness: "+solution.getFitness()+
							" total fitness: "+inverseTotaleFitness+
							" probability: "+ solution.getParentSelectionProbability());
		
		}
	}

	@Override
	public String toString() {
		return Arrays.toString(population.toArray());
	}

	public ArrayList<Solution> getPopulation() {
		return population;
	}

	public float getTotalFitness() {
		return inverseTotaleFitness;
	}
	
	
	
}
















