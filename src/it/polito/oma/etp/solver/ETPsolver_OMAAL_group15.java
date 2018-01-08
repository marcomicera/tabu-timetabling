package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import it.polito.oma.etp.reader.InputReader;
import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.initialization.InitializationPopulation;
import it.polito.oma.etp.solver.initialization.InitializationSolution;
import it.polito.oma.etp.solver.initialization.TabuInitialization;
import it.polito.oma.etp.solver.optimization.GeneticOptimization;
import it.polito.oma.etp.solver.optimization.OptimizationSolution;
import it.polito.oma.etp.solver.optimization.TabuOptimization;

public class ETPsolver_OMAAL_group15 {
	private InstanceData instance;
	private String instanceName;
	private Settings commonSettings;
	private int timeout;
	
	public ETPsolver_OMAAL_group15(String[] args) {
		inputArgumentsParsing(args);
		readingInstanceData();
		setCommonSettings();
	}
	
	private void inputArgumentsParsing(String[] args) {
		// Input argument number checking
		if(args.length != 3) {
			System.err.println(
				"Usage: java it.polito.oma.etp.solver.ETPsolver_OMAAL_group15 instance_id -t timeout"
			);
			System.exit(1);
		}
		if(args[1].compareTo("-t") != 0) {
			System.err.println("Second argoment must be the -t flag");
			System.exit(1);
		}
		
		// Input arguments
		instanceName = args[0];
		try {
			timeout = Integer.parseInt(args[2]);
			if(timeout < 0)
				throw new NumberFormatException();
		} catch(NumberFormatException e) {
			System.err.println("Invalid timeout value: it must be a positive integer");
			System.exit(1);
		}
	}
	
	private void readingInstanceData() {
		instance = InputReader.getData("res\\" + instanceName);
	}
	
	/**
	 * Commong settings to be shared among different solving
	 * algorithms.
	 * This will be used for tuning purposes.
	 */
	private void setCommonSettings() {
		commonSettings = new Settings(
			true,	// firstRandomSolution
			4,		// initialPopulationSize
					/**
					 * It will be overwritten to 1 if
					 * the optimization problem will be solved
					 * by the Tabu Search algorithm, since it
					 * just needs one initial feasible solution.
					 */
			
			false,	// geneticInitialization
			true	// geneticOptimization
		);
	}
	
	private InitializationPopulation initialization() {
		if(commonSettings.geneticInitialization)
			return geneticInitialization();
		else
			return tabuInitialization();
	}
	
