package it.polito.oma.etp.solver;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
		
		// Timer thread
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = executor.submit(() -> {
		    // Instance data
			InstanceData instanceData = InputReader.getData("res\\" + instanceName);
			
			// Tuning
			Settings initializationSettings = new Settings(
				false,	// firstRandomSolution
				0.6,	// neighborhoodGeneratingPairsPercentage
				false,	// considerAllTimeslots
				20 		// tabuListInitialSize
			);
			Settings optimizationSettings  = new Settings(
				false,	// firstRandomSolution
				1,		// neighborhoodGeneratingPairsPercentage
				true,	// considerAllTimeslots
				10		// tabuListInitialSize
			);
			
			// Computing the first feasible solution
			TabuSearch feasibleSolutionGenerator = new TabuInitialization(instanceData, initializationSettings);
			feasibleSolutionGenerator.solve(); // TODO stopping condition: fitness = 0 (finite execution time)
			InitializationSolution initialFeasibleSolution = (InitializationSolution)feasibleSolutionGenerator.getSolution();
			/*TODO debug*/System.out.println("Initial feasible solution found: " + initialFeasibleSolution);	
			/*TODO debug*/System.exit(0);
			
			// Computing the timetabling solution
			TabuSearch solutionGenerator = new TabuOptimization(instanceData, initialFeasibleSolution, optimizationSettings);
			solutionGenerator.solve(); // infinite loop, interrupted by the timer thread
			OptimizationSolution solution = (OptimizationSolution)solutionGenerator.getSolution();
			
			/*TODO debug*/System.out.println("Final solution found: " + solution);
			
			/*TODO wrong result*/return "OK"; 
		});
		
		try {
		    System.out.println(future.get(timeout, TimeUnit.SECONDS)); //timeout is in 2 seconds
		} catch (TimeoutException e) {
		    /*TODO debug*/ System.err.println("*********************** Timeout TimeoutException ************************");
		    
		    System.exit(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		// Printing the solution (?)
		/*try {
			System.out.println(future.get());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
