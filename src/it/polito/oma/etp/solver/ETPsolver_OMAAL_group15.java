package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InstanceData;
import it.polito.oma.etp.reader.InputReader;

public class ETPsolver_OMAAL_group15 {

	public static String instanceName;
	public static InstanceData idata;
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println(
				"Usage: java it.polito.oma.etp.solver.ETPsolver_OMAAL_group15 instance_number"
			);
			System.exit(1);
		}
		
		instanceName = args[0];
		
		//idata = InputReader.getData("res\\" + instanceName);
		
		// *********************** Testing code ***********************
		// TODO delete
		InstanceData testInstance = new InstanceData(
			"test instance",
			40,	// S
			5,	// E
			4, 	// Tmax
			new int[][]{ // N
				{0,		0,	15,	0,	0},
				{0,		0,	0,	5,	25},
				{15,	0,	0,	0,	0},
				{0,		5,	0,	0,	25},
				{0,		25,	0,	25,	0}
			}
		);
		
		TabuSearch.solve(testInstance);
	}
}
