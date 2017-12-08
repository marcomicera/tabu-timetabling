package it.polito.oma.etp.reader;

import it.polito.oma.etp.solver.TabuSearch;

public class Benchmark {
	
	/**
	 * Penalty for each couple of conflicting exams scheduled 
	 * up to K time-slots apart
	 */
	private static final int K = 5;
	
	/**
	 * Name of the instance in exam.
	 */
	private String instanceName;
	
	/**
	 * Cardinality of students.
	 */
	private int S;
	
	/**
	 * Cardinality of exams.
	 */
	private int E;
	
	/**
	 * Number of available timeslots.
	 */
	private int Tmax;
	
	/**
	 * Matrix containing number of students attending exams
	 * Rows and columns' indexes represent exams.
	 */
	private int[][] N;
	
	/**
	 * Timeslots-exams matrix, having element [i][j] set to
	 * 1 when exam j is scheduled in the i-th timeslot.
	 */
	private int[][] te;
	
	/**
	 * Penalty boolean variables, having element [k][i][j] set
	 * to 1 when exams i and j are scheduled k timeslots apart.
	 */
	private int[][][] y;
	
	/**
	 * Default constructor.
	 * @param in	Instance name.
	 * @param s		Number of students.
	 * @param e		Number of exams.
	 * @param tmax	Number of timeslots.
	 * @param n		Matrix containing number of students enrolled in every pair of exams.
	 * @param te	Decision matrix containing the solution: used in results.
	 */
	public Benchmark(String in, int s, int e, int tmax, int[][] n, int[][] te) {
		instanceName = in;
		S = s;
		E = e;
		Tmax = tmax;
		N = n;
		this.te = te;
		y = new int[K][E][E];
		//updatePenaltyVariables(); // da chiamare quando te viene fornita in ingresso
	}
	
	/**
	 * Constructor for an initial feasible solution.
	 * @param in	Instance name.
	 * @param s		Number of students.
	 * @param e		Number of exams.
	 * @param tmax	Number of timeslots.
	 * @param n		Matrix containing number of students enrolled in every pair of exams.
	 */
	public Benchmark(String in, int s, int e, int tmax, int[][] n) {
		this(
			in,
			s,
			e,
			tmax,
			n,
			TabuSearch.feasibleSolution()
		);
	}
	
	/**
	 * Fitness function computing the objective function
	 * value given the decision matrix te.
	 * @return	The objective function value.
	 */
	public double fitness() {
		double result = 0;
		
		updatePenaltyVariables();
		
		for(int i = 0; i < E; ++i)
			for(int j = 0; j < E; ++j)
				for(int k = 1; k <= K; ++k)
					result += Math.pow(2, K-k) * N[i][j] * 1/S * y[k-1][i][j];
		
		return result;
	}
	
	/**
	 * Updates penalty boolean variables y given the current
	 * decision matrix te.
	 */
	private void updatePenaltyVariables() {
		int[] schedule = new int[E];
		
		for(int j = 0; j < E; ++j) // for each exam
			for(int i = 0; i < Tmax; ++i) 
				if(te[i][j] == 1) {
					schedule[j] = i;
					continue;
				}
		
		for(int i = 0; i < E; ++i)
			for(int j = i + 1; j < E; ++j) {
				int distance = Math.abs(schedule[i] - schedule[j]);
				if(distance > 0 && distance <= K && N[i][j] > 0)
					y[distance - 1][i][j] = y[distance - 1][j][i] = 1; 
			}
	}
	
	/**
	 * @return Name of the instance in exam.
	 */
	public String getInstanceName() {
		return instanceName;
	}
	
	/**
	 * @return Cardinality of students.
	 */
	public int getS() {
		return S;
	}
	
	/**
	 * @return Cardinality of exams.
	 */
	public int getE() {
		return E;
	}
	
	/**
	 * @return Number of available timeslots.
	 */
	public int getTmax() {
		return Tmax;
	}
	
	/**
	 * @return Matrix containing number of students attending exams.
	 * Rows and columns' indexes represent exams.
	 */
	public int[][] getN(){
		return N;
	}

	/**
	 * Boolean decision variables pretty printing
	 */
	public void printY() {
		for(int k = 0; k < K; ++k) {
			System.out.println("****** Exams distant " + (k + 1) + " timeslots apart ******\n");
			for(int j = 0; j < E; ++j) {
				for(int i = 0; i < Tmax; ++i) {
					System.out.print(te[i][j] + "");
				}
				System.out.print("\n");
			}
			System.out.print("\n\n");
		}
	}
}