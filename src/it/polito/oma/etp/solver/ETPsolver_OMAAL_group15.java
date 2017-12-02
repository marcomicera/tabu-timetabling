package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InputReader;

public class ETPsolver_OMAAL_group15 {

	public static String instanceName;
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println(
				"Usage: java it.polito.oma.etp.solver.ETPsolver_OMAAL_group15 instance_number"
			);
			System.exit(1);
		}
		
		instanceName = args[0];
		InputReader.getBenchmark("res\\" + instanceName);
	}

}