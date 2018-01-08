package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.initialization.InitializationSolution;
import it.polito.oma.etp.solver.optimization.OptimizationSolution;

public abstract class GeneticAlgorithm {
	protected InstanceData instance;
	protected GeneticSettings settings;
	protected Population population;
	protected int iteration = 0;
	protected Solution bestSolution;
	private static boolean TIMER_EXPIRED = false;
	
	/**
	 * Children mutation is made following
	 * a dynamic probability.
	 */
	protected double mutationProbability;
	
	/**
	 * Genetic Algorithm abstract class constructor, initializing
	 * properties common to all Genetic Algorithms specific
	 * implementations.
	 * @param instance		the only common data available from the
	 * 						beginning is the one describing the instance.
	 * @param settings		Genetic Algorithm settings, used for tuning.
	 */
	public GeneticAlgorithm(InstanceData instance, GeneticSettings settings) {
		this.instance = instance;
		this.settings = settings;

		// Initializing the mutation probability to its initial value
		mutationProbability = settings.mutationProbabilityInitialValue;
	}

	public Solution solve() {
		// Population generation

		bestSolution = population.getBestSolution();
		/*	it's very important that the population is orderd in order to 
		 * 	select the correct parents that will generate childrens.
		 */
		Collections.sort(population.getChromosomes());
		
		/*TODO debug*/ System.out.println("Population: " +Arrays.toString(population.getChromosomes().toArray()));
				
		while(bestSolution.getFitness() > 0 && !TIMER_EXPIRED) {
			/*TODO debug (iteration)*/System.out.println("\n***** Iteration " + iteration + " *****");

			if(population.getBestSolution().getFitness() > population.getWorstSolution().getFitness())
				throw new AssertionError(
					"wrong update best and worst solution" + 
					" \n best solution = " + population.getBestSolution() +
					" \n worst solution = " + population.getWorstSolution())
				;
			
			// Mutation probability management
			if(iteration > settings.mutationProbabilityManagementThreshold)
				updateMutationProbability();
			
			// ***** Parents selection (for children generation) *********
		
			/* parents:	array list that contains the parents that will generate children.
			 * 			the size of the array depends on the numberOfReproductiveParents
			 * 			of the GASettings
			 */
			ArrayList<Solution> parents = new ArrayList<Solution>();
			
			// Random parent selection using the Cumulative Reproduction Probability
			if(settings.randomParentSelection) {
				double[] CRP = generateCRP(population);
				//for(int i = 0; i < gaSettings.numberOfReproductiveParents; i++)
				//	parents.add(selectRandomParent(CRP, population));
				parents = selectRandomChromosome(CRP, population, settings.numberOfReproductiveParents);
						
				/*TODO debug*/ System.out.println("Parents: "+parents);
			}
			// Deterministic parent selection using their fitness
			else {
				// Best parents (having a low fitness value) are selected for reproduction 
				for(int i = 0; i < settings.numberOfReproductiveParents; ++i)
					parents.add(population.getChromosomes().get(i));
			}
			
			// ***************** Crossover (generating new children) ******************
			
			/* TODO test the ordered crossover*/
			
			// in this case we have to generate random cutting points and so setting the value
			// whereToCut of GaSettings.
			if(settings.randomCuttingPoint)
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
					mutate(child, (int)(settings.mutatingGenesPercentage * instance.getE()));
			
			// update of population with new children
			for(Solution child : children)
				population.add(child);
			
			// Chromosomes selection (which chromosome survives)
			ArrayList<Solution> chromosomesToKill;
			if(settings.selectChromosomesToKillByRelativeFitness) {
				double[] CKP = generateCKP(population);
				chromosomesToKill = selectRandomChromosome(CKP, population, settings.numberOfChildrenToGenerate);
			}
			else
				/*TODO implement*/throw new AssertionError("Random chromosomes to kill selection still to be implemented");
			/*TODO debug*/System.out.println("Chromosomes killed: " + chromosomesToKill);
			
			for(Solution chromosomes : chromosomesToKill)
				population.delete(chromosomes);
			
			population.updateWorstAndBestSolution();
			
			/*TODO debug*/System.out.println("New population: " + Arrays.toString(population.getChromosomes().toArray()) + "\n");
			
			if(iteration % settings.cloningManagementThreshold == 0) {
				checkClones();
			}
			
			// TODO update best solution
			
			++iteration;
			
			if(population.getBestSolution().getFitness() < bestSolution.getFitness()) {
				bestSolution = population.getBestSolution();
				System.out.println("Best solution found until now :" + bestSolution);
			}

		} // while end
		
		return bestSolution;
	}
	
