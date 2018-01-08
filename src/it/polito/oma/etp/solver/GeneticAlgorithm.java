package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.initialization.InitializationSolution;
import it.polito.oma.etp.solver.optimization.OptimizationSolution;

public class GeneticAlgorithm {
	protected TsSettings tbSettings;
	protected GaSettings gaSettings;
	protected InstanceData instance;
	protected Population population;
	protected int iteration = 0;
	protected Solution bestSolution;
	
	/**
	 * Children mutation is made following
	 * a dynamic probability.
	 */
	protected double mutationProbability;
	
	public GeneticAlgorithm(TsSettings tbSettings, GaSettings gaSettings, InstanceData instance) {
		this.tbSettings = tbSettings;
		this.gaSettings = gaSettings;
		this.instance = instance;
		
		mutationProbability = gaSettings.mutationProbabilityInitialValue;
	}

	public Solution solve() {
		// Population generation
		
		population = new Population(instance, gaSettings, tbSettings);
		bestSolution = population.getBestSolution();
		/*	it's very important that the population is orderd in order to 
		 * 	select the correct parents that will generate childrens.
		 */
		Collections.sort(population.getPopulation());
		
		/*TODO debug*/ System.out.println("Population: " +Arrays.toString(population.getPopulation().toArray()));
		
		while(bestSolution.getFitness() > 0 /*TODO timer //&& !TIMER_EXPIRED*/) {
			/*TODO debug (iteration)*/System.out.println("\n***** Iteration " + iteration + " *****");
			
			// Mutation probability management
			if(iteration > gaSettings.mutationProbabilityManagementThreshold)
				updateMutationProbability();
			
			// ***** Parents selection (for children generation) *********
		
			/* parents:	array list that contains the parents that will generate children.
			 * 			the size of the array depends on the numberOfReproductiveParents
			 * 			of the GASettings
			 */
			ArrayList<Solution> parents = new ArrayList<Solution>();
			
			// Random parent selection using the Cumulative Reproduction Probability
			if(gaSettings.randomParentSelection) {				
				double[] CRP = generateCRP(population);
				//for(int i = 0; i < gaSettings.numberOfReproductiveParents; i++)
				//	parents.add(selectRandomParent(CRP, population));
				parents = selectRandomChromosome(CRP, population, gaSettings.numberOfReproductiveParents);
						
				/*TODO debug*/ System.out.println("Parents: "+parents);
			}
			// Deterministic parent selection using their fitness
			else {
				// Best parents (having a low fitness value) are selected for reproduction 
				for(int i = 0; i < gaSettings.numberOfReproductiveParents; ++i)
					parents.add(population.getPopulation().get(i));
			}
			
			// ***************** Crossover (generating new children) ******************
			
			/* TODO test the ordered crossover*/
			
			// in this case we have to generate random cutting points and so setting the value
			// whereToCut of GaSettings.
			if(gaSettings.randomCuttingPoint)
				generateRandomCuttingPoints();

			ArrayList<Solution> children = new ArrayList<Solution>();
			//TODO implement algorithm to generate more then 2 children
			children.add(crossover(parents.get(0), parents.get(1)));
			children.add(crossover(parents.get(1), parents.get(0)));
			/*TODO debug*/System.out.println("Childrens: " + children);
			
			//********************* end crossover *************************************

			// Children mutation
			if(Utility.getRandomDouble(0, 1) < mutationProbability)
				for(Solution child: children)
					mutate(child, (int)(gaSettings.mutatingGenesPercentage * instance.getE()));
			
			// update of population with new children
			for(Solution child : children)
				population.add(child);
			
			// Chromosomes selection (which chromosome survives)
			ArrayList<Solution> chromosomesToKill;
			if(gaSettings.selectChromosomesToKillByRelativeFitness) {
				double[] CKP = generateCKP(population);
				chromosomesToKill = selectRandomChromosome(CKP, population, gaSettings.numberOfChildrenToGenerate);
			}
			else
				/*TODO implement*/throw new AssertionError("Random chromosomes to kill selection still to be implemented");
			/*TODO debug*/System.out.println("Chromosomes killed: " + chromosomesToKill);
			
			for(Solution chromosomes : chromosomesToKill)
				population.delete(chromosomes);
			
			/*TODO debug*/System.out.println("New population: " + Arrays.toString(population.getPopulation().toArray()) + "\n");
			
			
			
			// TODO check clones
			
			// TODO update best solution
			
			++iteration;

		} // while end
		
		return bestSolution;
	}
	
