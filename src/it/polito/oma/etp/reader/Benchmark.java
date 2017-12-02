package it.polito.oma.etp.reader;

public class Benchmark {
	
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
	 * Matrix of conflicting exams.
	 */
	private boolean[][] EE;
	/**
	 * Matrix containing number of students attending exams.
	 * Rows and columns' indexes represent exams.
	 */
	private int[][] N;

	public Benchmark() {
		super();
	}
	public Benchmark(int s, int e, boolean[][] ee) {
		super();
	}
	
	public int getS() {
		return S;
	}
	public int getE() {
		return E;
	}
	public int getTmax() {
		return Tmax;
	}
	public boolean[][] getEE(){
		return EE;
	}
	public int[][] getN(){
		return N;
	}

}