	private void updateMutationProbability() {
		// Checking if its value it's already the maximum one 
		if(mutationProbability >= settings.mutationProbabilityMaximumValue)
			return;
		
		double deltaFitnessRatio = 
			(
				(population.getWorstSolution().getFitness() - population.getBestSolution().getFitness())
					/ 
				population.getBestSolution().getFitness()
			) * settings.mutationProbabilityConvergenceRatio;
		;
		
		// Updating the mutation probability
		mutationProbability = 
			((deltaFitnessRatio >= 1) ? 0 : (1 - deltaFitnessRatio))
				*
			(settings.mutationProbabilityMaximumValue - settings.mutationProbabilityMinimumValue)
				+
			settings.mutationProbabilityMinimumValue
		;
		
		if(mutationProbability > settings.mutationProbabilityMaximumValue)
			throw new AssertionError("Mutation probability is greater than its maximum value");
	}
	
	/*
	 * The first parent give the genes inside the cutting points, the second
	 * is scanned in order to implement the ordered crossover.
	 * 
	 * @param parent1 the parent that give the genes inside the cutting points
	 * @param parent2 the parent that is scanned in order to implement the ordered crossover.
	 * @return children the children generated by the algorithm.
	 */
	public Solution crossover(Solution parent1, Solution parent2) {
		// Child object initialization
		Solution child = null;
		
		// Child schedule initialization
		int[] childSchedule = new int[instance.getE()];
		
		// Retrieving parent schedules
		int[] parentSchedule1 = parent1.schedule;
		int[] parentSchedule2 = parent2.schedule;
		
		for (int examIterator = 0; examIterator < instance.getE(); examIterator++) {
			if(examIterator >= settings.whereToCut[0] &&
			   examIterator <= settings.whereToCut[1])
					childSchedule[examIterator] = parentSchedule2[examIterator];
			else
				childSchedule[examIterator] = parentSchedule1[examIterator];		
		}
		//TODO debug
		//System.out.println("parent1 schedule: " +Arrays.toString(parentSchedule1));
		//System.out.println("parent2 schedule: " +Arrays.toString(parentSchedule2));
		//System.out.println("child schedule: " +Arrays.toString(childSchedule));
	
		//TODO debug
		System.out.println("starting creossover");
		
		for (int examsInsideCut = settings.whereToCut[0]; examsInsideCut <= settings.whereToCut[1]; examsInsideCut++) {
			int childExamIterator = 0;
			
			// counts how many iteration the loop has done
			int loopIteration = 0;
			// threshold: when the algorithm reaches this value, the crossover is stopped and a new couple of parents is looked for.
			int maxLoopIteration = instance.getE()*10;
			
			while(childExamIterator < childSchedule.length) {
				++loopIteration;
				
				if(loopIteration >= maxLoopIteration) {
					if(settings.initializationProblem)
						child = new InitializationSolution(parent1);
					else
						child = new OptimizationSolution((OptimizationSolution)parent1);
					
					mutate(child, (int)settings.mutatingGenesPercentage*instance.getE());
					
					//TODO debug
					System.out.println("fail crossover -> mutation execution");
					return child;
				}
			//for (int childExamIterator = 0; 
				//	childExamIterator < childSchedule.length; 
				//	childExamIterator++) {
			
				if(childExamIterator!=examsInsideCut) {
					if(instance.getN()[examsInsideCut][childExamIterator]>0 &&
								childSchedule[childExamIterator]==childSchedule[examsInsideCut]) {
								//there is an infeasible pair, so we increment the value of the examsInsideCut timeSlot
								//in order to avoid new infeasible pairs between exams outside the cutted point.
								childSchedule[examsInsideCut] = (childSchedule[examsInsideCut]+1) % instance.getTmax();
								childExamIterator = 0;
								//TODO debug
								System.out.println("pair infeasible");
					}
					else //the pair childExamIterator-examsInsideCut is feasible
						childExamIterator++;
				}
				else //childExamIterator=examsInsideCut so we only increment the iterator
					childExamIterator++;

			}
			
		}
		
		//TODO debug
		System.out.println("end crossover");
		
		int[][] te = generateTe(childSchedule);
		   if(!settings.initializationProblem)
			   child = new OptimizationSolution(instance, te);
		   else
			   child = new InitializationSolution(instance, te);
				
		return child;
	}				 
	
