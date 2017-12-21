package it.polito.oma.etp.solver;

public class UnfeasibilityNeighbor extends Neighbor{
	
	private int oldTimeslot;
	private int[][] U;
	
	public UnfeasibilityNeighbor(int movingExam, int newTimeslot, float fitness, int oldTimeslot, int[][] U) {
		super(movingExam, newTimeslot, fitness);
		this.oldTimeslot = oldTimeslot;
		this.U = Utility.cloneMatrix(U);
	}

	public int getOldTimeslot() {
		return oldTimeslot;
	}
	
	public int[][] getU(){
		return U;
	}
	
}
