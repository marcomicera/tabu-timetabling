package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InstanceData;

public class GeneticAlgorithm {
	protected Settings settings;
	protected InstanceData instance;
	
	public GeneticAlgorithm(Settings settings, InstanceData instance) {
		this.settings = settings;
		this.instance = instance;
	}

	public Solution solve() {
		// Population generation
		Population initialPopulation = new Population(instance, settings);
		
		//while(...) {
			// Parents selection (for children generation)
			
			
			// Crossover (generating new children)
			
			
			// Chromosomes substitution (which chromosome survives)
			
		//}
		
		// TODO complete
		return null;
	}
}
