package it.polito.oma.etp.reader;

public class Benchmark {
	
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

	public Benchmark() {
		super();
	}
	public Benchmark(String in, int s, int e, int tmax, int[][] n) {
		super();
		instanceName = in;
		S = s;
		E = e;
		Tmax = tmax;
		N = n;
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