	private void updateMutationProbability() {
		// Checking if its value it's already the maximum one 
		if(mutationProbability >= gaSettings.mutationProbabilityMaximumValue)
			return;
		
		double deltaFitnessRatio = 
			(
				(population.getWorstSolution().getFitness() - population.getBestSolution().getFitness())
					/ 
				population.getBestSolution().getFitness()
			) * gaSettings.mutationProbabilityConvergenceRatio;
		;
		
		// Updating the mutation probability
		mutationProbability = 
			((deltaFitnessRatio >= 1) ? 0 : (1 - deltaFitnessRatio))
				*
			(gaSettings.mutationProbabilityMaximumValue - gaSettings.mutationProbabilityMinimumValue)
				+
			gaSettings.mutationProbabilityMinimumValue
		;
		
		if(mutationProbability > gaSettings.mutationProbabilityMaximumValue)
			throw new AssertionError("Mutation probability is greater than its maximum value");
	}
	
	/*
	 * ***************must be tested!!!*********************
	 * 
	 * the first parent give the genes inside the cutting points, the second
	 * is scanned in order to implement the ordered crossover.
	 * 
	 * @param parent1 the parent that give the genes inside the cutting points
	 * @param parent2 the parent that is scanned in order to implement the ordered crossover.
	 * @return children the children generated by the algorithm.
	 */
	public Solution crossover(Solution parent1, Solution parent2) {
		// initialization of the children and it's schedule
					Solution children = null;
					int[] childrenSchedule = new int[instance.getE()];
						
						int[] parentSchedule1 = parent1.schedule;
						int[] parentSchedule2 = parent2.schedule;
						
						//setting the part of the children schedule that is equal to the parent schedule part
						for (int i = gaSettings.whereToCut[0]; i <= gaSettings.whereToCut[1]; i++) {
							childrenSchedule[i] = parentSchedule1[i];
						}
						
						
						// the starting point is the first exam after the biggest cutting point.
						// the wheretoCut array must be ordered.
						
						//children 1 generation -> we use parent 2!
						int startingExam = gaSettings.whereToCut[1]+1; 
						int exam = startingExam;
						int childrenExamToSet = startingExam;
							do {
								   int parentTimeSlot = parentSchedule2[exam];
								   
								   //if it isn't an initialization problem we have to check the 
								   //feasibility of the move
								   if(!gaSettings.initializationProblem) { 
									  if(isFeasible(parentTimeSlot, exam, childrenSchedule, gaSettings.whereToCut[0])) {
										  //we update the children schedule only if it is feasible
										  //considering the current exam already set.
										  childrenSchedule[childrenExamToSet] = parentTimeSlot;
										  //we increment childrenExamToSet here 
										  //because if this exam generate an infeasible solution we have to 
										  //try with the next exam of the parent in order to set the current
										  //exam of the children that isn't set jet.
										  childrenExamToSet++;
										  	if(childrenExamToSet>=instance.getE()) childrenExamToSet = 0;
									  }
								   }
								   else { // this is the case of INITIALIZATION PROBLEM
									   	  // simple crossover -> we haven't to check the feasibility
									   	  // so we always set the exam 
										childrenSchedule[childrenExamToSet] = parentTimeSlot;
									   	childrenExamToSet++;
									  	if(childrenExamToSet>=instance.getE()) childrenExamToSet = 0;
								   }
								   
								   exam++; // the next exam of the parent
								   if(exam == instance.getE()) exam = 0;
								   
						    // continue until all exams are scheduled in the children schedule
							} while(childrenExamToSet != gaSettings.whereToCut[0]);
						
						/*TODO debug*/ //System.out.println("children schedule= "+Arrays.toString(childrenSchedule));
						/*TODO debug 
						for (int i = gaSettings.whereToCut[0]; i <= gaSettings.whereToCut[1]; i++) {
							
							//they must be equal!!!!!!!!!!!!!!
							//System.out.println("parent exam = "+i+" time slot = "+parentSchedule1[i]);
							//System.out.println("children exam = "+i+" time slot = "+childrenSchedule[i]);
						}*/
					
					int[][] te = generateTe(childrenSchedule);
					   if(!gaSettings.initializationProblem)
						   children = new OptimizationSolution(instance, te);
					   else
						   children = new InitializationSolution(instance, te);
	
				return children;
	}				 
	
