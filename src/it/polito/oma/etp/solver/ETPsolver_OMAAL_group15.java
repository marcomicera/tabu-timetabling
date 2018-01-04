package it.polito.oma.etp.solver;

import java.util.Timer;
import java.util.TimerTask;

import it.polito.oma.etp.reader.InputReader;
import it.polito.oma.etp.reader.InstanceData;

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
		
		// Tuning
		Settings initializationSettings = new Settings(
			// General Tabu List settings
			false,	// firstRandomSolution
			0.5,	// neighborhoodGeneratingPairsPercentage
			true,	// considerAllTimeslots
			20,		// tabuListInitialSize
			
			// Dynamic Tabu List section
			true,	// dynamicTabuList
			1,		// worseningCriterion (1: deltaFitness, 2: iterations, 3: time)
			40,		// tabuListMaxSize
			10000,	// maxNonImprovingIterationsAllowed
			5,		// tabuListIncrementSize
			
			// deltaFitness worsening criterion
			4,		// deltaFitnessThreshold
			
			// time worsening criterion
			5 		// tabuListIncrementTimeInterval
		);
		Settings optimizationSettings  = new Settings(
			// General Tabu List settings
			false,	// firstRandomSolution
			1,		// neighborhoodGeneratingPairsPercentage
			false,	// considerAllTimeslots
			20,		// tabuListInitialSize
			
			// Dynamic Tabu List section
			false,	// dynamicTabuList
			1,		// worseningCriterion (1: deltaFitness, 2: iterations, 3: time)
			40,		// tabuListMaxSize
			20000,	// maxNonImprovingIterationsAllowed
			5,		// tabuListIncrementSize
			
			// deltaFitness worsening criterion
			3,		// deltaFitnessThreshold
			
			// time worsening criterion
			5 		// tabuListIncrementTimeInterval
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
		
		// Computing the first feasible solution
		TabuSearch feasibleSolutionGenerator = new TabuInitialization(instanceData, initializationSettings);
		InitializationSolution initialFeasibleSolution = (InitializationSolution)feasibleSolutionGenerator.solve();
		/*TODO debug*/System.out.println("Initial feasible solution found: " + initialFeasibleSolution);
		/*TODO debug*/System.exit(0);
		
		// Computing the timetabling solution
		TabuSearch solutionGenerator = new TabuOptimization(instanceData, initialFeasibleSolution, optimizationSettings);
		OptimizationSolution solution = (OptimizationSolution)solutionGenerator.solve();
		/*TODO debug*/System.out.println("Final solution found: " + solution);
	}
}
