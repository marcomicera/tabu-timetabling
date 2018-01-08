package it.polito.oma.etp.solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import it.polito.oma.etp.reader.InputReader;
import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.solver.initialization.InitializationSolution;
import it.polito.oma.etp.solver.initialization.TabuInitialization;
import it.polito.oma.etp.solver.optimization.OptimizationSolution;
import it.polito.oma.etp.solver.optimization.TabuOptimization;

public class ETPsolver_OMAAL_group15 {
	public static void main(String[] args) {
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
		String instanceName = args[0];
		int timeout = 0;
		try {
			timeout = Integer.parseInt(args[2]);
			if(timeout < 0)
				throw new NumberFormatException();
		} catch(NumberFormatException e) {
			System.err.println("Invalid timeout value: it must be a positive integer");
			System.exit(1);
		}
		
		// Instance data
		InstanceData instanceData = InputReader.getData("res\\" + instanceName);
		
		// Tabu List dynamic size: time worsening criterion
	    int tabuListInitialSize = 20;
	    int tabuListMaxSize = 40;
	    int tabuListIntervalAdd = 5;
	    double tabuListIncrementTimeInterval = ((float)timeout / 3) / ((tabuListMaxSize-tabuListInitialSize)/tabuListIntervalAdd);
	  
	    // Tuning
		TsSettings initializationSettings = new TsSettings(
			// General Tabu List settings
			true,	// firstRandomSolution
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
			1,									// initialPopulationSize
			4									// numberOfThreads
		);
		TsSettings optimizationSettings  = new TsSettings(
			// General Tabu List settings
			false,	// firstRandomSolution
			1,	// neighborhoodGeneratingPairsPercentage
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
			0,									// initialPopulationSize
			1									// numberOfThreads
		);
		
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
		
		int[] cuttingPoints = {3,7};
		GaSettings gaSettings = new GaSettings(
			// general GA Setting
			true, 				// initializationProblem
			4, 					// initialPopulationSize
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
		
		//TODO debug(GA)
		/*GeneticAlgorithm gaAlgorithm = new GeneticAlgorithm(initializationSettings, gaSettings, instanceData);
		gaAlgorithm.solve();*/
		/*TODO debug*/ //System.exit(0);
		
		//*** Time begins to flow from here
		double old = System.nanoTime();
		
		/*
		 * Definition of threads run's methods 
		 */
		ArrayList<Thread> threads = new ArrayList<Thread>();
		ArrayList<InitializationSolution> setOfSolutions = new ArrayList<InitializationSolution>();
		
		for(int i = 0; i < initializationSettings.numberOfThreads; i++) {
			threads.add(new Thread() {
				public void run() {
					// Feasible solution generation.
					/*TODO debug*/System.out.println(this.getName() + " started looking for an unfeasible solution.");
					TabuSearch feasibleSolutionGenerator = new TabuInitialization(instanceData, initializationSettings);
					InitializationSolution initialFeasibleSolution = (InitializationSolution)feasibleSolutionGenerator.solve();
					//------
					synchronized(setOfSolutions) {
						setOfSolutions.add(initialFeasibleSolution);
						// wakes main thread to see if population is full.
						setOfSolutions.notifyAll();
					}
				}
			});
		}
		
		/*
		 * Make the threads start and make main thread wait their completion.
		 */
		for(Thread thread : threads) {
			thread.start();
		}
		
		/*
		 * Main thread will wait until the populations is generated or time is over
		 */
		synchronized(setOfSolutions) {
			while(	setOfSolutions.size() < initializationSettings.initialPopulationSize) {
				try {
					setOfSolutions.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		/*
		 * IMPORTANT! You must filter unfeasible solutions from setOfSolutions here!
		 */
		for(int i = 0; i < setOfSolutions.size(); i++) {
			if(setOfSolutions.size() != 0) {
				if(!setOfSolutions.get(i).isFeasible()) {
					setOfSolutions.remove(i);
					i--;
				}
			}
		}
		/*
		 * Have we found all the initial population? If not, exit.
		 */
		if(setOfSolutions.size() != initializationSettings.initialPopulationSize) {
			System.err.println("The solver couldn't find " +  initializationSettings.initialPopulationSize + 
							   " feasible solutions to start with.\nNumber of feasible solutions found: " +
							    setOfSolutions.size());
			System.exit(1);
		}
		
		double newt = System.nanoTime();
		double time = (newt - old)/1000000000;
		System.out.println("Time to generate " + initializationSettings.initialPopulationSize + 
						   " fesible solutions for " + instanceData.getInstanceName() + ": " +
						   time + " seconds");

		for(InitializationSolution sol : setOfSolutions) {
		/*TODO debug*/System.out.println("This solution was found!: " + sol);
		}

		/*TODO debug*/ //System.out.println("Initial feasible solution " + ((initialFeasibleSolution.getFitness() != 0) ? "not " : "") + "found: " + initialFeasibleSolution);
		/*TODO debug*/ //System.exit(0);
		
		// Computing the timetabling solution
		TabuSearch solutionGenerator = new TabuOptimization(instanceData, setOfSolutions.get(0), optimizationSettings);
		OptimizationSolution solution = (OptimizationSolution)solutionGenerator.solve();
		
		/*TODO debug*/System.out.println("Final solution found: " + solution);
	}
}
