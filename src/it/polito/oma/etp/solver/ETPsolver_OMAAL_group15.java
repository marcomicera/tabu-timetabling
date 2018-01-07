package it.polito.oma.etp.solver;

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
			0.5,	// neighborhoodGeneratingPairsPercentage
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
			tabuListIncrementTimeInterval 		// tabuListIncrementTimeInterval
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
			tabuListIncrementTimeInterval 		// tabuListIncrementTimeInterval
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
			true, 				// it's an initialization problem?
			4, 					// PopulationSize
			2, 					// numberOfReproductiveParents
			true, 				// randomParentSelection
			2, 					// cuttingPointsNumber -> NB: if we wont just one cutting point we have to 
								//							  set whereToCut = {0,x}
			true, 				// randomCuttingPoint
			null,				// whereToCut
			2,					// number of children to generate
			5,					// cloningManagementThreshold
			10					// genesToMutateIfClones
		);
		
		//TODO debug(GA)
		GeneticAlgorithm gaAlgorithm = new GeneticAlgorithm(initializationSettings, gaSettings, instanceData);
		gaAlgorithm.solve();
		/*TODO debug*/System.exit(0);
		
		// Computing the first feasible solution
		TabuSearch feasibleSolutionGenerator = new TabuInitialization(instanceData, initializationSettings);
		InitializationSolution initialFeasibleSolution = (InitializationSolution)feasibleSolutionGenerator.solve();
		/*TODO debug*/System.out.println("Initial feasible solution " + ((initialFeasibleSolution.getFitness() != 0) ? "not " : "") + "found: " + initialFeasibleSolution);
		/*TODO debug*/ //System.exit(0);
		
		// Computing the timetabling solution
		TabuSearch solutionGenerator = new TabuOptimization(instanceData, initialFeasibleSolution, optimizationSettings);
		OptimizationSolution solution = (OptimizationSolution)solutionGenerator.solve();
		/*TODO debug*/System.out.println("Final solution found: " + solution);
	}
}
