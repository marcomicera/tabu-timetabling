package it.polito.oma.etp.solver;

public class GaSettings {
	
	/**
	 * How many chromosomes belong to the initial population used by
	 * the Genetic Algorithm.
	 */
	public int PopulationSize;
	
	/**
	 * if true we select a parent that will generate children by using it's fitness
	 * as probability. if false we don't use probability to select the parents but
	 * just the value of the fitness.
	 */
	public boolean selectParentsByRelativeFitness;
	
	/**
	 * the number of parents that will be used to generate childrens.
	 */
	public int numberOfReproductiveParents;
	
	/**
	 * if true we select a chromosome to kill (among population + children) by using 
	 * their relative fitness.
	 */
	public boolean selectChromosomesToKillByRelativeFitness;
	
	/**
	 * the probability to chose the mutation operand in order to create childrens.
	 * it should be dynamic (higher at first and lower after).
	 */
	public double mutationProbability;
	
	/**
	 * the probability to chose the crossover operand in order to create childrens.
	 * it should be dynamic (lower at first and higher after).
	 */
	public double crossoverProbability;
	
	/**
	 * percentage of population replacement
	 */
	public double populationPercentageReplacement;
	
	/**
	 * the number of cutting points we wont to use in order to generate childrens.
	 */
	public int cuttingPointsNumber;
	
	/**
	 * if true the point where the cut is done will be chosen randomly. if it's false
	 * we have to set where we wont to cut.
	 */
	public boolean randomCuttingPoint;
	
	/**
	 * if we don't use the random selection of cutting points we have to decide where
	 * the cut will be done.
	 * it's dimension must be equal to "cuttingPointsNumber".
	 */
	public int[] whereToCut;

	public GaSettings(int InitialPopulationSize, boolean selectParentsByRelativeFitness,
			int numberOfReproductiveParents, boolean selectChromosomesToKillByRelativeFitness,
			double mutationProbability, double crossoverProbability, double populationPercentageReplacement,
			int cuttingPointsNumber, boolean randomCuttingPoint, int[] whereToCut) {
		super();
		this.PopulationSize = InitialPopulationSize;
		this.selectParentsByRelativeFitness = selectParentsByRelativeFitness;
		this.numberOfReproductiveParents = numberOfReproductiveParents;
		this.selectChromosomesToKillByRelativeFitness = selectChromosomesToKillByRelativeFitness;
		this.mutationProbability = mutationProbability;
		this.crossoverProbability = crossoverProbability;
		this.populationPercentageReplacement = populationPercentageReplacement;
		this.cuttingPointsNumber = cuttingPointsNumber;
		this.randomCuttingPoint = randomCuttingPoint;
		
		//The dimension of the whereToCut array must be equal to the number of cutting points
		
		if(randomCuttingPoint == false && whereToCut.length == cuttingPointsNumber)
		this.whereToCut = whereToCut;
		else
			System.err.println("The dimension of the whereToCut array must be equel to the number of cutting points: "
								+ cuttingPointsNumber );
	}

	public GaSettings(int PopulationSize, int numberOfReproductiveParents, boolean selectParentsByRelativeFitness) {
		this.PopulationSize = PopulationSize;
		this.numberOfReproductiveParents = numberOfReproductiveParents;
		this.selectParentsByRelativeFitness = selectParentsByRelativeFitness;
	}
	
	
	
	
	
	
	
	
	
	
	
	
}




























