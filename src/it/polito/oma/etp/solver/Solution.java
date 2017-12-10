package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InstanceData;

public class Solution {
	/**
	 * Instance to which this solution refers to. 
	 */
	private InstanceData instance;
	
	/**
	 * Timeslots-exams matrix, having element [i][j] set to
	 * 1 when exam j is scheduled in the i-th timeslot.
	 */
	private int[][] te;
	
	/**
	 * Exams distance matrix, having element [i][j] set to a value k
	 * when exams i and j are scheduled k timeslots apart.
	 */
	private int[][] y;
	
	/**
	 * Objective function value
	 */
	private float fitness;

	public Solution(int[][] te) {
		this.te = te;
		updateDistanceMatrix();
	}
	
	/**
	 * Updates the exams distance matrix given the current
	 * decision matrix te.
	 */
	private void updateDistanceMatrix() {
		int E = instance.getE();
		int tmax = instance.getTmax();
		
		// For each exam, the timeslot number is stored 
		int[] schedule = new int[E];
		
		for(int j = 0; j < E; ++j) // for each exam
			for(int i = 0; i < tmax; ++i) 
				if(te[i][j] == 1) {
					schedule[j] = i;
					continue;
				}
		
		for(int i = 0; i < E; ++i)
			for(int j = i + 1; j < E; ++j) {
				int distance = Math.abs(schedule[i] - schedule[j]);
				y[i][j] = distance; 
			}
	}
	
	/**
	 * Boolean function indicating if two exams i and j will generate
	 * a fee to be paid. 
	 * @param i	first exam index.
	 * @param j	second exam index.
	 * @return	true if exam i and j are in conflict and are scheduled
	 * 			between 1 and 5 timeslots apart	
	 */
	public boolean arePenalized(int i, int j) {
		if(y[i][j] > 0 && y[i][j] <= instance.getK() && instance.getN()[i][j] > 0)
			return true;
		
		return false;
	}
	
	/**
	 * Updates the current solution objective function value
	 */
	private void udpateFitness() {
		int E = instance.getE();
		int S = instance.getS();
		int K = instance.getK();
		int[][] N = instance.getN();
		 
		fitness = 0;
		
		for(int i = 0; i < E; ++i) {
			for(int j = 0; j < E; ++j) {
				if(i != j && y[i][j] > 0 && y[i][j] <= K) {
					fitness += Math.pow(2, K - y[i][j]) * N[i][j] / S;
				}
			}
		}
	}
	
	/**
	 * Returns the distance in terms of timeslots between
	 * exams i and j according to the solution that this
	 * object represents.
	 * @param i	first exam index.
	 * @param j	second exam index.
	 * @return	distance between the two exams i and j
	 */
	public int getDistance(int i, int j) {
		return y[i][j];
	}
}