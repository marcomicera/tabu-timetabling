package it.polito.oma.etp.solver;

public class Neighbor implements Comparable<Neighbor> {
	private int movingExam;
	private int newTimeslot;
	private float fitness;
 	
	public Neighbor() {
	}
	
	public Neighbor(int movingExam, int newTimeslot, float fitness) {
		this.movingExam = movingExam;
		this.newTimeslot = newTimeslot;
		this.fitness = fitness;
	}
	
	public void update(Neighbor neighbor) {
		movingExam = neighbor.movingExam;
		newTimeslot = neighbor.newTimeslot;
		fitness = neighbor.fitness;
	}
	
	@Override
	public int compareTo(Neighbor otherNeighbor) {
		return (int)(fitness - otherNeighbor.fitness);
	}
	
	@Override
	public String toString() {
		return	"[Neighbor] movingExam = " + movingExam +
				"; newTimeslot = " + newTimeslot +
				"; fitness = " + fitness;
	}
	
	public float getFitness() {
		return fitness;
	}

	public int getMovingExam() {
		return movingExam;
	}

	public int getNewTimeslot() {
		return newTimeslot;
	}
}