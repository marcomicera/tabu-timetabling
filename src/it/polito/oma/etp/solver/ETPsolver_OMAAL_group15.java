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
		
		for(int i = 1; i <= 7; ++i)
			//idata = InputReader.getData("res\\" + instanceName);
			idata = InputReader.getData("res\\instance0" + i);
			
		idata = InputReader.getData("res\\test");

		//TabuSearch.solve(idata);
		}
}
