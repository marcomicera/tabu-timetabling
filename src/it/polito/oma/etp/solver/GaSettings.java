package it.polito.oma.etp.solver;

/**
 * Genetic Algorithm settings used for tuning
 */
public class GaSettings {
	/*
	 * to know if it's an initialization problem or not (in this case 
	 * we have an optimization problem)
	 */
	public boolean initializationProblem;
	
	/**
	 * How many chromosomes belong to the initial population used by
	 * the Genetic Algorithm.
	 */
	public int initialPopulationSize;
	
	/**
	 * if true we select a parent that will generate children by using it's fitness
	 * as probability. if false we don't use probability to select the parents but
	 * just the value of the fitness.
	 */
	public boolean randomParentSelection;
	
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
	 * we have to set where we wont to cut. the array must be ordered.
	 */
	public boolean randomCuttingPoint;
	
	/**
	 * if we don't use the random selection of cutting points we have to decide where
	 * the cut will be done. in particular the first number (for ex.) indicate the first
	 * gene that will not be changed.
	 * it's dimension must be equal to "cuttingPointsNumber".
	 */
	public int[] whereToCut;

	public GaSettings(boolean initializationProblem, int InitialPopulationSize, boolean selectParentsByRelativeFitness,
			int numberOfReproductiveParents, boolean selectChromosomesToKillByRelativeFitness,
			double mutationProbability, double crossoverProbability, double populationPercentageReplacement,
			int cuttingPointsNumber, boolean randomCuttingPoint, int[] whereToCut) {
		this.initializationProblem = initializationProblem;
		this.initialPopulationSize = InitialPopulationSize;
		this.randomParentSelection = selectParentsByRelativeFitness;
		this.numberOfReproductiveParents = numberOfReproductiveParents;
		this.selectChromosomesToKillByRelativeFitness = selectChromosomesToKillByRelativeFitness;
		this.mutationProbability = mutationProbability;
		this.crossoverProbability = crossoverProbability;
		this.populationPercentageReplacement = populationPercentageReplacement;
		this.cuttingPointsNumber = cuttingPointsNumber;
		this.randomCuttingPoint = randomCuttingPoint;
		
		//The dimension of the whereToCut array must be equal to the number of cutting points
		
		if(randomCuttingPoint == false && whereToCut.length == cuttingPointsNumber && cuttingPointsNumber<=2)
		this.whereToCut = whereToCut;
		else if(whereToCut.length != cuttingPointsNumber)
			System.err.println("The dimension of the whereToCut array must be equel to the number of cutting points: "
								+ cuttingPointsNumber );
		else if(cuttingPointsNumber>2)
			System.err.println("the maximum number of cutting points is 2" );
	}

	public GaSettings(boolean initializationProblem, int PopulationSize, int numberOfReproductiveParents, boolean selectParentsByRelativeFitness,
				      int cuttingPointsNumber, boolean randomCuttingPoint, int[] whereToCut) {
		this.initializationProblem = initializationProblem;
		this.initialPopulationSize = PopulationSize;
		this.numberOfReproductiveParents = numberOfReproductiveParents;
		this.randomParentSelection = selectParentsByRelativeFitness;
		this.cuttingPointsNumber = cuttingPointsNumber;
		this.randomCuttingPoint = randomCuttingPoint;
		this.whereToCut = whereToCut;
	}
	
	
	
	
	
	
	
	
	
	
	
	
}




