	private InitializationPopulation tabuInitialization() {
		// Tabu List dynamic size: time worsening criterion
	    int tabuListInitialSize = 20;
	    int tabuListMaxSize = 40;
	    int tabuListIntervalAdd = 5;
	    double tabuListIncrementTimeInterval = ((float)timeout / 3) / ((tabuListMaxSize - tabuListInitialSize) / tabuListIntervalAdd);
	  
	    // Tabu Search initialization tuning
	    TabuSettings initializationSettings = new TabuSettings(
			// Common settings
	    	commonSettings,
	    		
	    	// General Tabu List settings
			1.0,	// neighborhoodGeneratingPairsPercentage
			false,	// considerAllTimeslots
			20,		// tabuListInitialSize
			
			// Dynamic Tabu List section
			true,	// dynamicTabuList
			2,		// worseningCriterion (1: deltaFitness, 2: iterations, 3: time)
			45,		// tabuListMaxSize
			5000,	// maxNonImprovingIterationsAllowed
			4,		// tabuListIncrementSize
			
			// deltaFitness worsening criterion
			50,		// movingAveragePeriod
			
			// time worsening criterion
			tabuListIncrementTimeInterval, 		// tabuListIncrementTimeInterval
			4									// numberOfThreads
		);
		
		// Starting a stopwatch
		double startTime = System.nanoTime();
		
		// Thread definitions running TabuInitialization implementations
		ArrayList<Thread> tabuInitializationThreads = new ArrayList<Thread>();
		InitializationPopulation initialPopulation = new InitializationPopulation(
			instance,
			initializationSettings.initialPopulationSize,
			initializationSettings.firstRandomSolution
		);
		
		for(int i = 0; i < initializationSettings.numberOfThreads; ++i) {
			tabuInitializationThreads.add(
				new Thread() {
					public void run() {
						/*TODO debug*/System.out.println(this.getName() + " started looking for an infeasible solution.");
						
						// Feasible solution generation.
						TabuSearch feasibleSolutionGenerator = new TabuInitialization(instance, initializationSettings);
						InitializationSolution initialFeasibleSolution = (InitializationSolution)feasibleSolutionGenerator.solve();
						
						synchronized(initialPopulation) {
							/*TODO debug*/System.out.println(this.getName() + " has found a solution. It's " + (!initialFeasibleSolution.isFeasible() ? "not" : "") + "feasible: " + initialFeasibleSolution);
							
							/**
							 * Adding the new feasible solution to the initial population
							 * if it is feasible.
							 */
							initialPopulation.add(initialFeasibleSolution);
							
							/**
							 * Waking main thread to let it check if the initial population
							 * has been completely filled.
							 */
							initialPopulation.notifyAll();
						}
					}
				}
			);
		}
		
		// Starting all TabuInitialization threads
		for(Thread thread : tabuInitializationThreads)
			thread.start();
		
		/**
		 * Main thread will wait until:
		 * 
		 * - the initial population has been completely filled
		 * - time is over
		 */
		synchronized(initialPopulation) {
			while(initialPopulation.getSize() < initializationSettings.initialPopulationSize) {
				try {
					initialPopulation.wait();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		/*TODO debug*/System.out.println("before filter initialPopulation.getSize(): " + initialPopulation.getSize());

		/*
		 * IMPORTANT! You must filter infeasible solutions from setOfSolutions here!
		 */
		for(int i = 0; i < initialPopulation.getSize(); i++) {
			if(initialPopulation.getSize() != 0) {
				if(!initialPopulation.getSolution(i).isFeasible()) {
					/*TODO debug*/System.out.println("Removing " + initialPopulation.getSolution(i));
					initialPopulation.delete(i);
					i--;
				}
			}
		}
		
		// Checking whether the initial population solution has been completely filled
		/*TODO debug*/System.out.println("after filter initialPopulation.getSize(): " + initialPopulation.getSize());
		/*TODO debug*/System.out.println("initializationSettings.initialPopulationSize: " + initializationSettings.initialPopulationSize);
		if(initialPopulation.getSize() != initializationSettings.initialPopulationSize)
			throw new AssertionError(
				"The solver couldn't find " +  initializationSettings.initialPopulationSize + 
				" feasible solutions to start with." + 
				"\nNumber of feasible solutions found: " + initialPopulation.getSize()
			);
		
		// Computing the elapsed time since starting searching for feasible solutions
		double finishTime = System.nanoTime();
		double elapsedTime = (finishTime - startTime) / 1000000000;
		System.out.println(
			"Time to generate " + initializationSettings.initialPopulationSize + 
			" feasible solutions for " + instance.getInstanceName() + ": " +
			elapsedTime + " seconds"
		);

		// TODO (debug) Printing initial feasible population
		System.out.println("initialPopulation found:");
		for(Solution initialFeasibleSolution: initialPopulation.getChromosomes())
			System.out.println(initialFeasibleSolution);
		
		return initialPopulation;
	}
	
	// TODO implement
	private InitializationPopulation geneticInitialization() {
		throw new AssertionError("GeneticInitialization still to be implemented");
	}
	
	private OptimizationSolution optimization(InitializationPopulation initialPopulation) {
		if(commonSettings.geneticOptimization)
			return geneticOptimization(initialPopulation);
		else
			return tabuOptimization((InitializationSolution)initialPopulation.getChromosomes().get(0));
	}
	
	private OptimizationSolution tabuOptimization(InitializationSolution initialSolution) {
		// Tabu List dynamic size: time worsening criterion
	    int tabuListInitialSize = 20;
	    int tabuListMaxSize = 40;
	    int tabuListIntervalAdd = 5;
	    double tabuListIncrementTimeInterval = ((float)timeout / 3) / ((tabuListMaxSize - tabuListInitialSize) / tabuListIntervalAdd);
		
		// Tabu Search optimization tuning
		TabuSettings optimizationSettings  = new TabuSettings(
			// Common settings
	    	commonSettings,
				
			// General Tabu List settings
			1,		// neighborhoodGeneratingPairsPercentage
			true,	// considerAllTimeslots
			20,		// tabuListInitialSize
			
			// Dynamic Tabu List section
			true,	// dynamicTabuList
			1,		// worseningCriterion (1: deltaFitness, 2: iterations, 3: time)
			45,		// tabuListMaxSize
			9000,	// maxNonImprovingIterationsAllowed
			7,		// tabuListIncrementSize
			
			// deltaFitness worsening criterion
			50,		// movingAveragePeriod
			
			// time worsening criterion
			tabuListIncrementTimeInterval, 		// tabuListIncrementTimeInterval
			1									// numberOfThreads
		);
		
		TabuSearch solutionGenerator = new TabuOptimization(
			instance, 
			initialSolution, 
			optimizationSettings
		);
		
		return (OptimizationSolution)solutionGenerator.solve();
	}
	
	private OptimizationSolution geneticOptimization(InitializationPopulation initialPopulation) {
		// Genetic Algorithm tuning
		int[] cuttingPoints = {3,7};
		GeneticSettings gaSettings = new GeneticSettings(
			// Common settings
	    	commonSettings,
				
			// General GA settings
			false, 				// initializationProblem
			true,				// randomParentSelection
			2, 					// numberOfReproductiveParents
			true,				// selectChromosomesToKillByRelativeFitness
			2, 					// cuttingPointsNumber -> NB: if we wont just one cutting point we have to 
								// set whereToCut = {0,x}
			true, 				// randomCuttingPoint
			null,				// whereToCut
			2,					// numberOfChildrenToGenerate
			20,					// cloningManagementThreshold
			10,					// genesToMutateIfClones
			0.05,				// mutationProbabilityInitialValue
			0.05,				// mutationProbabilityMinimumValue
			0.5,				// mutationProbabilityMaximumValue
			50,					// mutationProbabilityManagementThreshold
			0.5,				// mutationProbabilityConvergenceRatio
			0.1					// mutatingGenesPercentage
		);
		
		GeneticAlgorithm solutionGenerator = new GeneticOptimization(instance, gaSettings, initialPopulation);
		return (OptimizationSolution)solutionGenerator.solve();
	}
	
	private void startTimer() {
		// Starting the execution timer 
		new Timer().schedule(
			// TimerTask that will stop the algorithm execution
			new TimerTask() {
				@Override
				public void run() {
					TabuSearch.stopExecution();
				}
			},
			
			// Timeout value in milliseconds
			timeout * 1000
		);
	}
	
	public static void main(String[] args) {
		// Initializing solver having all needed instance data
		ETPsolver_OMAAL_group15 solver = new ETPsolver_OMAAL_group15(args);
		
		// Starting execution timer within the solution must be returned
		solver.startTimer();
		
		// Searching for feasible solutions
		InitializationPopulation initialPopulation = solver.initialization();
		
		// Computing the timetabling solution
		OptimizationSolution solution = solver.optimization(initialPopulation);
		
		// Printing final solution in cons
		System.out.println("Final solution found: " + solution);
		
		// Printing final solution to file
		InputReader.printSolution(solution);
	}	

}