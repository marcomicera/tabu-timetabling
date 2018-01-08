package it.polito.oma.etp.solver.optimization;

import java.util.ArrayList;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.Population;
import it.polito.oma.etp.solver.initialization.InitializationPopulation;

public class OptimizationPopulation extends Population {
	/**
	 * Population constructor for finding the timetabling problem's best
	 * solution (optimization problem), starting from a population of
	 * initial feasible solutions.
	 * @param instance				instance problem data.
	 * @param initialPopulation		initial feasible solutions the Genetic
	 * 								Algorithm must start with.
	 */
	public OptimizationPopulation(	InstanceData instance,
									InitializationPopulation initialPopulation
	) {
		super(instance);
		
		// Converting the population type
		chromosomes = new ArrayList<>(initialPopulation.getChromosomes());
		totalFitness = initialPopulation.getTotalFitness();
		totalInverseFitness = initialPopulation.getTotalInverseFitness();
		bestSolution = initialPopulation.getBestSolution();
		worstSolution = initialPopulation.getWorstSolution();
	}
}
