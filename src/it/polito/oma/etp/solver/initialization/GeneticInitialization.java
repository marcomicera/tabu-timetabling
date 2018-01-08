package it.polito.oma.etp.solver.initialization;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.GeneticAlgorithm;
import it.polito.oma.etp.solver.GeneticSettings;

public class GeneticInitialization extends GeneticAlgorithm {
	/**
	 * Genetic Algorithm implementation for finding a feasible
	 * solution (initialization problem), creating an initial population.
	 * @param instance		instance problem data.
	 * @param settings		Genetic Algorithms settings.
	 */
	public GeneticInitialization(InstanceData instance, GeneticSettings settings) {
		super(instance, settings);
		
		population = new InitializationPopulation(
			instance,
			settings.initialPopulationSize,
			settings.firstRandomSolution
		);
	}
}
