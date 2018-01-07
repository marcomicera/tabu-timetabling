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
	 * it's useful only for random generation of cutting points, becouse in a  
	 * deterministic case we always use 2 values, and if we wont to use just 1
	 * cutting point we have to set the first number of "whereToCut" to zero.
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
	
	/**
	 * number of children to generate.
	 */
	public int numberOfChildrenToGenerate;
	
	/**
	 * number of iterations to wait until the algorithm checks the presence of clones 
	 * in the population.
	 */
	public int cloningManagementThreshold;
	
	/**
	 * number of genes to mutate in the clone chromosomes.
	 */
	public int genesToMutateIfClones;
	
	public GaSettings(boolean initializationProblem, int PopulationSize, int numberOfReproductiveParents, boolean selectParentsByRelativeFitness,
				      int cuttingPointsNumber, boolean randomCuttingPoint, int[] whereToCut, 
				      int numberOfChildrenToGenerate, int cloningManagementThreshold, int genesToMutateIfClones) {
		this.initializationProblem = initializationProblem;
		this.initialPopulationSize = PopulationSize;
		this.numberOfReproductiveParents = numberOfReproductiveParents;
		this.randomParentSelection = selectParentsByRelativeFitness;
		this.cuttingPointsNumber = cuttingPointsNumber;
		this.randomCuttingPoint = randomCuttingPoint;
		this.whereToCut = whereToCut;
		this.numberOfChildrenToGenerate = numberOfChildrenToGenerate;
		this.cloningManagementThreshold = cloningManagementThreshold;
		this.genesToMutateIfClones = genesToMutateIfClones;
		
		//The dimension of the whereToCut array must be equal to the number of cutting points
		if(!randomCuttingPoint && cuttingPointsNumber == 1 && whereToCut[0]!=0) {
			System.err.println("in order to use one cutting point, the "
					+ "first element of whereToCut must be zero!!!");
			System.exit(0);
		}
		else if(!randomCuttingPoint && cuttingPointsNumber == 2 && whereToCut[0]==0)
			System.err.println("you have set 2 cutting points!!!!!!! \n"
					         + "but setting the first element of whereToCut to zero"
							 + " correspond to have only one cutting point!!!"
							 );
		
		if(randomCuttingPoint && whereToCut!=null)
			System.err.println("whereToCut should be null");
		if(!randomCuttingPoint && whereToCut==null) {
			System.err.println("in order to use deterministic cutting points, the "
					+ "whereToCut mustn't be null!!!");
			System.exit(0);
		}
	}
}