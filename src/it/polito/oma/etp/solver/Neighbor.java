package it.polito.oma.etp.solver;

public class Neighbor implements Comparable<Neighbor> {
	private int movingExam;
	private int newTimeslot;
	private Float fitness;
 	
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
	
	// Searching in Lists
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Neighbor)) return false;
		
		Neighbor otherNeighbor = (Neighbor)o;
		
		return	movingExam == otherNeighbor.movingExam
					&&
				newTimeslot == otherNeighbor.newTimeslot;
	}

	// Ordering
	@Override
	public int compareTo(Neighbor otherNeighbor) {
		return fitness.compareTo(Float.valueOf(otherNeighbor.fitness));
	}
	
	@Override
	public String toString() {
		return	"<movingExam = " + movingExam +
				", newTimeslot = " + newTimeslot +
				", fitness = " + fitness + ">";
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