	/**
	 * Performs the mutation operator on the specified chromosome.
	 * @param mutatingChromosome	the mutating chromosome
	 * @param mutatingGenesNumber	number of genes that will be modified during each mutation. 
	 */
	public void mutate(Solution mutatingChromosome, int mutatingGenesNumber) {
		// Set of already-mutated gene indexes
		Set<Integer> mutatedGenesIndexes = new HashSet<Integer>(instance.getE());
		
		// For each gene that must be mutated
		for(int i = 0; i < mutatingGenesNumber; ++i) {
			// Random gene index generator
			int mutatingGeneIndex;
			do {
				mutatingGeneIndex = Utility.getRandomInt(0, instance.getE() - 1);
			} while(mutatedGenesIndexes.contains(mutatingGeneIndex));
			mutatedGenesIndexes.add(mutatingGeneIndex);
			
			// Random new gene value generator
			
			// True when a new feasible gene value has been found
			boolean newFeasibleGeneValueFound = false;
			while(!newFeasibleGeneValueFound) {
				// New gene value generation
				int newGeneValue = Utility.getRandomInt(0, instance.getTmax() - 1);
				
				try {
					/**
					 * Performing the move: if the corresponding neighbor is infeasible, an
					 * exception is thrown
					 */
					mutatingChromosome.move(mutatingChromosome.getNeighbor(mutatingGeneIndex, newGeneValue));
					
					/**
					 * If the code reaches this point, the corresponding neighbor is feasible,
					 * so the move has been performed successfully
					 */
					newFeasibleGeneValueFound = true;
				} catch(InvalidMoveException e) {
					/**
					 *  The neighbor corresponding to the move was not feasible, a new
					 *  random gene value has to be computed
					 */
					continue;
				}
			}
		}
	}
	
	/*
	 * randomly generation of the cutting points values. if the number of cutting cutting points
	 * in the settings is 1, only one value is generated, and the other one is set to 0.
	 * otherwise, two random value are generated.
	 * all random values must be greater then 0, because if we have more then one cutting points 
	 * we don't wont a zero in the whereToCut array.
	 * 
	 * the function set the value of whereToCut in the gaSettings.
	 */
	
	private void generateRandomCuttingPoints() {
		int firstRandomCuttingPoint = Utility.getRandomInt(1, instance.getE()-1);
		/*TODO debug*/ System.out.println("first random cutting point " + firstRandomCuttingPoint);
		int[] cuttingPoints = new int[2];
		if(gaSettings.cuttingPointsNumber == 1) {
			cuttingPoints[0] = 0;
			cuttingPoints[1] = firstRandomCuttingPoint;
		}
		else{
			int secondRandomCuttingPoint = Utility.getRandomInt(1, instance.getE()-1);
			while (secondRandomCuttingPoint == firstRandomCuttingPoint) {
				secondRandomCuttingPoint = Utility.getRandomInt(1, instance.getE());
			}
			/*TODO debug*/ System.out.println("second random cutting point " + secondRandomCuttingPoint);

			
			if(firstRandomCuttingPoint < secondRandomCuttingPoint) {
				cuttingPoints[0] = firstRandomCuttingPoint; cuttingPoints[1]=secondRandomCuttingPoint;}
				else {
					cuttingPoints[0] = secondRandomCuttingPoint; cuttingPoints[1]=firstRandomCuttingPoint;}

		}
		gaSettings.whereToCut = cuttingPoints;
		/*TODO debug*/System.out.println("random cutting points: "+Arrays.toString(gaSettings.whereToCut));

	}

	
	/*
	 * generate the te matrix starting from the schedule
	 * @param schedule
	 */
	private int[][] generateTe(int[] schedule){
		int[][] te = new int[instance.getTmax()][instance.getE()];
			for (int exam = 0; exam < instance.getE(); exam++) {
				te[schedule[exam]][exam] = 1;
			}		
		return te;
		}
	
