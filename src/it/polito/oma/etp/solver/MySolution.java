package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InstanceData;

public class MySolution {
	
	private InstanceData instance;
	private int fitness;
	private int[][] te;
	private int[][] U;
	
	
	public MySolution(InstanceData idata, int[][] te, int[][] U) {
		instance = idata;
		this.te = Utility.cloneMatrix(te);
		this.U = Utility.cloneMatrix(U);
		fitness = calculateFitness();
	}
	
	public MySolution() {
		this(null, null, null);
		System.err.println("Warning: bad initialization of MySolution");
	}
	
	
	
	private int calculateFitness() {
		int fitnessV = 0;
		int E = instance.getE();
		for(int i = 0; i < E; i++) {
			for(int j = i + 1; j < E; j++) {
				fitnessV += U[i][j];
			}
		}
		return fitnessV;
	}
	
	public InstanceData getInstanceData() {
		return instance;
	}
	
	public int getFitness() {
		return fitness;
	}
	
	public int[][] getTE(){
		return te;
	}
	
	public int[][] getU(){
		return U;
	}
}
