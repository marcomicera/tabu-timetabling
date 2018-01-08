package it.polito.oma.etp.solver.optimization;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.GeneticAlgorithm;
import it.polito.oma.etp.solver.GeneticSettings;
import it.polito.oma.etp.solver.initialization.InitializationPopulation;

public class GeneticOptimization extends GeneticAlgorithm {
	/**
	 * Genetic Algorithm implementation for finding the timetabling
	 * problem's best solution (optimization problem), 
	 * @param instance				instance problem data.
	 * @param settings				Genetic Algorithm settings.
	 * @param initialPopulation		initial feasible solution the Genetic
	 * 								Algorithm must start with.
	 */
	public GeneticOptimization(	InstanceData instance,
								GeneticSettings settings,
								InitializationPopulation initialPopulation
	) {
		super(instance, settings);
		
		population = new OptimizationPopulation(
			instance,
			initialPopulation
		);
	}
}
