package it.polito.oma.etp.reader;

public class Benchmark {
	
	/**
	 * Penalty for each couple of conflicting exams scheduled 
	 * up to K time-slots apart.
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
	 */
	public Benchmark(String in, int s, int e, int tmax, int[][] n) {
		instanceName = in;
		S = s;
		E = e;
		Tmax = tmax;
		N = n;
		te = new int[Tmax][E];
		y = new int[K][E][E];
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

}