	/**
	 * Performs the mutation operator on the specified chromosome.
	 * @param mutatingChromosome	the mutating chromosome
	 * @param mutatingGenesNumber	number of genes that will be modified during each mutation. 
	 */
	public void mutate(Solution mutatingChromosome, int mutatingGenesNumber) {
		// Set of already-mutated gene indexes
		
				//TODO debug 
				System.out.println("start mutate");
				
				Set<Integer> mutatedGenesIndexes = new HashSet<Integer>(instance.getE());
				
				// For each gene that must be mutated
				for(int i = 0; i < mutatingGenesNumber; ++i) {
					// Random gene index generator
					int mutatingGeneIndex;
					do {
						mutatingGeneIndex = Utility.getRandomInt(0, instance.getE() - 1);
					} while(mutatedGenesIndexes.contains(mutatingGeneIndex));
					//mutatedGenesIndexes.add(mutatingGeneIndex);
					
					// Random new gene value generator
					
					// True when a new feasible gene value has been found
					boolean newFeasibleGeneValueFound = false;
					
					//generate the initial time slot in a random way.
					int newGeneValue = Utility.getRandomInt(0, instance.getTmax() - 1);
					int initialTimeSlot = newGeneValue;
					
					while(!newFeasibleGeneValueFound) {
						// New gene value generation
						
						try {
							/**
							 * Performing the move: if the corresponding neighbor is infeasible, an
							 * exception is thrown
							 */
							mutatingChromosome.move(mutatingChromosome.getNeighbor(mutatingGeneIndex, newGeneValue));
							
							//if the exam is correctly rescheduled we add the exam to mutatedGenesIndexes
							mutatedGenesIndexes.add(mutatingGeneIndex);
							/**
							 * If the code reaches this point, the corresponding neighbor is feasible,
							 * so the move has been performed successfully
							 */
							newFeasibleGeneValueFound = true;
							
							
						} catch(InvalidMoveException e) {
							//e.printStackTrace();
							/**
							 *  The neighbor corresponding to the move was not feasible, a new
							 *  random gene value has to be computed
							 */
							
							newGeneValue = (newGeneValue+1) % instance.getTmax();
							if(newGeneValue == initialTimeSlot) {
								--i;
								break;
							}
							continue;
						}
					}
				}
				//TODO debug 
				System.out.println("end mutate");
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
		if(settings.cuttingPointsNumber == 1) {
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
		settings.whereToCut = cuttingPoints;
		/*TODO debug*/System.out.println("random cutting points: "+Arrays.toString(settings.whereToCut));

	}
	/**
	 * Every 'gaSettings.cloningManagementThreshold' iterations this method checks whether
	 * clones are present in the current population. If clones are found, they are mutated
	 * of a 
	 */
	//TODO we change all n clones or n-1 clones?
	private void checkClones() {
		ArrayList<Integer> cloneIndexes = new ArrayList<Integer>();
		ArrayList<Solution> p = population.getChromosomes();
		
		// Populate the list of indexes of clones.
		for(int i = 0; i < p.size(); i++) {
			for(int j = 0; j < p.size(); j++) {
				if(i != j) {
					if(!cloneIndexes.contains(i)) {
						float f1 = p.get(i).getFitness();
						float f2 = p.get(j).getFitness();
						if(f1 == f2) {
							cloneIndexes.add(i);
						}
					}
				}
			}
		}
				
		// mutate the clones, if they are found
		if(!cloneIndexes.isEmpty()) {
			int i = (cloneIndexes.size() == p.size()) ? 1 : 0;
			for(; i < cloneIndexes.size(); i++) {
				mutate(p.get(cloneIndexes.get(i)), settings.genesToMutateIfClones);
			}
		}
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
	
	/**
	 * 
	 * @param childSchedule			currently-generating child's schedule.
	 * @param firstCuttingPoint		first child's schedule element index to be checked.
	 * @param childExamToSet		child's element index to be set.
	 * @param parentValue			the child's element index should be set to this value, according
	 * 								to the crossover operator.
	 * @return						whether the temporary child's schedule is feasible
	 * 								or not.
	 */
	private boolean isFeasible(int[] childSchedule, int firstCuttingPoint, int childExamToSet, int parentValue) {
		int examIndexIterator = firstCuttingPoint;
		
		while(examIndexIterator != childExamToSet) {
			if(	// If the two exams have more than one student enrolled in both of them 
				instance.getN()[examIndexIterator][childExamToSet] > 0 &&
				
				// They will be placed in the same timeslot
				childSchedule[examIndexIterator] == parentValue
			) {
				return false;
			}
			
			examIndexIterator = (examIndexIterator + 1 == childSchedule.length) ? 0 : (examIndexIterator + 1);
		}
		
		return true;
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
		double[] CRP = new double[settings.initialPopulationSize];
		
		double cumulativeInverseSum = 0;
		for(int i = 0; i < population.getChromosomes().size() - 1; ++i) {
			cumulativeInverseSum += 1 / population.getChromosomes().get(i).getFitness();
			CRP[i] = cumulativeInverseSum / population.getTotalInverseFitness();
		}
		CRP[population.getChromosomes().size() - 1] = 1;
		
		/*TODO debug*/System.out.println("CRP: "+Arrays.toString(CRP));
		
		return CRP;
	}
	
	/**
	 * Cumulative Killing Probability generator function, starting from a given population.
	 * @param population	population 
	 * @return
	 */
	private double[] generateCKP(Population population) {
		double[] CKP = new double[settings.initialPopulationSize+settings.numberOfChildrenToGenerate];
		
		double cumulativeSum = 0;
		for(int i = 0; i < population.getChromosomes().size() - 1; ++i) {
			cumulativeSum += population.getChromosomes().get(i).getFitness();
			CKP[i] = cumulativeSum / population.getTotalFitness();
		}
		CKP[population.getChromosomes().size() - 1] = 1;
		
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
						chromosomes.add(population.getChromosomes().get(0));
						//update the selected numbers in order to avoid to select same parents
						selectedNum[numberOfSelectedParents] = 1;
						numberOfSelectedParents++;
			}
			else
				for (int i = 1; i < relativeFitness.length; i++) {
					if(randomNum <= relativeFitness[i] 
							&& randomNum>relativeFitness[i-1] 
									&& !Arrays.asList(selectedNum).contains(i+1)) {						
											chromosomes.add(population.getChromosomes().get(i));	
											//update the selected numbers in order to avoid to select same parents
											selectedNum[numberOfSelectedParents] = i+1;
											numberOfSelectedParents++;
										}
				}
		}
		
		return chromosomes;
	}
	
	// Stopping condition
	public static void stopExecution() {
		TIMER_EXPIRED = true;
	}
}
