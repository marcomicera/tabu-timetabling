package it.polito.oma.etp.solver.optimization;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.Population;
import it.polito.oma.etp.solver.Solution;
import it.polito.oma.etp.solver.initialization.InitializationPopulation;
import it.polito.oma.etp.solver.initialization.InitializationSolution;

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
		for(Solution chromosome: initialPopulation.getChromosomes()) {
			OptimizationSolution tempSolution = new OptimizationSolution((InitializationSolution)chromosome);
			chromosomes.add(tempSolution);
			
			// Total fitness measures update
			totalFitness += tempSolution.getFitness();
			totalInverseFitness += 1/tempSolution.getFitness();
			
			// Updating bestSolution
			if(bestSolution > tempSolution.getFitness())
				bestSolution = tempSolution.getFitness();
			
			// Updating worstSolution
			if(worstSolution < tempSolution.getFitness())
				worstSolution = tempSolution.getFitness();
		}
	}
}
