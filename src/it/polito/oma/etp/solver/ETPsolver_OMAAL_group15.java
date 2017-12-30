package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InputReader;
import it.polito.oma.etp.reader.InstanceData;

public class ETPsolver_OMAAL_group15 {
	public static String instanceName;
	public static InstanceData instanceData;
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println(
				"Usage: java it.polito.oma.etp.solver.ETPsolver_OMAAL_group15 instance_number"
			);
			System.exit(1);
		}
		
		// TODO starting timer thread
		
		// Instance data
		instanceName = args[0];
		instanceData = InputReader.getData("res\\" + instanceName);
		
		// Computing the first feasible solution
		TabuSearch feasibleSolutionGenerator = new TabuInitialization(instanceData);
		feasibleSolutionGenerator.solve(); // TODO stopping condition: fitness = 0 (finite execution time)
		InitializationSolution initialFeasibleSolution = (InitializationSolution)feasibleSolutionGenerator.getSolution();
		/*TODO debug*/System.out.println("Initial feasible solution found: " + initialFeasibleSolution);	
		
		// Computing the timetabling solution
		TabuSearch solutionGenerator = new TabuOptimization(instanceData, initialFeasibleSolution);
		solutionGenerator.solve(); // infinite loop, interrupted by the timer thread
		OptimizationSolution solution = (OptimizationSolution)solutionGenerator.getSolution();
		
		/*TODO debug*/System.out.println("Final solution found: " + solution);
	}
}
