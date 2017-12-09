package it.polito.oma.etp.reader;

public class InstanceData {
	
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
	 * Constructor for an initial feasible solution.
	 * @param in	Instance name.
	 * @param s		Number of students.
	 * @param e		Number of exams.
	 * @param tmax	Number of timeslots.
	 * @param n		Matrix containing number of students enrolled in every pair of exams.
	 */
	public InstanceData(String in, int s, int e, int tmax, int[][] n) {
		instanceName = in;
		S = s;
		E = e;
		Tmax = tmax;
		N = n;
	}
	
	public int getK() {
		return K;
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