	/*
	   * it's used to check if the exam that we wont to put in the time slot given as
	   * parameter is a feasible move considering the actual children schedule set
	   * so far.
	   * 
	   * @param TimeSlot  the time slot we wont to use for the exam given by currentExam param
	   * @param currentExam we are setting the children schedule, and this is the current exam
	   *                   we want to schedule
	   * @param childrenSchedule  the childrenSchedule that dynamically change by inserting 
	   *                 exams in the order given by it's parent.
	   * 
	   * @return isFeasible true if we can put the currentExam in the TimeSlot given by param
	   */
	  private boolean isFeasible(int timeslot, int currentExam, int[] childrenSchedule, int startingExam) {
	    boolean isFeasible = true;
	    
	    //int startingExam = gaSettings.whereToCut[0]; 
	    int exam = startingExam;
	    
	      do {
	           if(instance.getN()[currentExam][exam]>0 && 
	            timeslot == childrenSchedule[exam])
	             isFeasible = false;
	           exam++;
	           if(exam == instance.getE()) exam = 0;

	      } while(exam != startingExam && !isFeasible);
	    
	    return isFeasible;
	  }

	
	/**
	 * Returns the Cumulative Reproduction Probability of each element of the population.
	 * 
	 * EXAMPLE:
	 * 
	 * 			FITNESS		PERCENTAGE FIT  		     	  		  CRP
	 * 			   8	    (1/8) / 1/8+1/7+1/8+1/8		    (1/8) / 1/8+1/7+1/8+1/8
	 * 			   7	    (1/7) / 1/8+1/7+1/8+1/8      (1/8 + 1/7) / 1/8+1/7+1/8+1/8
	 * 			   8	    (1/8) / 1/8+1/7+1/8+1/8     (1/8 + 1/7 + 1/8) / 1/8+1/7+1/8+1/8
	 * 			   8	    (1/8) / 1/8+1/7+1/8+1/8 				   1		
	 * 
	 * 
	 * @param 		the population of which we'll calculate the relative fitness.
	 * @return		Cumulative Reproduction Probability of each element of the population.
	 */
	private double[] generateCRP(Population population) {
		double[] CRP = new double[gaSettings.initialPopulationSize];
		
		double cumulativeInverseSum = 0;
		for(int i = 0; i < population.getPopulation().size() - 1; ++i) {
			cumulativeInverseSum += 1 / population.getPopulation().get(i).getFitness();
			CRP[i] = cumulativeInverseSum / population.getTotalInverseFitness();
		}
		CRP[population.getPopulation().size() - 1] = 1;
		
		/*TODO debug*/System.out.println("CRP: "+Arrays.toString(CRP));
		
		return CRP;
	}
	
	/**
	 * Cumulative Killing Probability generator function, starting from a given population.
	 * @param population	population 
	 * @return
	 */
	private double[] generateCKP(Population population) {
		double[] CKP = new double[gaSettings.initialPopulationSize+gaSettings.numberOfChildrenToGenerate];
		
		double cumulativeSum = 0;
		for(int i = 0; i < population.getPopulation().size() - 1; ++i) {
			cumulativeSum += population.getPopulation().get(i).getFitness();
			CKP[i] = cumulativeSum / population.getTotalFitness();
		}
		CKP[population.getPopulation().size() - 1] = 1;
		
		/*TODO debug*/System.out.println("CKP: "+Arrays.toString(CKP));
		
		return CKP;
	}
	
	/*(tested)
	 * in order to select a parent accordingly with a probability we
	 * use the range between consecutive values of CRP
	 */
	
	private ArrayList<Solution> selectRandomChromosome(double[] relativeFitness, Population population, int numberOfChromosome) {
		
		ArrayList<Solution> chromosomes = new ArrayList<Solution>();
		
		// to avoid the selection of the same parents
		Integer[] selectedNum = new Integer[numberOfChromosome];
		int numberOfSelectedParents = 0;
		
		// already selected numbers
		// generate a random number in the range [0,1]
		Random rnd = new Random();
		
		while (numberOfSelectedParents!=numberOfChromosome) {
			// Returns the next pseudo-random, uniformly distributed double value between 0.0 and 1.0 
			double randomNum = rnd.nextDouble();
			
			/*
			 * example:  CRP= [0,26; 0,48; 0,78; 1]
			 * 			 if the random value is 0,3
			 * 				 -> we select the second element of population
			 */
			if(randomNum <= relativeFitness[0] &&
					!Arrays.asList(selectedNum).contains(1)) {
						chromosomes.add(population.getPopulation().get(0));
						//update the selected numbers in order to avoid to select same parents
						selectedNum[numberOfSelectedParents] = 1;
						numberOfSelectedParents++;
			}
			else
				for (int i = 1; i < relativeFitness.length; i++) {
					if(randomNum <= relativeFitness[i] 
							&& randomNum>relativeFitness[i-1] 
									&& !Arrays.asList(selectedNum).contains(i+1)) {						
											chromosomes.add(population.getPopulation().get(i));	
											//update the selected numbers in order to avoid to select same parents
											selectedNum[numberOfSelectedParents] = i+1;
											numberOfSelectedParents++;
										}
				}
		}
		
		return chromosomes;
	}
}
























