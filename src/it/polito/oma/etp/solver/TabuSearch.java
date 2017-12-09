package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.Benchmark;

public class TabuSearch {
	
	private Benchmark benchmark;
	
	public static int[][] feasibleSolution() {
		// TODO implement
		return null;
	}
	
	/**
	 * Fitness function computing the objective function
	 * value given the decision matrix te.
	 * @return	The objective function value.
	 */
	public double fitness() {
		int E = benchmark.getE();
		int S = benchmark.getS();
		int K = benchmark.getK();
		int[][] n = benchmark.getN();
		int[][][] y = benchmark.getY();

		double result = 0;
		
		updatePenaltyVariables();
		
		for(int i = 0; i < E; ++i) {
			for(int j = 0; j < E; ++j) {
				for(int k = 1; k <= K; ++k) {
					
					result += Math.pow(2, K-k) * n[i][j] * 1/S * y[k-1][i][j];
				}
			}
		}
		return result;
	}

	
	/**
	 * Updates penalty boolean variables y given the current
	 * decision matrix te.
	 */
	private void updatePenaltyVariables() {
		int E = benchmark.getE();
		int tmax = benchmark.getTmax();
		int K = benchmark.getK();
		int[][] n = benchmark.getN();
		int[][][] y = benchmark.getY();
		int[] schedule = new int[E];
		
		for(int j = 0; j < E; ++j) // for each exam
			for(int i = 0; i < tmax; ++i) 
				if(benchmark.getTe()[i][j] == 1) {
					schedule[j] = i;
					continue;
				}
		
		for(int i = 0; i < E; ++i)
			for(int j = i + 1; j < E; ++j) {
				int distance = Math.abs(schedule[i] - schedule[j]);
				if(distance > 0 && distance <= K && n[i][j] > 0)
					y[distance - 1][i][j] = y[distance - 1][j][i] = 1; 
			}
	}
	

	/**
	 * Boolean decision variables pretty printing
	 */
	public void printY() {
		
		int E = benchmark.getE();
		int tmax = benchmark.getTmax();
		int K = benchmark.getK();
		int te[][] = benchmark.getTe();

		for(int k = 0; k < K; ++k) {
			System.out.println("****** Exams distant " + (k + 1) + " timeslots apart ******\n");
			for(int j = 0; j < E; ++j) {
				for(int i = 0; i < tmax; ++i) {
					System.out.print(te[i][j] + "");
				}
				System.out.print("\n");
			}
			System.out.print("\n\n");
		}
	}

	
	
}